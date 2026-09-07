import {
  AccessoryType,
  CameraAngle,
  DesignState,
  FilmStock,
  FootShape,
  FootwearState,
  FootwearType,
  HosieryDenier,
  HosieryPattern,
  HosieryType,
  LightingStyle,
  LocationCategory,
  LocationType,
  NailStyle,
  PoseType,
  SkinTexture,
} from "../types";
import {
  CLOSED_TOE_SHOES,
  LOCATION_CATEGORIES_MAP,
  PROPS_BY_LOCATION,
} from "../constants";
import {
  DerivedFacts,
  ReferenceAsset,
  ReferencePlanEntry,
  RenderContract,
  RenderPlan,
  ResolverDecision,
  StudioV4State,
  cameraNeedsVehicleContext,
  hosieryCoversFeet,
} from "../typesV4";

const uid = () =>
  typeof crypto !== "undefined" && "randomUUID" in crypto
    ? crypto.randomUUID()
    : `render-${Date.now()}-${Math.random().toString(36).slice(2)}`;

const cloneDesign = (state: DesignState): DesignState => ({ ...state });

const clamp = (n: number, min: number, max: number) =>
  Math.min(max, Math.max(min, Number.isFinite(n) ? n : min));

const normalizeHex = (value: string, fallback: string): string =>
  /^#[0-9a-f]{6}$/i.test(value) ? value : fallback;

const getCategory = (location: LocationType): LocationCategory => {
  for (const [category, locations] of Object.entries(LOCATION_CATEGORIES_MAP)) {
    if (locations.includes(location)) return category as LocationCategory;
  }
  return LocationCategory.ABSTRACT;
};

const footGeometry = (shape: FootShape): string => {
  switch (shape) {
    case FootShape.GREEK:
      return "Greek foot geometry: second toe slightly longer than the big toe.";
    case FootShape.EGYPTIAN:
      return "Egyptian foot geometry: toes taper gradually from big toe to little toe.";
    case FootShape.ROMAN:
      return "Roman foot geometry: first three toes are close in length.";
    case FootShape.PEASANT:
      return "Broad forefoot with relatively even toe lengths.";
    case FootShape.MOUNTAIN_PEAK:
      return "Central toes form a gentle peak, with balanced forefoot proportions.";
    case FootShape.PETITE:
      return "Petite compact foot geometry with fine proportions.";
    case FootShape.SLENDER:
      return "Long slender foot geometry with elegant narrow proportions.";
    case FootShape.EAST_ASIAN:
      return "Petite compact geometry with shorter toe proportions. Do not infer skin tone from this geometry label.";
    case FootShape.ARABIC:
      return "Narrow elegant geometry with a defined instep. Do not infer skin tone from this geometry label.";
    case FootShape.SOUTH_ASIAN:
      return "Balanced compact geometry with natural toe variation. Do not infer skin tone from this geometry label.";
    case FootShape.LATINA:
      return "Curved arch-oriented geometry with strong definition. Do not infer skin tone from this geometry label.";
    case FootShape.NORDIC:
      return "Longer foot geometry with a higher instep. Do not infer skin tone from this geometry label.";
    case FootShape.AFRICAN:
      return "Strong athletic foot geometry with natural sole contrast. Do not infer skin tone from this geometry label.";
    default:
      return String(shape);
  }
};

const isOpaqueDenier = (denier: HosieryDenier) =>
  denier === HosieryDenier.D60 || denier === HosieryDenier.D100;

export const normalizeDesignState = (input: DesignState): DesignState => {
  const state = cloneDesign(input);
  state.modelAge = Math.round(clamp(state.modelAge, 18, 80));
  state.shoeSize = clamp(state.shoeSize, 35, 45);
  state.nailColor = normalizeHex(state.nailColor, "#b91c4b");
  state.hosieryColor = normalizeHex(state.hosieryColor, "#111111");
  state.footwearColor = normalizeHex(state.footwearColor, "#111111");

  if (state.footwearType === FootwearType.NONE) state.footwearState = FootwearState.WORN;
  if (state.hosieryType === HosieryType.NONE) state.wornKnit = false;

  const props = PROPS_BY_LOCATION[state.location];
  if (!state.prop?.trim()) state.prop = props?.[0] || "Natural interaction";

  return state;
};

