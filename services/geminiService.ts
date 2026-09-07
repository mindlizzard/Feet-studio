import { GoogleGenAI } from "@google/genai";
import { DesignState, HosieryType, HosieryDenier, SkinTexture, HosieryPattern, LocationType, LocationCategory, CameraAngle, PoseType, FootShape, NailStyle, AccessoryType, FilmStock, FootwearType, FootwearState, LightingStyle, AIConcept } from "../types";
import { LOCATION_CATEGORIES_MAP, CLOSED_TOE_SHOES, OPEN_TOE_SHOES } from "../constants";

// Helper to determine lighting based on location category
const getLightingAndAtmosphere = (location: LocationType, override?: LightingStyle): string => {
    
    // Manual Override Logic
    if (override && override !== LightingStyle.LOCATION_DEFAULT) {
        switch (override) {
            case LightingStyle.GOLDEN_HOUR: return "LIGHTING: Golden Hour (Sunset). Warm, low-angle directional sunlight. Orange/Gold hues. Long shadows. Romantic atmosphere.";
            case LightingStyle.STUDIO_SOFTBOX: return "LIGHTING: High-End Studio. Huge Softbox. Even, diffuse, flattering light. Pure white highlights. Clean commercial look.";
            case LightingStyle.REMBRANDT: return "LIGHTING: Rembrandt. Dramatic chiaroscuro. Single light source, dark shadows, moody, artistic, painterly.";
            case LightingStyle.NEON_NOIR: return "LIGHTING: Neon Noir. Dual tone lighting (Teal and Pink). Dark background, bright saturated rim lights. Cyberpunk aesthetic.";
            case LightingStyle.CANDLELIGHT: return "LIGHTING: Candlelight. Extremely warm (1800K), flickering shadows, intimate, low key, soft glow.";
            case LightingStyle.FLASH_HARD: return "LIGHTING: Direct Flash (Paparazzi). Hard shadows directly behind the subject. High contrast, overexposed highlights. Raw energy.";
            case LightingStyle.CINEMATIC_TEAL: return "LIGHTING: Blockbuster Cinematic. Teal shadows, Orange skin tones. Color graded. Anamorphic lens flare.";
        }
    }

    // Default Location Logic
    let category = LocationCategory.ABSTRACT;
    for (const [cat, locs] of Object.entries(LOCATION_CATEGORIES_MAP)) {
        if (locs.includes(location)) {
            category = cat as LocationCategory;
            break;
        }
    }

    switch (category) {
        case LocationCategory.NATURE:
            return "Natural daylight, sun dappled lighting filtering through environment, organic shadows, soft earth tones. 5600K color temperature. High dynamic range (HDR).";
        case LocationCategory.LUXURY:
            return "High-key fashion lighting, bright, crisp, airy atmosphere, sparkling reflections, expensive aesthetic. Golden hour sunlight. Ultra-clean look.";
        case LocationCategory.DOMESTIC:
            return "Soft diffused window light, cozy ambient occlusion, warm indoor tones, authentic home atmosphere. realistic depth of field. Dust motes in air.";
        case LocationCategory.URBAN:
            return "High contrast street lighting, neon accents, gritty realism, dramatic shadows, urban decay texture. Mixed lighting sources. Cinematic lighting.";
        case LocationCategory.VEHICLE:
            return "Confined interior lighting, leather reflections, specular highlights from windows, luxury car interior atmosphere. Directional sunlight.";
        case LocationCategory.PROFESSIONAL:
            return "Neutral overhead lighting, sterile and clean, flat realistic lighting found in public spaces. Even illumination.";
        case LocationCategory.EROTIC:
            return "Cinematic Boudoir lighting. Moody, low-key atmosphere. Deep red, purple or warm amber gel lights. High contrast shadows. Hazy, steamy or smoky air. Seductive and intimate mood. Soft focus background.";
        default: // Abstract
            return "Studio strobe lighting, dramatic rim light, dark background, cinematic focus. Hard shadows.";
    }
};

