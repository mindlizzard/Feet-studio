import React, { useMemo, useState } from "react";
import {
  AccessoryType,
  ArchType,
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
  NailFinish,
  NailShape,
  NailStyle,
  PoseType,
  SkinTexture,
  SkinTone,
} from "../types";
import {
  AspectRatio,
  DetailPriority,
  InspectorMode,
  LensPreset,
  ReferenceAsset,
  RenderMode,
  RenderResolution,
  ResolverMode,
  StudioSection,
  StudioV4State,
} from "../typesV4";
import {
  FOOTWEAR_BY_CATEGORY,
  LOCATION_CATEGORIES_MAP,
  POSES_BY_CATEGORY,
  PROPS_BY_LOCATION,
} from "../constants";
import PoseVisualizer from "./PoseVisualizer";
import ReferenceManager from "./ReferenceManager";
import { generateCreativeConcept } from "../services/geminiService";
import {
  clearGeminiApiKey,
  getGeminiApiKey,
  setGeminiApiKey,
} from "../services/apiKeyStore";

interface Props {
  section: StudioSection;
  state: DesignState;
  studio: StudioV4State;
  references: ReferenceAsset[];
  onChange: (updates: Partial<DesignState>) => void;
  onStudioChange: (updates: Partial<StudioV4State>) => void;
  onReferencesChange: (references: ReferenceAsset[]) => void;
  onCloseMobile?: () => void;
}

const titles: Record<StudioSection, { title: string; subtitle: string }> = {
  feet: { title: "Foot Designer", subtitle: "Geometrie, wreef en verhoudingen" },
  skin: { title: "Skin Studio", subtitle: "Tint, textuur en natuurlijk detail" },
  nails: { title: "Nail Studio", subtitle: "Vorm, kleur, finish en nail art" },
  hosiery: { title: "Hosiery Designer", subtitle: "Panty, kousen, mesh en fabric physics" },
  shoes: { title: "Footwear", subtitle: "Type, staat, materiaal-look en accessoires" },
  pose: { title: "Pose Studio", subtitle: "Pose, visualizer en locks" },
  camera: { title: "Camera Studio", subtitle: "Lens, framing en perspectief" },
  scene: { title: "Scene Builder", subtitle: "Omgeving, surface en interactie" },
  light: { title: "Light Studio", subtitle: "Belichting, contrast en film look" },
  references: { title: "Reference Manager", subtitle: "Alleen geselecteerde refs gaan naar Gemini" },
  render: { title: "Render Lab", subtitle: "Model, kwaliteit, resolver en inspectie" },
};

const Select = ({
  label,
  value,
  options,
  onChange,
}: {
  label: string;
  value: string;
  options: string[];
  onChange: (value: string) => void;
}) => (
  <label className="studio-field">
    <span>{label}</span>
    <select className="studio-select" value={value} onChange={(e) => onChange(e.target.value)}>
      {options.map((option) => <option key={option} value={option}>{option}</option>)}
    </select>
  </label>
);

const Slider = ({
  label,
  value,
  min = 0,
  max = 100,
  step = 1,
  suffix = "",
  onChange,
}: {
  label: string;
  value: number;
  min?: number;
  max?: number;
  step?: number;
  suffix?: string;
  onChange: (value: number) => void;
}) => (
  <label className="studio-field">
    <span className="flex justify-between"><span>{label}</span><b className="font-mono text-zinc-300">{value}{suffix}</b></span>
    <input
      type="range"
      min={min}
      max={max}
      step={step}
      value={value}
      onChange={(e) => onChange(Number(e.target.value))}
      className="studio-range"
    />
  </label>
);

const ChipGroup = ({
  values,
  value,
  onChange,
}: {
  values: string[];
  value: string;
  onChange: (value: string) => void;
}) => (
  <div className="flex flex-wrap gap-1.5">
    {values.map((item) => (
      <button
        key={item}
        onClick={() => onChange(item)}
        className={`studio-chip ${value === item ? "studio-chip-active" : ""}`}
      >
        {item}
      </button>
    ))}
  </div>
);

const getCategory = (location: LocationType): LocationCategory => {
  for (const [category, locations] of Object.entries(LOCATION_CATEGORIES_MAP)) {
    if (locations.includes(location)) return category as LocationCategory;
  }
  return LocationCategory.ABSTRACT;
};

const randomItem = <T,>(items: readonly T[]): T => items[Math.floor(Math.random() * items.length)];

