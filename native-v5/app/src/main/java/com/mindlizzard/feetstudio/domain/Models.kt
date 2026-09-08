package com.mindlizzard.feetstudio.domain

import android.net.Uri

enum class StudioMode { SIMPLE, PRO }
enum class RenderMode { FAST, QUALITY, PRO }
enum class Resolution { K1, K2, K4 }
enum class AspectRatio(val apiValue: String) {
    SQUARE("1:1"), SOCIAL("4:5"), PORTRAIT("3:4"), CLASSIC("2:3"), TALL("9:16"), LANDSCAPE("16:9")
}
enum class ResolverMode { AUTO, ASK, STRICT, CREATIVE }
enum class DetailPriority { BALANCED, ANATOMY, HOSIERY, FOOTWEAR, SKIN, NAILS, SCENE }
enum class QualityProfile(val label: String) { STANDARD("Standard"), AURA("Aura"), ULTRA("Ultra 2-pass") }

enum class ImageEngine(val label: String) {
    GEMINI("Gemini"),
    HF_FREE("Hugging Face Free"),
    FLUX_HOSIERY("FLUX Hosiery Lab")
}

enum class HosieryLoraPreset(
    val label: String,
    val modelId: String,
    val triggerWord: String
) {
    SHEER_15D(
        "Sheer 15D",
        "civitai:866931@970135",
        "15tights"
    ),
    REINFORCED_TOE(
        "Reinforced toe",
        "civitai:1693845@1916978",
        "reinforced toes"
    ),
    SHINY(
        "Shiny pantyhose",
        "civitai:865294@1241316",
        "shiny pantyhose"
    ),
    AURORA_8D(
        "Aurora 8D",
        "civitai:1051712@1180123",
        "Tutu Aurora 8D Pantyhose"
    ),
    METALLIC(
        "Metallic / lamé",
        "civitai:1320762@1491138",
        "lame pantyhose"
    )
}


enum class FootShape(val label: String) {
    GREEK("Greek / second toe longer"), EGYPTIAN("Egyptian / tapered"), ROMAN("Roman / square"),
    PEASANT("Broad / peasant"), MOUNTAIN_PEAK("Mountain peak"), PETITE("Petite"), SLENDER("Slender / model"),
    EAST_ASIAN("East Asian / petite"), ARABIC("Arabic / Middle Eastern"), SOUTH_ASIAN("South Asian"),
    LATINA("Latina / South American"), NORDIC("Nordic / Scandinavian"), AFRICAN("African / deep melanin")
}

enum class ArchType(val label: String) {
    FLAT("Flat"), NORMAL("Normal"), HIGH("High arch"), EXTREME("Extreme / ballerina"), COMPRESSED("Compressed")
}

enum class SkinTone(val label: String) {
    PORCELAIN("Porcelain"), FAIR("Fair"), OLIVE("Olive"), TAN("Tan"), DEEP_BRONZE("Deep bronze"), EBONY("Ebony")
}

enum class SkinTexture(val label: String) {
    NATURAL("Natural"), REALISTIC("Realistic pores"), SMOOTH("Smooth"), SWEATY("Sweaty"), OILED("Oiled"),
    VEINY("Veiny"), DRY("Dry"), CALLOUSED("Calloused")
}

enum class SkinUndertone(val label: String) { COOL("Cool"), NEUTRAL("Neutral"), WARM("Warm"), OLIVE("Olive") }

enum class NailShape(val label: String) {
    NATURAL("Natural / short"), SQUOVAL("Squoval"), ALMOND("Almond"), STILETTO("Stiletto"),
    COFFIN("Coffin / ballerina"), EDGE("Edge"), DUCK("Flare / duck"), LIPSTICK("Lipstick")
}

enum class NailStyle(val label: String) {
    SOLID("Solid"), FRENCH("Classic French"), MICRO_FRENCH("Micro French"), FRENCH_BLACK("French noir"),
    OMBRE("Ombré / babyboomer"), CHROME("Chrome"), CAT_EYE("Cat eye"), MARBLE("Marble"),
    GEMS_BASE("Cuticle crystals"), GEMS_FULL("Full crystal"), MINIMALIST_LINE("Minimal line"), FLORAL("Floral")
}

