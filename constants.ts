import { LocationType, LocationCategory, PoseType, LightingStyle, FootwearType } from "./types";

export const COLOR_PALETTE = {
  primary: '#ec4899', // Pink-500
  secondary: '#831843', // Pink-900
  dark: '#0f172a', // Slate-900
  panel: '#1e293b', // Slate-800
  text: '#f1f5f9' // Slate-100
};

// --- FOOTWEAR PHYSICS CATEGORIES ---
export const CLOSED_TOE_SHOES = [
  FootwearType.SNEAKERS,
  FootwearType.BOOTS_ANKLE,
  FootwearType.BOOTS_KNEE,
  FootwearType.BALLET_FLATS,
  FootwearType.HEELS_STILETTO, // Often closed, though can be peep. We treat generic stiletto as closed pump by default unless specified otherwise.
];

export const OPEN_TOE_SHOES = [
  FootwearType.SANDALS_STRAPPY,
  FootwearType.FLIP_FLOPS,
  FootwearType.MULES,
  FootwearType.HEELS_PLATFORM // Often open pleaser style
];

// Helper to categorize locations for the UI
export const LOCATION_CATEGORIES_MAP: Record<LocationCategory, LocationType[]> = {
  [LocationCategory.ABSTRACT]: [LocationType.STUDIO, LocationType.CYBERPUNK, LocationType.VAPORWAVE, LocationType.VOID, LocationType.CLOUD, LocationType.GOLD_ROOM, LocationType.ICE_CAVE],
  [LocationCategory.DOMESTIC]: [LocationType.BEDROOM, LocationType.LIVING_ROOM, LocationType.KITCHEN, LocationType.BATHROOM, LocationType.CLOSET, LocationType.LAUNDRY, LocationType.HOME_OFFICE, LocationType.BALCONY],
  [LocationCategory.LUXURY]: [LocationType.POOL, LocationType.YACHT, LocationType.SPA, LocationType.HOT_TUB, LocationType.BEACH, LocationType.GOLF_COURSE, LocationType.TENNIS_COURT, LocationType.WINERY, LocationType.HELIPAD, LocationType.CASINO, LocationType.OPERA],
  [LocationCategory.URBAN]: [LocationType.ROOFTOP, LocationType.ALLEY, LocationType.SUBWAY, LocationType.STAIRS, LocationType.NIGHTCLUB, LocationType.ARCADE, LocationType.GAS_STATION, LocationType.PARKING_GARAGE, LocationType.ELEVATOR, LocationType.ESCALATOR],
  [LocationCategory.NATURE]: [LocationType.NATURE, LocationType.GARDEN, LocationType.DESERT, LocationType.SNOW, LocationType.RIVER, LocationType.WATERFALL, LocationType.FIELD_FLOWERS, LocationType.MOUNTAIN_PEAK],
  [LocationCategory.PROFESSIONAL]: [LocationType.OFFICE, LocationType.LIBRARY, LocationType.GYM, LocationType.CLASSROOM, LocationType.MUSEUM, LocationType.HOSPITAL, LocationType.BALLET, LocationType.SUPERMARKET, LocationType.CINEMA],
  [LocationCategory.VEHICLE]: [LocationType.PRIVATE_JET, LocationType.SPORTS_CAR, LocationType.VINTAGE_CAR, LocationType.LIMOUSINE, LocationType.TESLA, LocationType.YACHT_INTERIOR],
  [LocationCategory.EROTIC]: [LocationType.RED_WINDOW, LocationType.DUNGEON, LocationType.STRIP_STAGE, LocationType.LOVE_HOTEL, LocationType.MASSAGE_TABLE, LocationType.POLE_STAGE, LocationType.SHOWER_GLASS, LocationType.LOCKER_ROOM, LocationType.SHIBARI_ROOM, LocationType.GOLDEN_CAGE, LocationType.VELVET_SWING, LocationType.MIRROR_MAZE, LocationType.ROUND_BED, LocationType.LATEX_CHAMBER, LocationType.DARK_ALCOVE, LocationType.PEEP_BOOTH, LocationType.LEATHER_SOFA, LocationType.PRIVATE_SAUNA, LocationType.BED_RESTRAINTS, LocationType.XXX_NEON, LocationType.EROTIC_EXHIBITION]
};

