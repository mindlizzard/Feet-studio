import React, { useState } from 'react';
import { DesignState, FootShape, ArchType, SkinTone, SkinTexture, NailShape, NailFinish, NailStyle, HosieryType, HosieryDenier, HosieryPattern, CameraAngle, PoseType, LocationType, LocationCategory, AccessoryType, FilmStock, FootwearType, FootwearState, LightingStyle } from '../types';
import { PROPS_BY_LOCATION, LOCATION_CATEGORIES_MAP, POSES_BY_CATEGORY, FOOTWEAR_BY_CATEGORY } from '../constants';
import { generateCreativeConcept } from '../services/geminiService'; // Import new function
import PoseVisualizer from './PoseVisualizer';

interface Props {
  state: DesignState;
  onChange: (updates: Partial<DesignState>) => void;
  onGenerate: () => void;
  isGenerating: boolean;
  onCloseMobile?: () => void;
}

const SectionTitle: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <h3 className="text-pink-500 font-bold uppercase text-xs tracking-wider mb-3 border-b border-gray-800 pb-1 mt-6">
    {children}
  </h3>
);

const Select: React.FC<{ label: string; value: string; options: string[]; onChange: (val: string) => void }> = ({ label, value, options, onChange }) => (
  <div className="mb-3">
    <label className="block text-gray-400 text-xs mb-1">{label}</label>
    <select 
      value={value} 
      onChange={(e) => onChange(e.target.value)}
      className="w-full bg-gray-800 border border-gray-700 text-gray-200 text-sm rounded px-2 py-2 focus:border-pink-500 focus:outline-none transition-colors"
    >
      {options.map(opt => <option key={opt} value={opt}>{opt}</option>)}
    </select>
  </div>
);

const Toggle: React.FC<{ label: string; checked: boolean; onChange: (val: boolean) => void }> = ({ label, checked, onChange }) => (
    <div className="flex items-center justify-between mb-3 bg-gray-800/50 p-2 rounded">
        <span className="text-gray-300 text-sm">{label}</span>
        <button 
            onClick={() => onChange(!checked)}
            className={`w-10 h-5 rounded-full relative transition-colors ${checked ? 'bg-pink-500' : 'bg-gray-600'}`}
        >
            <div className={`absolute top-1 w-3 h-3 bg-white rounded-full transition-all ${checked ? 'left-6' : 'left-1'}`}></div>
        </button>
    </div>
)