enum class NailFinish(val label: String) {
    CREME("Crème"), JELLY("Jelly"), HOLOGRAPHIC("Holographic"), CHROME("Mirror chrome"),
    MATTE("Matte"), GLITTER("Glitter"), CHIPPED("Chipped")
}

enum class NailLength(val label: String) { SHORT("Short"), MEDIUM("Medium"), LONG("Long") }

enum class HosieryType(val label: String) {
    NONE("Bare"), ANKLE_SOCKS("Ankle socks"), KNEE_HIGH("Knee-high"), THIGH_HIGH("Thigh-high / stockings"),
    STOCKINGS("Stockings"), PANTYHOSE("Pantyhose"), FISHNET("Fishnet"), TOE_SOCKS("Toe socks"),
    VINTAGE_FF("Vintage FF seam"), VINTAGE_RHT("Vintage RHT")
}

enum class Denier(val label: String) { D5("5D"), D15("15D"), D30("30D"), D60("60D"), D100("100D") }

enum class HosieryPattern(val label: String) {
    NONE("Plain"), DOTS("Polka dots"), HEARTS("Hearts"), BOWS("Bows"), LACE("Floral lace"),
    SEAM("Back seam / Cuban heel"), DIAMOND("Diamond / argyle"), LOGOS("Monogram")
}

enum class HosieryFinish { MATTE, NATURAL, GLOSSY }
enum class MeshSize { MICRO, FINE, MEDIUM, WIDE }

enum class FootwearType(val label: String, val closedToe: Boolean) {
    NONE("None", false), STILETTO("Stiletto heels", true), PLATFORM("Platform heels", false),
    SANDALS("Strappy sandals", false), MULES("Mules", false), FLATS("Ballet flats", true),
    ANKLE_BOOTS("Ankle boots", true), KNEE_BOOTS("Knee boots", true), SNEAKERS("Sneakers", true),
    FLIP_FLOPS("Flip-flops", false)
}

enum class FootwearState(val label: String) {
    WORN("Worn"), HALF_OFF("Half off"), DANGLING("Dangling"), NEARBY("Nearby"), ONE_OFF("One off"), BOTH_OFF("Both off")
}

enum class AccessoryType(val label: String) {
    NONE("None"), ANKLET_GOLD("Gold anklet"), ANKLET_SILVER("Silver charm anklet"), ANKLET_LEATHER("Leather anklet"),
    TOE_RING_GOLD("Gold toe ring"), TOE_RING_SILVER("Silver toe ring"), TOE_RING_CHAIN("Toe ring chain"),
    PIERCING_ANKLE("Ankle dermal piercing"), TATTOO_ANKLE("Ankle tattoo"), TATTOO_INSTEP("Instep tattoo"),
    TATTOO_LEG_SLEEVE("Full leg tattoo"), TATTOO_MINIMAL("Minimal line tattoo"), TATTOO_HENNA("Henna"), PEARLS("Pearl strand")
}

enum class PoseType(val label: String) {
    STANDING("Standing"), POINTED("Pointed toes"), CROSSED("Crossed ankles"), ARCHED("High arch / flex"),
    SCRUNCHED("Curled toes"), LOTUS("Lotus / seated"), DANGLED("Dangling from edge"), DRIVING("Driving"),
    ACTION_PEDAL("Pressing pedal"), DASHBOARD("Feet on dashboard"), RECLINED("Reclined"), DESK_BOSS("Feet on desk"),
    DESK_UNDER("Under desk"), WALL_LEGS("Legs against wall"), KNEELING("Kneeling"), SOLE_FOCUS("Sole-focused"),
    PINUP_KICK("Pin-up kick"), PINUP_KNEEL("Pin-up kneel"), PINUP_CROSS("Pin-up crossed"), PINUP_RECLINE("Pin-up recline")
}