const ControlPanel: React.FC<Props> = ({
  section,
  state,
  studio,
  references,
  onChange,
  onStudioChange,
  onReferencesChange,
  onCloseMobile,
}) => {
  const [apiKey, setApiKey] = useState(() => getGeminiApiKey());
  const [suggesting, setSuggesting] = useState(false);
  const meta = titles[section];
  const pro = studio.mode === "pro";
  const locationCategory = useMemo(() => getCategory(state.location), [state.location]);

  const updateFoot = (patch: Partial<StudioV4State["foot"]>) =>
    onStudioChange({ foot: { ...studio.foot, ...patch } });
  const updateSkin = (patch: Partial<StudioV4State["skin"]>) =>
    onStudioChange({ skin: { ...studio.skin, ...patch } });
  const updateHosiery = (patch: Partial<StudioV4State["hosiery"]>) =>
    onStudioChange({ hosiery: { ...studio.hosiery, ...patch } });
  const updateCamera = (patch: Partial<StudioV4State["camera"]>) =>
    onStudioChange({ camera: { ...studio.camera, ...patch } });
  const updateLighting = (patch: Partial<StudioV4State["lighting"]>) =>
    onStudioChange({ lighting: { ...studio.lighting, ...patch } });
  const updateLocks = (patch: Partial<StudioV4State["locks"]>) =>
    onStudioChange({ locks: { ...studio.locks, ...patch } });

  const shuffleSection = () => {
    if (section === "nails" && !studio.locks.nails) {
      const color = `#${Math.floor(Math.random() * 0xffffff).toString(16).padStart(6, "0")}`;
      onChange({
        nailColor: color,
        nailShape: randomItem(Object.values(NailShape)),
        nailFinish: randomItem(Object.values(NailFinish)),
        nailStyle: randomItem(Object.values(NailStyle)),
      });
    } else if (section === "hosiery" && !studio.locks.hosiery) {
      onChange({
        hosieryType: randomItem(Object.values(HosieryType)),
        hosieryDenier: randomItem(Object.values(HosieryDenier)),
        hosieryPattern: randomItem(Object.values(HosieryPattern)),
      });
    } else if (section === "pose" && !studio.locks.pose) {
      const valid = POSES_BY_CATEGORY[locationCategory] || Object.values(PoseType);
      onChange({ pose: randomItem(valid), customPose: undefined });
    } else if (section === "camera" && !studio.locks.camera) {
      onChange({ cameraAngle: randomItem(Object.values(CameraAngle)), customCamera: undefined });
    } else if (section === "scene" && !studio.locks.scene) {
      const location = randomItem(Object.values(LocationType));
      const props = PROPS_BY_LOCATION[location] || ["Natural interaction"];
      onChange({
        location,
        prop: randomItem(props),
        customPose: undefined,
        customCamera: undefined,
      });
    } else if (section === "shoes" && !studio.locks.shoes) {
      const options = FOOTWEAR_BY_CATEGORY[locationCategory] || Object.values(FootwearType);
      onChange({ footwearType: randomItem(options), footwearState: FootwearState.WORN });
    } else if (section === "feet" && !studio.locks.feet) {
      onChange({ footShape: randomItem(Object.values(FootShape)), archType: randomItem(Object.values(ArchType)) });
    }
  };

  const aiConcept = async () => {
    setSuggesting(true);
    try {
      const concept = await generateCreativeConcept(state.location);
      onChange({
        prop: `✨ AI: ${concept.prop}`,
        customPose: concept.customPose,
        customCamera: concept.customCamera,
        pose: concept.visualizerPose,
        cameraAngle: concept.visualizerCamera,
        footwearType: concept.visualizerFootwear,
        footwearState: FootwearState.WORN,
      });
    } finally {
      setSuggesting(false);
    }
  };

  return (
    <aside className="studio-panel h-full overflow-y-auto">
      <div className="sticky top-0 z-20 border-b border-white/5 bg-[#101319]/95 px-4 py-4 backdrop-blur-xl">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h2 className="text-sm font-bold tracking-wide text-zinc-100">{meta.title}</h2>
            <p className="mt-0.5 text-[10px] text-zinc-500">{meta.subtitle}</p>
          </div>
          <div className="flex gap-1">
            <button onClick={shuffleSection} className="studio-icon-button" title="Shuffle alleen deze sectie">↻</button>
            {onCloseMobile && <button onClick={onCloseMobile} className="studio-icon-button md:hidden">×</button>}
          </div>
        </div>
        <div className="mt-3 flex rounded-xl bg-black/30 p-1">
          {(["simple", "pro"] as const).map((mode) => (
            <button
              key={mode}
              onClick={() => onStudioChange({ mode })}
              className={`flex-1 rounded-lg px-2 py-1.5 text-[10px] font-bold uppercase tracking-wider transition ${
                studio.mode === mode ? "bg-white/10 text-white" : "text-zinc-600 hover:text-zinc-300"
              }`}
            >
              {mode}
            </button>
          ))}
        </div>
      </div>

      <div className="space-y-4 p-4">
        {section === "feet" && (
          <>
            <Select label="Voetvorm" value={state.footShape} options={Object.values(FootShape)} onChange={(v) => onChange({ footShape: v as FootShape })} />
            <Select label="Wreef / boog" value={state.archType} options={Object.values(ArchType)} onChange={(v) => onChange({ archType: v as ArchType })} />
            <Slider label="Schoenmaat EU" value={state.shoeSize} min={35} max={45} step={0.5} onChange={(shoeSize) => onChange({ shoeSize })} />
            {pro && (
              <div className="studio-card">
                <h3 className="studio-card-title">Geometrie fijnregeling</h3>
                <Slider label="Voetbreedte" value={studio.foot.width} onChange={(width) => updateFoot({ width })} />
                <Slider label="Hielbreedte" value={studio.foot.heelWidth} onChange={(heelWidth) => updateFoot({ heelWidth })} />
                <Slider label="Wreefhoogte" value={studio.foot.instep} onChange={(instep) => updateFoot({ instep })} />
                <Slider label="Teen-spread" value={studio.foot.toeSpread} onChange={(toeSpread) => updateFoot({ toeSpread })} />
                <Slider label="Teenlengte" value={studio.foot.toeLength} onChange={(toeLength) => updateFoot({ toeLength })} />
                <Slider label="Natuurlijke asymmetrie" value={studio.foot.asymmetry} onChange={(asymmetry) => updateFoot({ asymmetry })} />
              </div>
            )}
            <button onClick={() => updateLocks({ feet: !studio.locks.feet })} className={`studio-lock ${studio.locks.feet ? "studio-lock-active" : ""}`}>
              {studio.locks.feet ? "🔒 Feet gelockt" : "🔓 Lock Feet"}
            </button>
          </>
        )}

        {section === "skin" && (
          <>
            <Select label="Huidskleur" value={state.skinTone} options={Object.values(SkinTone)} onChange={(v) => onChange({ skinTone: v as SkinTone })} />
            <Select label="Huidtextuur" value={state.skinTexture} options={Object.values(SkinTexture)} onChange={(v) => onChange({ skinTexture: v as SkinTexture })} />
            <div className="studio-field">
              <span>Undertone</span>
              <ChipGroup values={["cool", "neutral", "warm", "olive"]} value={studio.skin.undertone} onChange={(undertone) => updateSkin({ undertone: undertone as any })} />
            </div>
            <Slider label="Modelleeftijd" value={state.modelAge} min={18} max={80} onChange={(modelAge) => onChange({ modelAge })} />
            {pro && (
              <div className="studio-card">
                <h3 className="studio-card-title">Natural detail</h3>
                <Slider label="Poriën" value={studio.skin.pores} onChange={(pores) => updateSkin({ pores })} />
                <Slider label="Aderen" value={studio.skin.veins} onChange={(veins) => updateSkin({ veins })} />
                <Slider label="Droogte" value={studio.skin.dryness} onChange={(dryness) => updateSkin({ dryness })} />
                <Slider label="Roodheid" value={studio.skin.redness} onChange={(redness) => updateSkin({ redness })} />
                <Slider label="Vocht / glans" value={studio.skin.moisture} onChange={(moisture) => updateSkin({ moisture })} />
              </div>
            )}
          </>
        )}

        {section === "nails" && (
          <>
            <Select label="Nagelvorm" value={state.nailShape} options={Object.values(NailShape)} onChange={(v) => onChange({ nailShape: v as NailShape })} />
            <Select label="Nail art" value={state.nailStyle} options={Object.values(NailStyle)} onChange={(v) => onChange({ nailStyle: v as NailStyle })} />
            <Select label="Finish" value={state.nailFinish} options={Object.values(NailFinish)} onChange={(v) => onChange({ nailFinish: v as NailFinish })} />
            <label className="studio-field">
              <span>Basiskleur</span>
              <div className="flex gap-2">
                <input type="color" value={state.nailColor} onChange={(e) => onChange({ nailColor: e.target.value })} className="h-10 w-14 rounded-xl border border-white/10 bg-transparent" />
                <input value={state.nailColor} onChange={(e) => onChange({ nailColor: e.target.value })} className="studio-input flex-1" />
              </div>
            </label>
            {pro && (
              <div className="studio-field">
                <span>Nagellengte</span>
                <ChipGroup values={["Short", "Medium", "Long"]} value={state.nailLength} onChange={(nailLength) => onChange({ nailLength })} />
              </div>
            )}
            <button onClick={() => updateLocks({ nails: !studio.locks.nails })} className={`studio-lock ${studio.locks.nails ? "studio-lock-active" : ""}`}>
              {studio.locks.nails ? "🔒 Nails gelockt" : "🔓 Lock Nails"}
            </button>
          </>
        )}

        {section === "hosiery" && (
          <>
            <Select label="Type" value={state.hosieryType} options={Object.values(HosieryType)} onChange={(v) => onChange({ hosieryType: v as HosieryType })} />
            {state.hosieryType !== HosieryType.NONE && (
              <>
                {state.hosieryType !== HosieryType.FISHNET && (
                  <Select label="Denier" value={state.hosieryDenier} options={Object.values(HosieryDenier)} onChange={(v) => onChange({ hosieryDenier: v as HosieryDenier })} />
                )}
                <Select label="Patroon" value={state.hosieryPattern} options={Object.values(HosieryPattern)} onChange={(v) => onChange({ hosieryPattern: v as HosieryPattern })} />
                <label className="studio-field">
                  <span>Kleur</span>
                  <input type="color" value={state.hosieryColor} onChange={(e) => onChange({ hosieryColor: e.target.value })} className="h-10 w-full rounded-xl border border-white/10 bg-transparent" />
                </label>
                <div className="studio-field">
                  <span>Finish</span>
                  <ChipGroup values={["matte", "natural", "glossy"]} value={studio.hosiery.finish} onChange={(finish) => updateHosiery({ finish: finish as any })} />
                </div>
                {state.hosieryType === HosieryType.FISHNET && (
                  <div className="studio-card">
                    <h3 className="studio-card-title">Fishnet physics</h3>
                    <div className="studio-field">
                      <span>Mesh size</span>
                      <ChipGroup values={["micro", "fine", "medium", "wide"]} value={studio.hosiery.meshSize} onChange={(meshSize) => updateHosiery({ meshSize: meshSize as any })} />
                    </div>
                    <Slider label="Draaddikte" value={studio.hosiery.threadThickness} onChange={(threadThickness) => updateHosiery({ threadThickness })} />
                  </div>
                )}
                {pro && (
                  <div className="studio-card">
                    <h3 className="studio-card-title">Fabric physics</h3>
                    <Slider label="Tension" value={studio.hosiery.tension} onChange={(tension) => updateHosiery({ tension })} />
                    <Slider label="Compression" value={studio.hosiery.compression} onChange={(compression) => updateHosiery({ compression })} />
                    <Slider label="Wrinkles" value={studio.hosiery.wrinkles} onChange={(wrinkles) => updateHosiery({ wrinkles })} />
                    <button onClick={() => onChange({ wornKnit: !state.wornKnit })} className={`studio-chip ${state.wornKnit ? "studio-chip-active" : ""}`}>Worn fabric</button>
                  </div>
                )}
              </>
            )}
            <button onClick={() => updateLocks({ hosiery: !studio.locks.hosiery })} className={`studio-lock ${studio.locks.hosiery ? "studio-lock-active" : ""}`}>
              {studio.locks.hosiery ? "🔒 Hosiery gelockt" : "🔓 Lock Hosiery"}
            </button>
          </>
        )}

        {section === "shoes" && (
          <>
            <Select label="Schoenen" value={state.footwearType} options={Object.values(FootwearType)} onChange={(v) => onChange({ footwearType: v as FootwearType })} />
            {state.footwearType !== FootwearType.NONE && (
              <>
                <Select label="Status" value={state.footwearState} options={Object.values(FootwearState)} onChange={(v) => onChange({ footwearState: v as FootwearState })} />
                <label className="studio-field">
                  <span>Kleur</span>
                  <input type="color" value={state.footwearColor} onChange={(e) => onChange({ footwearColor: e.target.value })} className="h-10 w-full rounded-xl border border-white/10 bg-transparent" />
                </label>
              </>
            )}
            <Select label="Accessoires / tattoo" value={state.accessory} options={Object.values(AccessoryType)} onChange={(v) => onChange({ accessory: v as AccessoryType })} />
            <button onClick={() => updateLocks({ shoes: !studio.locks.shoes })} className={`studio-lock ${studio.locks.shoes ? "studio-lock-active" : ""}`}>
              {studio.locks.shoes ? "🔒 Shoes gelockt" : "🔓 Lock Shoes"}
            </button>
          </>
        )}

        {section === "pose" && (
          <>
            <PoseVisualizer state={state} />
            <Select label="Pose" value={state.pose} options={Object.values(PoseType)} onChange={(v) => onChange({ pose: v as PoseType, customPose: undefined })} />
            {pro && (
              <label className="studio-field">
                <span>Custom pose instruction</span>
                <textarea className="studio-textarea" value={state.customPose || ""} onChange={(e) => onChange({ customPose: e.target.value || undefined })} placeholder="Optionele precieze pose-instructie..." />
              </label>
            )}
            <button onClick={() => updateLocks({ pose: !studio.locks.pose })} className={`studio-lock ${studio.locks.pose ? "studio-lock-active" : ""}`}>
              {studio.locks.pose ? "🔒 Pose gelockt" : "🔓 Lock Pose"}
            </button>
          </>
        )}

        {section === "camera" && (
          <>
            <Select label="Camera angle" value={state.cameraAngle} options={Object.values(CameraAngle)} onChange={(v) => onChange({ cameraAngle: v as CameraAngle, customCamera: undefined })} />
            <div className="studio-field">
              <span>Lens</span>
              <ChipGroup values={["16mm", "24mm", "35mm", "50mm", "85mm", "105mm", "Macro"]} value={studio.camera.lens} onChange={(lens) => updateCamera({ lens: lens as LensPreset })} />
            </div>
            <div className="studio-field">
              <span>Aspect ratio</span>
              <ChipGroup values={["1:1", "4:5", "3:4", "2:3", "9:16", "16:9"]} value={studio.aspectRatio} onChange={(aspectRatio) => onStudioChange({ aspectRatio: aspectRatio as AspectRatio })} />
            </div>
            {pro && (
              <div className="studio-card">
                <h3 className="studio-card-title">Camera tuning</h3>
                <Slider label="Afstand" value={studio.camera.distance} onChange={(distance) => updateCamera({ distance })} />
                <Slider label="Hoogte" value={studio.camera.height} onChange={(height) => updateCamera({ height })} />
                <Slider label="Tilt" value={studio.camera.tilt} min={-45} max={45} suffix="°" onChange={(tilt) => updateCamera({ tilt })} />
                <Slider label="Roll" value={studio.camera.roll} min={-45} max={45} suffix="°" onChange={(roll) => updateCamera({ roll })} />
                <Slider label="Depth of field" value={studio.camera.depthOfField} onChange={(depthOfField) => updateCamera({ depthOfField })} />
                <label className="studio-field">
                  <span>Custom camera instruction</span>
                  <textarea className="studio-textarea" value={state.customCamera || ""} onChange={(e) => onChange({ customCamera: e.target.value || undefined })} />
                </label>
              </div>
            )}
            <button onClick={() => updateLocks({ camera: !studio.locks.camera })} className={`studio-lock ${studio.locks.camera ? "studio-lock-active" : ""}`}>
              {studio.locks.camera ? "🔒 Camera gelockt" : "🔓 Lock Camera"}
            </button>
          </>
        )}

        {section === "scene" && (
          <>
            <Select
              label="Categorie"
              value={locationCategory}
              options={Object.values(LocationCategory)}
              onChange={(v) => {
                const category = v as LocationCategory;
                const location = LOCATION_CATEGORIES_MAP[category]?.[0] || LocationType.STUDIO;
                onChange({
                  location,
                  prop: PROPS_BY_LOCATION[location]?.[0] || "Natural interaction",
                  customPose: undefined,
                  customCamera: undefined,
                });
              }}
            />
            <Select
              label="Locatie"
              value={state.location}
              options={LOCATION_CATEGORIES_MAP[locationCategory] || Object.values(LocationType)}
              onChange={(v) => {
                const location = v as LocationType;
                onChange({
                  location,
                  prop: PROPS_BY_LOCATION[location]?.[0] || "Natural interaction",
                  customPose: undefined,
                  customCamera: undefined,
                });
              }}
            />
            <Select label="Surface / prop" value={state.prop} options={PROPS_BY_LOCATION[state.location] || [state.prop]} onChange={(prop) => onChange({ prop, customPose: undefined, customCamera: undefined })} />
            <button onClick={() => void aiConcept()} disabled={suggesting} className="studio-primary-button w-full">
              {suggesting ? "AI concept maken..." : "✨ AI Scene Concept"}
            </button>
            <button onClick={() => updateLocks({ scene: !studio.locks.scene })} className={`studio-lock ${studio.locks.scene ? "studio-lock-active" : ""}`}>
              {studio.locks.scene ? "🔒 Scene gelockt" : "🔓 Lock Scene"}
            </button>
          </>
        )}

        {section === "light" && (
          <>
            <Select label="Lighting preset" value={state.lightingStyle} options={Object.values(LightingStyle)} onChange={(v) => onChange({ lightingStyle: v as LightingStyle })} />
            <Select label="Film look" value={state.filmStock} options={Object.values(FilmStock)} onChange={(v) => onChange({ filmStock: v as FilmStock })} />
            {pro && (
              <div className="studio-card">
                <h3 className="studio-card-title">Light tuning</h3>
                <Slider label="Intensity" value={studio.lighting.intensity} onChange={(intensity) => updateLighting({ intensity })} />
                <Slider label="Softness" value={studio.lighting.softness} onChange={(softness) => updateLighting({ softness })} />
                <Slider label="Temperature" value={studio.lighting.temperature} onChange={(temperature) => updateLighting({ temperature })} />
                <Slider label="Contrast" value={studio.lighting.contrast} onChange={(contrast) => updateLighting({ contrast })} />
              </div>
            )}
          </>
        )}

        {section === "references" && (
          <ReferenceManager references={references} onChange={onReferencesChange} />
        )}

        {section === "render" && (
          <>
            <div className="studio-field">
              <span>Render mode</span>
              <ChipGroup values={["fast", "quality", "pro"]} value={studio.renderMode} onChange={(renderMode) => onStudioChange({ renderMode: renderMode as RenderMode, resolution: renderMode === "pro" ? "4K" : studio.resolution })} />
            </div>
            <div className="studio-field">
              <span>Resolutie</span>
              <ChipGroup values={["1K", "2K", "4K"]} value={studio.resolution} onChange={(resolution) => onStudioChange({ resolution: resolution as RenderResolution })} />
            </div>
            <Select label="Detail priority" value={studio.detailPriority} options={["balanced", "anatomy", "hosiery", "footwear", "skin", "nails", "scene"]} onChange={(v) => onStudioChange({ detailPriority: v as DetailPriority })} />
            <Select label="Resolver" value={studio.resolverMode} options={["auto", "ask", "strict", "creative"]} onChange={(v) => onStudioChange({ resolverMode: v as ResolverMode })} />
            <Select label="Post-render inspector" value={studio.inspectorMode} options={["off", "quick", "full"]} onChange={(v) => onStudioChange({ inspectorMode: v as InspectorMode })} />
            <div className="studio-field">
              <span>Batch</span>
              <ChipGroup values={["1", "2", "4"]} value={String(studio.batchCount)} onChange={(v) => onStudioChange({ batchCount: Number(v) as 1 | 2 | 4 })} />
            </div>

            <div className="studio-card">
              <h3 className="studio-card-title">Gemini API</h3>
              <input
                type="password"
                autoComplete="off"
                className="studio-input"
                placeholder="AIza..."
                value={apiKey}
                onChange={(e) => {
                  setApiKey(e.target.value);
                  setGeminiApiKey(e.target.value);
                }}
              />
              <div className="mt-2 flex items-center justify-between">
                <span className="text-[10px] text-zinc-600">Alleen sessionStorage. Niet in de build gebakken.</span>
                <button
                  className="text-[10px] text-zinc-500 hover:text-red-300"
                  onClick={() => {
                    clearGeminiApiKey();
                    setApiKey("");
                  }}
                >
                  Wissen
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </aside>
  );
};

export default ControlPanel;
