import {
  CameraAngle,
  FootwearType,
  LocationCategory,
  LocationType,
  PoseType,
} from "../types";
import { LOCATION_CATEGORIES_MAP } from "../constants";
import {
  GenerationResult,
  InspectorMode,
  InspectorResult,
  ReferenceAsset,
  RenderRecord,
  StudioV4State,
} from "../typesV4";
import { DesignState } from "../types";
import { createGeminiClient } from "./apiKeyStore";
import { buildRenderContract, getRenderReadiness } from "./renderPipeline";

const enumValueOr = <T extends string>(
  values: readonly T[],
  candidate: unknown,
  fallback: T
): T =>
  typeof candidate === "string" && values.includes(candidate as T)
    ? (candidate as T)
    : fallback;

const parseDataUrl = (dataUrl: string): { mimeType: string; data: string } => {
  const match = /^data:([^;]+);base64,(.+)$/s.exec(dataUrl);
  if (!match) throw new Error("Ongeldige referentie-afbeelding.");
  return { mimeType: match[1], data: match[2] };
};

const imageParts = (references: ReferenceAsset[]) =>
  references.map((ref) => {
    const parsed = parseDataUrl(ref.dataUrl);
    return {
      inlineData: {
        mimeType: parsed.mimeType,
        data: parsed.data,
      },
    };
  });

export const generateImage = async (
  state: DesignState,
  studio: StudioV4State,
  references: ReferenceAsset[] = []
): Promise<GenerationResult> => {
  const { contract, selectedReferences } = buildRenderContract(state, studio, references);
  const readiness = getRenderReadiness(contract);

  if (studio.resolverMode === "strict" && !readiness.ready) {
    throw new Error(readiness.blockers.map((b) => b.detail).join(" "));
  }

  const ai = createGeminiClient();
  const response = await ai.models.generateContent({
    model: contract.model,
    contents: {
      parts: [
        { text: contract.compiledPrompt },
        ...imageParts(selectedReferences),
      ],
    },
    config: {
      responseModalities: ["IMAGE"] as any,
      imageConfig: {
        aspectRatio: contract.aspectRatio,
        imageSize: contract.resolution,
      },
    },
  });

  for (const part of response.candidates?.[0]?.content?.parts || []) {
    if (part.inlineData?.data) {
      const mimeType = part.inlineData.mimeType || "image/png";
      return {
        imageData: `data:${mimeType};base64,${part.inlineData.data}`,
        contract,
      };
    }
  }

  throw new Error("Gemini gaf geen afbeeldingsdata terug.");
};

export const inspectRender = async (
  imageData: string,
  record: RenderRecord,
  mode: InspectorMode
): Promise<InspectorResult | undefined> => {
  if (mode === "off") return undefined;

  const ai = createGeminiClient();
  const parsed = parseDataUrl(imageData);
  const focus =
    mode === "quick"
      ? "Check only major anatomy, toe count, hosiery coverage, clipping and obvious realism failures."
      : "Check anatomy, toe count, foot proportions, hosiery physics, nails, footwear, pose, camera adherence, realism and render-plan adherence.";

  const response = await ai.models.generateContent({
    model: "gemini-3.6-flash",
    contents: {
      parts: [
        {
          text: `You are a visual QA inspector for an adult fashion image generator.
${focus}
Judge the image against this resolved render contract. Do not invent hidden details.
Return JSON only with keys anatomy, hosiery, nails, footwear, pose, realism (each "ok", "warning" or "unknown"), issues (array of short strings), summary (one short sentence).

RENDER CONTRACT:
${record.contract.compiledPrompt}`,
        },
        {
          inlineData: {
            mimeType: parsed.mimeType,
            data: parsed.data,
          },
        },
      ],
    },
    config: { responseMimeType: "application/json" },
  });

  try {
    const raw = (response.text || "{}").replace(/```json|```/g, "").trim();
    const json = JSON.parse(raw);
    const status = (value: unknown): "ok" | "warning" | "unknown" =>
      value === "ok" || value === "warning" ? value : "unknown";
    return {
      anatomy: status(json.anatomy),
      hosiery: status(json.hosiery),
      nails: status(json.nails),
      footwear: status(json.footwear),
      pose: status(json.pose),
      realism: status(json.realism),
      issues: Array.isArray(json.issues) ? json.issues.map(String).slice(0, 8) : [],
      summary: typeof json.summary === "string" ? json.summary : "Inspectie voltooid.",
    };
  } catch {
    return {
      anatomy: "unknown",
      hosiery: "unknown",
      nails: "unknown",
      footwear: "unknown",
      pose: "unknown",
      realism: "unknown",
      issues: [],
      summary: "Inspectieantwoord kon niet betrouwbaar worden gelezen.",
    };
  }
};

export type FixTarget =
  | "anatomy"
  | "hosiery"
  | "nails"
  | "footwear"
  | "pose"
  | "realism";