// Helper to enforce physical constraints based on props
const getPoseConstraints = (prop: string, location: LocationType, pose: PoseType, customPose?: string): string => {
    
    // 0. AI Custom Override (Highest Priority)
    if (customPose) {
        return `CONTEXT: CUSTOM CREATIVE POSE. ${customPose}. Ensure anatomy is physically plausible but adheres to this creative description.`;
    }

    // 1. Explicit Pose Override
    switch (pose) {
        case PoseType.DRIVING:
            return "CONTEXT: Driver's seat. Right foot extending towards accelerator pedal, left foot resting on dead pedal. Relaxed driving pose.";
        case PoseType.ACTION_PEDAL:
            return "CONTEXT: Action shot. Driver's seat. Foot is flexed, pressing down hard on the accelerator pedal. Calf muscles engaged. Dynamic driving pose.";
        case PoseType.DASHBOARD:
            return "CONTEXT: Passenger seat. Legs elevated high, ankles crossed, resting comfortably on the car dashboard. Reclined body position.";
        case PoseType.DESK_BOSS:
            return "CONTEXT: 'Boss' pose. Feet crossed at ankles, resting confidently on top of the desk surface. Leaning back.";
        case PoseType.DESK_UNDER:
            return "CONTEXT: Under the desk. Feet out of shoes (or wearing them), resting on the carpet or chair base. Discreet angle.";
        case PoseType.RECLINED:
            return "CONTEXT: Lying down / Reclining fully. Legs extended horizontally, relaxed weight distribution.";
        case PoseType.WALL_LEGS:
            return "CONTEXT: Lying on back, legs extended vertically up against a wall. Blood rushing down.";
        case PoseType.KNEELING:
            return "CONTEXT: Kneeling position. Tops of feet (dorsum) pressed against the surface. Soles visible from behind.";
        case PoseType.PINUP_KICK:
            return "CONTEXT: Pin-up Style. Standing on one leg, the other leg kicks back playfully high in the air. Retro aesthetic.";
        case PoseType.PINUP_KNEEL:
            return "CONTEXT: Pin-up Style. Kneeling on a chair or surface, looking back over the shoulder. Soles facing camera.";
        case PoseType.PINUP_CROSS:
             return "CONTEXT: Pin-up Style. Sitting with legs crossed tightly, toes pointed, accentuating the calf shape.";
        case PoseType.PINUP_RECLINE:
             return "CONTEXT: Pin-up Style. Lying on stomach, feet crossed and raised in the air behind. Playful demeanor.";
    }

    // 2. Fallback to Prop-based logic if Pose is Generic
    const p = prop.toLowerCase();
    
    // CAR SPECIFIC FALLBACKS
    if (p.includes('dashboard')) return "CONTEXT: Passenger perspective. Legs are elevated and crossed, resting on the dashboard. Body is reclined in the seat.";
    if (p.includes('pedalen') || p.includes('pedals') || p.includes('pedaal')) return "CONTEXT: Driver perspective (Footwell). Feet are hovering near or resting on the car pedals. Darker footwell lighting.";
    if (p.includes('raam') || p.includes('window')) return "CONTEXT: Legs extending out of the open car window. Sunlight hitting the legs.";
    if (p.includes('stuur') || p.includes('steering')) return "CONTEXT: Driver's seat. Camera looking down past the steering wheel at the lap and legs.";
    if (p.includes('golf')) return "CONTEXT: On the golf course green. Feet interacting with grass or golf equipment.";
    if (p.includes('tennis')) return "CONTEXT: On the clay court. Feet near the white line or racket.";
    if (p.includes('pool') || p.includes('zwembad')) return "CONTEXT: Poolside. Feet dangling in water or resting on wet tiles.";

    // INTERACTION PHYSICS (Squishing/Messy)
    if (p.includes('cake') || p.includes('taart') || p.includes('cream') || p.includes('slagroom') || p.includes('modder') || p.includes('mud') || p.includes('sand') || p.includes('zand') || p.includes('water') || p.includes('foam')) {
        return `CONTEXT: PHYSICS INTERACTION. The feet are physically interacting with the soft material '${prop}'. Material deformation: The substance yields under the weight of the foot. Squishing between toes. Skin compression visible (blanching).`;
    }

    return "CONTEXT: Feet interacting naturally with the '" + prop + "'. Weight distribution matches the surface.";
};

// Helper to translate FootShape enum (which contains Dutch UI text) into physical English descriptions
const getMorphologyDescription = (shape: FootShape): string => {
    switch (shape) {
        case FootShape.EAST_ASIAN:
            return "East Asian morphology. Petite bone structure, shorter toes, smooth skin texture, warm yellow/beige undertones. Soft, varying arch height.";
        case FootShape.ARABIC:
            return "Middle Eastern / Arabic morphology. Olive skin tones, elegant narrow structure, well-defined high arch, almond-shaped toes. Sophisticated appearance.";
        case FootShape.SOUTH_ASIAN:
            return "South Asian / Indian morphology. Rich skin tones, distinct toe shape, intricate skin texture, natural aesthetic.";
        case FootShape.LATINA:
            return "Latina / South American morphology. Golden/Tan skin tones, curvy arch structure, strong definition.";
        case FootShape.NORDIC:
            return "Nordic / Scandinavian morphology. Pale/Fair skin, larger bone structure, long toes, higher instep.";
        case FootShape.AFRICAN:
            return "African / Deep Melanin morphology. Deep rich skin tones, strong athletic structure, distinct sole contrast.";
        case FootShape.GREEK:
            return "Greek Foot (Morton's Toe). Second toe is longer than the big toe. Classic artistic shape.";
        case FootShape.EGYPTIAN:
            return "Egyptian Foot. Toes taper diagonally from big toe to little toe in a straight line.";
        case FootShape.ROMAN:
            return "Roman Foot. First three toes are approximately the same length. Square aesthetic.";
        default:
            return shape; // Fallback for standard shapes
    }
};