enum class CameraAngle(val label: String) {
    MACRO_TOES("Macro toes"), MACRO_SOLE("Macro sole"), LOW("Low angle"), TOP("Top-down"), SIDE("Side profile"),
    DUTCH("Dutch angle"), SELFIE("Mirror selfie"), LAP_VIEW("Lap / hip POV"), DRIVER("Driver POV"), POV("POV"),
    BOTTOM_UP_GLASS("Bottom-up through glass"), HEEL_FOCUS("Heel close-up"), ARCH_FOCUS("Arch close-up"),
    FISH_EYE("Fish-eye"), CCTV("CCTV"), DRONE("Drone"), OVER_SHOULDER("Over shoulder"), REAR_VIEW("Rear view"),
    WIDE_STANCE("Wide stance low"), CINEMATIC("Cinematic"), FULL_BODY("Full-body"), POLAROID("Polaroid flash"),
    NIGHT_VISION("Night vision / IR"), UNDERWATER_SPLIT("Half underwater"), KEYHOLE("Keyhole"),
    BETWEEN_LEGS("Between legs"), LYING_SIDE("Lying side"), BED_POV("Bed POV"), GLAMOUR("Soft glamour"),
    PAPARAZZI("Paparazzi zoom"), REFLECTION_PUDDLE("Puddle reflection"), SHADOW_PLAY("Shadow silhouette"),
    UPSIDE_DOWN("Upside down")
}

enum class Lens(val label: String) { MM16("16mm"), MM24("24mm"), MM35("35mm"), MM50("50mm"), MM85("85mm"), MM105("105mm"), MACRO("Macro") }

enum class SceneType(val label: String, val vehicle: Boolean = false) {
    STUDIO("Minimal studio"), CYBERPUNK("Cyberpunk neon city"), VAPORWAVE("Vaporwave grid"), VOID("Black velvet void"),
    CLOUD("Cloud set"), GOLD_ROOM("Gold room"), ICE_CAVE("Ice cave"),
    BEDROOM("Luxury bedroom"), LIVING_ROOM("Living room"), KITCHEN("Kitchen"), BATHROOM("Marble bathroom"),
    CLOSET("Walk-in closet"), LAUNDRY("Laundry room"), HOME_OFFICE("Home office"), BALCONY("Balcony / sunset"),
    POOL("Poolside"), BEACH("Sand beach"), YACHT("Yacht deck"), SPA("Luxury spa / sauna"), HOT_TUB("Hot tub"),
    GOLF_COURSE("Golf course"), TENNIS_COURT("Tennis court"), WINERY("Winery"), HELIPAD("Helipad"), CASINO("Casino"), OPERA("Opera VIP box"),
    OFFICE("Office"), SUBWAY("Subway station"), ROOFTOP("City rooftop"), ALLEY("Concrete alley"), STAIRS("Industrial stairs"),
    NIGHTCLUB("Nightclub"), ARCADE("Retro arcade"), GAS_STATION("Gas station / night"), PARKING_GARAGE("Parking garage"),
    ELEVATOR("Mirror elevator"), ESCALATOR("Escalator"),
    NATURE("Forest / moss"), GARDEN("Flower garden"), DESERT("Desert dunes"), SNOW("Snow / cabin"), RIVER("River stones"),
    WATERFALL("Waterfall mist"), FIELD_FLOWERS("Wildflower field"), MOUNTAIN_PEAK("Mountain viewpoint"),
    LIBRARY("Old library"), GYM("Gym / yoga"), CLASSROOM("Classroom"), MUSEUM("Museum gallery"), HOSPITAL("Hospital room"),
    BALLET("Ballet studio"), SUPERMARKET("Supermarket aisle"), CINEMA("Cinema"),
    PRIVATE_JET("Private jet"), SPORTS_CAR("Sports car interior", true), VINTAGE_CAR("Vintage convertible", true),
    LIMOUSINE("Limousine interior", true), TESLA("Modern EV interior", true), YACHT_INTERIOR("Yacht interior"),
    RED_WINDOW("Red-light window"), DUNGEON("Dungeon / cellar"), STRIP_STAGE("Club stage"), LOVE_HOTEL("Love hotel / neon"),
    MASSAGE_TABLE("Oil massage room"), POLE_STAGE("Pole studio"), SHOWER_GLASS("Steamy glass shower"), LOCKER_ROOM("Locker room"),
    SHIBARI_ROOM("Rope studio"), GOLDEN_CAGE("Golden cage room"), VELVET_SWING("Velvet swing room"), MIRROR_MAZE("Mirror room"),
    ROUND_BED("Round satin bed"), LATEX_CHAMBER("Latex / rubber room"), DARK_ALCOVE("Dark alcove"), PEEP_BOOTH("Private booth"),
    LEATHER_SOFA("Leather sofa studio"), PRIVATE_SAUNA("Private sauna"), BED_RESTRAINTS("Restraint-style bed room"), XXX_NEON("Neon X room"),
    EROTIC_EXHIBITION("Erotic exhibition")
}

