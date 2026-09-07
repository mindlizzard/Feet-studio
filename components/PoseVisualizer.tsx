import React, { useState, useRef, useEffect } from 'react';
import { DesignState, CameraAngle, PoseType, SkinTone, HosieryDenier, HosieryPattern, HosieryType, SkinTexture, NailStyle, FootwearType, FootwearState } from '../types';
import { CLOSED_TOE_SHOES } from '../constants';

interface Props {
  state: DesignState;
}

const getSkinColor = (tone: SkinTone): string => {
  switch (tone) {
    case SkinTone.PORCELAIN: return '#ffe0d0';
    case SkinTone.FAIR: return '#f5cbb8';
    case SkinTone.OLIVE: return '#d4a88c';
    case SkinTone.TAN: return '#c68642';
    case SkinTone.DEEP_BRONZE: return '#8d5524';
    case SkinTone.EBONY: return '#3b2219';
    default: return '#f5cbb8';
  }
};

const getHosieryOpacity = (denier: HosieryDenier): number => {
  switch (denier) {
    case HosieryDenier.D5: return 0.15;
    case HosieryDenier.D15: return 0.35;
    case HosieryDenier.D30: return 0.55;
    case HosieryDenier.D60: return 0.85;
    case HosieryDenier.D100: return 0.98;
    default: return 0;
  }
};

interface ViewTransform {
    x: number;
    y: number;
    z: number;
    scale: number;
    tz: number;
}

const DEFAULT_VIEW: ViewTransform = { x: 10, y: 15, z: 0, scale: 1, tz: 0 };

const CAMERA_PRESETS: Record<CameraAngle, ViewTransform> = {
    // Standard
    [CameraAngle.TOP_DOWN]: { x: 60, y: 0, z: 0, scale: 1, tz: 0 },
    [CameraAngle.LOW_ANGLE]: { x: -20, y: 10, z: 0, scale: 1, tz: 0 },
    [CameraAngle.SIDE_PROFILE]: { x: 0, y: 80, z: 0, scale: 1, tz: 0 },
    [CameraAngle.MACRO_SOLE]: { x: 90, y: 0, z: 0, scale: 1.5, tz: 0 },
    [CameraAngle.MACRO_TOES]: { x: 10, y: 0, z: 0, scale: 1.5, tz: 50 },
    [CameraAngle.DUTCH]: { x: 10, y: 0, z: 35, scale: 1, tz: 0 },
    [CameraAngle.SELFIE]: { x: 45, y: 180, z: 0, scale: 1, tz: 0 },

    // New / Extended
    [CameraAngle.POV]: { x: 50, y: 0, z: 0, scale: 1, tz: 0 },
    [CameraAngle.LAP_VIEW]: { x: 45, y: 0, z: 0, scale: 0.6, tz: -50 }, 
    [CameraAngle.DRIVER_POV]: { x: 35, y: -10, z: 5, scale: 0.65, tz: -40 },
    [CameraAngle.BOTTOM_UP_GLASS]: { x: -80, y: 0, z: 0, scale: 1, tz: 0 },
    [CameraAngle.HEEL_FOCUS]: { x: 10, y: 160, z: 0, scale: 1.4, tz: 20 },
    [CameraAngle.ARCH_FOCUS]: { x: 0, y: 90, z: -10, scale: 1.3, tz: 10 },
    [CameraAngle.FISH_EYE]: { x: 0, y: 0, z: 0, scale: 0.5, tz: -100 },
    [CameraAngle.CCTV]: { x: 45, y: 45, z: 0, scale: 0.7, tz: -50 },
    [CameraAngle.DRONE]: { x: 75, y: 20, z: 0, scale: 0.4, tz: -100 },
    [CameraAngle.OVER_SHOULDER]: { x: 30, y: 20, z: 0, scale: 0.8, tz: -20 },
    [CameraAngle.REAR_VIEW]: { x: 0, y: 180, z: 0, scale: 1, tz: 0 },
    [CameraAngle.WIDE_STANCE]: { x: -10, y: 0, z: 0, scale: 0.8, tz: -20 },
    [CameraAngle.CINEMATIC]: { x: 5, y: 45, z: 0, scale: 0.9, tz: -10 },
    [CameraAngle.POLAROID]: { x: 0, y: 0, z: 0, scale: 1, tz: 0 },
    [CameraAngle.NIGHT_VISION]: { x: 10, y: 10, z: 0, scale: 1, tz: 0 },
    [CameraAngle.UNDERWATER_SPLIT]: { x: -5, y: 0, z: 0, scale: 1, tz: 0 },
    [CameraAngle.KEYHOLE]: { x: 0, y: 0, z: 0, scale: 1.5, tz: 0 },
    [CameraAngle.BETWEEN_LEGS]: { x: -30, y: 0, z: 0, scale: 1, tz: 0 },
    [CameraAngle.LYING_SIDE]: { x: 0, y: 90, z: 90, scale: 1, tz: 0 },
    [CameraAngle.BED_POV]: { x: 60, y: 10, z: 0, scale: 0.8, tz: -10 },
    [CameraAngle.GLAMOUR]: { x: 15, y: 30, z: 5, scale: 1.1, tz: 0 },
    [CameraAngle.PAPARAZZI]: { x: 10, y: 20, z: 0, scale: 0.5, tz: -100 },
    [CameraAngle.REFLECTION_PUDDLE]: { x: -60, y: 0, z: 0, scale: 1, tz: 0 },
    [CameraAngle.SHADOW_PLAY]: { x: 0, y: 90, z: 0, scale: 1, tz: 0 },
    [CameraAngle.UPSIDE_DOWN]: { x: 0, y: 0, z: 180, scale: 1, tz: 0 }
};