const getFilmStockSpecs = (film: FilmStock): string => {
    switch (film) {
        case FilmStock.KODAK_PORTRA_400: return "FILM LOOK: Kodak Portra 400. Warm, natural skin tones. Fine grain. Slightly overexposed highlights.";
        case FilmStock.KODAK_GOLD_200: return "FILM LOOK: Kodak Gold 200. Vintage vacation vibe. Golden hues, higher saturation, nostalgic grain.";
        case FilmStock.FUJIFILM_VELVIA: return "FILM LOOK: Fujifilm Velvia. High saturation, vivid colors, deep blacks, high contrast. Dramatic landscape feel.";
        case FilmStock.ILFORD_HP5: return "FILM LOOK: Ilford HP5 Plus. Black and White photography. High contrast, visible grit and grain. Artistic shadows.";
        case FilmStock.CINESTILL_800T: return "FILM LOOK: CineStill 800T. Tungsten balanced. Halation around bright lights (red/orange glow). Cool shadows.";
        case FilmStock.POLAROID_600: return "FILM LOOK: Polaroid 600. Soft focus, vintage color shift, slight fading, flash fall-off.";
        case FilmStock.KODACHROME: return "FILM LOOK: Kodachrome 64. 1980s aesthetic. Rich reds and yellows, strong contrast, distinct archival look.";
        default: return "FILM LOOK: Digital Clean. Modern DSLR sharpness, neutral color profile, no grain.";
    }
};

