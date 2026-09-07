export enum FootShape {
  GREEK = 'Grieks (Morton\'s Toe)',
  EGYPTIAN = 'Egyptisch (Aflopend)',
  ROMAN = 'Romeins (Vierkant)',
  PEASANT = 'Breed / Boer',
  MOUNTAIN_PEAK = 'Bergtop (Midden lang)',
  PETITE = 'Klein / Schattig',
  SLENDER = 'Slank / Model',
  
  // Etnische Morfologieën
  EAST_ASIAN = 'Aziatisch / Chinees (Petite)',
  ARABIC = 'Arabisch / Midden-Oosters',
  SOUTH_ASIAN = 'Indiaas / Zuid-Aziatisch',
  LATINA = 'Latina / Zuid-Amerikaans',
  NORDIC = 'Noors / Scandinavisch',
  AFRICAN = 'Afrikaans / Diep Melanine'
}

export enum ArchType {
  FLAT = 'Platvoet (Pes Planus)',
  NORMAL = 'Normaal',
  HIGH = 'Hoge Wreef',
  EXTREME = 'Extreem (Ballerina)',
  COMPRESSED = 'Samengeknepen'
}

export enum SkinTone {
  PORCELAIN = 'Porselein',
  FAIR = 'Licht',
  OLIVE = 'Olijfkleurig',
  TAN = 'Gebruind',
  DEEP_BRONZE = 'Diep Brons',
  EBONY = 'Ebbenhout'
}

export enum SkinTexture {
  SMOOTH = 'Glad / Airbrushed',
  REALISTIC = 'Realistisch (Poriën)',
  SWEATY = 'Bezweet (Reflecterende Druppels)',
  OILED = 'Geolied (Hoogglans)',
  VEINY = 'Aderig / Doorscijnend',
  CALLOUSED = 'Eeltig / Gehard'
}

export enum NailShape {
  NATURAL = 'Natuurlijk / Kort',
  SQUOVAL = 'Squoval (Recht met ronde hoek)',
  ALMOND = 'Amandel',
  STILETTO = 'Stiletto (Puntig)',
  COFFIN = 'Coffin / Ballerina',
  EDGE = 'Edge (Scherp)',
  DUCK = 'Flare / Duck',
  LIPSTICK = 'Lipstick (Schuin)'
}

export enum NailStyle {
  SOLID = 'Effen / Solid (Standaard)',
  FRENCH_CLASSIC = 'French Manicure (Klassiek Wit)',
  FRENCH_MICRO = 'Micro French (Dunne Kleurlijn)',
  FRENCH_BLACK = 'French Noir (Zwarte Tip)',
  OMBRE = 'Ombré / Babyboomer (Fade)',
  MARBLE = 'Marmer Effect (Steen)',
  CAT_EYE = 'Cat Eye (Magnetisch)',
  GEMS_BASE = 'Kristallen op Maan (Cuticle)',
  GEMS_FULL = 'Volledig Ingelegd (Bling)',
  MINIMALIST_LINE = 'Minimalistische Lijn',
  FLORAL = 'Handgeschilderde Bloemen'
}

export enum NailFinish {
  CREME = 'Crème (Effen)',
  JELLY = 'Jelly (Transparant)',
  HOLOGRAPHIC = 'Holografisch',
  CHROME = 'Chroom / Spiegel',
  MATTE = 'Mat',
  GLITTER = 'Glitter',
  CHIPPED = 'Beschadigd / Afgebladderd'
}

export enum HosieryType {
  NONE = 'Blote Voeten',
  ANKLE_SOCKS = 'Enkelsokjes',
  KNEE_HIGHS = 'Kniekousen',
  THIGH_HIGHS = 'Jarretelkousen (Dijbeen)',
  PANTYHOSE = 'Panty (Tights)',
  FISHNET = 'Netkousen (Fishnet)',
  TOE_SOCKS = 'Teensokken',
  VINTAGE_FF = 'Vintage Fully Fashioned (Naad)',
  VINTAGE_RHT = 'Vintage RHT (Versterkt)'
}

