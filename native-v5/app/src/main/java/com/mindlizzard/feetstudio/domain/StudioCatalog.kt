package com.mindlizzard.feetstudio.domain

data class NamedColor(val label: String, val hex: String, val promptName: String = label)
enum class SceneCategory(val label: String) {
    ABSTRACT("Abstract"), HOME("Home"), LUXURY("Luxury"), URBAN("Urban"), NATURE("Nature"), PROFESSIONAL("Professional"), VEHICLE("Vehicle"), PRIVATE("Private / adult")
}
data class SceneSurface(val label: String, val prompt: String = label.lowercase())

object ColorCatalog {
    val hosiery = listOf(
        NamedColor("Black", "#111111", "black"), NamedColor("Jet black", "#000000", "jet black"),
        NamedColor("Charcoal", "#343434", "charcoal"), NamedColor("Graphite", "#565656", "graphite grey"),
        NamedColor("Smoke", "#767676", "smoke grey"), NamedColor("Grey", "#9A9A9A", "medium grey"),
        NamedColor("White", "#F4F4F2", "soft white"), NamedColor("Ivory", "#F1E7D0", "ivory"),
        NamedColor("Nude light", "#E9C8AF", "light nude"), NamedColor("Nude", "#D4A886", "natural nude"),
        NamedColor("Tan", "#B9825B", "tan"), NamedColor("Bronze", "#8C5B3D", "bronze brown"),
        NamedColor("Chocolate", "#4D2F24", "chocolate brown"), NamedColor("Navy", "#18233F", "navy blue"),
        NamedColor("Burgundy", "#6A1E2D", "burgundy"), NamedColor("Red", "#B51F32", "deep red")
    )
    val nails = listOf(
        NamedColor("Classic red", "#D7263D", "classic red"), NamedColor("Cherry", "#B11226", "cherry red"),
        NamedColor("Burgundy", "#681825", "burgundy"), NamedColor("Wine", "#7B1E3A", "wine red"),
        NamedColor("Pink", "#E85D8A", "pink"), NamedColor("Soft pink", "#F3B6C9", "soft pink"),
        NamedColor("Nude pink", "#D9A4A7", "nude pink"), NamedColor("Beige nude", "#D8B39A", "beige nude"),
        NamedColor("White", "#F7F7F3", "white"), NamedColor("Black", "#111111", "black"),
        NamedColor("Chocolate", "#5B342E", "chocolate brown"), NamedColor("Coral", "#E66C5C", "coral"),
        NamedColor("Orange", "#F07C33", "orange"), NamedColor("Lavender", "#A88AC7", "lavender"),
        NamedColor("Purple", "#6C4BA3", "purple"), NamedColor("Cobalt", "#2F5ED7", "cobalt blue"),
        NamedColor("Navy", "#1D2C4D", "navy"), NamedColor("Emerald", "#1F7A5B", "emerald green"),
        NamedColor("Silver", "#BFC3C7", "metallic silver"), NamedColor("Gold", "#C9A24E", "metallic gold")
    )
    val footwear = listOf(
        NamedColor("Black", "#111111", "black"), NamedColor("White", "#F5F5F2", "white"),
        NamedColor("Cream", "#EFE4CE", "cream"), NamedColor("Nude", "#D5AE91", "nude"),
        NamedColor("Beige", "#C7AD8A", "beige"), NamedColor("Tan", "#A86F45", "tan"),
        NamedColor("Cognac", "#8B4E2F", "cognac brown"), NamedColor("Chocolate", "#4B2B24", "chocolate brown"),
        NamedColor("Red", "#B51F32", "red"), NamedColor("Burgundy", "#681825", "burgundy"),
        NamedColor("Pink", "#DF6C9D", "pink"), NamedColor("Hot pink", "#E8328A", "hot pink"),
        NamedColor("Navy", "#172540", "navy"), NamedColor("Silver", "#B8BDC4", "silver"), NamedColor("Gold", "#C8A451", "gold")
    )
    private val all = (hosiery + nails + footwear).distinctBy { it.hex.uppercase() }
    fun describe(hex: String): String {
        val known = all.firstOrNull { it.hex.equals(hex, ignoreCase = true) }
        return if (known != null) "${known.promptName} (${known.hex})" else hex
    }
}