const constructPrompt = (state: DesignState): string => {
  const lighting = getLightingAndAtmosphere(state.location, state.lightingStyle);
  const poseConstraints = getPoseConstraints(state.prop, state.location, state.pose, state.customPose);
  const morphology = getMorphologyDescription(state.footShape);
  const filmLook = getFilmStockSpecs(state.filmStock || FilmStock.DIGITAL_CLEAN);

  // 1. Core Photography Style (The "Camera")
  // Detect if a wide angle is used (Lap View / Driver POV) to adjust focus strategy
  const isWideAngle = [CameraAngle.LAP_VIEW, CameraAngle.DRIVER_POV, CameraAngle.POV, CameraAngle.WIDE_STANCE, CameraAngle.BED_POV].includes(state.cameraAngle);

  let focusInstruction = "Focus: Razor sharp focus on the skin texture of the feet. Shallow depth of field.";
  if (isWideAngle) {
      focusInstruction = "Focus: Wide POV shot from chest/chin height looking down. Framing includes the lap, thighs, knees, and feet. Contextual background (e.g. car interior, bed, chair) is visible around the legs.";
  }

  // ULTRA-RES CAMERA SIMULATION
  const cameraSpecs = state.useProModel 
    ? "Phase One XF IQ4 150MP System, Schneider Kreuznach 80mm LS f/2.8 Blue Ring Lens. Resolution: 150 Megapixels. Format: RAW (TIFF 16-bit). Detail: Maximum Micro-contrast."
    : "Sony A7R V, 61MP, FE 85mm f/1.4 GM Lens. Resolution: 4K UHD.";

  // Determine Clothing States (Source of Truth)
  const isWearingHosiery = state.hosieryType !== HosieryType.NONE;
  // Strictly define Opaque. even 30D is semi-transparent, but socks and 60D+ are opaque.
  const isSock = [HosieryType.ANKLE_SOCKS, HosieryType.TOE_SOCKS, HosieryType.KNEE_HIGHS].includes(state.hosieryType);
  const isOpaqueDenier = [HosieryDenier.D60, HosieryDenier.D100].includes(state.hosieryDenier);
  // Opaque is strictly enforced if it's a sock OR high denier
  const isOpaqueHosiery = isWearingHosiery && (isSock || isOpaqueDenier);
  
  const isWearingShoes = state.footwearType !== FootwearType.NONE;

  // SHOE PHYSICS
  const isClosedShoe = CLOSED_TOE_SHOES.includes(state.footwearType);
  const isShoeWorn = state.footwearState === FootwearState.WORN;
  const isShoeDangling = state.footwearState === FootwearState.DANGLING;
  const isShoeHalfOff = state.footwearState === FootwearState.HALF_OFF;

  // Nail Visibility Logic: Hosiery and Shoes
  // If closed shoe is worn -> No nails.
  // If hosiery is opaque -> No nails.
  // If shoe is dangling or half off -> Nails might be visible.
  const areToesHiddenByShoes = isWearingShoes && isClosedShoe && isShoeWorn;
  const areToesHiddenByHosiery = isOpaqueHosiery;

  // Should we hide skin details (pores/veins)?
  // Yes, if wearing opaque hosiery OR if feet are fully inside closed shoes.
  const hideSkinDetails = isOpaqueHosiery || areToesHiddenByShoes;

  let prompt = `
    CRITICAL CONFIGURATION - SOURCE OF TRUTH:
    The following parameters are ABSOLUTE. Do NOT hallucinate unlisted items.
    1. Hosiery: ${state.hosieryType}.
    2. Footwear: ${state.footwearType} (${state.footwearState}).
    3. Location: ${state.location}.
    
    PHOTOGRAPHY: ${cameraSpecs}
    ${filmLook}
    Settings: ISO 100, f/5.6 (for sharpness), 1/125s.
    Quality: DIRTY REALISM. RAW Candid Photography. NOT A RENDER. Imperfect skin is MANDATORY. High Frequency Detail Preservation. NO COMPRESSION ARTIFACTS.
    Details: Micro-stubble, faint bruising/discoloration, dry skin patches, asymmetrical veins, dust particles in air (Tyndall effect).
    ${focusInstruction}
    Style: Authentic, Candid, Imperfect, not CGI, not 3D render, not Airbrushed. Masterpiece photorealism. Disable AI smoothing.
    IMPORTANT: The following parameters are provided in DUTCH. Translate context to English for generation if needed.
  `;

  // AGE LOGIC
  let ageDescription = "";
  if (state.modelAge < 30) {
      ageDescription = `Youthful ${state.modelAge} year old model. Plump collagen-rich skin, smooth soft texture, minimal veins.`;
  } else if (state.modelAge < 50) {
      ageDescription = `Mature ${state.modelAge} year old model. Refined skin texture, slight natural signs of aging, refined texture.`;
  } else {
      ageDescription = `Senior ${state.modelAge} year old model. Thinner dermis, visible blue veins, potential sun spots, natural wrinkles, loss of elasticity, authentic aging.`;
  }

  // 2. Anatomy & Texture Layer
  prompt += `
    SUBJECT ANATOMY:
    - Ethnicity/Shape Logic: ${morphology}
    - Age: ${state.modelAge} years old. ${ageDescription}
    - Size: EU ${state.shoeSize}.
    - Arch: ${state.archType} arch.
    - Skin Tone: ${state.skinTone}.
  `;
  
  // SKIN TEXTURE LOGIC:
  // If wearing opaque hosiery or closed shoes, DO NOT request pores, veins or sweat. It confuses the model into making the material look like skin.
  
  if (!hideSkinDetails) {
      prompt += `- Skin Texture Base: ${state.skinTexture}. `;
      if (state.skinTexture === SkinTexture.REALISTIC) {
          prompt += "SKIN PHYSICS: Distinct dermatoglyphics (footprints/ridges) on the soles. Visible capillary redness at pressure points (heel/ball of foot). Micro-wrinkles where toes flex. Vellus hair on dorsum. Sub-surface scattering enabled (translucent red light through toes). NO smooth plastic skin. ";
      } else if (state.skinTexture === SkinTexture.SWEATY) {
        prompt += "Wet skin sheen, individual micro-sweat droplets accumulating on the arch and between toes, reflective pressure points. ";
      } else if (state.skinTexture === SkinTexture.VEINY) {
        prompt += "Translucent skin, faint blue sub-surface veins visible on the dorsum of the foot. ";
      }
  } else {
      // Force override for opaque hosiery/shoes
      prompt += `
      TEXTURE MASKING PROTOCOL: ACTIVE.
      - The skin is COMPLETELY OCCLUDED by the opaque material (${isOpaqueHosiery ? state.hosieryType : state.footwearType}).
      - DO NOT render pores, veins, wrinkles, moles, or skin tone gradients under the fabric/leather.
      - Render the MATERIAL TEXTURE (e.g., Knit, Velvet, Leather) strictly.
      - NO bleed-through of underlying anatomy.
      `;
  }

  // 3. Styling Layer (Nails)
  prompt += `
    PEDICURE:
  `;

  if (areToesHiddenByShoes) {
      prompt += " VISIBILITY: ZERO. NAILS AND TOES ARE INVISIBLE. They are completely inside the closed footwear. Do NOT render them. Do NOT render polish on the shoe material. Render the smooth outer toe box of the shoe. ";
  } else if (areToesHiddenByHosiery) {
      prompt += " VISIBILITY: ZERO. HIDDEN under thick opaque hosiery. No polish or nail shape visible through the fabric. ";
  } else {
      prompt += `
      - Nails: ${state.nailLength} length, ${state.nailShape} shape.
      - Base Color: ${state.nailColor}. 
      - Finish: ${state.nailFinish}.
      - DESIGN STYLE: ${state.nailStyle}.
      - TEXTURE: Natural keratin texture visible under polish.
      - VISIBILITY: Nails are visible. Polish is applied STRICTLY to the keratin nail plate only. ABSOLUTELY NO polish on the skin. `;
      
      // Specific Nail Art Logic
      switch (state.nailStyle) {
          case NailStyle.FRENCH_CLASSIC: prompt += " ART DETAIL: Classic French Manicure. Nude/Pink base with a crisp white curved tip. "; break;
          case NailStyle.FRENCH_MICRO: prompt += " ART DETAIL: Micro French. Very thin, delicate color line at the tip. "; break;
          case NailStyle.FRENCH_BLACK: prompt += " ART DETAIL: French Noir. Nude base with a jet black tip. "; break;
          case NailStyle.OMBRE: prompt += " ART DETAIL: Babyboomer / Ombré. Seamless gradient fade. "; break;
          case NailStyle.MARBLE: prompt += " ART DETAIL: Marble effect with veining. "; break;
          case NailStyle.CAT_EYE: prompt += " ART DETAIL: Magnetic Cat Eye effect. Diagonal shimmering streak. "; break;
          case NailStyle.GEMS_BASE: prompt += " ART DETAIL: Rhinestones applied near cuticle. "; break;
          case NailStyle.GEMS_FULL: prompt += " ART DETAIL: Full Bling crystals. "; break;
          case NailStyle.MINIMALIST_LINE: prompt += " ART DETAIL: Minimalist Art. Single fine line. "; break;
          case NailStyle.FLORAL: prompt += " ART DETAIL: Hand-painted floral tiny flowers. "; break;
      }
      if (state.nailFinish === 'Holografisch') prompt += "Prismatic linear holographic flare on nails. ";
  }
  
  // 3.5 Accessories
  if (state.accessory && state.accessory !== AccessoryType.NONE) {
      prompt += `ACCESSORIES: Wearing ${state.accessory}. `;
      switch (state.accessory) {
          case AccessoryType.ANKLET_GOLD: prompt += "Delicate 14k gold chain resting loosely on the malleolus ankle bone. "; break;
          case AccessoryType.ANKLET_SILVER: prompt += "Sterling silver chain with small charms hanging around the ankle. "; break;
          case AccessoryType.ANKLET_LEATHER: prompt += "Bohemian braided leather cord around the ankle. "; break;
          case AccessoryType.TOE_RING_GOLD: prompt += "Small gold ring on the second toe. "; break;
          case AccessoryType.TOE_RING_SILVER: prompt += "Silver ring band on the second toe. "; break;
          case AccessoryType.TOE_RING_CHAIN: prompt += "Slave anklet connecting toe ring to ankle chain. Fine metalwork. "; break;
          case AccessoryType.PIERCING_ANKLE: prompt += "Single dermal piercing anchor on the outer ankle bone. Small crystal. "; break;
          case AccessoryType.TATTOO_ANKLE: prompt += "Small, fine-line tattoo of a rose or flower on the outer ankle. "; break;
          case AccessoryType.TATTOO_INSTEP: prompt += "Cursive script text tattoo running along the arch/instep of the foot. "; break;
          case AccessoryType.TATTOO_LEG_SLEEVE: prompt += "Full leg sleeve tattoo (Irezumi or Traditional). Complex ink covering calf/shin. "; break;
          case AccessoryType.TATTOO_HENNA: prompt += "Intricate Henna / Mehndi designs drawn on feet in brown ink. "; break;
          case AccessoryType.TATTOO_MINIMAL: prompt += "Minimalist geometric line art tattoo on the top of the foot. "; break;
          case AccessoryType.PEARLS: prompt += "String of pearls draped loosely around the ankle. "; break;
      }
  }

  // 3.6 Footwear (V3.0) - REFINED LOGIC
  if (state.footwearType && state.footwearType !== FootwearType.NONE) {
      prompt += `FOOTWEAR: Type: ${state.footwearType}. Color: ${state.footwearColor}. `;
      
      switch (state.footwearState) {
          case FootwearState.WORN:
              prompt += "State: FULLY WORN. Feet are inside the shoes. ";
              if (areToesHiddenByShoes) {
                  prompt += "VISUAL LOGIC: The shoe material is OPAQUE. Feet are concealed. Render high quality shoe material (Leather/Canvas/Rubber) with light reflections. Do NOT render toes merging with the shoe. ";
              } else {
                  prompt += "TOES VISIBLE. Open-toe design allows toes and nails to be seen clearly. ";
              }
              break;
          case FootwearState.DANGLING:
              prompt += "State: DANGLING (Dangle). The shoe is hanging loosely from the toes, heel exposed. 'Shoeplay' aesthetic. ";
              break;
          case FootwearState.HALF_OFF:
              prompt += "State: HALF OFF. The heel is popped out of the shoe back, resting on the heel counter. ";
              break;
          case FootwearState.NEARBY:
              prompt += "State: NEARBY / TAKEN OFF. The shoes are visible in the background or next to the bare feet. The feet themselves are BARE (or hosiery only). ";
              break;
      }
  } else {
      prompt += "FOOTWEAR: NONE. The subject is NOT wearing shoes. Bare feet (or hosiery only). ";
  }

  // 4. Hosiery Physics Engine (REBUILT FOR ACCURACY)
  if (state.hosieryType !== HosieryType.NONE) {
    prompt += `
    HOSIERY & FABRIC PHYSICS:
    - ITEM: ${state.hosieryColor} ${state.hosieryType}.
    - DENIER: ${state.hosieryDenier}. 
    - INTEGRITY: This is a SINGLE, UNIFIED garment. Both legs must be wearing the EXACT SAME material. Symmetrical coverage. NO "one leg bare" errors.
    - LAYERING: The fabric sits ON TOP of the skin. It acts as a physical layer with volume. It is NOT painted on skin.
    - COVERAGE: The fabric completely encases the foot, including the SOLES, HEELS, and TOES. It is NOT footless.
    - EXCLUSIVITY: The subject is wearing ONLY this hosiery item. Do NOT render additional socks over tights.
    `;
    
    // --- Pattern / Motif Logic ---
    if (state.hosieryPattern && state.hosieryPattern !== HosieryPattern.NONE) {
        prompt += `- Pattern: ${state.hosieryPattern}. `;
        switch (state.hosieryPattern) {
            case HosieryPattern.DOTS: prompt += "Small 'Swiss Dot' raised flocking pattern. "; break;
            case HosieryPattern.HEARTS: prompt += "Small repeated heart motif. "; break;
            case HosieryPattern.BOWS: prompt += "Tiny aesthetic coquette bows. "; break;
            case HosieryPattern.SEAM: prompt += "Vintage dark back-seam running down the leg (Cuban Heel). "; break;
            case HosieryPattern.LACE: prompt += "Complex floral lace texture. "; break;
            case HosieryPattern.DIAMOND: prompt += "Argyle diamond pattern. "; break;
            case HosieryPattern.LOGOS: prompt += "Woven luxury monogram pattern. "; break;
        }
    }

    // --- Color Mixing & Texture Physics ---
    if (!isOpaqueHosiery) {
        // Sheer / Semi-Sheer
        prompt += `
        SHEER PHYSICS:
        - Transparency: High transparency (${state.hosieryDenier}).
        - Color Interaction: The final color is a physical blend of the ${state.skinTone} skin tone seen THROUGH the ${state.hosieryColor} nylon mesh.
        - Example: Black tights on Fair skin = Smoky Grey appearance. White tights on Deep skin = Milky/Ashy contrast.
        - Texture: Nylon shine/sheen on the curves (shins/instep).
        - Nail Visibility: The ${state.nailColor} nail polish is VISIBLE through the fabric but slightly muted by the mesh.
        - TENSION BRIDGING (Toe Gaps): The fabric creates a 'tent' or 'bridge' spanning straight across the gaps between toes. It DOES NOT touch the skin deep between toes. The fabric is stretched tight here, creating a lighter, more transparent triangle.
        `;
    } else {
        // Opaque / Socks
        prompt += `
        OPAQUE PHYSICS:
        - Transparency: 0%. Solid Block Color.
        - Texture: Thick Matte fabric (Cotton, Wool, or Velvet). NO skin shine. NO pores visible.
        - Nail Visibility: HIDDEN. Toes are covered by thick fabric.
        - MASKING: Skin details (veins, scars, pores) are FULLY HIDDEN by the material layer.
        `;
    }

    // --- Coverage Rules ---
    switch (state.hosieryType) {
        case HosieryType.PANTYHOSE:
        case HosieryType.FISHNET:
            if (isWideAngle) {
                prompt += "Coverage: Seamless garment covering feet, ankles, calves, knees and thighs. Connected at the waist. ";
            } else {
                prompt += "Coverage: Seamless from toes up past the frame. NO ankle lines. ";
            }
            break;
        case HosieryType.THIGH_HIGHS:
            prompt += "Coverage: Covers feet and legs, ending at mid-thigh. WELT PHYSICS: Wide, elastic band. It has VOLUMETRIC THICKNESS (3D). It creates a realistic soft indentation (flesh displacement) on the thigh. Visible shadow under the band's edge. It is NOT a flat line texture. ";
            break;
        case HosieryType.VINTAGE_FF:
             prompt += "Vintage Fully Fashioned Stockings. 100% Non-stretch Nylon. Features: Dark back seam running down the leg, Cuban Heel reinforcement, Keyhole welt at top. Texture: Glassy sheen, loose fit at ankle (wrinkling). ";
             break;
        case HosieryType.VINTAGE_RHT:
             prompt += "Vintage Reinforced Heel and Toe (RHT) Stockings. 100% Nylon. Features: Seamless leg, but distinct darker reinforced weaving at the heel and toe areas. ";
             break;
        case HosieryType.KNEE_HIGHS:
            prompt += "Coverage: Ends just below the knee. WELT PHYSICS: The cuff has thickness and creates a slight compression indentation on the calf. Integrated knit structure. ";
            break;
        case HosieryType.ANKLE_SOCKS:
        case HosieryType.TOE_SOCKS:
            prompt += "Coverage: Standard ankle sock height. Covers foot and ankle bone. ";
            break;
    }

    if (state.wornKnit) {
      prompt += "Details: Slight pilling on heel, fabric thinning at toes (stress points), lived-in look. ";
    }
  } else {
    // ONLY render dermatoglyphics if feet are NOT covered by shoes
    if (!areToesHiddenByShoes) {
        prompt += "Barefoot. Distinct sole texture, fingerprints of the foot (dermatoglyphics), natural skin folds. ";
    }
  }

  // 5. Scene & Lighting
  prompt += `
    ENVIRONMENT & INTERACTION:
    - Location: ${state.location}.
    - Prop: ${state.prop}.
    - SCENE LOGIC: ${poseConstraints}
    - Lighting: ${lighting}
    - Integration: Feet are realistically interacting with the surface. Realistic shadows (Ambient Occlusion) where the foot touches the ${state.location}.
    
    CAMERA ANGLE: ${state.customCamera ? `CUSTOM CREATIVE ANGLE: ${state.customCamera}` : state.cameraAngle}.
    POSE: ${state.customPose ? `CUSTOM CREATIVE POSE: ${state.customPose}` : state.pose}. 
  `;

  // Specific Car Context for Wide Shots - Double enforcement
  if (state.cameraAngle === CameraAngle.DRIVER_POV && (state.location.includes('Auto') || state.location.includes('Car') || state.location.includes('Jet'))) {
      prompt += "CONTEXT: Shot from the driver's perspective looking down. Steering wheel or dashboard visible in the upper peripheral frame. Legs extended towards pedals or resting. ";
  }
  if (state.cameraAngle === CameraAngle.LAP_VIEW) {
      prompt += "CONTEXT: View from top-down looking at one's own lap and legs. Hands might be resting on thighs. ";
  }

  // Sole View Constraint
  // Hosiery must cover soles if selected
  const hosieryCoversFeet = [
    HosieryType.PANTYHOSE, HosieryType.THIGH_HIGHS, HosieryType.KNEE_HIGHS,
    HosieryType.ANKLE_SOCKS, HosieryType.TOE_SOCKS, HosieryType.FISHNET,
    HosieryType.VINTAGE_FF, HosieryType.VINTAGE_RHT
  ].includes(state.hosieryType);

  if (state.cameraAngle === CameraAngle.MACRO_SOLE || state.cameraAngle === CameraAngle.LOW_ANGLE || state.cameraAngle === CameraAngle.BOTTOM_UP_GLASS) {
      prompt += "CONTEXT: View focusing on the soles/bottom of feet. ";
      
      if (hosieryCoversFeet) {
          prompt += `The soles are FULLY COVERED by the ${state.hosieryColor} ${state.hosieryType} fabric. The mesh texture continues seamlessly over the heel, arch, and toe pads. NO bare skin on soles. `;
          if (state.hosieryType === HosieryType.VINTAGE_RHT || state.hosieryType === HosieryType.VINTAGE_FF) {
               prompt += "Visible darker reinforcement weaving (RHT) on the sole and heel area. ";
          }
      } else if (!areToesHiddenByShoes) {
          prompt += "Ensure toe pads are clean skin texture. Dermatoglyphics visible. No nail polish visible on the underside. ";
      }
  }

  // --- NEGATIVE PROMPT CONSTRUCTION ---
  let excludeItems = "smooth soles, plastic skin, doll feet, airbrushed skin, noise reduction, filter, cgi, 3d render, blender, glossy skin (unless oil), cartoon, drawing, painting, bad anatomy, extra toes, missing toes, fused toes, watermark, text, signature, low resolution, ugly, deformed, disfigured, amputation, bad proportions, floating feet, impossible pose, legs merging with car parts, nail polish on skin, paint on sole, polish on toe pads, messy pedicure, bleeding color, mismatched legs, asymmetrical hosiery, one leg bare, patchwork skin, texture glitch, seamless skin, blur, jpeg artifacts, painted on tights, footless tights, bare soles with pantyhose, exposed toes with stockings";
  
  // Smart Exclusions based on Hosiery selection
  if (state.hosieryType === HosieryType.PANTYHOSE || state.hosieryType === HosieryType.THIGH_HIGHS || state.hosieryType === HosieryType.FISHNET) {
      excludeItems += ", ankle socks, white socks, knee highs, sport socks, woolen socks, bare legs";
  } else if (state.hosieryType === HosieryType.ANKLE_SOCKS || state.hosieryType === HosieryType.TOE_SOCKS) {
      excludeItems += ", pantyhose, tights, stockings, nylon legs, sheer hosiery, bare feet";
  } else if (state.hosieryType === HosieryType.NONE) {
      excludeItems += ", socks, pantyhose, stockings, tights, fishnet, hosiery";
  }

  // Smart Exclusions based on Footwear
  if (state.footwearType === FootwearType.NONE) {
      excludeItems += ", wearing shoes, wearing sneakers, wearing heels, wearing boots";
  } else if (state.footwearState === FootwearState.NEARBY) {
      excludeItems += ", feet inside shoes, wearing shoes";
  } else if (isClosedShoe && isShoeWorn) {
      excludeItems += ", toes sticking out of shoes, toes clipping through shoes, transparent leather, bare toes, feet merging with soles, nails visible through shoes, transparent boots, visible toes, visible toenails, nail polish on shoes, transparent shoes, toe cleavage";
  }

  prompt += `
    NEGATIVE PROMPT: ${excludeItems}
  `;

  return prompt;
};

