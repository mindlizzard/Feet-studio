import {
  DesignState,
  CameraAngle,
  FootwearType,
  HosieryType,
  PoseType,
} from "./types";

export type StudioMode = "simple" | "pro";
export type RenderMode = "fast" | "quality" | "pro";
export type RenderResolution = "1K" | "2K" | "4K";
export type AspectRatio = "1:1" | "4:5" | "3:4" | "2:3" | "9:16" | "16:9";
export type ResolverMode = "auto" | "ask" | "strict" | "creative";
export type InspectorMode = "off" | "quick" | "full";
export type DetailPriority = "balanced" | "anatomy" | "hosiery" | "footwear" | "skin" | "nails" | "scene";
export type LensPreset = "16mm" | "24mm" | "35mm" | "50mm" | "85mm" | "105mm" | "Macro";
export type HosieryFinish = "matte" | "natural" | "glossy";
export type MeshSize = "micro" | "fine" | "medium" | "wide";
export type SkinUndertone = "cool" | "neutral" | "warm" | "olive";
export type ReferenceStrength = "exact" | "strong" | "guided" | "inspiration";
export type ReferenceRole =
  | "foot-shape"
  | "skin"
  | "nails"
  | "hosiery"
  | "footwear"
  | "pose"
  | "camera"
  | "scene"
  | "style";

export type StudioSection =
  | "feet"
  | "skin"
  | "nails"
  | "hosiery"
  | "shoes"
  | "pose"
  | "camera"
  | "scene"
  | "light"
  | "references"
  | "render";

export interface FootTuning {
  width: number;
  heelWidth: number;
  instep: number;
  toeSpread: number;
  toeLength: number;
  asymmetry: number;
}

export interface SkinTuning {
  undertone: SkinUndertone;
  pores: number;
  veins: number;
  dryness: number;
  redness: number;
  moisture: number;
}

export interface HosieryTuning {
  finish: HosieryFinish;
  tension: number;
  compression: number;
  wrinkles: number;
  meshSize: MeshSize;
  threadThickness: number;
}

export interface CameraTuning {
  lens: LensPreset;
  distance: number;
  height: number;
  tilt: number;
  roll: number;
  depthOfField: number;
}

export interface LightingTuning {
  intensity: number;
  softness: number;
  temperature: number;
  contrast: number;
}

export interface StudioLocks {
  feet: boolean;
  nails: boolean;
  hosiery: boolean;
  shoes: boolean;
  pose: boolean;
  camera: boolean;
  scene: boolean;
}

export interface StudioV4State {
  version: 4;
  projectName: string;
  mode: StudioMode;
  renderMode: RenderMode;
  resolution: RenderResolution;
  aspectRatio: AspectRatio;
  resolverMode: ResolverMode;
  inspectorMode: InspectorMode;
  detailPriority: DetailPriority;
  batchCount: 1 | 2 | 4;
  variationStrength: number;
  foot: FootTuning;
  skin: SkinTuning;
  hosiery: HosieryTuning;
  camera: CameraTuning;
  lighting: LightingTuning;
  locks: StudioLocks;
}

export interface ReferenceAsset {
  id: string;
  name: string;
  mimeType: string;
  dataUrl: string;
  role: ReferenceRole;
  strength: ReferenceStrength;
  enabled: boolean;
}

export interface ReferencePlanEntry {
  id: string;
  name: string;
  role: ReferenceRole;
  strength: ReferenceStrength;
  mimeType: string;
}

export interface ResolverDecision {
  id: string;
  level: "info" | "warning" | "fix" | "block";
  title: string;
  detail: string;
  applied: boolean;
}

export interface DerivedFacts {
  isWearingHosiery: boolean;
  isFishnet: boolean;
  isSock: boolean;
  isOpaqueHosiery: boolean;
  isWearingShoes: boolean;
  isClosedShoe: boolean;
  shoeIsWorn: boolean;
  shoeIsNearby: boolean;
  toesVisible: boolean;
  nailsVisible: boolean;
  skinVisible: boolean;
  solesCoveredByHosiery: boolean;
  soleFocused: boolean;
  wideCamera: boolean;
}

export interface RenderPlan {
  subject: string[];
  anatomy: string[];
  visibility: string[];
  nails: string[];
  hosiery: string[];
  footwear: string[];
  pose: string[];
  camera: string[];
  scene: string[];
  lighting: string[];
  realism: string[];
  negative: string[];
}

export interface RenderContract {
  id: string;
  createdAt: string;
  requestedState: DesignState;
  effectiveState: DesignState;
  studio: StudioV4State;
  facts: DerivedFacts;
  decisions: ResolverDecision[];
  referencePlan: ReferencePlanEntry[];
  plan: RenderPlan;
  compiledPrompt: string;
  model: string;
  resolution: RenderResolution;
  aspectRatio: AspectRatio;
}

export interface InspectorResult {
  anatomy: "ok" | "warning" | "unknown";
  hosiery: "ok" | "warning" | "unknown";
  nails: "ok" | "warning" | "unknown";
  footwear: "ok" | "warning" | "unknown";
  pose: "ok" | "warning" | "unknown";
  realism: "ok" | "warning" | "unknown";
  issues: string[];
  summary: string;
}

export interface RenderRecord {
  id: string;
  createdAt: string;
  imageData: string;
  parentRenderId?: string;
  fixTarget?: string;
  favorite: boolean;
  contract: RenderContract;
  inspector?: InspectorResult;
}

export interface GenerationResult {
  imageData: string;
  contract: RenderContract;
}

export const INITIAL_STUDIO_V4: StudioV4State = {
  version: 4,
  projectName: "Untitled Project",
  mode: "simple",
  renderMode: "quality",
  resolution: "2K",
  aspectRatio: "3:4",
  resolverMode: "auto",
  inspectorMode: "quick",
  detailPriority: "balanced",
  batchCount: 1,
  variationStrength: 25,
  foot: {
    width: 50,
    heelWidth: 50,
    instep: 60,
    toeSpread: 35,
    toeLength: 50,
    asymmetry: 8,
  },
  skin: {
    undertone: "neutral",
    pores: 45,
    veins: 20,
    dryness: 18,
    redness: 10,
    moisture: 8,
  },
  hosiery: {
    finish: "natural",
    tension: 70,
    compression: 25,
    wrinkles: 12,
    meshSize: "medium",
    threadThickness: 45,
  },
  camera: {
    lens: "85mm",
    distance: 50,
    height: 35,
    tilt: 0,
    roll: 0,
    depthOfField: 48,
  },
  lighting: {
    intensity: 55,
    softness: 72,
    temperature: 50,
    contrast: 45,
  },
  locks: {
    feet: false,
    nails: false,
    hosiery: false,
    shoes: false,
    pose: false,
    camera: false,
    scene: false,
  },
};

export interface WorkspaceState {
  design: DesignState;
  studio: StudioV4State;
}

export const cameraNeedsVehicleContext = (angle: CameraAngle, pose: PoseType): boolean =>
  angle === CameraAngle.DRIVER_POV ||
  pose === PoseType.DRIVING ||
  pose === PoseType.ACTION_PEDAL ||
  pose === PoseType.DASHBOARD;

export const hosieryCoversFeet = (type: HosieryType): boolean => type !== HosieryType.NONE;

export const footwearExists = (type: FootwearType): boolean => type !== FootwearType.NONE;