export enum HosieryDenier {
  D5 = '5D (Ultra Dun / Onzichtbaar)',
  D15 = '15D (Transparant)',
  D30 = '30D (Semi-Transparant)',
  D60 = '60D (Ondoorzichtig)',
  D100 = '100D (Blackout / Fluweel)'
}

export enum HosieryPattern {
  NONE = 'Effen / Geen',
  DOTS = 'Polka Dots (Stippen)',
  HEARTS = 'Hartjes Patroon',
  BOWS = 'Strikjes / Lint',
  LACE = 'Bloemenkant',
  SEAM = 'Naad Achterkant (Cuban Heel)',
  DIAMOND = 'Ruitpatroon (Argyle)',
  LOGOS = 'Luxe Monogram'
}

// NIEUW: SCHOEISEL
export enum FootwearType {
  NONE = 'Geen (Blootsvoets)',
  HEELS_STILETTO = 'Hoge Hakken (Stiletto)',
  HEELS_PLATFORM = 'Platform Hakken (Pleaser)',
  SANDALS_STRAPPY = 'Sandaaltjes (Bandjes)',
  MULES = 'Muiltjes / Slippers (Open Hiel)',
  BALLET_FLATS = 'Ballerina\'s',
  BOOTS_ANKLE = 'Enkellaarsjes',
  BOOTS_KNEE = 'Knie Laarzen (Leer)',
  SNEAKERS = 'Sneakers (Wit)',
  FLIP_FLOPS = 'Teenslippers'
}

export enum FootwearState {
  WORN = 'Aan (Gedragen)',
  DANGLING = 'Bungelend (Dangling)',
  HALF_OFF = 'Half Uit (Hiel bloot)',
  NEARBY = 'Ernaast (Uitgetrokken)'
}

// NIEUW: LICHTSTUDIO
export enum LightingStyle {
  LOCATION_DEFAULT = 'Locatie Standaard (Auto)',
  GOLDEN_HOUR = 'Golden Hour (Zon)',
  STUDIO_SOFTBOX = 'Studio Softbox (Clean)',
  REMBRANDT = 'Rembrandt (Moody/Dramatisch)',
  NEON_NOIR = 'Neon Noir (Blauw/Roze)',
  CANDLELIGHT = 'Kaarslicht (Warm/Intiem)',
  FLASH_HARD = 'Harde Flash (Paparazzi)',
  CINEMATIC_TEAL = 'Cinematic Teal & Orange'
}

// ACCESSOIRES (Uitgebreid)
export enum AccessoryType {
  NONE = 'Geen',
  ANKLET_GOLD = 'Gouden Enkelbandje (Fijn)',
  ANKLET_SILVER = 'Zilveren Enkelbandje (Bedels)',
  ANKLET_LEATHER = 'Leren Bandje (Boho)',
  TOE_RING_GOLD = 'Gouden Teenring',
  TOE_RING_SILVER = 'Zilveren Teenring',
  TOE_RING_CHAIN = 'Teenring met Ketting (Slave)',
  PIERCING_ANKLE = 'Dermal Piercing (Enkel)',
  TATTOO_ANKLE = 'Tattoo: Enkel (Roos/Bloem)',
  TATTOO_INSTEP = 'Tattoo: Wreef (Script)',
  TATTOO_LEG_SLEEVE = 'Tattoo: Full Leg Sleeve (Japans)',
  TATTOO_MINIMAL = 'Tattoo: Minimalistisch Lijnwerk',
  TATTOO_HENNA = 'Henna Versiering',
  PEARLS = 'Parelsnoer (Los om enkel)'
}