// NEW: Online Research / AI Prop Suggestion -> NOW GENERATES FULL CUSTOM CONCEPT
// Returns detailed custom strings for Prompting, but also Fallback Enums for the 3D Visualizer
export const generateCreativeConcept = async (location: LocationType): Promise<AIConcept> => {
    const ai = new GoogleGenAI({ apiKey: process.env.API_KEY || 'dummy' });
    
    // Detect Erotic Context for AI Logic
    let isErotic = false;
    for (const [cat, locs] of Object.entries(LOCATION_CATEGORIES_MAP)) {
        if (cat === LocationCategory.EROTIC && locs.includes(location)) {
            isErotic = true;
            break;
        }
    }

    const validPoses = Object.values(PoseType).join(', ');
    const validAngles = Object.values(CameraAngle).join(', ');
    const validFootwear = Object.values(FootwearType).join(', ');

    // We use gemini-2.5-flash for speed
    const prompt = `
        You are a high-end fashion director specializing in ${isErotic ? "Erotic / Fetish" : ""} foot photography.
        The user has selected the location: "${location}".
        
        ${isErotic ? "CONTEXT: The user has explicitly selected an EROTIC category location. You MUST generate 'spicy', suggestive, and intimate concepts suited for adult artistic photography. Do not be prudish. Suggestions like binding, dangling, heels, and teasing are expected." : ""}

        1. Invent a UNIQUE, CREATIVE interaction/prop idea for this location.
        2. Invent a CREATIVE POSE description that fits this interaction perfectly.
           NOTE: If suggesting footwear (e.g. boots), ensure the pose matches the shoe type (e.g. no toe-wiggling in rigid boots).
        3. Invent a CREATIVE CAMERA ANGLE description for maximum artistic impact.
        4. Suggest appropriate FOOTWEAR (or barefoot).
        
        5. CRITICAL: For the interactive 3D preview tool, select the CLOSEST EXISTING MATCH from the lists below that approximates your creative concept.

        AVAILABLE VISUALIZER POSES:
        ${validPoses}

        AVAILABLE VISUALIZER ANGLES:
        ${validAngles}
        
        AVAILABLE VISUALIZER FOOTWEAR:
        ${validFootwear}

        OUTPUT JSON ONLY:
        {
            "creativeProp": "Descriptive phrase",
            "creativePose": "Detailed pose description",
            "creativeAngle": "Detailed angle description",
            "visualizerPoseFallback": "Exact string from Available Visualizer Poses list",
            "visualizerCameraFallback": "Exact string from Available Visualizer Angles list",
            "visualizerFootwearFallback": "Exact string from Available Visualizer Footwear list"
        }
    `;
    
    try {
        const response = await ai.models.generateContent({
            model: 'gemini-2.5-flash',
            contents: prompt,
            config: { responseMimeType: "application/json" }
        });
        
        const text = response.text || "{}";
        // sanitize markdown code blocks which cause SyntaxError
        const cleanText = text.replace(/```json\n?|```/g, '').trim();
        const json = JSON.parse(cleanText);

        return {
            prop: json.creativeProp || "Creative Interaction",
            customPose: json.creativePose || "Creative Pose",
            customCamera: json.creativeAngle || "Creative Angle",
            visualizerPose: json.visualizerPoseFallback || PoseType.STANDING,
            visualizerCamera: json.visualizerCameraFallback || CameraAngle.LOW_ANGLE,
            visualizerFootwear: json.visualizerFootwearFallback || FootwearType.NONE
        };
    } catch (e) {
        console.error("AI Concept Generation failed", e);
        // Fallback
        return { 
            prop: "Unieke Interactie", 
            customPose: "Creative Pose",
            customCamera: "Creative Angle",
            visualizerPose: PoseType.STANDING, 
            visualizerCamera: CameraAngle.LOW_ANGLE,
            visualizerFootwear: FootwearType.NONE
        };
    }
};