const PoseVisualizer: React.FC<Props> = ({ state }) => {
  const [rotation, setRotation] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const containerRef = useRef<HTMLDivElement>(null);
  const isDragging = useRef(false);
  const lastMouse = useRef({ x: 0, y: 0 });

  // Reset interaction when camera angle changes
  useEffect(() => {
    setRotation({ x: 0, y: 0 });
    setZoom(1);
  }, [state.cameraAngle]);

  const handleStart = (clientX: number, clientY: number) => {
    isDragging.current = true;
    lastMouse.current = { x: clientX, y: clientY };
  };

  const handleMove = (clientX: number, clientY: number) => {
    if (!isDragging.current) return;
    const deltaX = clientX - lastMouse.current.x;
    const deltaY = clientY - lastMouse.current.y;
    setRotation(prev => ({
      x: prev.x - deltaY * 0.5,
      y: prev.y + deltaX * 0.5
    }));
    lastMouse.current = { x: clientX, y: clientY };
  };

  const handleEnd = () => {
    isDragging.current = false;
  };

  // Mouse Events
  const onMouseDown = (e: React.MouseEvent) => handleStart(e.clientX, e.clientY);
  const onMouseMove = (e: React.MouseEvent) => handleMove(e.clientX, e.clientY);
  const onMouseUp = handleEnd;
  const onMouseLeave = handleEnd;
  const onWheel = (e: React.WheelEvent) => {
    setZoom(prev => Math.min(Math.max(0.5, prev - e.deltaY * 0.001), 3));
  };

  // Touch Events
  const onTouchStart = (e: React.TouchEvent) => handleStart(e.touches[0].clientX, e.touches[0].clientY);
  const onTouchMove = (e: React.TouchEvent) => handleMove(e.touches[0].clientX, e.touches[0].clientY);
  const onTouchEnd = handleEnd;

  // Visual Helper Functions
  const skinColor = getSkinColor(state.skinTone);
  const baseView = CAMERA_PRESETS[state.cameraAngle] || DEFAULT_VIEW;

  // Combine automatic camera preset with manual interaction
  const finalTransform = `
    scale(${baseView.scale * zoom}) 
    translateX(0px) translateY(0px) translateZ(${baseView.tz}px)
    rotateX(${baseView.x + rotation.x}deg) 
    rotateY(${baseView.y + rotation.y}deg) 
    rotateZ(${baseView.z}deg)
  `;

  // --- Footwear Physics Visualizer Logic ---
  const isClosedShoe = CLOSED_TOE_SHOES.includes(state.footwearType);
  const isShoeWorn = state.footwearType !== FootwearType.NONE && state.footwearState === FootwearState.WORN;
  
  // Logic to determine if nails should be rendered
  // Hidden if Closed Shoe is WORN
  // Visible if Closed Shoe is DANGLING / HALF_OFF
  let showNails = state.hosieryType === HosieryType.NONE || 
                    state.hosieryType === HosieryType.PANTYHOSE || 
                    state.hosieryType === HosieryType.THIGH_HIGHS || 
                    state.hosieryType === HosieryType.KNEE_HIGHS || 
                    state.hosieryType === HosieryType.FISHNET ||
                    state.hosieryType === HosieryType.VINTAGE_FF ||
                    state.hosieryType === HosieryType.VINTAGE_RHT;
  
  if (isClosedShoe && isShoeWorn) {
      showNails = false;
  }

  // --- Skin Texture Logic ---
  const getSkinStyle = () => {
    // Base Ambient Occlusion (Cylindrical Shape)
    const baseStyle: React.CSSProperties = { 
        backgroundColor: skinColor,
        backgroundImage: `linear-gradient(90deg, rgba(0,0,0,0.15) 0%, transparent 20%, transparent 80%, rgba(0,0,0,0.15) 100%)`
    };

    // Check if covered by opaque hosiery (D60, D100, Socks)
    const isSock = state.hosieryType === HosieryType.ANKLE_SOCKS || state.hosieryType === HosieryType.TOE_SOCKS;
    const isFishnet = state.hosieryType === HosieryType.FISHNET;
    const isOpaque = !isFishnet && (isSock || state.hosieryDenier === HosieryDenier.D60 || state.hosieryDenier === HosieryDenier.D100);

    // CRITICAL: If opaque hosiery is worn OR closed shoes are worn, return plain base skin.
    // This PREVENTS texture bleed-through (veins/sweat) entirely by not rendering them at all.
    if ((state.hosieryType !== HosieryType.NONE && isOpaque) || (isClosedShoe && isShoeWorn)) {
        return baseStyle;
    }

    // Only apply textures if skin is visible/sheer
    const style = { ...baseStyle };

    switch (state.skinTexture) {
      case SkinTexture.OILED:
        // High gloss, cylindrical sheen animation
        style.boxShadow = 'inset 0 0 20px rgba(0,0,0,0.2)'; // Deep depth
        // Layered: Specular Highlight (Animated) + Cylindrical Highlight (Static)
        style.backgroundImage = `
            linear-gradient(105deg, transparent 30%, rgba(255,255,255,0.7) 45%, rgba(255,255,255,0.9) 50%, rgba(255,255,255,0.7) 55%, transparent 70%),
            linear-gradient(90deg, rgba(0,0,0,0.1) 0%, rgba(255,255,255,0.3) 25%, rgba(255,255,255,0.1) 50%, rgba(0,0,0,0.1) 100%)
        `;
        style.backgroundSize = '200% 100%, 100% 100%';
        style.animation = 'skin-shine 3s infinite linear';
        break;

      case SkinTexture.SWEATY:
        // Wet skin look: High contrast sheen + droplets
        style.boxShadow = 'inset 0 0 10px rgba(0,0,0,0.1)';
        style.backgroundImage = `
            radial-gradient(circle, rgba(255,255,255,0.6) 1px, transparent 1.5px),
            linear-gradient(90deg, rgba(0,0,0,0.05) 0%, rgba(255,255,255,0.2) 30%, rgba(255,255,255,0.4) 50%, rgba(255,255,255,0.2) 70%, rgba(0,0,0,0.05) 100%)
        `;
        style.backgroundSize = '12px 12px, 100% 100%';
        break;

      case SkinTexture.VEINY:
        // Subtle blue veins running longitudinally
        style.backgroundImage = `
            linear-gradient(90deg, rgba(0,0,0,0.1) 0%, transparent 100%),
            linear-gradient(170deg, transparent 40%, rgba(0,40,100,0.06) 45%, transparent 50%),
            linear-gradient(10deg, transparent 30%, rgba(0,40,100,0.04) 35%, transparent 40%)
        `;
        break;

      case SkinTexture.REALISTIC:
        // Pores + soft cylindrical shading
        style.backgroundImage = `
            radial-gradient(rgba(0,0,0,0.05) 1px, transparent 0),
            linear-gradient(90deg, rgba(0,0,0,0.1) 0%, transparent 20%, transparent 80%, rgba(0,0,0,0.1) 100%)
        `;
        style.backgroundSize = '4px 4px, 100% 100%';
        break;

      default: // Smooth
        // Just the base cylindrical shading
        break;
    }
    return style;
  };

  const skinStyle = getSkinStyle();

  // --- Nail Art Visualizer Logic ---
  const getNailStyle = () => {
    const baseColor = state.nailColor;
    const style: React.CSSProperties = {
        backgroundColor: baseColor,
        boxShadow: '0 1px 1px rgba(0,0,0,0.2)'
    };

    switch (state.nailStyle) {
        case NailStyle.FRENCH_CLASSIC:
            style.backgroundImage = `linear-gradient(to bottom, white 30%, ${baseColor} 30%)`;
            break;
        case NailStyle.FRENCH_MICRO:
            style.backgroundImage = `linear-gradient(to bottom, ${baseColor} 5%, ${baseColor} 10%, ${baseColor} 100%)`; 
            style.borderTop = `1px solid ${baseColor === '#ffffff' ? '#000' : '#fff'}`; // Tiny tip hint
            break;
        case NailStyle.FRENCH_BLACK:
            style.backgroundImage = `linear-gradient(to bottom, black 30%, ${baseColor} 30%)`;
            break;
        case NailStyle.OMBRE:
            style.backgroundImage = `linear-gradient(to bottom, white, ${baseColor})`;
            break;
        case NailStyle.MARBLE:
             style.backgroundImage = `
                radial-gradient(circle, rgba(255,255,255,0.4) 2px, transparent 3px),
                linear-gradient(45deg, ${baseColor}, #eee)
             `;
             break;
        case NailStyle.CAT_EYE:
            style.backgroundImage = `linear-gradient(120deg, ${baseColor} 40%, #fff 50%, ${baseColor} 60%)`;
            break;
        case NailStyle.GEMS_BASE:
            // Simulate a gem at base
            style.boxShadow = '0 1px 1px rgba(0,0,0,0.2), inset 0 -3px 0 transparent, inset 0 2px 2px rgba(255,255,255,0.9)';
            break;
    }
    return style;
  };

  const nailStyle = getNailStyle();

  // --- Footwear Overlay Logic (NEW) ---
  const getFootwearStyle = () => {
      if (state.footwearType === FootwearType.NONE || state.footwearState === FootwearState.NEARBY) return null;

      const baseStyle: React.CSSProperties = {
          backgroundColor: state.footwearColor,
          opacity: 1,
          zIndex: 40, // Topmost
          borderRadius: '1.5rem', // Match foot rounding (rounded-3xl)
          boxShadow: 'inset 0 0 10px rgba(0,0,0,0.5)',
          position: 'absolute',
          inset: 0,
          transition: 'transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1)'
      };

      // Open toe logic (Straps representation)
      const isOpen = !CLOSED_TOE_SHOES.includes(state.footwearType);
      
      if (isOpen) {
          // Represent sandals as straps (Transparent bands)
          baseStyle.background = `repeating-linear-gradient(90deg, ${state.footwearColor}, ${state.footwearColor} 15px, transparent 15px, transparent 35px)`;
          baseStyle.backgroundColor = 'transparent'; // Important
          baseStyle.boxShadow = 'none';
          baseStyle.borderBottom = `4px solid ${state.footwearColor}`; // Sole
      }

      // State Transformations (Physics)
      switch (state.footwearState) {
          case FootwearState.DANGLING:
              // Shoe hangs off the toes. Rotate down/out.
              // Added strong internal shadow (inset) to simulate the empty heel cup
              // Added external shadow to show separation from foot.
              baseStyle.transform = 'rotateX(60deg) translateY(45px) translateZ(15px)';
              baseStyle.transformOrigin = 'top center'; 
              baseStyle.boxShadow = '0 -15px 30px rgba(0,0,0,0.4), inset 0 25px 20px rgba(0,0,0,0.5)';
              break;
          case FootwearState.HALF_OFF:
              // Shoe slipped down the heel significantly.
              // Slight Z rotation for realism (slipped sideways).
              baseStyle.transform = 'translateY(55px) translateZ(-15px) rotateX(25deg) rotateZ(-5deg)';
              baseStyle.boxShadow = '0 -5px 15px rgba(0,0,0,0.2), inset 0 15px 15px rgba(0,0,0,0.4)';
              break;
          case FootwearState.WORN:
          default:
              baseStyle.transform = 'none';
              break;
      }

      return baseStyle;
  };

  const footwearStyle = getFootwearStyle();

  // --- Hosiery Texture Logic ---
  const getHosieryStyle = (part: 'leg' | 'foot') => {
    // 1. FOOTWEAR OVERRIDE (Feet only)
    // Only apply the shoe color override to the FOOT segment. The LEG segment should remain hosiery-clad.
    if (part === 'foot' && isClosedShoe && isShoeWorn) {
        return {
            backgroundColor: state.footwearColor,
            opacity: 1,
            zIndex: 30, // Above everything
            boxShadow: 'inset 0 0 10px rgba(0,0,0,0.5)' // Shoe depth
        } as React.CSSProperties;
    }

    if (state.hosieryType === HosieryType.NONE) return {};
    
    // Check if this is a Sock type (Strict opacity enforcement)
    const isSock = state.hosieryType === HosieryType.ANKLE_SOCKS || state.hosieryType === HosieryType.TOE_SOCKS;

    // Denier Opacity
    let opacity = getHosieryOpacity(state.hosieryDenier);
    if (isSock) opacity = 1; // Force opaque for socks

    const isSheer = !isSock && opacity < 0.6;
    const isVintage = state.hosieryType === HosieryType.VINTAGE_FF || state.hosieryType === HosieryType.VINTAGE_RHT;
    
    // Check for Wet Locations
    const isWetLocation = state.location.includes('Zwembad') || 
                          state.location.includes('Pool') || 
                          state.location.includes('Waterval') || 
                          state.location.includes('Waterfall') ||
                          state.location.includes('Rivier') || 
                          state.location.includes('River') ||
                          state.location.includes('Douche') ||
                          state.location.includes('Shower');

    // Smart Blend Mode Logic
    let mixBlendMode = 'normal';
    
    if (isSheer) {
        // Calculate brightness of hosiery color
        const hex = state.hosieryColor.replace('#', '');
        if (hex.length === 6) {
            const r = parseInt(hex.substring(0, 2), 16);
            const g = parseInt(hex.substring(2, 4), 16);
            const b = parseInt(hex.substring(4, 6), 16);
            const brightness = (r * 299 + g * 587 + b * 114) / 1000;
            mixBlendMode = brightness < 128 ? 'multiply' : 'normal'; 
        }
    }

    // CSS Layer Stack (Order is: TOP -> BOTTOM)
    // IMPORTANT: Top layers are listed first in background-image
    let backgroundLayers = [];

    // 0. WET EFFECT (Absolute Top Priority - Physics Layer)
    if (isWetLocation) {
        // Sharp specular highlight for wet sheen (clinging water tension)
        backgroundLayers.push(`linear-gradient(105deg, transparent 40%, rgba(255,255,255,0.5) 45%, rgba(255,255,255,0.9) 48%, rgba(255,255,255,0.5) 55%, transparent 60%)`);
        // Darkening/Saturation gradient (Wet fabric looks darker)
        backgroundLayers.push(`linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, transparent 50%, rgba(0,0,0,0.2) 100%)`);
    }

    // VISUAL BAND for Thigh Highs / Knee Highs (Leg Part Only)
    if (part === 'leg') {
        if (state.hosieryType === HosieryType.THIGH_HIGHS) {
            backgroundLayers.push(`linear-gradient(to bottom, rgba(0,0,0,0.5) 0%, rgba(0,0,0,0.5) 8%, transparent 8.5%)`);
        } else if (state.hosieryType === HosieryType.KNEE_HIGHS) {
            backgroundLayers.push(`linear-gradient(to bottom, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0.3) 5%, transparent 5.5%)`);
        }
    }

    // 1. PILLING (Refined - Subtler & Randomized)
    if (state.wornKnit) {
        // Layer 1: Micro-fuzz (very dense, very transparent)
        backgroundLayers.push(`radial-gradient(circle, rgba(255,255,255,0.05) 0.5px, transparent 0.5px) 0 0 / 3px 3px`);
        // Layer 2: Small pills (scattered) using prime number spacing to avoid grid
        backgroundLayers.push(`radial-gradient(circle, rgba(255,255,255,0.1) 0.8px, transparent 1px) 0 0 / 17px 17px`); 
        // Layer 3: Occasional larger pill
        backgroundLayers.push(`radial-gradient(circle, rgba(255,255,255,0.08) 1px, transparent 1.5px) 9px 9px / 37px 37px`);
    }
    
    // 2. FINISH / SHEEN / TEXTURE
    // Move these ABOVE patterns so they apply over the pattern
    if (isSheer) {
        // VINTAGE SPECIFIC: Glassy/Nylon Sheen (Distinct from modern lycra)
        if (isVintage && !isWetLocation) { // Wet sheen overrides/combines
             backgroundLayers.push(`linear-gradient(100deg, transparent 35%, rgba(255,255,255,0.15) 45%, rgba(255,255,255,0.3) 50%, rgba(255,255,255,0.15) 55%, transparent 65%)`);
        }

        // Standard Anisotropic Highlight (Key Light)
        backgroundLayers.push(`linear-gradient(105deg, transparent 40%, rgba(255,255,255,0.4) 45%, rgba(255,255,255,0.7) 48%, rgba(255,255,255,0.4) 52%, transparent 60%)`);
        // General ambient glow
        backgroundLayers.push(`radial-gradient(circle at 30% 20%, rgba(255,255,255,0.2) 0%, transparent 60%)`); 
        // Micro-mesh noise
        backgroundLayers.push(`radial-gradient(rgba(255,255,255,0.1) 0.5px, transparent 0.5px) 0 0 / 3px 3px`);
    } else {
        // OPAQUE / MATTE FINISH:
        // No white highlights. Just absorption shadow gradients.
        backgroundLayers.push(`linear-gradient(to bottom, transparent 0%, rgba(0,0,0,0.1) 50%, rgba(0,0,0,0.4) 100%)`);
        // Subtle knit texture
        backgroundLayers.push(`radial-gradient(rgba(255,255,255,0.03) 1px, transparent 1px) 0 0 / 2px 2px`);
    }

    // 3. THINNING (Structural wear - Stress Points Only - Foot Part Only)
    // Only applied if fabric is not fully opaque/thick socks (unless wornKnit is on, then we allow some thinning even on opaque for holes, but here we keep it subtle)
    if (part === 'foot' && state.wornKnit && state.hosieryColor !== 'transparent' && !isSock) {
         // Heel Thinning (Top of foot segment) - blend skin color in
         // Tight gradient at the very top edge
         backgroundLayers.push(`radial-gradient(circle at 50% 0%, ${skinColor} 0%, rgba(255,255,255,0) 30%)`);
         
         // Toe Thinning (Bottom of foot segment) - blend skin color in
         // Tight gradient at the very bottom edge
         backgroundLayers.push(`radial-gradient(circle at 50% 100%, ${skinColor} 0%, rgba(255,255,255,0) 25%)`);
    }

    // 4. VINTAGE FF & RHT LOGIC
    // Moved INSIDE background stack so wet/sheen layers apply ON TOP of the seam
    const pColor = isSheer ? 'rgba(0,0,0,0.7)' : 'rgba(0,0,0,0.2)';

    // VINTAGE FF: Seam + Cuban Heel (Rectangular Block)
    if (state.hosieryType === HosieryType.VINTAGE_FF || state.hosieryPattern === HosieryPattern.SEAM) {
        // The Seam (Thin vertical Line)
        backgroundLayers.push(`linear-gradient(90deg, transparent 49%, ${pColor} 49.5%, ${pColor} 50.5%, transparent 51%)`);
        
        // The Cuban Heel (Block at bottom) - Only if specifically FF
        if (state.hosieryType === HosieryType.VINTAGE_FF) {
             backgroundLayers.push(`linear-gradient(to top, ${pColor} 0%, ${pColor} 15%, transparent 15.1%)`);
        }
    }

    // VINTAGE RHT: Reinforced Heel & Toe (Darker patches, No Seam)
    if (state.hosieryType === HosieryType.VINTAGE_RHT) {
         // Darker Toe (Bottom)
         backgroundLayers.push(`radial-gradient(ellipse at 50% 100%, ${pColor} 0%, transparent 40%)`);
         // Darker Heel (Top - approximate)
         backgroundLayers.push(`radial-gradient(ellipse at 50% 0%, ${pColor} 0%, transparent 30%)`);
    }

    // 5. PATTERNS
    if (state.hosieryPattern !== HosieryPattern.NONE) {
        switch (state.hosieryPattern) {
            case HosieryPattern.DOTS:
                backgroundLayers.push(`radial-gradient(${pColor} 1.5px, transparent 2px) 0 0 / 12px 12px`);
                break;
            case HosieryPattern.HEARTS:
                backgroundLayers.push(`radial-gradient(${pColor} 1px, transparent 1.5px) 0 0 / 14px 14px`); 
                backgroundLayers.push(`radial-gradient(${pColor} 1.5px, transparent 2px) 7px 7px / 14px 14px`);
                break;
            case HosieryPattern.BOWS:
                backgroundLayers.push(`radial-gradient(ellipse at center, ${pColor} 20%, transparent 25%) 0 0 / 20px 20px`);
                break;
            case HosieryPattern.DIAMOND:
                backgroundLayers.push(`repeating-linear-gradient(45deg, ${pColor} 0, ${pColor} 1px, transparent 0, transparent 50%)`);
                backgroundLayers.push(`repeating-linear-gradient(-45deg, ${pColor} 0, ${pColor} 1px, transparent 0, transparent 50%)`);
                break;
            case HosieryPattern.LACE:
                backgroundLayers.push(`radial-gradient(circle, ${pColor} 1px, transparent 1.5px) 0 0 / 6px 6px`);
                backgroundLayers.push(`radial-gradient(circle, ${pColor} 1px, transparent 2px) 3px 3px / 8px 8px`);
                break;
            case HosieryPattern.LOGOS:
                backgroundLayers.push(`linear-gradient(0deg, transparent 95%, ${pColor} 96%, transparent 97%)`);
                backgroundLayers.push(`linear-gradient(90deg, transparent 95%, ${pColor} 96%, transparent 97%)`);
                break;
        }
    }

    return {
        backgroundColor: state.hosieryColor,
        opacity: opacity,
        backgroundImage: backgroundLayers.join(', '),
        backgroundSize: 'auto',
        boxShadow: !isSheer 
            ? 'inset 0 0 25px rgba(0,0,0,0.6), inset 0 5px 15px rgba(0,0,0,0.2)' // Matte/Opaque depth (Heavy inset)
            : 'inset 0 0 5px rgba(255,255,255,0.2)', // Sheer contour
        mixBlendMode: mixBlendMode
    } as React.CSSProperties;
  };

  // Pre-calculate styles for Leg and Foot independently
  const hosieryStyleLeg = getHosieryStyle('leg');
  const hosieryStyleFoot = getHosieryStyle('foot');
  
  // Coverage Logic
  const isLegCovering = [
      HosieryType.PANTYHOSE, 
      HosieryType.THIGH_HIGHS, 
      HosieryType.KNEE_HIGHS, 
      HosieryType.FISHNET,
      HosieryType.VINTAGE_FF, 
      HosieryType.VINTAGE_RHT
  ].includes(state.hosieryType);

  const isAnkleSock = state.hosieryType === HosieryType.ANKLE_SOCKS;
  const isToeSock = state.hosieryType === HosieryType.TOE_SOCKS;

  // --- Dynamic Environment / Achtergrondeffecten ---
  const renderDynamicAmbience = () => {
    // Match strings from types.ts (Dutch)
    const loc = state.location;
    
    let ambienceClass = "bg-gradient-to-t from-gray-900 via-gray-800 to-gray-900"; // Fallback
    let content = null;

    // 1. Water / Pool / Yacht
    if (loc.includes('Zwembad') || loc.includes('Pool') || loc.includes('Jacht') || loc.includes('Spa') || loc.includes('Bubbelbad') || loc.includes('Rivier')) {
        ambienceClass = "bg-gradient-to-b from-cyan-900/60 to-blue-950";
        content = (
          <>
            {/* Caustics Simulation: Rotating overlapping gradients */}
            <div className="absolute inset-0 opacity-20 animate-[spin_20s_linear_infinite]" 
                 style={{backgroundImage: 'radial-gradient(circle, rgba(255,255,255,0.15) 2px, transparent 4px)', backgroundSize: '40px 40px'}}>
            </div>
             <div className="absolute inset-0 opacity-20 animate-[spin_15s_linear_infinite_reverse]" 
                 style={{backgroundImage: 'radial-gradient(ellipse, rgba(100,200,255,0.1) 10%, transparent 60%)', backgroundSize: '100% 100%'}}>
            </div>
          </>
        );
    } 
    // 2. Nature / Forest / Garden
    else if (loc.includes('Natuur') || loc.includes('Bos') || loc.includes('Tuin') || loc.includes('Mos')) {
        ambienceClass = "bg-gradient-to-b from-green-900/50 to-emerald-950";
        content = (
             <div className="absolute inset-0 overflow-hidden">
                 {/* Fireflies / Pollen */}
                 <div className="absolute top-1/4 left-1/4 w-1.5 h-1.5 bg-yellow-100/40 rounded-full animate-[ping_3s_ease-in-out_infinite]"></div>
                 <div className="absolute top-3/4 left-2/3 w-1 h-1 bg-white/30 rounded-full animate-[pulse_4s_ease-in-out_infinite]"></div>
                 <div className="absolute top-1/2 right-1/4 w-1 h-1 bg-yellow-100/20 rounded-full animate-[bounce_5s_infinite]"></div>
                 {/* Dappled Light */}
                 <div className="absolute inset-0 opacity-20" style={{background: 'radial-gradient(circle at 50% 0, rgba(255,255,200,0.1), transparent 70%)'}}></div>
             </div>
        );
    } 
    // 3. Warm / Domestic / Luxury
    else if (loc.includes('Slaapkamer') || loc.includes('Woonkamer') || loc.includes('Privéjet') || loc.includes('Luxe') || loc.includes('Hotel')) {
        ambienceClass = "bg-gradient-to-r from-rose-900/30 to-orange-900/20";
        content = (
            <div className="absolute inset-0">
                {/* Dust motes */}
                <div className="absolute top-1/3 left-1/3 w-20 h-20 bg-orange-500/5 rounded-full blur-2xl animate-pulse"></div>
            </div>
        );
    }
    // 4. Urban / Cyberpunk / Club
    else if (loc.includes('Stad') || loc.includes('Cyberpunk') || loc.includes('Nachtclub') || loc.includes('Neon') || loc.includes('Metro')) {
        ambienceClass = "bg-gray-950";
        content = (
             <div className="absolute inset-0 opacity-30">
                 {/* Neon Grid Hint */}
                 <div className="absolute bottom-0 w-full h-1/2 bg-gradient-to-t from-pink-500/10 to-transparent" 
                      style={{backgroundImage: 'linear-gradient(90deg, rgba(236,72,153,0.1) 1px, transparent 1px)', backgroundSize: '40px 100%'}}>
                 </div>
                 {/* Smog/Fog */}
                 <div className="absolute top-0 w-full h-full bg-gradient-to-b from-purple-900/20 to-transparent"></div>
             </div>
        );
    }

    return (
       <div className={`absolute inset-0 ${ambienceClass} -z-10 overflow-hidden transition-colors duration-1000`}>
           {content}
           {/* Universal Floor Grid for Perspective */}
           <div className="absolute bottom-0 w-full h-1/2 bg-gradient-to-t from-black/60 to-transparent" 
                style={{
                    backgroundImage: 'linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px)', 
                    backgroundSize: '40px 40px', 
                    transform: 'perspective(500px) rotateX(60deg) translateY(100px) scale(2)',
                    opacity: 0.5
                }}>
           </div>
       </div>
    );
  };
  
  // Render Nearby Shoes (Shoes on floor next to feet)
  const renderNearbyShoes = () => {
      if (state.footwearState !== FootwearState.NEARBY || state.footwearType === FootwearType.NONE) return null;
      
      return (
          <div className="absolute bottom-10 right-20 w-12 h-20 opacity-80" style={{ transform: 'rotateX(70deg) rotateZ(-30deg)' }}>
              <div className="w-full h-full bg-gray-800 rounded-2xl border-4 border-gray-700" style={{ backgroundColor: state.footwearColor }}></div>
              <div className="absolute top-0 -left-14 w-full h-full bg-gray-800 rounded-2xl border-4 border-gray-700" style={{ backgroundColor: state.footwearColor, transform: 'rotateZ(20deg)' }}></div>
          </div>
      );
  };

  // --- Poses (CSS Transforms) ---
  const getLegTransform = (side: 'left' | 'right') => {
      const isLeft = side === 'left';
      
      switch (state.pose) {
          case PoseType.CROSSED:
              return isLeft ? 'rotateZ(15deg) translate(20px, 0)' : 'rotateZ(-15deg) translate(-20px, 0) translateZ(10px)';
          case PoseType.POINTING:
              return isLeft ? 'rotateX(30deg)' : 'rotateX(30deg) translateZ(-10px)';
          case PoseType.ARCHED:
              return isLeft ? 'rotateX(45deg)' : 'rotateX(50deg) translateY(-10px)';
          case PoseType.SCRUNCHED:
              return 'scale(0.95)'; // Simplified
          case PoseType.DRIVING: // Right leg extended (pedal), Left leg relaxed
              return isLeft ? 'rotateX(10deg) translateX(-10px)' : 'rotateX(40deg) translateX(10px) translateZ(-20px)';
          case PoseType.ACTION_PEDAL: // Active pressing
               return isLeft ? 'rotateX(10deg) translateX(-10px)' : 'rotateX(55deg) translateX(10px) translateZ(-25px)';
          case PoseType.DASHBOARD: // Legs up high
              return isLeft ? 'rotateX(-60deg) rotateZ(10deg) translateY(-40px)' : 'rotateX(-60deg) rotateZ(-10deg) translateY(-40px) translateZ(10px)';
          case PoseType.DESK_BOSS: // Legs crossed on table
              return isLeft ? 'rotateX(-45deg) rotateZ(5deg) translateY(-30px)' : 'rotateX(-45deg) rotateZ(-5deg) translateY(-30px) translateZ(15px)';
          case PoseType.DESK_UNDER:
              return isLeft ? 'rotateX(20deg) translateX(-5px)' : 'rotateX(20deg) translateX(5px)';
          case PoseType.RECLINED:
              return isLeft ? 'rotateX(-10deg) translateX(-5px)' : 'rotateX(-10deg) translateX(5px)';
          case PoseType.WALL_LEGS:
              return isLeft ? 'rotateX(-80deg) translateX(-5px)' : 'rotateX(-80deg) translateX(5px)';
          case PoseType.PINUP_KICK:
              return isLeft ? 'rotateX(0deg)' : 'rotateX(-60deg) translateY(-30px) rotateZ(10deg)';
          case PoseType.PINUP_KNEEL:
              return isLeft ? 'rotateX(70deg) translateZ(-10px)' : 'rotateX(70deg) translateZ(-10px) translateX(10px)';
          case PoseType.PINUP_CROSS:
              return isLeft ? 'rotateZ(25deg) translate(20px, 0)' : 'rotateZ(-25deg) translate(-20px, 0) translateZ(15px)';
          case PoseType.PINUP_RECLINE:
              return isLeft ? 'rotateX(-40deg) rotateZ(5deg)' : 'rotateX(-50deg) rotateZ(-5deg) translateZ(10px)';
          default:
              return '';
      }
  };

  const renderLegContent = () => (
    <>
         {/* Calf / Leg Segment */}
         {/* Using rounded-t-md instead of full to imply continuity up to hips/body */}
         <div className="mannequin-part w-12 h-40 bg-gray-300 rounded-b-full rounded-t-md overflow-hidden" style={skinStyle}>
             {/* Hosiery Overlay (Only for full-leg hosiery like Panty/ThighHighs) */}
             {isLegCovering && (
                 <div className="absolute inset-0 z-10" style={hosieryStyleLeg}></div>
             )}
         </div>
         {/* Foot */}
         <div className="mannequin-part w-14 h-24 bg-gray-300 rounded-3xl origin-top absolute top-36 left-0 -translate-x-1 overflow-hidden" 
              style={{ ...skinStyle, transform: 'rotateX(30deg)' }}>
              
              {/* Toes (Nails) - CONDITIONALLY RENDERED */}
              {showNails && (
                  <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1 z-10">
                    {[1,2,3,4,5].map(i => (
                        <div key={i} className="w-2 h-2 rounded-sm" style={nailStyle}></div>
                    ))}
                  </div>
              )}

              {/* Hosiery Overlay (Rendered AFTER nails to simulate transparency) */}
              {/* 1. Full coverage types (Panty, Thigh, Knee, Net) */}
              {isLegCovering && (
                  <div className="absolute inset-0 rounded-3xl z-20" style={hosieryStyleFoot}></div>
              )}
              
              {/* 2. Socks OR Shoe Overlay (Full foot coverage) */}
              {((isAnkleSock || isToeSock) || (isClosedShoe && isShoeWorn)) && (
                   <div className="absolute inset-0 rounded-3xl z-20" style={hosieryStyleFoot}></div>
              )}

              {/* 3. FOOTWEAR OVERLAY (New Layer) */}
              {footwearStyle && (
                  <div className="absolute inset-0 z-40" style={footwearStyle}></div>
              )}
         </div>
    </>
  );

  return (
    <div 
        ref={containerRef}
        className="w-full h-64 bg-gray-900 rounded-lg relative overflow-hidden cursor-move touch-none mb-6 shadow-inner border border-gray-800 group"
        onMouseDown={onMouseDown}
        onMouseMove={onMouseMove}
        onMouseUp={onMouseUp}
        onMouseLeave={onMouseLeave}
        onWheel={onWheel}
        onTouchStart={onTouchStart}
        onTouchMove={onTouchMove}
        onTouchEnd={onTouchEnd}
        title="Interactief 3D Voorbeeld (Sleep om te draaien, Scroll om te zoomen)"
    >
      <style>{`
        @keyframes skin-shine {
          0% { background-position: 200% 0, 0 0; }
          100% { background-position: -200% 0, 0 0; }
        }
      `}</style>

      {renderDynamicAmbience()}
      
      {/* 3D Scene */}
      <div className="scene-container w-full h-full flex items-center justify-center" style={{ transform: finalTransform, transition: isDragging.current ? 'none' : 'transform 0.5s cubic-bezier(0.2, 0.8, 0.2, 1)' }}>
        
        {/* Helper Grid/Floor for Context */}
        <div className="absolute w-[400px] h-[400px] bg-white/5 rounded-full transform rotateX(90deg) translateZ(-100px) blur-xl"></div>
        
        {/* Nearby Shoes (Floor) */}
        {renderNearbyShoes()}

        {/* --- LEFT LEG --- */}
        <div className="absolute" style={{ transform: `translateX(-25px) ${getLegTransform('left')}` }}>
            {renderLegContent()}
        </div>

        {/* --- RIGHT LEG --- */}
        <div className="absolute" style={{ transform: `translateX(25px) ${getLegTransform('right')}` }}>
            {renderLegContent()}
        </div>

      </div>

      {/* Camera UI Overlay */}
      <div className="absolute top-2 right-2 flex flex-col gap-1 items-end pointer-events-none">
          <div className="text-[10px] text-pink-500 font-bold bg-black/50 px-2 py-1 rounded backdrop-blur">
              CAM: {state.cameraAngle}
          </div>
          {state.wornKnit && (
              <div className="text-[10px] text-yellow-500 font-bold bg-black/50 px-2 py-1 rounded backdrop-blur border border-yellow-500/30">
                  ⚠️ VINTAGE LOOK
              </div>
          )}
      </div>

    </div>
  );
};

export default PoseVisualizer;