object SceneCatalog {
    fun categoryOf(scene: SceneType): SceneCategory = when (scene) {
        SceneType.STUDIO, SceneType.CYBERPUNK, SceneType.VAPORWAVE, SceneType.VOID, SceneType.CLOUD, SceneType.GOLD_ROOM, SceneType.ICE_CAVE -> SceneCategory.ABSTRACT
        SceneType.BEDROOM, SceneType.LIVING_ROOM, SceneType.KITCHEN, SceneType.BATHROOM, SceneType.CLOSET, SceneType.LAUNDRY, SceneType.HOME_OFFICE, SceneType.BALCONY -> SceneCategory.HOME
        SceneType.POOL, SceneType.BEACH, SceneType.YACHT, SceneType.SPA, SceneType.HOT_TUB, SceneType.GOLF_COURSE, SceneType.TENNIS_COURT, SceneType.WINERY, SceneType.HELIPAD, SceneType.CASINO, SceneType.OPERA -> SceneCategory.LUXURY
        SceneType.OFFICE, SceneType.SUBWAY, SceneType.ROOFTOP, SceneType.ALLEY, SceneType.STAIRS, SceneType.NIGHTCLUB, SceneType.ARCADE, SceneType.GAS_STATION, SceneType.PARKING_GARAGE, SceneType.ELEVATOR, SceneType.ESCALATOR -> SceneCategory.URBAN
        SceneType.NATURE, SceneType.GARDEN, SceneType.DESERT, SceneType.SNOW, SceneType.RIVER, SceneType.WATERFALL, SceneType.FIELD_FLOWERS, SceneType.MOUNTAIN_PEAK -> SceneCategory.NATURE
        SceneType.LIBRARY, SceneType.GYM, SceneType.CLASSROOM, SceneType.MUSEUM, SceneType.HOSPITAL, SceneType.BALLET, SceneType.SUPERMARKET, SceneType.CINEMA -> SceneCategory.PROFESSIONAL
        SceneType.PRIVATE_JET, SceneType.SPORTS_CAR, SceneType.VINTAGE_CAR, SceneType.LIMOUSINE, SceneType.TESLA, SceneType.YACHT_INTERIOR -> SceneCategory.VEHICLE
        SceneType.RED_WINDOW, SceneType.DUNGEON, SceneType.STRIP_STAGE, SceneType.LOVE_HOTEL, SceneType.MASSAGE_TABLE, SceneType.POLE_STAGE, SceneType.SHOWER_GLASS, SceneType.LOCKER_ROOM, SceneType.SHIBARI_ROOM, SceneType.GOLDEN_CAGE, SceneType.VELVET_SWING, SceneType.MIRROR_MAZE, SceneType.ROUND_BED, SceneType.LATEX_CHAMBER, SceneType.DARK_ALCOVE, SceneType.PEEP_BOOTH, SceneType.LEATHER_SOFA, SceneType.PRIVATE_SAUNA, SceneType.BED_RESTRAINTS, SceneType.XXX_NEON, SceneType.EROTIC_EXHIBITION -> SceneCategory.PRIVATE
    }

    fun scenesIn(category: SceneCategory): List<SceneType> = SceneType.entries.filter { categoryOf(it) == category }

    fun recommendedPoses(scene: SceneType): List<PoseType> = when (categoryOf(scene)) {
        SceneCategory.HOME -> listOf(PoseType.RECLINED, PoseType.CROSSED, PoseType.KNEELING, PoseType.WALL_LEGS, PoseType.STANDING)
        SceneCategory.LUXURY -> listOf(PoseType.RECLINED, PoseType.ARCHED, PoseType.DANGLED, PoseType.CROSSED)
        SceneCategory.URBAN -> listOf(PoseType.STANDING, PoseType.CROSSED, PoseType.SCRUNCHED, PoseType.POINTED)
        SceneCategory.NATURE -> listOf(PoseType.STANDING, PoseType.SCRUNCHED, PoseType.POINTED, PoseType.ARCHED, PoseType.KNEELING)
        SceneCategory.PROFESSIONAL -> listOf(PoseType.DESK_BOSS, PoseType.DESK_UNDER, PoseType.CROSSED, PoseType.STANDING)
        SceneCategory.VEHICLE -> listOf(PoseType.DRIVING, PoseType.ACTION_PEDAL, PoseType.DASHBOARD, PoseType.RECLINED, PoseType.CROSSED)
        SceneCategory.ABSTRACT -> listOf(PoseType.POINTED, PoseType.ARCHED, PoseType.STANDING, PoseType.DANGLED, PoseType.PINUP_KICK)
        SceneCategory.PRIVATE -> listOf(PoseType.KNEELING, PoseType.ARCHED, PoseType.PINUP_KNEEL, PoseType.PINUP_RECLINE, PoseType.CROSSED, PoseType.RECLINED)
    }