// FILM STOCKS (Analoge Fotografie)
export enum FilmStock {
  DIGITAL_CLEAN = 'Digitaal (Modern Scherp)',
  KODAK_PORTRA_400 = 'Kodak Portra 400 (Warme Huidtint)',
  KODAK_GOLD_200 = 'Kodak Gold (Vintage Vakantie)',
  FUJIFILM_VELVIA = 'Fujifilm Velvia (Hoog Contrast/Kleur)',
  ILFORD_HP5 = 'Ilford HP5 (Zwart/Wit Korrel)',
  CINESTILL_800T = 'CineStill 800T (Neon Halos)',
  POLAROID_600 = 'Polaroid 600 (Soft Vintage)',
  KODACHROME = 'Kodachrome 64 (Jaren 80 Look)'
}

export enum CameraAngle {
  // Standaard
  MACRO_TOES = 'Macro (Focus op Tenen)',
  MACRO_SOLE = 'Macro (Focus op Zool)',
  LOW_ANGLE = 'Kikkerperspectief (Vloer)',
  TOP_DOWN = 'Vogelperspectief (Top Down)',
  SIDE_PROFILE = 'Zijprofiel',
  DUTCH = 'Dutch Angle (Schuin)',
  SELFIE = 'Spiegel Selfie',
  
  // Nieuw / Uitgebreid
  LAP_VIEW = 'Schoot / Heupen (Wijde POV)',
  DRIVER_POV = 'Bestuurdersstoel (Auto POV)',
  POV = 'POV (Eigen Ogen)',
  BOTTOM_UP_GLASS = 'Onderkant (Door Glas)',
  HEEL_FOCUS = 'Close-up Hiel',
  ARCH_FOCUS = 'Close-up Wreef',
  FISH_EYE = 'Fish-Eye Lens',
  CCTV = 'CCTV (Beveiligingscamera)',
  DRONE = 'Drone Shot (Hoog)',
  OVER_SHOULDER = 'Over de Schouder',
  REAR_VIEW = 'Achteraanzicht',
  WIDE_STANCE = 'Wijde Stand (Laag)',
  CINEMATIC = 'Cinematisch Breedbeeld',
  POLAROID = 'Polaroid / Vintage Flash',
  NIGHT_VISION = 'Nachtkijker / IR',
  UNDERWATER_SPLIT = 'Half Onderwater',
  KEYHOLE = 'Sleutelgat Perspectief',
  BETWEEN_LEGS = 'Tussen de Benen Door',
  LYING_SIDE = 'Liggend op Zij',
  BED_POV = 'Vanuit Bed (Ooghoogte)',
  GLAMOUR = 'Soft Focus Glamour',
  PAPARAZZI = 'Paparazzi (Zoom van ver)',
  REFLECTION_PUDDLE = 'Reflectie in Plas',
  SHADOW_PLAY = 'Schaduwspel (Silhouet)',
  UPSIDE_DOWN = 'Ondersteboven'
}

export enum PoseType {
  // Standaard
  STANDING = 'Staand Natuurlijk',
  POINTING = 'Tenen Wijzen (Point)',
  CROSSED = 'Enkels Gekruist',
  ARCHED = 'Hoge Wreef (Flex)',
  SCRUNCHED = 'Tenen Gekruld (Scrunch)',
  LOTUS = 'Lotus / Zittend',
  DANGLED = 'Bungelend (Rand)',
  
  // Context Specifiek
  DRIVING = 'Autorijden (Pedalen)',
  ACTION_PEDAL = 'Pedaal Intrappen (Actie)',
  DASHBOARD = 'Op Dashboard (Passagier)',
  RECLINED = 'Liggend / Relaxed',
  DESK_BOSS = 'Op Bureau (Macht)',
  DESK_UNDER = 'Onder Bureau (Verstopt)',
  WALL_LEGS = 'Benen tegen Muur',
  KNEELING = 'Knielend / Kruipend',

  // Pin-up / Retro
  PINUP_KICK = 'Pin-up Kick',
  PINUP_KNEEL = 'Pin-up Knielend',
  PINUP_CROSS = 'Pin-up Gekruist',
  PINUP_RECLINE = 'Pin-up Liggend'
}