export const generateImage = async (state: DesignState): Promise<string> => {
  const prompt = constructPrompt(state);
  
  // Use gemini-2.5-flash for fast standard generation
  let model = 'gemini-2.5-flash'; 
  
  // For images, we use imagen or specific models. But per instructions:
  // "General Image Generation: gemini-2.5-flash-image"
  // "High Quality: gemini-3-pro-image-preview"
  model = 'gemini-2.5-flash-image';

  let imageConfig: any = {
      aspectRatio: "3:4"
  };

  // Logic for Pro Model (Veo/High Quality)
  if (state.useProModel) {
    model = 'gemini-3-pro-image-preview';
    // STRICTLY ENFORCE RESOLUTION FOR PRO MODEL
    // Note: '4K' can cause 500 errors on some endpoints, fallback to 2048x2048 if unstable. 2K is still high quality.
    imageConfig.imageSize = "2048x2048"; 
    
    if (window.aistudio) {
        const hasKey = await window.aistudio.hasSelectedApiKey();
        if (!hasKey) {
            await window.aistudio.openSelectKey();
        }
    }
  }

  // Always create a new instance to ensure fresh config/key
  const ai = new GoogleGenAI({ apiKey: process.env.API_KEY || 'dummy' }); 

  try {
    const response = await ai.models.generateContent({
      model: model,
      contents: {
        parts: [{ text: prompt }],
      },
      config: {
        imageConfig: imageConfig
      }
    });

    for (const part of response.candidates?.[0]?.content?.parts || []) {
      if (part.inlineData) {
        return `data:image/png;base64,${part.inlineData.data}`;
      }
    }
    throw new Error("Geen afbeeldingsdata gevonden.");
  } catch (error) {
    console.error("Gemini Generation Error:", error);
    throw error;
  }
};