export const deriveDesignFacts = (state: DesignState): DerivedFacts => {
  const isWearingHosiery = state.hosieryType !== HosieryType.NONE;
  const isFishnet = state.hosieryType === HosieryType.FISHNET;
  const isSock =
    state.hosieryType === HosieryType.ANKLE_SOCKS ||
    state.hosieryType === HosieryType.TOE_SOCKS ||
    state.hosieryType === HosieryType.KNEE_HIGHS;
  const isOpaqueHosiery =
    isWearingHosiery && !isFishnet && (isSock || isOpaqueDenier(state.hosieryDenier));
  const isWearingShoes = state.footwearType !== FootwearType.NONE;
  const isClosedShoe = CLOSED_TOE_SHOES.includes(state.footwearType);
  const shoeIsWorn = isWearingShoes && state.footwearState === FootwearState.WORN;
  const shoeIsNearby = isWearingShoes && state.footwearState === FootwearState.NEARBY;
  const toesHiddenByShoes = shoeIsWorn && isClosedShoe;
  const toesVisible = !toesHiddenByShoes;
  const nailsVisible = toesVisible && !isOpaqueHosiery;
  const skinVisible = !toesHiddenByShoes && !isOpaqueHosiery;
  const soleFocused = [
    CameraAngle.MACRO_SOLE,
    CameraAngle.BOTTOM_UP_GLASS,
    CameraAngle.LOW_ANGLE,
  ].includes(state.cameraAngle);
  const wideCamera = [
    CameraAngle.LAP_VIEW,
    CameraAngle.DRIVER_POV,
    CameraAngle.POV,
    CameraAngle.WIDE_STANCE,
    CameraAngle.BED_POV,
    CameraAngle.CINEMATIC,
  ].includes(state.cameraAngle);

  return {
    isWearingHosiery,
    isFishnet,
    isSock,
    isOpaqueHosiery,
    isWearingShoes,
    isClosedShoe,
    shoeIsWorn,
    shoeIsNearby,
    toesVisible,
    nailsVisible,
    skinVisible,
    solesCoveredByHosiery: hosieryCoversFeet(state.hosieryType),
    soleFocused,
    wideCamera,
  };
};

export const resolveDesignState = (
  raw: DesignState,
  studio: StudioV4State
): { effective: DesignState; decisions: ResolverDecision[] } => {
  const effective = normalizeDesignState(raw);
  const decisions: ResolverDecision[] = [];

  const add = (
    id: string,
    level: ResolverDecision["level"],
    title: string,
    detail: string,
    applied: boolean
  ) => decisions.push({ id, level, title, detail, applied });

  if (raw.modelAge < 18) {
    add("adult-age", "fix", "Leeftijd gecorrigeerd", "De minimale modelleeftijd is 18.", true);
  }

  if (
    effective.hosieryType === HosieryType.FISHNET &&
    isOpaqueDenier(effective.hosieryDenier)
  ) {
    add(
      "fishnet-opacity",
      "info",
      "Fishnet blijft open mesh",
      "Hoge denier maakt fishnet niet massief. Mesh-structuur heeft voorrang.",
      true
    );
  }

  const factsBefore = deriveDesignFacts(effective);

  if (
    factsBefore.soleFocused &&
    [PoseType.STANDING, PoseType.PINUP_KICK].includes(effective.pose)
  ) {
    if (studio.resolverMode === "auto" || studio.resolverMode === "strict") {
      effective.pose = PoseType.RECLINED;
      effective.customPose = undefined;
      add(
        "sole-pose",
        "fix",
        "Pose aangepast voor zoolbeeld",
        "Staande pose vervangen door een liggende pose zodat de zool fysiek logisch zichtbaar kan zijn.",
        true
      );
    } else {
      add(
        "sole-pose",
        studio.resolverMode === "ask" ? "warning" : "info",
        "Zoolcamera + staande pose",
        "Deze combinatie kan onnatuurlijke enkel- of beenrotatie veroorzaken.",
        false
      );
    }
  }

  const category = getCategory(effective.location);
  if (
    cameraNeedsVehicleContext(effective.cameraAngle, effective.pose) &&
    category !== LocationCategory.VEHICLE
  ) {
    if (studio.resolverMode === "auto" || studio.resolverMode === "strict") {
      effective.location = LocationType.SPORTS_CAR;
      effective.prop = PROPS_BY_LOCATION[LocationType.SPORTS_CAR]?.[0] || "Car interior";
      add(
        "vehicle-context",
        "fix",
        "Voertuigcontext hersteld",
        "Driver/pedal framing is gekoppeld aan een auto-interieur.",
        true
      );
    } else {
      add(
        "vehicle-context",
        studio.resolverMode === "ask" ? "warning" : "info",
        "Voertuigpose zonder voertuigscene",
        "Driver/pedal framing botst met de gekozen omgeving.",
        false
      );
    }
  }

  const factsAfter = deriveDesignFacts(effective);
  if (factsAfter.isClosedShoe && factsAfter.shoeIsWorn && studio.detailPriority === "nails") {
    add(
      "nails-hidden",
      studio.resolverMode === "strict" ? "block" : "warning",
      "Nagels niet zichtbaar",
      "Gesloten gedragen schoenen bedekken de teennagels. Kies open schoenen, schoenen ernaast of een andere detailprioriteit.",
      false
    );
  }

  if (factsAfter.isOpaqueHosiery && studio.detailPriority === "skin") {
    add(
      "skin-covered",
      "warning",
      "Huiddetail bedekt",
      "Dikke beenmode verbergt huidporiën en aderen. De prompt geeft materiaaltextuur voorrang.",
      true
    );
  }

  return { effective, decisions };
};