    fun recommendedFootwear(scene: SceneType): List<FootwearType> = when (categoryOf(scene)) {
        SceneCategory.HOME -> listOf(FootwearType.NONE, FootwearType.FLATS)
        SceneCategory.LUXURY -> listOf(FootwearType.SANDALS, FootwearType.STILETTO, FootwearType.MULES, FootwearType.NONE)
        SceneCategory.URBAN -> listOf(FootwearType.SNEAKERS, FootwearType.ANKLE_BOOTS, FootwearType.PLATFORM)
        SceneCategory.NATURE -> listOf(FootwearType.NONE, FootwearType.SANDALS, FootwearType.ANKLE_BOOTS)
        SceneCategory.PROFESSIONAL -> listOf(FootwearType.STILETTO, FootwearType.FLATS, FootwearType.ANKLE_BOOTS)
        SceneCategory.VEHICLE -> listOf(FootwearType.STILETTO, FootwearType.SNEAKERS, FootwearType.KNEE_BOOTS, FootwearType.MULES)
        SceneCategory.ABSTRACT -> listOf(FootwearType.STILETTO, FootwearType.NONE, FootwearType.PLATFORM)
        SceneCategory.PRIVATE -> listOf(FootwearType.PLATFORM, FootwearType.STILETTO, FootwearType.KNEE_BOOTS, FootwearType.NONE)
    }

    private fun s(vararg values: String): List<SceneSurface> = values.map { SceneSurface(it, it.lowercase()) }
    private val abstractGeneric = s("Seamless floor", "Mirror floor", "Velvet fabric", "Acrylic pedestal", "Glass floor", "Mist")
    private val homeGeneric = s("Soft rug", "Wooden floor", "Carpet", "Chair edge", "Couch edge", "Tile floor")
    private val luxuryGeneric = s("Marble floor", "Plush carpet", "Lounge chair", "Stone edge", "Wooden deck", "Velvet seat")
    private val urbanGeneric = s("Concrete", "Pavement", "Metal step", "Wet asphalt", "Bench edge", "Tiled floor")
    private val natureGeneric = s("Grass", "Moss", "Natural stone", "Wooden deck", "Earth", "Sand")
    private val professionalGeneric = s("Carpet", "Tile floor", "Desk edge", "Chair", "Rubber mat", "Wooden floor")
    private val vehicleGeneric = s("Passenger footwell", "Seat cushion", "Dashboard", "Pedals", "Door sill", "Center console")
    private val privateGeneric = s("Velvet sofa", "Satin bed", "Mirror floor", "Padded bench", "Soft rug", "Glass platform")