const ControlPanel: React.FC<Props> = ({ state, onChange, onGenerate, isGenerating, onCloseMobile }) => {
  const [isSuggestingProp, setIsSuggestingProp] = useState(false); 
  
  // Dynamic Props Logic
  const currentProps = PROPS_BY_LOCATION[state.location] || [];

  // Helper for random selection
  const pickRandom = <T,>(arr: T[]): T => arr[Math.floor(Math.random() * arr.length)];
  const pickRandomEnum = (enumObj: any) => pickRandom(Object.values(enumObj));

  // --- SMART AI LINKING LOGIC ---
  const handlePropChange = (newProp: string) => {
      let updates: Partial<DesignState> = { prop: newProp };
      const p = newProp.toLowerCase();

      // Clear custom overrides if user manually changes prop
      updates.customPose = undefined;
      updates.customCamera = undefined;

      // Auto-switch Pose based on Prop keywords
      if (p.includes('pedal') || p.includes('pedaal') || p.includes('gas') || p.includes('actie')) {
           updates.pose = PoseType.ACTION_PEDAL;
      } else if (p.includes('bestuurder') || p.includes('driver')) {
           updates.pose = PoseType.DRIVING;
      } else if (p.includes('dashboard')) {
           updates.pose = PoseType.DASHBOARD;
      } else if (p.includes('boss') || (p.includes('bureau') && p.includes('op'))) {
           updates.pose = PoseType.DESK_BOSS;
      } else if (p.includes('onder bureau') || p.includes('verstopt')) {
           updates.pose = PoseType.DESK_UNDER;
      }

      onChange(updates);
  };

  const handleSmartShuffle = () => {
      // 1. Pick Location first
      const randomLocation = pickRandomEnum(LocationType) as LocationType;
      
      // 2. Determine Category to find matching Pose & Check if EROTIC
      let category = LocationCategory.ABSTRACT;
      for (const [cat, locs] of Object.entries(LOCATION_CATEGORIES_MAP)) {
        if (locs.includes(randomLocation)) {
            category = cat as LocationCategory;
            break;
        }
      }

      const isErotic = category === LocationCategory.EROTIC;

      // 3. Pick valid Pose for this category
      const validPoses = POSES_BY_CATEGORY[category] || Object.values(PoseType);
      const randomPose = pickRandom(validPoses);

      // 4. Pick Prop specific to that location (CRITICAL for logic)
      const compatibleProps = PROPS_BY_LOCATION[randomLocation] || ['Standing'];
      const randomProp = pickRandom(compatibleProps);

      // 5. Pick random Hosiery or Barefoot (50/50 chance)
      const useHosiery = Math.random() > 0.5;
      const hosieryType = useHosiery ? (pickRandom(Object.values(HosieryType).filter(t => t !== HosieryType.NONE)) as HosieryType) : HosieryType.NONE;

      // 6. Pick valid Footwear for this category (NEW)
      const validFootwear = FOOTWEAR_BY_CATEGORY[category] || Object.values(FootwearType);
      const footwearType = Math.random() > 0.6 ? pickRandom(validFootwear) : FootwearType.NONE;

      const newState: Partial<DesignState> = {
          location: randomLocation,
          prop: randomProp,
          cameraAngle: pickRandomEnum(CameraAngle) as CameraAngle,
          pose: randomPose,
          
          // Erotic AI Logic: Enforce Erotic Vibe if location is Erotic
          lightingStyle: isErotic ? LightingStyle.CANDLELIGHT : LightingStyle.LOCATION_DEFAULT,

          // Clear Custom
          customPose: undefined,
          customCamera: undefined,
          
          // Styling
          nailColor: '#' + Math.floor(Math.random()*16777215).toString(16),
          nailFinish: pickRandomEnum(NailFinish) as NailFinish,
          nailShape: pickRandomEnum(NailShape) as NailShape,
          nailStyle: pickRandomEnum(NailStyle) as NailStyle,
          
          hosieryType: hosieryType,
          hosieryColor: useHosiery ? (Math.random() > 0.7 ? '#ffffff' : '#000000') : '#000000', // Mostly black/white if hosiery
          hosieryDenier: pickRandomEnum(HosieryDenier) as HosieryDenier,
          
          skinTone: pickRandomEnum(SkinTone) as SkinTone,
          
          // Keep Model Age random but realistic (mostly young adult)
          modelAge: Math.floor(Math.random() * (40 - 18 + 1)) + 18,

          // Randomize new features
          accessory: Math.random() > 0.7 ? pickRandomEnum(AccessoryType) as AccessoryType : AccessoryType.NONE,
          filmStock: Math.random() > 0.5 ? pickRandomEnum(FilmStock) as FilmStock : FilmStock.DIGITAL_CLEAN,
          
          // Footwear (Sometimes)
          footwearType: footwearType,
          footwearState: footwearType !== FootwearType.NONE ? pickRandomEnum(FootwearState) as FootwearState : FootwearState.WORN,
          footwearColor: '#000000'
      };

      onChange(newState);
  };
  
  // New handler for AI Concept Suggestion (Prop + Pose + Camera + Footwear)
  const handleAISuggestConcept = async () => {
      setIsSuggestingProp(true);
      try {
          // Now returns object with creative text strings AND visualizer fallbacks
          const concept = await generateCreativeConcept(state.location);
          onChange({ 
              prop: "✨ AI: " + concept.prop,
              
              // Store the fully custom creative text
              customPose: concept.customPose,
              customCamera: concept.customCamera,
              
              // Set the visualizer to the closest match
              pose: concept.visualizerPose,
              cameraAngle: concept.visualizerCamera,

              // Set the suggested footwear
              footwearType: concept.visualizerFootwear,
              footwearState: FootwearState.WORN // Default to worn for simplicity, user can change
          });
      } catch (e) {
          console.error(e);
      } finally {
          setIsSuggestingProp(false);
      }
  };

  return (
    <div className="w-full h-full bg-gray-950 border-r border-gray-800 overflow-y-auto p-4 flex flex-col scrollbar-thin">
      <div className="mb-6 flex justify-between items-start">
        <div>
            <h1 className="text-2xl font-light text-white tracking-tight">Velvet<span className="text-pink-500 font-bold">Sole</span></h1>
            <p className="text-gray-500 text-xs">Ontwerpstudio v3.0 (NL)</p>
        </div>
        {onCloseMobile && (
            <button 
                onClick={onCloseMobile}
                className="lg:hidden bg-gray-800 text-gray-400 p-2 rounded hover:text-white"
            >
                ✕
            </button>
        )}
      </div>
      
      {/* Magic Shuffle Button */}
      <button 
        onClick={handleSmartShuffle}
        className="w-full mb-6 py-2 bg-gradient-to-r from-indigo-900 to-purple-900 border border-purple-500/30 rounded text-purple-200 text-xs uppercase font-bold tracking-widest hover:brightness-110 transition-all flex items-center justify-center gap-2"
      >
        <span>✨ AI Verrassing / Shuffle</span>
      </button>

      <PoseVisualizer state={state} />

      <SectionTitle>Fotografie & Sfeer</SectionTitle>
      <Select label="Film Stock (Look)" value={state.filmStock} options={Object.values(FilmStock)} onChange={(v) => onChange({ filmStock: v as FilmStock })} />
      <Select label="Belichting (Lichtstudio)" value={state.lightingStyle} options={Object.values(LightingStyle)} onChange={(v) => onChange({ lightingStyle: v as LightingStyle })} />

      <SectionTitle>Anatomie & Biologisch</SectionTitle>
      <Select label="Voetvorm" value={state.footShape} options={Object.values(FootShape)} onChange={(v) => onChange({ footShape: v as FootShape })} />
      <Select label="Wreef / Boog" value={state.archType} options={Object.values(ArchType)} onChange={(v) => onChange({ archType: v as ArchType })} />
      
      {/* AGE SLIDER */}
      <div className="mb-3">
         <label className="block text-gray-400 text-xs mb-1">Model Leeftijd: <span className="text-pink-400">{state.modelAge}</span> jaar</label>
         <input 
            type="range" 
            min="18" 
            max="80" 
            step="1" 
            value={state.modelAge} 
            onChange={(e) => onChange({ modelAge: parseInt(e.target.value) })} 
            className="w-full accent-pink-500 h-1 bg-gray-800 rounded-lg appearance-none cursor-pointer" 
         />
         <div className="flex justify-between text-[9px] text-gray-600 px-1 mt-1 font-mono">
             <span>Jong (18)</span>
             <span>Volwassen (40)</span>
             <span>Senior (80)</span>
         </div>
      </div>

      <div className="grid grid-cols-2 gap-2">
        <Select label="Huidskleur" value={state.skinTone} options={Object.values(SkinTone)} onChange={(v) => onChange({ skinTone: v as SkinTone })} />
        <Select label="Huidtextuur" value={state.skinTexture} options={Object.values(SkinTexture)} onChange={(v) => onChange({ skinTexture: v as SkinTexture })} />
      </div>
      <div className="mb-3">
         <label className="block text-gray-400 text-xs mb-1">Schoenmaat (EU): {state.shoeSize}</label>
         <input type="range" min="35" max="45" step="0.5" value={state.shoeSize} onChange={(e) => onChange({ shoeSize: parseFloat(e.target.value) })} className="w-full accent-pink-500 h-1 bg-gray-800 rounded-lg appearance-none cursor-pointer" />
      </div>

      <SectionTitle>Schoeisel & Accessoires</SectionTitle>
      <Select label="Schoenen / Hakken" value={state.footwearType} options={Object.values(FootwearType)} onChange={(v) => onChange({ footwearType: v as FootwearType })} />
      {state.footwearType !== FootwearType.NONE && (
          <>
            <Select label="Status (Aan/Uit)" value={state.footwearState} options={Object.values(FootwearState)} onChange={(v) => onChange({ footwearState: v as FootwearState })} />
            <div className="mb-3">
                <label className="block text-gray-400 text-xs mb-1">Schoenkleur</label>
                <input type="color" value={state.footwearColor} onChange={(e) => onChange({ footwearColor: e.target.value })} className="w-full h-8 bg-transparent border border-gray-700 rounded cursor-pointer" />
            </div>
          </>
      )}
      <Select label="Sieraden & Tattoos" value={state.accessory || AccessoryType.NONE} options={Object.values(AccessoryType)} onChange={(v) => onChange({ accessory: v as AccessoryType })} />

      <SectionTitle>Nagelart Studio</SectionTitle>
      <div className="bg-gray-900/50 p-3 rounded-lg border border-gray-800 mb-2">
        <Select label="Nagelvorm" value={state.nailShape} options={Object.values(NailShape)} onChange={(v) => onChange({ nailShape: v as NailShape })} />
        
        {/* New Nail Art Selector */}
        <div className="mb-3">
            <label className="block text-gray-400 text-xs mb-1">Design & Art Style</label>
            <select 
              value={state.nailStyle} 
              onChange={(e) => onChange({ nailStyle: e.target.value as NailStyle })}
              className="w-full bg-gray-800 border border-pink-900/30 text-pink-100 text-sm rounded px-2 py-2 focus:border-pink-500 focus:outline-none transition-colors"
            >
              {Object.values(NailStyle).map(opt => <option key={opt} value={opt}>{opt}</option>)}
            </select>
        </div>

        <Select label="Afwerking" value={state.nailFinish} options={Object.values(NailFinish)} onChange={(v) => onChange({ nailFinish: v as NailFinish })} />
        
        <div className="mb-1">
           <label className="block text-gray-400 text-xs mb-1">Basiskleur</label>
           <div className="flex gap-2">
               <input type="color" value={state.nailColor} onChange={(e) => onChange({ nailColor: e.target.value })} className="h-8 w-12 bg-transparent border-none" />
               <input type="text" value={state.nailColor} onChange={(e) => onChange({ nailColor: e.target.value })} className="flex-1 bg-gray-800 border border-gray-700 text-xs px-2 rounded" />
           </div>
        </div>
      </div>

      <SectionTitle>Beenmode (Panty's & Sokken)</SectionTitle>
      <Select label="Type" value={state.hosieryType} options={Object.values(HosieryType)} onChange={(v) => onChange({ hosieryType: v as HosieryType })} />
      {state.hosieryType !== HosieryType.NONE && (
          <>
            <Select label="Dikte (Denier)" value={state.hosieryDenier} options={Object.values(HosieryDenier)} onChange={(v) => onChange({ hosieryDenier: v as HosieryDenier })} />
            <Select label="Patroon / Motief" value={state.hosieryPattern || HosieryPattern.NONE} options={Object.values(HosieryPattern)} onChange={(v) => onChange({ hosieryPattern: v as HosieryPattern })} />
            
            <div className="mb-3">
                <label className="block text-gray-400 text-xs mb-1">Beenmode Kleur</label>
                <input type="color" value={state.hosieryColor} onChange={(e) => onChange({ hosieryColor: e.target.value })} className="w-full h-8 bg-transparent border border-gray-700 rounded cursor-pointer" />
            </div>
            <Toggle label="Vintage / Gedragen Staat" checked={state.wornKnit} onChange={(v) => onChange({ wornKnit: v })} />
          </>
      )}

      <SectionTitle>Scene & Camera</SectionTitle>
      
      {/* Enhanced Grouped Location Selector */}
      <div className="mb-3">
        <label className="block text-gray-400 text-xs mb-1">Locatie</label>
        <select 
            value={state.location}
            onChange={(e) => {
                const newLoc = e.target.value as LocationType;
                // Reset prop to first available for this location to avoid mismatches
                onChange({ location: newLoc, prop: PROPS_BY_LOCATION[newLoc]?.[0] || 'Standing' });
            }}
            className="w-full bg-gray-800 border border-gray-700 text-gray-200 text-sm rounded px-2 py-2 focus:border-pink-500 focus:outline-none transition-colors"
        >
            {Object.entries(LOCATION_CATEGORIES_MAP).map(([category, locations]) => (
                <optgroup key={category} label={category} className="text-gray-400 bg-gray-900 font-bold">
                    {locations.map(loc => (
                        <option key={loc} value={loc} className="text-white bg-gray-800">
                            {loc}
                        </option>
                    ))}
                </optgroup>
            ))}
        </select>
      </div>

      <div className="mb-3">
          <label className="block text-gray-400 text-xs mb-1">Interactie / Object</label>
          <div className="flex gap-2">
               <select 
                  value={state.prop} 
                  onChange={(e) => handlePropChange(e.target.value)}
                  className="flex-1 bg-gray-800 border border-gray-700 text-gray-200 text-sm rounded px-2 py-2 focus:border-pink-500 focus:outline-none transition-colors"
                >
                  {/* If the current prop is a custom AI prop (starts with sparkle), add it to options momentarily so it displays correctly */}
                  {!currentProps.includes(state.prop) && state.prop.startsWith('✨') && (
                      <option key="custom" value={state.prop}>{state.prop}</option>
                  )}
                  {currentProps.map(opt => <option key={opt} value={opt}>{opt}</option>)}
               </select>
               
               {/* NEW: AI Concept Button (Suggests Prop + Pose + Angle) */}
               <button
                  onClick={handleAISuggestConcept}
                  disabled={isSuggestingProp}
                  className="bg-indigo-900/50 hover:bg-indigo-800 border border-indigo-500/30 text-indigo-300 rounded px-3 flex items-center justify-center transition-colors text-xs font-bold"
                  title="Laat AI een volledig concept (Interactie + Pose + Camera + Schoeisel) bedenken"
               >
                   {isSuggestingProp ? '...' : '🧠 AI'}
               </button>
          </div>
      </div>
      
      <div className="mb-3">
          <label className="block text-gray-400 text-xs mb-1">Pose (Auto-aangepast)</label>
          <select 
            value={state.pose} 
            onChange={(e) => onChange({ pose: e.target.value as PoseType, customPose: undefined })} // Clear custom on manual change
            className="w-full bg-gray-800 border border-gray-700 text-gray-200 text-sm rounded px-2 py-2 focus:border-pink-500 focus:outline-none transition-colors"
          >
            {Object.values(PoseType).map(opt => <option key={opt} value={opt}>{opt}</option>)}
          </select>
          {/* Custom Pose Feedback */}
          {state.customPose && (
              <p className="text-[10px] text-purple-400 mt-1 italic border-l-2 border-purple-500 pl-2">
                 ✨ AI Custom: "{state.customPose}"
              </p>
          )}
          {/* Smart Link Feedback (Only if no custom pose) */}
          {!state.customPose && state.pose === PoseType.ACTION_PEDAL && state.prop.toLowerCase().includes('pedal') && (
              <p className="text-[10px] text-pink-400 mt-1 italic">✨ AI: Actieve rij-houding geselecteerd.</p>
          )}
      </div>

      <div className="mb-3">
         <label className="block text-gray-400 text-xs mb-1">Camerahoek</label>
         <select 
            value={state.cameraAngle} 
            onChange={(e) => onChange({ cameraAngle: e.target.value as CameraAngle, customCamera: undefined })} // Clear custom on manual change
            className="w-full bg-gray-800 border border-gray-700 text-gray-200 text-sm rounded px-2 py-2 focus:border-pink-500 focus:outline-none transition-colors"
         >
            {Object.values(CameraAngle).map(opt => <option key={opt} value={opt}>{opt}</option>)}
         </select>
         {/* Custom Camera Feedback */}
         {state.customCamera && (
              <p className="text-[10px] text-purple-400 mt-1 italic border-l-2 border-purple-500 pl-2">
                 ✨ AI Custom: "{state.customCamera}"
              </p>
         )}
      </div>

      <div className="mt-8 mb-20 lg:mb-10">
         <Toggle label="Hoge Kwaliteit (Gemini 3 Pro)" checked={state.useProModel} onChange={(v) => onChange({ useProModel: v })} />
         {state.useProModel && <p className="text-[10px] text-yellow-500/80 mb-2">Vereist aparte API Key selectie via AI Studio.</p>}
         
         <button 
           onClick={() => {
             onGenerate();
             if (onCloseMobile) onCloseMobile();
           }}
           disabled={isGenerating}
           className={`w-full py-4 text-sm uppercase font-bold tracking-widest rounded-lg shadow-lg shadow-pink-500/20 transition-all ${isGenerating ? 'bg-gray-700 cursor-wait' : 'bg-gradient-to-r from-pink-600 to-rose-600 hover:from-pink-500 hover:to-rose-500 text-white'}`}
         >
           {isGenerating ? 'Aan het renderen...' : 'Genereer Ontwerp'}
         </button>
      </div>
    </div>
  );
};

export default ControlPanel;