export const buildReferencePlan = (
  references: ReferenceAsset[],
  useProModel: boolean
): { selected: ReferenceAsset[]; manifest: ReferencePlanEntry[] } => {
  const enabled = references.filter((r) => r.enabled);
  const budget = useProModel ? 5 : 5;
  const priority = { exact: 0, strong: 1, guided: 2, inspiration: 3 } as const;
  const selected = [...enabled]
    .sort((a, b) => priority[a.strength] - priority[b.strength])
    .slice(0, budget);

  return {
    selected,
    manifest: selected.map(({ id, name, role, strength, mimeType }) => ({
      id,
      name,
      role,
      strength,
      mimeType,
    })),
  };
};

export const buildRenderPlan = (
  state: DesignState,
  studio: StudioV4State,
  facts: DerivedFacts,
  referencePlan: ReferencePlanEntry[]
): RenderPlan => {
  const category = getCategory(state.location);
  const foot = studio.foot;
  const skin = studio.skin;
  const hosiery = studio.hosiery;
  const camera = studio.camera;
  const lighting = studio.lighting;

  const plan: RenderPlan = {
    subject: [
      `Adult model, age ${state.modelAge}.`,
      `Skin tone: ${state.skinTone}; undertone: ${skin.undertone}.`,
      `EU shoe size ${state.shoeSize}.`,
    ],
    anatomy: [
      footGeometry(state.footShape),
      `Arch profile: ${state.archType}.`,
      `Foot geometry tuning: width ${foot.width}/100, heel width ${foot.heelWidth}/100, instep ${foot.instep}/100, toe spread ${foot.toeSpread}/100, toe length ${foot.toeLength}/100.`,
      `Natural left/right asymmetry target ${foot.asymmetry}/100. Keep exactly five toes per foot, anatomically connected and physically plausible.`,
    ],
    visibility: [
      `Toes visible: ${facts.toesVisible ? "yes" : "no"}.`,
      `Toenails visible: ${facts.nailsVisible ? "yes" : "no"}.`,
      `Visible skin surface: ${facts.skinVisible ? "yes" : "no"}.`,
      `Soles covered by selected hosiery: ${facts.solesCoveredByHosiery ? "yes" : "no"}.`,
    ],
    nails: [],
    hosiery: [],
    footwear: [],
    pose: [
      `Pose: ${state.customPose?.trim() || state.pose}.`,
      "Weight distribution, ankle rotation, toe flexion and leg joints must remain physically plausible.",
    ],
    camera: [
      `Camera angle: ${state.customCamera?.trim() || state.cameraAngle}.`,
      `Lens look: ${camera.lens}. Aspect ratio: ${studio.aspectRatio}.`,
      `Camera tuning: distance ${camera.distance}/100, height ${camera.height}/100, tilt ${camera.tilt} degrees, roll ${camera.roll} degrees, depth-of-field ${camera.depthOfField}/100.`,
      facts.wideCamera
        ? "Use environmental context and coherent full-leg perspective without distorting foot proportions."
        : "Keep perspective natural and avoid wide-angle stretching of toes or feet.",
    ],
    scene: [
      `Environment: ${state.location} (${category}).`,
      `Surface/interaction prop: ${state.prop}.`,
      "Feet and footwear must cast contact shadows and physically interact with the surface.",
    ],
    lighting: [
      `Lighting preset: ${state.lightingStyle}. Film look: ${state.filmStock || FilmStock.DIGITAL_CLEAN}.`,
      `Light tuning: intensity ${lighting.intensity}/100, softness ${lighting.softness}/100, temperature ${lighting.temperature}/100, contrast ${lighting.contrast}/100.`,
    ],
    realism: [
      "Photorealistic editorial photography. Preserve natural microcontrast and believable material response.",
      "No beauty-filter plastic skin, no waxy AI smoothing, no floating anatomy, no duplicated body parts.",
    ],
    negative: [
      "extra toes",
      "missing toes",
      "fused toes",
      "duplicated feet",
      "detached feet",
      "impossible ankle rotation",
      "nail polish on skin",
      "toes clipping through closed shoes",
      "painted-on hosiery",
      "mismatched hosiery between legs",
      "bare sole leaking through full-foot hosiery",
      "CGI",
      "3D render",
      "plastic skin",
      "heavy beauty filter",
      "watermark",
      "text artifacts",
    ],
  };

  if (facts.nailsVisible) {
    plan.nails.push(
      `Toenails: ${state.nailLength} length, ${state.nailShape}, ${state.nailStyle}, ${state.nailFinish}, base color ${state.nailColor}.`,
      "Polish is confined to keratin nail plates. Preserve individual nail boundaries and cuticles."
    );
  } else {
    plan.nails.push("Toenail styling is physically hidden by the selected material/footwear. Do not hallucinate visible polish through opaque coverage.");
  }

  if (state.accessory && state.accessory !== AccessoryType.NONE) {
    plan.nails.push(`Accessory detail: ${state.accessory}.`);
  }

  if (!facts.isWearingHosiery) {
    plan.hosiery.push("No hosiery. Bare visible skin where footwear does not cover the feet.");
  } else if (facts.isFishnet) {
    plan.hosiery.push(
      `Fishnet: ${state.hosieryColor}, mesh size ${hosiery.meshSize}, thread thickness ${hosiery.threadThickness}/100.`,
      `Open mesh remains open regardless of denier control. Skin is visible through holes; threads have real thickness and tiny contact shadows.`,
      `Tension ${hosiery.tension}/100, compression ${hosiery.compression}/100, wrinkles ${hosiery.wrinkles}/100, finish ${hosiery.finish}.`,
      "Mesh continues consistently over toes, heel and sole without turning into an opaque sock."
    );
  } else {
    plan.hosiery.push(
      `Hosiery item: ${state.hosieryType}; color ${state.hosieryColor}; denier ${state.hosieryDenier}; pattern ${state.hosieryPattern}.`,
      `Finish ${hosiery.finish}; tension ${hosiery.tension}/100; compression ${hosiery.compression}/100; wrinkles ${hosiery.wrinkles}/100.`,
      facts.isOpaqueHosiery
        ? "Opaque material: show fibers, knit/nylon structure and folds. Hide pores, veins, nail polish and skin gradients under the opaque layer."
        : "Sheer material: visible skin and nail color are optically filtered through the fabric; show stretched nylon fibers, sheen and tension bridging between toes.",
      "The garment is one coherent physical layer on both legs with symmetrical material behavior."
    );
    if (state.wornKnit) plan.hosiery.push("Add subtle realistic wear at stress points, never random damage.");
  }

  if (!facts.isWearingShoes) {
    plan.footwear.push("No shoes worn or present.");
  } else {
    plan.footwear.push(
      `Footwear: ${state.footwearType}, color ${state.footwearColor}, state ${state.footwearState}.`
    );
    if (facts.isClosedShoe && facts.shoeIsWorn) {
      plan.footwear.push("Closed opaque footwear fully contains the toes. Do not render toes or nail polish through the shoe material.");
    } else if (facts.shoeIsNearby) {
      plan.footwear.push("Shoes are next to the feet, not worn. Bare/hosiery-covered feet remain fully visible.");
    } else if (state.footwearState === FootwearState.HALF_OFF) {
      plan.footwear.push("Heel is out of the shoe while the forefoot remains naturally engaged with the shoe.");
    } else if (state.footwearState === FootwearState.DANGLING) {
      plan.footwear.push("Shoe hangs naturally from the forefoot/toes without impossible clipping.");
    }
  }

  if (facts.skinVisible) {
    plan.realism.push(
      `Visible skin: ${state.skinTexture}; pores ${skin.pores}/100, veins ${skin.veins}/100, dryness ${skin.dryness}/100, redness ${skin.redness}/100, moisture ${skin.moisture}/100.`,
      "Skin detail must follow age and pressure/contact areas, with subtle variation rather than noise."
    );
  } else {
    plan.realism.push("Opaque material is the visible surface. Do not force skin pores, veins or bruising through it.");
  }

  if (referencePlan.length) {
    plan.subject.unshift(
      `Reference authority: ${referencePlan
        .map((r) => `${r.role}=${r.strength} (${r.name})`)
        .join("; ")}. Only these listed reference images are actually supplied.`
    );
  }

  return plan;
};