const fixInstruction: Record<FixTarget, string> = {
  anatomy:
    "Correct only foot anatomy: toe count, toe attachment, proportions, arch and ankle plausibility. Preserve hosiery, nails, footwear, pose framing, scene and lighting unless anatomy physically requires a tiny local correction.",
  hosiery:
    "Correct only hosiery coverage and material physics: symmetry, sole/toe coverage, transparency or mesh, tension and clipping. Preserve anatomy, nails, footwear, pose, camera, scene and lighting.",
  nails:
    "Correct only visible toenails: count, shape, polish placement, color and requested nail-art fidelity. Preserve anatomy, hosiery, footwear, pose, camera, scene and lighting.",
  footwear:
    "Correct only footwear geometry, straps, material, state and clipping. Preserve anatomy, hosiery, nails, pose, camera, scene and lighting.",
  pose:
    "Correct only physically implausible foot, ankle or leg pose while preserving foot identity, styling, hosiery, footwear, camera framing, scene and lighting.",
  realism:
    "Reduce AI-looking artifacts only: restore believable skin/material microtexture, shadows, edge transitions and optical realism. Preserve design choices, pose, camera and scene.",
};

export const generateTargetedFix = async (
  source: RenderRecord,
  target: FixTarget,
  studio: StudioV4State,
  references: ReferenceAsset[] = []
): Promise<GenerationResult> => {
  const ai = createGeminiClient();
  const parsed = parseDataUrl(source.imageData);
  const { contract, selectedReferences } = buildRenderContract(
    source.contract.requestedState,
    studio,
    references
  );

  const response = await ai.models.generateContent({
    model: contract.model,
    contents: {
      parts: [
        {
          text: `EDIT THE SUPPLIED IMAGE. ${fixInstruction[target]}
Do not redesign the image. Treat the existing image as the primary composition source.
Resolved contract remains authoritative:
${contract.compiledPrompt}`,
        },
        {
          inlineData: {
            mimeType: parsed.mimeType,
            data: parsed.data,
          },
        },
        ...imageParts(selectedReferences),
      ],
    },
    config: {
      responseModalities: ["IMAGE"] as any,
      imageConfig: {
        aspectRatio: contract.aspectRatio,
        imageSize: contract.resolution,
      },
    },
  });

  for (const part of response.candidates?.[0]?.content?.parts || []) {
    if (part.inlineData?.data) {
      const mimeType = part.inlineData.mimeType || "image/png";
      return {
        imageData: `data:${mimeType};base64,${part.inlineData.data}`,
        contract,
      };
    }
  }

  throw new Error("Targeted Fix gaf geen afbeeldingsdata terug.");
};

export interface AIConcept {
  prop: string;
  customPose: string;
  customCamera: string;
  visualizerPose: PoseType;
  visualizerCamera: CameraAngle;
  visualizerFootwear: FootwearType;
}

export const generateCreativeConcept = async (
  location: LocationType
): Promise<AIConcept> => {
  const ai = createGeminiClient();
  const isErotic = LOCATION_CATEGORIES_MAP[LocationCategory.EROTIC]?.includes(location);
  const validPoses = Object.values(PoseType);
  const validAngles = Object.values(CameraAngle);
  const validFootwear = Object.values(FootwearType);

  const response = await ai.models.generateContent({
    model: "gemini-3.6-flash",
    contents: `Create one coherent adult fashion foot-photography concept for location "${location}".
${isErotic ? "The selected category is adult/private; keep the concept adult and artistic." : ""}
Return JSON only:
{
  "creativeProp":"...",
  "creativePose":"...",
  "creativeAngle":"...",
  "visualizerPoseFallback":"exact value from: ${validPoses.join(" | ")}",
  "visualizerCameraFallback":"exact value from: ${validAngles.join(" | ")}",
  "visualizerFootwearFallback":"exact value from: ${validFootwear.join(" | ")}"
}`,
    config: { responseMimeType: "application/json" },
  });

  try {
    const json = JSON.parse((response.text || "{}").replace(/```json|```/g, "").trim());
    return {
      prop: String(json.creativeProp || "Natural interaction"),
      customPose: String(json.creativePose || PoseType.STANDING),
      customCamera: String(json.creativeAngle || CameraAngle.LOW_ANGLE),
      visualizerPose: enumValueOr(validPoses, json.visualizerPoseFallback, PoseType.STANDING),
      visualizerCamera: enumValueOr(validAngles, json.visualizerCameraFallback, CameraAngle.LOW_ANGLE),
      visualizerFootwear: enumValueOr(validFootwear, json.visualizerFootwearFallback, FootwearType.NONE),
    };
  } catch {
    return {
      prop: "Natural interaction",
      customPose: PoseType.STANDING,
      customCamera: CameraAngle.LOW_ANGLE,
      visualizerPose: PoseType.STANDING,
      visualizerCamera: CameraAngle.LOW_ANGLE,
      visualizerFootwear: FootwearType.NONE,
    };
  }
};

export const generateSocialCaption = async (
  state: DesignState
): Promise<string> => {
  const ai = createGeminiClient();
  const response = await ai.models.generateContent({
    model: "gemini-3.6-flash",
    contents: `Write one short English Instagram caption under 45 words for an adult fashion/editorial image.
Location: ${state.location}
Pose: ${state.customPose || state.pose}
Hosiery: ${state.hosieryType}
Footwear: ${state.footwearType}
Nails: ${state.nailStyle}, ${state.nailColor}
Include 4-7 relevant hashtags. No explanation.`,
  });
  return response.text?.trim() || "Editorial detail study.";
};