// --- FOOTWEAR PER CATEGORY (FOR SMART SHUFFLE) ---
export const FOOTWEAR_BY_CATEGORY: Record<LocationCategory, FootwearType[]> = {
  [LocationCategory.ABSTRACT]: [FootwearType.HEELS_STILETTO, FootwearType.NONE, FootwearType.HEELS_PLATFORM],
  [LocationCategory.DOMESTIC]: [FootwearType.NONE, FootwearType.BALLET_FLATS], // Mostly barefoot/socks
  [LocationCategory.LUXURY]: [FootwearType.SANDALS_STRAPPY, FootwearType.HEELS_STILETTO, FootwearType.MULES, FootwearType.NONE],
  [LocationCategory.URBAN]: [FootwearType.SNEAKERS, FootwearType.BOOTS_ANKLE, FootwearType.HEELS_PLATFORM],
  [LocationCategory.NATURE]: [FootwearType.NONE, FootwearType.SANDALS_STRAPPY, FootwearType.BOOTS_ANKLE],
  [LocationCategory.PROFESSIONAL]: [FootwearType.HEELS_STILETTO, FootwearType.BALLET_FLATS, FootwearType.BOOTS_ANKLE],
  [LocationCategory.VEHICLE]: [FootwearType.HEELS_STILETTO, FootwearType.SNEAKERS, FootwearType.BOOTS_KNEE, FootwearType.MULES],
  [LocationCategory.EROTIC]: [FootwearType.HEELS_PLATFORM, FootwearType.HEELS_STILETTO, FootwearType.BOOTS_KNEE]
};

// --- LOGICAL POSES PER CATEGORY (FOR SHUFFLE) ---
export const POSES_BY_CATEGORY: Record<LocationCategory, PoseType[]> = {
  [LocationCategory.ABSTRACT]: [PoseType.POINTING, PoseType.ARCHED, PoseType.STANDING, PoseType.DANGLED, PoseType.PINUP_KICK],
  [LocationCategory.DOMESTIC]: [PoseType.RECLINED, PoseType.CROSSED, PoseType.KNEELING, PoseType.WALL_LEGS, PoseType.STANDING, PoseType.PINUP_RECLINE],
  [LocationCategory.LUXURY]: [PoseType.RECLINED, PoseType.ARCHED, PoseType.DANGLED, PoseType.CROSSED, PoseType.PINUP_CROSS],
  [LocationCategory.URBAN]: [PoseType.STANDING, PoseType.CROSSED, PoseType.SCRUNCHED, PoseType.POINTING, PoseType.PINUP_KNEEL],
  [LocationCategory.NATURE]: [PoseType.STANDING, PoseType.SCRUNCHED, PoseType.POINTING, PoseType.ARCHED, PoseType.KNEELING],
  [LocationCategory.PROFESSIONAL]: [PoseType.DESK_BOSS, PoseType.DESK_UNDER, PoseType.CROSSED, PoseType.STANDING, PoseType.ARCHED, PoseType.PINUP_CROSS],
  [LocationCategory.VEHICLE]: [PoseType.DRIVING, PoseType.ACTION_PEDAL, PoseType.DASHBOARD, PoseType.RECLINED, PoseType.CROSSED],
  [LocationCategory.EROTIC]: [PoseType.KNEELING, PoseType.ARCHED, PoseType.PINUP_KNEEL, PoseType.PINUP_RECLINE, PoseType.PINUP_CROSS, PoseType.RECLINED, PoseType.WALL_LEGS, PoseType.CROSSED]
};

// Base interactions that apply to almost any surface (Translated)
const UNIVERSAL_INTERACTIONS = [
  'Natuurlijk staand',
  'Op tenen staan / Reiken',
  'Eén voet rustend op enkel',
  'Zolen naar camera gericht',
  'Tenen gekruld tegen oppervlak',
  'Stap naar voren',
  'Weglopend',
  'Gekruiste enkels',
  'Losjes bungelend',
  'Wreef strekken'
];