const section = (name: string, rows: string[]) =>
  `${name}:\n${rows.map((row) => `- ${row}`).join("\n")}`;

export const compilePrompt = (
  state: DesignState,
  studio: StudioV4State,
  facts: DerivedFacts,
  plan: RenderPlan,
  references: ReferencePlanEntry[]
): string => {
  const priority =
    studio.detailPriority === "balanced"
      ? "Balance all visible requirements."
      : `Give extra fidelity to ${studio.detailPriority} without violating visibility or physical constraints.`;

  const authority = [
    "AUTHORITY ORDER:",
    "1. Physical visibility and occlusion rules in VISIBILITY / HOSIERY / FOOTWEAR.",
    "2. Actually supplied reference images listed in REFERENCE MANIFEST.",
    "3. Resolved anatomy, pose and camera plan.",
    "4. Styling and atmosphere.",
    "Never resurrect a lower-priority detail that a higher-priority physical layer hides.",
  ].join("\n");

  return [
    "Create ONE photorealistic adult fashion/editorial photograph focused on feet, legwear, footwear and pose fidelity.",
    authority,
    `OUTPUT: ${studio.resolution}, aspect ratio ${studio.aspectRatio}. ${priority}`,
    section("REFERENCE MANIFEST", references.length ? references.map((r) => `${r.role}: ${r.strength} -> ${r.name}`) : ["No reference images supplied."]),
    section("SUBJECT", plan.subject),
    section("ANATOMY", plan.anatomy),
    section("VISIBILITY", plan.visibility),
    section("NAILS", plan.nails),
    section("HOSIERY", plan.hosiery),
    section("FOOTWEAR", plan.footwear),
    section("POSE", plan.pose),
    section("CAMERA", plan.camera),
    section("SCENE", plan.scene),
    section("LIGHTING", plan.lighting),
    section("REALISM", plan.realism),
    `NEGATIVE / AVOID:\n- ${plan.negative.join("\n- ")}`,
    "FINAL CHECK: exactly five toes per visible foot; coherent left/right anatomy; no material clipping; no contradiction with opaque coverage; no extra people or duplicate limbs unless explicitly requested.",
  ].join("\n\n");
};