    fun surfacesFor(scene: SceneType): List<SceneSurface> = when (scene) {
        SceneType.STUDIO -> s("Seamless studio floor", "Mirror floor", "Velvet drape", "White cube", "Acrylic block", "Glass floor", "Low pedestal")
        SceneType.CYBERPUNK -> s("Wet asphalt", "Neon-lit pavement", "Metal grate", "Chrome tube", "Glowing floor grid")
        SceneType.VAPORWAVE -> s("Checkerboard floor", "Pink water", "Marble pedestal", "Floating platform", "Neon grid")
        SceneType.ICE_CAVE -> s("Blue ice", "Frozen lake", "Snow", "Ice ledge", "Crystal floor")
        SceneType.BEDROOM -> s("Soft sheets", "Satin sheets", "Duvet", "Bed edge", "Pillow pile", "Soft rug", "Carpet")
        SceneType.LIVING_ROOM -> s("Soft rug", "Couch cushion", "Leather sofa", "Coffee table edge", "Wooden floor", "Ottoman")
        SceneType.KITCHEN -> s("Tile floor", "Wooden floor", "Counter edge", "Bar stool", "Kitchen island", "Stone floor")
        SceneType.BATHROOM -> s("Wet tiles", "Bath mat", "Bathtub edge", "Shower floor", "Marble floor", "Folded towel")
        SceneType.CLOSET -> s("Soft carpet", "Dressing bench", "Wooden floor", "Shoe-box stack", "Fur rug", "Mirror platform")
        SceneType.LAUNDRY -> s("Cold tile floor", "Laundry basket", "Towel pile", "Washer edge", "Wooden bench")
        SceneType.HOME_OFFICE -> s("Under desk", "Desk edge", "Office chair", "Carpet", "Wooden floor")
        SceneType.BALCONY -> s("Concrete floor", "Wooden decking", "Lounge chair", "Metal railing edge", "Outdoor rug")
        SceneType.POOL -> s("Wet pool deck", "Pool edge", "Blue tiles", "Pool steps", "Lounger", "Shallow water")
        SceneType.BEACH -> s("Dry white sand", "Wet sand", "Shoreline foam", "Beach towel", "Driftwood", "Boardwalk")
        SceneType.YACHT -> s("Teak deck", "Sun lounger", "Glass table edge", "Yacht steps", "Railing edge", "White cushion")
        SceneType.SPA -> s("Massage table", "White towel", "Sauna bench", "Warm stone floor", "Spa lounger", "Wooden platform")
        SceneType.HOT_TUB -> s("Tub edge", "Underwater seat", "Wet deck", "Towel", "Pool steps", "Stone surround")
        SceneType.GOLF_COURSE -> s("Green grass", "Sand bunker", "Golf cart step", "Clubhouse deck", "Stone path")
        SceneType.TENNIS_COURT -> s("Clay court", "White line", "Court bench", "Hard court", "Net post")
        SceneType.WINERY -> s("Stone floor", "Oak barrel", "Wooden bench", "Vineyard grass", "Cellar step")
        SceneType.HELIPAD -> s("Helipad marking", "Asphalt", "Metal skid", "Concrete", "Roof edge")
        SceneType.CASINO -> s("Red carpet", "Roulette table edge", "Velvet seat", "Marble floor", "Casino stool")
        SceneType.OPERA -> s("Velvet seat", "Balcony edge", "Carpet", "Marble floor", "Private box sofa")
        SceneType.OFFICE -> s("Carpet", "Under desk", "Desk edge", "Office chair", "Wooden floor", "Glass desk")
        SceneType.SUBWAY -> s("Platform tiles", "Train seat", "Yellow safety line", "Metal step", "Station bench")
        SceneType.ROOFTOP -> s("Rooftop gravel", "Concrete", "Wet reflection", "Roof ledge", "Lounge chair", "Metal platform")
        SceneType.ALLEY -> s("Cracked concrete", "Wet asphalt", "Metal grate", "Brick ledge", "Cardboard", "Pavement")
        SceneType.STAIRS -> s("Concrete step", "Metal step", "Wooden step", "Stair landing", "Handrail edge")
        SceneType.NIGHTCLUB -> s("Club floor", "VIP sofa", "Speaker box", "Stage edge", "Velvet rope")
        SceneType.ARCADE -> s("Arcade floor", "Dance pad", "Cabinet edge", "Bench", "Neon platform")
        SceneType.GAS_STATION -> s("Concrete", "Pump island", "Curb", "Wet asphalt", "Car hood edge")
        SceneType.PARKING_GARAGE -> s("Concrete", "Parking line", "Car hood", "Low wall", "Metal barrier")
        SceneType.ELEVATOR -> s("Metal floor", "Mirror wall", "Handrail", "Door sill", "Elevator corner")
        SceneType.ESCALATOR -> s("Escalator step", "Landing plate", "Rubber handrail edge", "Glass side")
        SceneType.NATURE -> s("Mossy ground", "Forest floor", "Tree root", "Flat rock", "Ferns", "Wooden log")
        SceneType.GARDEN -> s("Green grass", "Stone path", "Picnic blanket", "Garden bench", "Flower bed edge", "Wooden deck")
        SceneType.DESERT -> s("Sand dune", "Cracked earth", "Flat rock", "Warm sand", "Desert blanket")
        SceneType.SNOW -> s("Fresh snow", "Wooden porch", "Ice patch", "Cabin rug", "Snow-covered step")
        SceneType.RIVER -> s("Smooth river stones", "River bank", "Shallow water", "Wooden dock", "Flat rock")
        SceneType.WATERFALL -> s("Wet rock", "Mossy stone", "Shallow water", "River pebbles", "Wooden platform")
        SceneType.FIELD_FLOWERS -> s("Wildflower grass", "Picnic blanket", "Stone", "Wooden bench", "Dirt path")
        SceneType.MOUNTAIN_PEAK -> s("Flat rock", "Alpine grass", "Snow patch", "Wooden platform", "Lookout bench")
        SceneType.LIBRARY -> s("Carpet", "Wooden floor", "Reading chair", "Desk edge", "Library steps")
        SceneType.GYM -> s("Yoga mat", "Rubber gym floor", "Workout bench", "Wooden studio floor", "Foam mat", "Locker bench")
        SceneType.CLASSROOM -> s("Tile floor", "Desk edge", "Chair", "Wooden floor", "Teacher platform")
        SceneType.MUSEUM -> s("Gallery floor", "Museum bench", "Marble floor", "Display plinth", "Stair")
        SceneType.HOSPITAL -> s("Hospital bed edge", "Vinyl floor", "Chair", "Blanket", "Foot stool")
        SceneType.BALLET -> s("Wooden dance floor", "Ballet barre", "Practice mat", "Mirror wall edge", "Studio bench")
        SceneType.SUPERMARKET -> s("Tile aisle", "Cart edge", "Shelf base", "Checkout floor", "Store bench")
        SceneType.CINEMA -> s("Cinema seat", "Carpeted aisle", "Seat armrest", "Front-row ledge", "Lobby floor")
        SceneType.PRIVATE_JET -> s("Cream carpet", "Leather jet seat", "Cabin floor", "Ottoman", "Window-side ledge")
        SceneType.SPORTS_CAR -> s("Passenger footwell", "Dashboard", "Leather seat", "Pedals", "Door sill", "Center console")
        SceneType.VINTAGE_CAR -> s("Passenger footwell", "Vintage leather seat", "Dashboard", "Chrome door sill", "Pedals", "Bench seat")
        SceneType.LIMOUSINE -> s("Leather rear seat", "Carpeted footwell", "Center console", "Door sill", "Rear lounge seat")
        SceneType.TESLA -> s("Passenger footwell", "White seat", "Dashboard", "Pedals", "Door sill", "Center console")
        SceneType.YACHT_INTERIOR -> s("Teak floor", "Cream sofa", "Cabin bed edge", "Carpet", "Leather seat")
        SceneType.RED_WINDOW -> s("Window platform", "Velvet stool", "Red carpet", "Glass floor", "Curtain edge")
        SceneType.DUNGEON -> s("Stone floor", "Padded bench", "Leather sofa", "Metal platform", "Dark rug")
        SceneType.STRIP_STAGE, SceneType.POLE_STAGE -> s("Stage floor", "Pole base", "Velvet lounge seat", "Mirror floor", "Neon platform")
        SceneType.LOVE_HOTEL, SceneType.ROUND_BED, SceneType.BED_RESTRAINTS -> s("Satin bed", "Bed edge", "Soft rug", "Velvet bench", "Mirror platform")
        SceneType.MASSAGE_TABLE -> s("Massage table", "White towel", "Wooden floor", "Padded stool", "Warm stone floor")
        SceneType.SHOWER_GLASS -> s("Wet tiles", "Shower floor", "Glass ledge", "Bath mat", "Marble floor")
        SceneType.LOCKER_ROOM -> s("Locker bench", "Tile floor", "Rubber mat", "Shower step", "Wooden bench")
        SceneType.PRIVATE_SAUNA -> s("Sauna bench", "Wooden floor", "Towel", "Stone step", "Warm platform")
        else -> when (categoryOf(scene)) {
            SceneCategory.ABSTRACT -> abstractGeneric
            SceneCategory.HOME -> homeGeneric
            SceneCategory.LUXURY -> luxuryGeneric
            SceneCategory.URBAN -> urbanGeneric
            SceneCategory.NATURE -> natureGeneric
            SceneCategory.PROFESSIONAL -> professionalGeneric
            SceneCategory.VEHICLE -> vehicleGeneric
            SceneCategory.PRIVATE -> privateGeneric
        }
    }

    fun defaultSurface(scene: SceneType): String = surfacesFor(scene).firstOrNull()?.prompt ?: "natural surface"
    private val allKnownSurfacePrompts: Set<String> by lazy { SceneType.entries.flatMap { surfacesFor(it) }.map { it.prompt.lowercase() }.toSet() }
    fun isSurfaceCompatible(scene: SceneType, surface: String): Boolean {
        val normalized = surface.trim().lowercase()
        if (normalized.isBlank()) return false
        if (normalized !in allKnownSurfacePrompts) return true
        return surfacesFor(scene).any { it.prompt.equals(normalized, ignoreCase = true) }
    }
}