// --- LOCATIES ---
export enum LocationType {
  // Abstract
  STUDIO = 'Minimalistische Studio',
  CYBERPUNK = 'Neon Cyberpunk Stad',
  VAPORWAVE = 'Vaporwave Grid',
  VOID = 'Fluwelen Leegte (Zwart)',
  CLOUD = 'Wolkenrijk',
  GOLD_ROOM = 'Gouden Kamer',
  ICE_CAVE = 'IJsgrot',

  // Huiselijk
  BEDROOM = 'Luxe Slaapkamer',
  LIVING_ROOM = 'Woonkamer / Bank',
  KITCHEN = 'Keuken Aanrecht',
  BATHROOM = 'Marmeren Badkamer',
  CLOSET = 'Inloopkast',
  LAUNDRY = 'Wasruimte',
  HOME_OFFICE = 'Thuiswerkplek',
  BALCONY = 'Balkon (Zonsondergang)',
  
  // Luxe / Vakantie
  POOL = 'Zwembadrand',
  BEACH = 'Zandstrand',
  YACHT = 'Superjacht Dek',
  SPA = 'Luxe Spa / Sauna',
  HOT_TUB = 'Jacuzzi / Bubbelbad',
  GOLF_COURSE = 'Golfbaan (Green)',
  TENNIS_COURT = 'Tennisbaan (Clay)',
  WINERY = 'Wijnkelder / Wijngaard',
  HELIPAD = 'Helikopterplatform',
  CASINO = 'Casino Roulette Tafel',
  OPERA = 'Opera VIP Box',

  // Stedelijk / Rauw
  OFFICE = 'Kantoor / Onder Bureau',
  SUBWAY = 'Metro Station',
  ROOFTOP = 'Dakterras Stad',
  ALLEY = 'Betonnen Steeg',
  STAIRS = 'Industrieel Trappenhuis',
  NIGHTCLUB = 'Nachtclub VIP',
  ARCADE = 'Retro Arcadehal',
  GAS_STATION = 'Tankstation (Nacht)',
  PARKING_GARAGE = 'Parkeergarage',
  ELEVATOR = 'Lift (Spiegel)',
  ESCALATOR = 'Roltrap',

  // Natuur
  NATURE = 'Bosgrond / Mos',
  GARDEN = 'Bloementuin',
  DESERT = 'Woestijn Duinen',
  SNOW = 'Sneeuw / Blokhut',
  RIVER = 'Rivierkeien',
  WATERFALL = 'Waterval Mist',
  FIELD_FLOWERS = 'Veld met Wilde Bloemen',
  MOUNTAIN_PEAK = 'Bergtop Uitzicht',

  // Publiek / Professioneel
  LIBRARY = 'Oude Bibliotheek',
  GYM = 'Sportschool / Yoga Mat',
  CLASSROOM = 'Klaslokaal / Schoolbord',
  MUSEUM = 'Kunstgalerij',
  HOSPITAL = 'Ziekenhuisbed',
  BALLET = 'Ballet Studio',
  SUPERMARKET = 'Supermarkt Gangpad',
  CINEMA = 'Bioscoopzaal',

  // Voertuigen
  PRIVATE_JET = 'Privéjet',
  SPORTS_CAR = 'Sportauto Interieur',
  VINTAGE_CAR = 'Vintage Cabrio',
  LIMOUSINE = 'Limousine Achterbank',
  TESLA = 'Tesla Supercharger',
  YACHT_INTERIOR = 'Jacht Interieur',

