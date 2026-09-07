package com.mindlizzard.feetstudio.domain

import android.net.Uri

enum class StudioMode { SIMPLE, PRO }
enum class RenderMode { FAST, QUALITY, PRO }
enum class Resolution { K1, K2, K4 }
enum class AspectRatio(val apiValue: String) {
    SQUARE("1:1"), PORTRAIT("3:4"), SOCIAL("4:5"), TALL("9:16"), LANDSCAPE("16:9")
}
enum class ResolverMode { AUTO, ASK, STRICT, CREATIVE }
enum class DetailPriority { BALANCED, ANATOMY, HOSIERY, FOOTWEAR, SKIN, NAILS, SCENE }

enum class FootShape(val label: String) {
    GREEK("Greek / second toe longer"),
    EGYPTIAN("Egyptian / tapered"),
    ROMAN("Roman / first three even"),
    BROAD("Broad"),
    PETITE("Petite"),
    SLENDER("Slender")
}
enum class ArchType(val label: String) {
    FLAT("Flat"), NORMAL("Normal"), HIGH("High"), EXTREME("Extreme")
}
enum class SkinTone(val label: String) {
    PORCELAIN("Porcelain"), FAIR("Fair"), OLIVE("Olive"), TAN("Tan"), DEEP("Deep")
}
enum class SkinTexture(val label: String) {
    NATURAL("Natural"), SMOOTH("Smooth"), VEINY("Veiny"), DRY("Dry"), OILED("Oiled")
}
enum class NailShape(val label: String) {
    NATURAL("Natural"), SQUOVAL("Squoval"), ALMOND("Almond"), COFFIN("Coffin")
}
enum class NailStyle(val label: String) {
    SOLID("Solid"), FRENCH("French"), MICRO_FRENCH("Micro French"), OMBRE("Ombré"),
    CHROME("Chrome"), CAT_EYE("Cat Eye"), MARBLE("Marble"), FLORAL("Floral")
}
enum class HosieryType(val label: String) {
    NONE("Bare"), PANTYHOSE("Pantyhose"), STOCKINGS("Stockings"), THIGH_HIGH("Thigh-high"),
    KNEE_HIGH("Knee-high"), SOCKS("Socks"), FISHNET("Fishnet"), VINTAGE_FF("Vintage FF")
}
enum class Denier(val label: String) {
    D5("5D"), D15("15D"), D30("30D"), D60("60D"), D100("100D")
}
enum class HosieryFinish { MATTE, NATURAL, GLOSSY }
enum class MeshSize { MICRO, FINE, MEDIUM, WIDE }

enum class FootwearType(val label: String, val closedToe: Boolean) {
    NONE("None", false),
    STILETTO("Stiletto heels", true),
    PLATFORM("Platform heels", false),
    SANDALS("Strappy sandals", false),
    MULES("Mules", false),
    FLATS("Ballet flats", true),
    ANKLE_BOOTS("Ankle boots", true),
    KNEE_BOOTS("Knee boots", true),
    SNEAKERS("Sneakers", true),
    FLIP_FLOPS("Flip-flops", false)
}
enum class FootwearState(val label: String) {
    WORN("Worn"), HALF_OFF("Half off"), DANGLING("Dangling"), NEARBY("Nearby"), ONE_OFF("One off"), BOTH_OFF("Both off")
}