export const buildRenderContract = (
  requested: DesignState,
  studio: StudioV4State,
  references: ReferenceAsset[] = []
): { contract: RenderContract; selectedReferences: ReferenceAsset[] } => {
  const { effective, decisions } = resolveDesignState(requested, studio);
  const facts = deriveDesignFacts(effective);
  const ref = buildReferencePlan(references, studio.renderMode === "pro");
  const plan = buildRenderPlan(effective, studio, facts, ref.manifest);
  const compiledPrompt = compilePrompt(effective, studio, facts, plan, ref.manifest);
  const model = studio.renderMode === "pro" ? "gemini-3-pro-image" : "gemini-3.1-flash-image";

  const contract: RenderContract = {
    id: uid(),
    createdAt: new Date().toISOString(),
    requestedState: cloneDesign(requested),
    effectiveState: cloneDesign(effective),
    studio: JSON.parse(JSON.stringify(studio)),
    facts,
    decisions,
    referencePlan: ref.manifest,
    plan,
    compiledPrompt,
    model,
    resolution: studio.resolution,
    aspectRatio: studio.aspectRatio,
  };

  return { contract, selectedReferences: ref.selected };
};

export const getRenderReadiness = (contract: RenderContract) => {
  const blockers = contract.decisions.filter((d) => d.level === "block");
  const warnings = contract.decisions.filter((d) => d.level === "warning");
  return {
    ready: blockers.length === 0,
    blockers,
    warnings,
    adjustments: contract.decisions.filter((d) => d.applied),
  };
};