  // Erotiek / Privé (NSFW)
  RED_WINDOW = 'Ramenkwartier (Red Light)',
  DUNGEON = 'Dungeon / Kelder',
  STRIP_STAGE = 'Stripclub Podium',
  LOVE_HOTEL = 'Love Hotel (Neon)',
  MASSAGE_TABLE = 'Olie Massage Tafel',
  POLE_STAGE = 'Paaldans Studio',
  SHOWER_GLASS = 'Stomende Douche (Glas)',
  LOCKER_ROOM = 'Dames Kleedkamer',
  SHIBARI_ROOM = 'Shibari Kamer (Touwen)',
  GOLDEN_CAGE = 'Gouden Kooi',
  VELVET_SWING = 'Fluwelen Schommel',
  MIRROR_MAZE = 'Spiegelkamer',
  ROUND_BED = 'Rond Satijnen Bed',
  LATEX_CHAMBER = 'Latex / Rubber Kamer',
  DARK_ALCOVE = 'Donkere Nis',
  PEEP_BOOTH = 'Kijkdoos / Booth',
  LEATHER_SOFA = 'Leren Casting Bank',
  PRIVATE_SAUNA = 'Privé Sauna (Rood)',
  BED_RESTRAINTS = 'Bed met Boeien',
  XXX_NEON = 'Neon X Kamer',
  EROTIC_EXHIBITION = 'Erotische Tentoonstelling'
}

export enum LocationCategory {
  DOMESTIC = '🏠 Huiselijk',
  LUXURY = '💎 Luxe & Sport',
  URBAN = '🏙️ Stad & Nacht',
  NATURE = '🌿 Natuur & Wild',
  PROFESSIONAL = '💼 Publiek & Werk',
  VEHICLE = '🏎️ Voertuigen',
  ABSTRACT = '🎨 Abstract & Kunst',
  EROTIC = '💋 Erotiek & Privé'
}

export interface DesignState {
  footShape: FootShape;
  archType: ArchType;
  skinTone: SkinTone;
  skinTexture: SkinTexture;
  modelAge: number;
  shoeSize: number;
  nailShape: NailShape;
  nailLength: string; 
  nailColor: string;
  nailStyle: NailStyle;
  nailFinish: NailFinish;
  hosieryType: HosieryType;
  hosieryDenier: HosieryDenier;
  hosieryPattern: HosieryPattern;
  hosieryColor: string;
  wornKnit: boolean;
  pose: PoseType;
  cameraAngle: CameraAngle;
  location: LocationType;
  prop: string;
  useProModel: boolean;
  
  // Nieuwe Velden
  accessory: AccessoryType;
  filmStock: FilmStock;
  
  // V3.0 Features
  footwearType: FootwearType;
  footwearState: FootwearState;
  footwearColor: string;
  lightingStyle: LightingStyle;

  // Custom AI Overrides
  customPose?: string;
  customCamera?: string;
}

export interface AIConcept {
    prop: string;
    customPose: string;
    customCamera: string;
    visualizerPose: PoseType;
    visualizerCamera: CameraAngle;
    visualizerFootwear: FootwearType; // Added
}

export const INITIAL_STATE: DesignState = {
  footShape: FootShape.GREEK,
  archType: ArchType.HIGH,
  skinTone: SkinTone.FAIR,
  skinTexture: SkinTexture.REALISTIC,
  modelAge: 24,
  shoeSize: 38,
  nailShape: NailShape.SQUOVAL,
  nailLength: 'Medium',
  nailColor: '#ff0044',
  nailStyle: NailStyle.SOLID,
  nailFinish: NailFinish.CREME,
  hosieryType: HosieryType.FISHNET,
  hosieryDenier: HosieryDenier.D15,
  hosieryPattern: HosieryPattern.LACE,
  hosieryColor: '#000000',
  wornKnit: false,
  pose: PoseType.STANDING, 
  cameraAngle: CameraAngle.LOW_ANGLE, 
  location: LocationType.BEDROOM,
  prop: 'Zijden Lakens',
  useProModel: false,
  
  // Nieuw
  accessory: AccessoryType.NONE,
  filmStock: FilmStock.DIGITAL_CLEAN,
  
  // V3.0
  footwearType: FootwearType.NONE,
  footwearState: FootwearState.WORN,
  footwearColor: '#000000',
  lightingStyle: LightingStyle.LOCATION_DEFAULT
};