// Helper to combine specific props with universal actions
const combine = (specifics: string[]) => [...specifics, ...UNIVERSAL_INTERACTIONS];

export const PROPS_BY_LOCATION: Record<LocationType, string[]> = {
  // --- ABSTRACT ---
  [LocationType.STUDIO]: combine(['Geen (Schoon)', 'Glazen Blok', 'Spiegelvloer', 'Fluwelen Doek', 'Hangende Ring', 'Witte Kubus', 'Rook', 'Spotlight Straal']),
  [LocationType.CYBERPUNK]: combine(['Neon Kabels', 'Nat Asfalt', 'Hologram Projector', 'Chroom Buis', 'Oude Printplaat', 'Gloeiend Rooster', 'Robot Onderdeel']),
  [LocationType.VAPORWAVE]: combine(['Grieks Borstbeeld', 'Schaakbord Vloer', 'Zwevend Palmblad', 'Roze Water', 'Oude TV', 'Glitch Effect']),
  [LocationType.VOID]: combine(['Zwart Water', 'Zwevend in Duisternis', 'Spotlight van boven', 'Onzichtbaar Oppervlak', 'Mist']),
  [LocationType.CLOUD]: combine(['Zachte Wolken', 'Gouden Hek', 'Zwevende Veer', 'Zonnestraal', 'Glazen Podium']),
  [LocationType.GOLD_ROOM]: combine(['Gouden Staven', 'Vloeibaar Goud', 'Muntstukken', 'Gouden Troon', 'Glitter Regen']),
  [LocationType.ICE_CAVE]: combine(['Blauw IJs', 'Bevroren Meer', 'Ijskristallen', 'Sneeuwvlokken', 'Gletsjerspleet']),

  // --- DOMESTIC ---
  [LocationType.BEDROOM]: combine(['Zijden Lakens', 'Hoogpolig Tapijt', 'Rand van Bed', 'Stapel Kussens', 'Lotion Fles', 'Wekker', 'Kleding op Grond', 'Ochtendzon']),
  [LocationType.LIVING_ROOM]: combine(['Koffietafel Rand', 'Zacht Vloerkleed', 'Leren Bankleuning', 'Afstandsbediening', 'Wijnglas', 'Open Haard', 'Tijdschrift']),
  [LocationType.KITCHEN]: combine(['Granieten Aanrecht', 'Tegelvloer', 'Gemorste Melk', 'Gevallen Kers', 'Koelkast Licht', 'Barkruk', 'Keramische Kom']),
  [LocationType.BATHROOM]: combine(['Natte Tegels', 'Badmat', 'Rand van Bad', 'Sopwater', 'Rubbereend', 'Handdoeken', 'Beslagen Spiegel']),
  [LocationType.CLOSET]: combine(['Schoenendozen', 'Kledingstapel', 'Spiegel', 'Bontjas op Grond', 'Hoge Hakken (Los)', 'Sieradendoos']),
  [LocationType.LAUNDRY]: combine(['Wasmachine Bovenkant', 'Wasmand', 'Gemorst Wasmiddel', 'Stapel Handdoeken', 'Koude Tegelvloer']),
  [LocationType.HOME_OFFICE]: combine(['Laptop Rand', 'Notitieblok', 'Koffiemok', 'Bureaustoel Wiel', 'Stapel Papier', 'Bril']),
  [LocationType.BALCONY]: combine(['Metalen Railing', 'Betonvloer', 'Zonsondergang Licht', 'Plant in Pot', 'Ligstoel', 'Stadszicht']),

  // --- LUXURY ---
  [LocationType.POOL]: combine(['Wateroppervlak', 'Zwembad Trap', 'Nat Dek', 'Drijvende Blaadjes', 'Opblaasbare Flamingo', 'Blauwe Tegels', 'Cocktailglas']),
  [LocationType.YACHT]: combine(['Teakhouten Dek', 'Railing (Leunend)', 'Ligbed', 'Champagnekoeler', 'Touw', 'Oceaanwater (Achtergrond)', 'Glazen Tafel']),
  [LocationType.SPA]: combine(['Massagetafel', 'Hete Stenen', 'Orchidee', 'Houten Sauna Bank', 'Stoom', 'Witte Handdoek', 'Etherische Olie']),
  [LocationType.HOT_TUB]: combine(['Bubbelend Water', 'Jets (Massagestraal)', 'Stoom', 'Plastic Beker', 'Onderwaterlicht', 'Sneeuw rondom Bad']),
  [LocationType.BEACH]: combine(['Nat Zand', 'Droog Wit Zand', 'Zeeschuim', 'Drijfhout', 'Schelpen', 'Zeester', 'Strandlaken', 'Zandkasteel']),
  [LocationType.GOLF_COURSE]: combine(['Golfballetje', 'Green (Gras)', 'Golfclub', 'Zandbunker', 'Golfkarretje']),
  [LocationType.TENNIS_COURT]: combine(['Gravel (Rood)', 'Witte Lijn', 'Tennisbal', 'Tennisracket', 'Net', 'Scheidsrechterstoel']),
  [LocationType.WINERY]: combine(['Eikenhouten Vat', 'Wijnfles', 'Druiven', 'Kurk', 'Stenen Vloer', 'Wijnglas (Omgevallen)']),
  [LocationType.HELIPAD]: combine(['H-Markering', 'Asfalt', 'Helikopter Skid', 'Stadslichten', 'Windzak']),
  [LocationType.CASINO]: combine(['Roulette Tafel', 'Fiches', 'Speelkaarten', 'Gokautomaat', 'Rood Tapijt', 'Dobbelstenen']),
  [LocationType.OPERA]: combine(['Fluwelen Stoel', 'Verrekijker', 'Balkonrand', 'Programma Boekje', 'Marmeren Zuil']),

  // --- URBAN ---
  [LocationType.ROOFTOP]: combine(['Grinddak', 'Ventilatiebuis', 'Rand van Dak', 'Stadverlichting Bokeh', 'Plas Reflectie', 'Loungestoel']),
  [LocationType.ALLEY]: combine(['Gescheurd Beton', 'Olievlek', 'Vuilniszak', 'Bakstenen Muur', 'Metalen Rooster', 'Kartonnen Doos', 'Neon Reflectie']),
  [LocationType.SUBWAY]: combine(['Gele Veiligheidslijn', 'Metrostoel (Zittend)', 'Toegangspoortje', 'Tegelmuur', 'Gevallen Ticket', 'Metalen Paal']),
  [LocationType.STAIRS]: combine(['Metalen Trede', 'Betonnen Trede', 'Leuning', 'Schaduwen', 'Industriële Lamp', 'Sigarettenpeuk (Prop)']),
  [LocationType.NIGHTCLUB]: combine(['Plakkerige Vloer', 'Confetti', 'Discobol Reflectie', 'Fluwelen Koord', 'Speaker Box', 'Laserlicht']),
  [LocationType.ARCADE]: combine(['Dance Dance Revolution Pad', 'Joystick', 'Neon Tokens', 'Flikkerend Scherm', 'Kauwgumbal']),
  [LocationType.GAS_STATION]: combine(['Benzinepomp', 'Olievlek', 'Beton', 'Snoepwikkel', 'TL-Licht']),
  [LocationType.PARKING_GARAGE]: combine(['Parkeervak Lijn', 'Betonnen Pylaar', 'Slagboom', 'Betaalautomaat', 'Schaduw']),
  [LocationType.ELEVATOR]: combine(['Spiegelwand', 'Metalen Vloer', 'Knopjespaneel', 'Handrail', 'Deur']),
  [LocationType.ESCALATOR]: combine(['Bewegende Trede', 'Borstelrand', 'Rubberen Leuning', 'Glaswand']),

  // --- NATURE ---
  [LocationType.NATURE]: combine(['Mossige Boomstam', 'Varens', 'Zandpad', 'Paddenstoel', 'Gevallen Bladeren', 'Boomwortels', 'Zonlicht door Bomen']),
  [LocationType.GARDEN]: combine(['Groen Gras', 'Bloeiende Rozen', 'Tuinslang', 'Stenen Pad', 'Madeliefjes', 'Gieter', 'Picknickkleed']),
  [LocationType.DESERT]: combine(['Zandduin', 'Gebarsten Aarde', 'Cactus', 'Droge Tak', 'Schorpioen (Prop)', 'Zonsondergang Schaduw']),
  [LocationType.SNOW]: combine(['Verse Sneeuw', 'IJsplaat', 'Houten Veranda', 'IJskegel', 'Bontlaarzen (Ernaast)', 'Vuurkorf']),
  [LocationType.RIVER]: combine(['Gladde Rivierkeien', 'Stromend Water', 'Modderige Oever', 'Kikker (Prop)', 'Drijfhout', 'Herfstblad']),
  [LocationType.WATERFALL]: combine(['Natte Rotsen', 'Mist/Nevel', 'Regenboog', 'Varens', 'Diep Water']),
  [LocationType.FIELD_FLOWERS]: combine(['Wilde Bloemen', 'Hoog Gras', 'Vlinder', 'Picknickmand', 'Zonlicht']),
  [LocationType.MOUNTAIN_PEAK]: combine(['Grijze Rots', 'Sneeuwresten', 'Klimtouw', 'Karabijnhaak', 'Verrekijker']),

  // --- PROFESSIONAL ---
  [LocationType.OFFICE]: combine(['Voeten op Bureau (Boss)', 'Onder Bureau (Schoenen uit)', 'Tegen Bureaustoel', 'Gevallen Pen', 'Kabels', 'Prullenbak']),
  [LocationType.LIBRARY]: combine(['Ladder (Klimmend)', 'Open Boek', 'Tafelrand', 'Stapel Boeken', 'Leesbril', 'Houten Vloer', 'Stilte Bord']),
  [LocationType.GYM]: combine(['Yoga Mat', 'Halter', 'Fitnessbal (Leunend)', 'Rubberen Vloer', 'Bankdrukken', 'Handdoek', 'Waterfles']),
  [LocationType.CLASSROOM]: combine(['Houten Tafel', 'Krijtstof', 'Gevallen Potlood', 'Linoleum Vloer', 'Stoelpoot', 'Rugzak']),
  [LocationType.MUSEUM]: combine(['Gepolijste Vloer', 'Afzetkoord', 'Marmeren Sokkel', 'Spotlight', 'Lijst', 'Bankje']),
  [LocationType.HOSPITAL]: combine(['Witte Lakens', 'Bedhek', 'Linoleum Tegel', 'Polsbandje', 'Kabel']),
  [LocationType.BALLET]: combine(['Houten Vloer', 'Harsbak', 'Barre (Rustend)', 'Lint', 'Spitzenschoenen', 'Spiegel']),
  [LocationType.SUPERMARKET]: combine(['Winkelwagenwiel', 'Gevallen Product', 'Koelcel Vloer', 'Tegels']),
  [LocationType.CINEMA]: combine(['Popcorn op Grond', 'Bioscoopstoel', 'Cola Beker', 'Rood Tapijt', 'Ticket']),

  // --- VEHICLE ---
  [LocationType.PRIVATE_JET]: combine(['Leren Stoel (Reclined)', 'Champagneglas', 'Dik Tapijt', 'Raamuitzicht (Wolken)', 'Uitklaptafel', 'Tablet']),
  [LocationType.SPORTS_CAR]: combine(['Voeten op Dashboard (Passagier)', 'Bestuurdersstoel (Rustend op Pedalen)', 'Pedaal Intrappen (Actie)', 'Benen uit het Raam', 'Op de Middenconsole', 'Stuurwiel (Van bovenaf)']),
  [LocationType.VINTAGE_CAR]: combine(['Voeten op Chroom Bumper', 'Beige Leer', 'Houtinleg', 'Dak Open (Lucht)', 'Radio Knop']),
  [LocationType.LIMOUSINE]: combine(['Voeten op de Bar', 'Sterrenhemel Plafond', 'Fluwelen Bank', 'Privacy Scherm', 'IJsemmer', 'Rode Loper (Buiten)']),
  [LocationType.TESLA]: combine(['Supercharger Kabel', 'Wit Leer', 'Groot Scherm', 'Glazen Dak', 'Autopilot Scherm']),
  [LocationType.YACHT_INTERIOR]: combine(['Hoogglans Hout', 'Marmeren Tafel', 'Nautische Kaart', 'Patrijspoort', 'Wit Linnen']),

  // --- EROTIC (NSFW/Suggestive) ---
  [LocationType.RED_WINDOW]: combine(['Neon Rode gloed', 'Fluwelen Kruk', 'Glazen Raam', 'Gordijn', 'Rode Lamp']),
  [LocationType.DUNGEON]: combine(['Stalen Ring (Vloer)', 'Leren Zweep (Prop)', 'Handboeien (Enkel)', 'Koude Stenen Vloer', 'Kettingen', 'Houten Kruis']),
  [LocationType.STRIP_STAGE]: combine(['Chromen Paal', 'Geld op Vloer', 'Hoge Hak (Uitgetrokken)', 'Glitter Podium', 'Spotlight']),
  [LocationType.LOVE_HOTEL]: combine(['Rond Draaiend Bed', 'Spiegelplafond', 'Neon Hart', 'Jacuzzi Rand', 'Bedieningspaneel']),
  [LocationType.MASSAGE_TABLE]: combine(['Olie Flesje', 'Handdoekrol', 'Gezichtsuitsparing', 'Warme Steen', 'Kaarslicht']),
  [LocationType.POLE_STAGE]: combine(['Paal (Klimmend)', 'Paal (Draaiend)', 'Hars Potje', 'Kniebeschermers', 'Spiegelwand']),
  [LocationType.SHOWER_GLASS]: combine(['Beslagen Glas', 'Waterdruppels', 'Handafdruk op Glas', 'Zeep', 'Tegelmuur']),
  [LocationType.LOCKER_ROOM]: combine(['Metalen Kluisje', 'Houten Bankje', 'Sporttas', 'Handdoek', 'Douchestraal (Achtergrond)']),
  [LocationType.SHIBARI_ROOM]: combine(['Rood Touw', 'Hangende Knoop', 'Tatami Mat', 'Bamboe', 'Zijde']),
  [LocationType.GOLDEN_CAGE]: combine(['Gouden Tralies', 'Fluwelen Kussen', 'Slot', 'Sleutel', 'Vogelkooi']),
  [LocationType.VELVET_SWING]: combine(['Schommelzitting', 'Kettingen', 'Pluche Tapijt', 'Spotlight']),
  [LocationType.MIRROR_MAZE]: combine(['Oneindige Spiegel', 'LED Strip', 'Vingerafdruk', 'Reflectie']),
  [LocationType.ROUND_BED]: combine(['Satijnen Lakens', 'Rozenblaadjes', 'Champagne', 'Kussens', 'Plafondspiegel']),
  [LocationType.LATEX_CHAMBER]: combine(['Zwart Rubber Vloer', 'Glanzend Laken', 'Gasmasker (Prop)', 'Olievlek']),
  [LocationType.DARK_ALCOVE]: combine(['Fluwelen Gordijn', 'Kaars', 'Schaduw', 'Geheim Luik']),
  [LocationType.PEEP_BOOTH]: combine(['Muntinworp', 'Glaswand', 'Tissue Box', 'Neon Bordje', 'Stoel']),
  [LocationType.LEATHER_SOFA]: combine(['Zwart Leer', 'Capitonnering', 'Armleuning', 'Casting Lamp', 'Statief']),
  [LocationType.PRIVATE_SAUNA]: combine(['Rood Infrarood Licht', 'Houten Bank', 'Thermometer', 'Handdoek', 'Wateremmer']),
  [LocationType.BED_RESTRAINTS]: combine(['Leren Manchet', 'Zijden Sjaal', 'Bedpost', 'Satijnen Laken']),
  [LocationType.XXX_NEON]: combine(['Neon Kruis', 'Rookmachine', 'Betonmuur', 'Graffiti', 'Stroboscoop']),
  [LocationType.EROTIC_EXHIBITION]: combine(['Marmeren Torso', 'Fluwelen Koord', 'Kunst Podium', 'Gouden Lijst', 'Glazen Vitrine', 'Spotlight'])
};