enum class PoseType(val label: String) {
    STANDING("Standing"), SITTING("Sitting"), RECLINED("Reclined"), KNEELING("Kneeling"),
    WALKING("Walking"), DRIVING("Driving"), DASHBOARD("Dashboard"), SOLE_FOCUS("Sole-focused"),
    POINTED("Pointed toes"), CROSSED("Crossed ankles")
}
enum class CameraAngle(val label: String) {
    MACRO_TOES("Macro toes"), MACRO_SOLE("Macro sole"), LOW("Low angle"), TOP("Top-down"),
    SIDE("Side profile"), POV("POV"), DRIVER("Driver POV"), CINEMATIC("Cinematic"), FULL_BODY("Full-body")
}
enum class Lens(val label: String) {
    MM16("16mm"), MM24("24mm"), MM35("35mm"), MM50("50mm"), MM85("85mm"), MM105("105mm"), MACRO("Macro")
}
enum class SceneType(val label: String, val vehicle: Boolean = false) {
    STUDIO("Minimal studio"),
    CYBERPUNK("Cyberpunk neon city"),
    VAPORWAVE("Vaporwave grid"),
    VOID("Black velvet void"),
    CLOUD("Cloud set"),
    GOLD_ROOM("Gold room"),
    ICE_CAVE("Ice cave"),
    BEDROOM("Luxury bedroom"),
    LIVING_ROOM("Living room"),
    KITCHEN("Kitchen"),
    BATHROOM("Marble bathroom"),
    CLOSET("Walk-in closet"),
    LAUNDRY("Laundry room"),
    HOME_OFFICE("Home office"),
    BALCONY("Balcony / sunset"),
    POOL("Poolside"),
    BEACH("Sand beach"),
    YACHT("Yacht deck"),
    SPA("Luxury spa / sauna"),
    HOT_TUB("Hot tub"),
    GOLF_COURSE("Golf course"),
    TENNIS_COURT("Tennis court"),
    WINERY("Winery"),
    HELIPAD("Helipad"),
    CASINO("Casino"),
    OPERA("Opera VIP box"),
    OFFICE("Office"),
    SUBWAY("Subway station"),
    ROOFTOP("City rooftop"),
    ALLEY("Concrete alley"),
    STAIRS("Industrial stairs"),
    NIGHTCLUB("Nightclub"),
    ARCADE("Retro arcade"),
    GAS_STATION("Gas station / night"),
    PARKING_GARAGE("Parking garage"),
    ELEVATOR("Mirror elevator"),
    ESCALATOR("Escalator"),
    NATURE("Forest / moss"),
    GARDEN("Flower garden"),
    DESERT("Desert dunes"),
    SNOW("Snow / cabin"),
    RIVER("River stones"),
    WATERFALL("Waterfall mist"),
    FIELD_FLOWERS("Wildflower field"),
    MOUNTAIN_PEAK("Mountain viewpoint"),
    LIBRARY("Old library"),
    GYM("Gym / yoga"),
    CLASSROOM("Classroom"),
    MUSEUM("Museum gallery"),
    HOSPITAL("Hospital room"),
    BALLET("Ballet studio"),
    SUPERMARKET("Supermarket aisle"),
    CINEMA("Cinema"),
    PRIVATE_JET("Private jet"),
    SPORTS_CAR("Sports car interior", true),
    VINTAGE_CAR("Vintage convertible", true),
    LIMOUSINE("Limousine interior", true),
    TESLA("Modern EV interior", true),
    YACHT_INTERIOR("Yacht interior")
}
enum class LightingPreset(val label: String) {
    NATURAL("Natural"), SOFTBOX("Softbox"), GOLDEN_HOUR("Golden hour"), FLASH("Flash"),
    NEON("Neon"), MOODY("Moody"), CINEMATIC("Cinematic")
}

enum class ReferenceRole { FOOT_SHAPE, SKIN, NAILS, HOSIERY, FOOTWEAR, POSE, CAMERA, SCENE, STYLE }
enum class ReferenceStrength { EXACT, STRONG, GUIDED, INSPIRATION }

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
    val skinTone: SkinTone = SkinTone.FAIR,
    val skinTexture: SkinTexture = SkinTexture.NATURAL,
    val skinPores: Int = 45,
    val skinVeins: Int = 20,
    val skinDryness: Int = 15,
    val nailShape: NailShape = NailShape.SQUOVAL,
    val nailStyle: NailStyle = NailStyle.SOLID,
    val nailColor: String = "#D9466F",
    val hosieryType: HosieryType = HosieryType.PANTYHOSE,
    val denier: Denier = Denier.D15,
    val hosieryColor: String = "#111111",
    val hosieryFinish: HosieryFinish = HosieryFinish.NATURAL,
    val hosieryTension: Int = 70,
    val hosieryWrinkles: Int = 12,
    val meshSize: MeshSize = MeshSize.MEDIUM,
    val meshThickness: Int = 45,
    val footwearType: FootwearType = FootwearType.NONE,
    val footwearState: FootwearState = FootwearState.WORN,
    val footwearColor: String = "#111111",
    val pose: PoseType = PoseType.RECLINED,
    val cameraAngle: CameraAngle = CameraAngle.LOW,
    val lens: Lens = Lens.MM85,
    val cameraDistance: Int = 50,
    val cameraHeight: Int = 35,
    val cameraTilt: Int = 0,
    val depthOfField: Int = 48,
    val scene: SceneType = SceneType.BEDROOM,
    val surface: String = "soft sheets",
    val lighting: LightingPreset = LightingPreset.NATURAL,
    val lightSoftness: Int = 70,
    val lightIntensity: Int = 55
)

data class StudioSettings(
    val mode: StudioMode = StudioMode.SIMPLE,
    val renderMode: RenderMode = RenderMode.QUALITY,
    val resolution: Resolution = Resolution.K2,
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT,
    val resolverMode: ResolverMode = ResolverMode.AUTO,
    val detailPriority: DetailPriority = DetailPriority.BALANCED,
    val batchCount: Int = 1,
    val lockFeet: Boolean = false,
    val lockNails: Boolean = false,
    val lockHosiery: Boolean = false,
    val lockShoes: Boolean = false,
    val lockPose: Boolean = false,
    val lockCamera: Boolean = false,
    val lockScene: Boolean = false
)

data class WorkspaceState(
    val design: DesignState = DesignState(),
    val settings: StudioSettings = StudioSettings()
)

data class DerivedFacts(
    val wearingHosiery: Boolean,
    val fishnet: Boolean,
    val opaqueHosiery: Boolean,
    val wearingShoes: Boolean,
    val closedShoeWorn: Boolean,
    val toesVisible: Boolean,
    val nailsVisible: Boolean,
    val skinVisible: Boolean,
    val soleFocused: Boolean
)

data class ResolverDecision(
    val title: String,
    val detail: String,
    val applied: Boolean,
    val blocking: Boolean = false
)

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