export const generateSocialCaption = async (state: DesignState): Promise<string> => {
   const ai = new GoogleGenAI({ apiKey: process.env.API_KEY || 'dummy' });
   
   let patternText = "";
   if (state.hosieryPattern && state.hosieryPattern !== HosieryPattern.NONE) {
       patternText = `with ${state.hosieryPattern} details`;
   }
   
   let nailText = `${state.nailColor} nails`;
   if (state.nailStyle !== NailStyle.SOLID) {
       nailText = `${state.nailStyle} nail art in ${state.nailColor}`;
   }

   const prompt = `
     You are a social media manager for a high-end fashion/footwear artistic account.
     Write a short, alluring, and aesthetic Instagram caption in ENGLISH for a photo with these specs:
     - Vibe: ${state.location}
     - Style: ${state.footShape} feet, ${nailText} (${state.nailFinish})
     - Hosiery: ${state.hosieryType === 'Blote Voeten' ? 'Bare skin' : state.hosieryType} ${patternText}
     - Accessory: ${state.accessory}
     - Footwear: ${state.footwearType} (${state.footwearState})
     - Prop: ${state.prop}
     - Pose: ${state.customPose || state.pose}
     
     Include 5-8 relevant hashtags. Do not use emojis excessively. Keep it under 50 words.
   `;
   
   const response = await ai.models.generateContent({
     model: 'gemini-2.5-flash',
     contents: prompt,
   });

   return response.text || "VelvetSole Design.";
};