enum class LightingPreset(val label: String) {
    LOCATION_DEFAULT("Location default"), NATURAL("Natural"), GOLDEN_HOUR("Golden hour"), SOFTBOX("Studio softbox"),
    REMBRANDT("Rembrandt / moody"), NEON("Neon noir"), CANDLELIGHT("Candlelight"), FLASH("Hard flash"), CINEMATIC("Cinematic teal & orange")
}

enum class FilmStock(val label: String) {
    DIGITAL_CLEAN("Digital clean"), KODAK_PORTRA_400("Kodak Portra 400"), KODAK_GOLD_200("Kodak Gold 200"),
    FUJIFILM_VELVIA("Fujifilm Velvia"), ILFORD_HP5("Ilford HP5 B&W"), CINESTILL_800T("CineStill 800T"),
    POLAROID_600("Polaroid 600"), KODACHROME("Kodachrome 64")
}

enum class ReferenceRole(val label: String) {
    FOOT_SHAPE("Foot shape"), SKIN("Skin"), NAILS("Nails"), HOSIERY("Hosiery"), FOOTWEAR("Footwear"),
    POSE("Pose"), CAMERA("Camera"), SCENE("Scene"), STYLE("Style")
}

enum class ReferenceStrength(val label: String) { EXACT("Exact"), STRONG("Strong"), GUIDED("Guided"), INSPIRATION("Inspiration") }

data class ReferenceAsset(
    val uri: Uri,
    val role: ReferenceRole = ReferenceRole.FOOT_SHAPE,
    val strength: ReferenceStrength = ReferenceStrength.STRONG,
    val enabled: Boolean = true
)

data class DesignState(
    val modelAge: Int = 24,
    val footShape: FootShape = FootShape.GREEK,
    val archType: ArchType = ArchType.HIGH,
    val shoeSize: Float = 38f,
    val footWidth: Int = 50,
    val heelWidth: Int = 50,
    val instep: Int = 60,
    val toeSpread: Int = 35,
    val toeLength: Int = 50,
    val footAsymmetry: Int = 8,
    val skinTone: SkinTone = SkinTone.FAIR,
    val skinTexture: SkinTexture = SkinTexture.REALISTIC,
    val skinUndertone: SkinUndertone = SkinUndertone.NEUTRAL,
    val skinPores: Int = 45,
    val skinVeins: Int = 20,
    val skinDryness: Int = 18,
    val skinRedness: Int = 10,
    val skinMoisture: Int = 8,
    val nailShape: NailShape = NailShape.SQUOVAL,
    val nailStyle: NailStyle = NailStyle.SOLID,
    val nailFinish: NailFinish = NailFinish.CREME,
    val nailLength: NailLength = NailLength.MEDIUM,
    val nailColor: String = "#D7263D",
    val hosieryType: HosieryType = HosieryType.FISHNET,
    val denier: Denier = Denier.D15,
    val hosieryPattern: HosieryPattern = HosieryPattern.LACE,
    val hosieryColor: String = "#111111",
    val hosieryFinish: HosieryFinish = HosieryFinish.NATURAL,
    val hosieryTension: Int = 70,
    val hosieryCompression: Int = 25,
    val hosieryWrinkles: Int = 12,
    val meshSize: MeshSize = MeshSize.MEDIUM,
    val meshThickness: Int = 45,
    val wornKnit: Boolean = false,
    val footwearType: FootwearType = FootwearType.NONE,
    val footwearState: FootwearState = FootwearState.WORN,
    val footwearColor: String = "#111111",
    val accessory: AccessoryType = AccessoryType.NONE,
    val pose: PoseType = PoseType.STANDING,
    val customPose: String = "",
    val cameraAngle: CameraAngle = CameraAngle.LOW,
    val cameraAzimuth: Int = 0,
    val cameraFocusY: Int = 50,
    val lens: Lens = Lens.MM85,
    val cameraDistance: Int = 50,
    val cameraHeight: Int = 35,
    val cameraTilt: Int = 0,
    val cameraRoll: Int = 0,
    val depthOfField: Int = 48,
    val customCamera: String = "",
    val scene: SceneType = SceneType.BEDROOM,
    val surface: String = "soft sheets",
    val lighting: LightingPreset = LightingPreset.LOCATION_DEFAULT,
    val filmStock: FilmStock = FilmStock.DIGITAL_CLEAN,
    val lightSoftness: Int = 72,
    val lightIntensity: Int = 55,
    val lightTemperature: Int = 50,
    val lightContrast: Int = 45
)

data class StudioSettings(
    val mode: StudioMode = StudioMode.SIMPLE,
    val renderMode: RenderMode = RenderMode.QUALITY,
    val resolution: Resolution = Resolution.K2,
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT,
    val resolverMode: ResolverMode = ResolverMode.AUTO,
    val detailPriority: DetailPriority = DetailPriority.BALANCED,
    val qualityProfile: QualityProfile = QualityProfile.AURA,
    val anatomyGuard: Boolean = true,
    val imageEngine: ImageEngine = ImageEngine.GEMINI,
    val hosieryLoraPreset: HosieryLoraPreset = HosieryLoraPreset.SHEER_15D,
    val hosieryLoraWeight: Int = 85,
    val batchCount: Int = 1,
    val variationStrength: Int = 25,
    val lockFeet: Boolean = false,
    val lockNails: Boolean = false,
    val lockHosiery: Boolean = false,
    val lockShoes: Boolean = false,
    val lockPose: Boolean = false,
    val lockCamera: Boolean = false,
    val lockScene: Boolean = false
)

data class WorkspaceState(val design: DesignState = DesignState(), val settings: StudioSettings = StudioSettings())

data class DerivedFacts(
    val wearingHosiery: Boolean,
    val fishnet: Boolean,
    val opaqueHosiery: Boolean,
    val wearingShoes: Boolean,
    val closedShoeWorn: Boolean,
    val toesVisible: Boolean,
    val nailsVisible: Boolean,
    val skinVisible: Boolean,
    val solesCoveredByHosiery: Boolean,
    val soleFocused: Boolean,
    val wideCamera: Boolean
)

data class ResolverDecision(val title: String, val detail: String, val applied: Boolean, val blocking: Boolean = false)

data class RenderContract(
    val requested: DesignState,
    val effective: DesignState,
    val settings: StudioSettings,
    val facts: DerivedFacts,
    val decisions: List<ResolverDecision>,
    val referenceManifest: List<String>,
    val prompt: String,
    val model: String,
    val imageSize: String,
    val aspectRatio: String
)

data class RenderRecord(
    val id: String,
    val imagePath: String,
    val createdAt: Long,
    val prompt: String,
    val model: String,
    val imageSize: String,
    val aspectRatio: String,
    val parentId: String? = null,
    val fixTarget: String? = null
)

enum class FixTarget { ANATOMY, HOSIERY, NAILS, FOOTWEAR, POSE, REALISM }
