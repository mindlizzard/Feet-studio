package com.mindlizzard.feetstudio.domain

data class NamedColor(
    val label: String,
    val hex: String,
    val promptName: String = label
)

enum class SceneCategory(val label: String) {
    ABSTRACT("Abstract"),
    HOME("Home"),
    LUXURY("Luxury"),
    URBAN("Urban"),
    NATURE("Nature"),
    PROFESSIONAL("Professional"),
    VEHICLE("Vehicle")
}

data class SceneSurface(
    val label: String,
    val prompt: String = label.lowercase()
)

object ColorCatalog {
    val hosiery = listOf(
        NamedColor("Black", "#111111", "black"),
        NamedColor("Jet black", "#000000", "jet black"),
        NamedColor("Charcoal", "#343434", "charcoal"),
        NamedColor("Graphite", "#565656", "graphite grey"),
        NamedColor("Smoke", "#767676", "smoke grey"),
        NamedColor("Grey", "#9A9A9A", "medium grey"),
        NamedColor("White", "#F4F4F2", "soft white"),
        NamedColor("Ivory", "#F1E7D0", "ivory"),
        NamedColor("Nude light", "#E9C8AF", "light nude"),
        NamedColor("Nude", "#D4A886", "natural nude"),
        NamedColor("Tan", "#B9825B", "tan"),
        NamedColor("Bronze", "#8C5B3D", "bronze brown"),
        NamedColor("Chocolate", "#4D2F24", "chocolate brown"),
        NamedColor("Navy", "#18233F", "navy blue"),
        NamedColor("Burgundy", "#6A1E2D", "burgundy"),
        NamedColor("Red", "#B51F32", "deep red")
    )

    val nails = listOf(
        NamedColor("Classic red", "#D7263D", "classic red"),
        NamedColor("Cherry", "#B11226", "cherry red"),
        NamedColor("Burgundy", "#681825", "burgundy"),
        NamedColor("Wine", "#7B1E3A", "wine red"),
        NamedColor("Pink", "#E85D8A", "pink"),
        NamedColor("Soft pink", "#F3B6C9", "soft pink"),
        NamedColor("Nude pink", "#D9A4A7", "nude pink"),
        NamedColor("Beige nude", "#D8B39A", "beige nude"),
        NamedColor("White", "#F7F7F3", "white"),
        NamedColor("Black", "#111111", "black"),
        NamedColor("Chocolate", "#5B342E", "chocolate brown"),
        NamedColor("Coral", "#E66C5C", "coral"),
        NamedColor("Orange", "#F07C33", "orange"),
        NamedColor("Lavender", "#A88AC7", "lavender"),
        NamedColor("Purple", "#6C4BA3", "purple"),
        NamedColor("Cobalt", "#2F5ED7", "cobalt blue"),
        NamedColor("Navy", "#1D2C4D", "navy"),
        NamedColor("Emerald", "#1F7A5B", "emerald green"),
        NamedColor("Silver", "#BFC3C7", "metallic silver"),
        NamedColor("Gold", "#C9A24E", "metallic gold")
    )

    val footwear = listOf(
        NamedColor("Black", "#111111", "black"),
        NamedColor("White", "#F5F5F2", "white"),
        NamedColor("Cream", "#EFE4CE", "cream"),
        NamedColor("Nude", "#D5AE91", "nude"),
        NamedColor("Beige", "#C7AD8A", "beige"),
        NamedColor("Tan", "#A86F45", "tan"),
        NamedColor("Cognac", "#8B4E2F", "cognac brown"),
        NamedColor("Chocolate", "#4B2B24", "chocolate brown"),
        NamedColor("Red", "#B51F32", "red"),
        NamedColor("Burgundy", "#681825", "burgundy"),
        NamedColor("Pink", "#DF6C9D", "pink"),
        NamedColor("Hot pink", "#E8328A", "hot pink"),
        NamedColor("Navy", "#172540", "navy"),
        NamedColor("Silver", "#B8BDC4", "silver"),
        NamedColor("Gold", "#C8A451", "gold")
    )

    private val all = (hosiery + nails + footwear).distinctBy { it.hex.uppercase() }

    fun describe(hex: String): String {
        val known = all.firstOrNull { it.hex.equals(hex, ignoreCase = true) }
        return if (known != null) "${known.promptName} (${known.hex})" else hex
    }
}

object SceneCatalog {
    fun categoryOf(scene: SceneType): SceneCategory = when (scene) {
        SceneType.STUDIO, SceneType.CYBERPUNK, SceneType.VAPORWAVE, SceneType.VOID,
        SceneType.CLOUD, SceneType.GOLD_ROOM, SceneType.ICE_CAVE -> SceneCategory.ABSTRACT

        SceneType.BEDROOM, SceneType.LIVING_ROOM, SceneType.KITCHEN, SceneType.BATHROOM,
        SceneType.CLOSET, SceneType.LAUNDRY, SceneType.HOME_OFFICE, SceneType.BALCONY -> SceneCategory.HOME

        SceneType.POOL, SceneType.BEACH, SceneType.YACHT, SceneType.SPA, SceneType.HOT_TUB,
        SceneType.GOLF_COURSE, SceneType.TENNIS_COURT, SceneType.WINERY, SceneType.HELIPAD,
        SceneType.CASINO, SceneType.OPERA -> SceneCategory.LUXURY

        SceneType.OFFICE, SceneType.SUBWAY, SceneType.ROOFTOP, SceneType.ALLEY, SceneType.STAIRS,
        SceneType.NIGHTCLUB, SceneType.ARCADE, SceneType.GAS_STATION, SceneType.PARKING_GARAGE,
        SceneType.ELEVATOR, SceneType.ESCALATOR -> SceneCategory.URBAN

        SceneType.NATURE, SceneType.GARDEN, SceneType.DESERT, SceneType.SNOW, SceneType.RIVER,
        SceneType.WATERFALL, SceneType.FIELD_FLOWERS, SceneType.MOUNTAIN_PEAK -> SceneCategory.NATURE

        SceneType.LIBRARY, SceneType.GYM, SceneType.CLASSROOM, SceneType.MUSEUM,
        SceneType.HOSPITAL, SceneType.BALLET, SceneType.SUPERMARKET, SceneType.CINEMA -> SceneCategory.PROFESSIONAL

        SceneType.PRIVATE_JET, SceneType.SPORTS_CAR, SceneType.VINTAGE_CAR,
        SceneType.LIMOUSINE, SceneType.TESLA, SceneType.YACHT_INTERIOR -> SceneCategory.VEHICLE
    }

    fun scenesIn(category: SceneCategory): List<SceneType> =
        SceneType.entries.filter { categoryOf(it) == category }

    private fun s(vararg values: String): List<SceneSurface> =
        values.map { SceneSurface(label = it, prompt = it.lowercase()) }

    private val homeGeneric = s("Soft rug", "Wooden floor", "Carpet", "Chair edge", "Couch edge", "Tile floor")
    private val luxuryGeneric = s("Marble floor", "Plush carpet", "Lounge chair", "Stone edge", "Wooden deck", "Velvet seat")
    private val urbanGeneric = s("Concrete", "Pavement", "Metal step", "Wet asphalt", "Bench edge", "Tiled floor")
    private val natureGeneric = s("Grass", "Moss", "Natural stone", "Wooden deck", "Earth", "Sand")
    private val professionalGeneric = s("Carpet", "Tile floor", "Desk edge", "Chair", "Rubber mat", "Wooden floor")
    private val abstractGeneric = s("Seamless floor", "Mirror floor", "Velvet fabric", "Acrylic pedestal", "Glass floor", "Mist")
    private val vehicleGeneric = s("Passenger footwell", "Seat cushion", "Dashboard", "Pedals", "Door sill", "Center console")

    fun surfacesFor(scene: SceneType): List<SceneSurface> = when (scene) {
        SceneType.STUDIO -> s("Seamless studio floor", "Mirror floor", "Velvet drape", "White cube", "Acrylic block", "Glass floor", "Low pedestal")
        SceneType.BEDROOM -> s("Soft sheets", "Satin sheets", "Duvet", "Bed edge", "Pillow pile", "Soft rug", "Carpet")
        SceneType.LIVING_ROOM -> s("Soft rug", "Couch cushion", "Leather sofa", "Coffee table edge", "Wooden floor", "Ottoman")
        SceneType.KITCHEN -> s("Tile floor", "Wooden floor", "Counter edge", "Bar stool", "Kitchen island", "Stone floor")
        SceneType.BATHROOM -> s("Wet tiles", "Bath mat", "Bathtub edge", "Shower floor", "Marble floor", "Folded towel")
        SceneType.CLOSET -> s("Soft carpet", "Dressing bench", "Wooden floor", "Shoe-box stack", "Fur rug", "Mirror platform")
        SceneType.BALCONY -> s("Concrete floor", "Wooden decking", "Lounge chair", "Metal railing edge", "Outdoor rug")
        SceneType.POOL -> s("Wet pool deck", "Pool edge", "Blue tiles", "Pool steps", "Lounger", "Shallow water")
        SceneType.BEACH -> s("Dry white sand", "Wet sand", "Shoreline foam", "Beach towel", "Driftwood", "Boardwalk")
        SceneType.YACHT -> s("Teak deck", "Sun lounger", "Glass table edge", "Yacht steps", "Railing edge", "White cushion")
        SceneType.SPA -> s("Massage table", "White towel", "Sauna bench", "Warm stone floor", "Spa lounger", "Wooden platform")
        SceneType.HOT_TUB -> s("Tub edge", "Underwater seat", "Wet deck", "Towel", "Pool steps", "Stone surround")
        SceneType.OFFICE -> s("Carpet", "Under desk", "Desk edge", "Office chair", "Wooden floor", "Glass desk")
        SceneType.SUBWAY -> s("Platform tiles", "Train seat", "Yellow safety line", "Metal step", "Station bench")
        SceneType.ROOFTOP -> s("Rooftop gravel", "Concrete", "Wet reflection", "Roof ledge", "Lounge chair", "Metal platform")
        SceneType.ALLEY -> s("Cracked concrete", "Wet asphalt", "Metal grate", "Brick ledge", "Cardboard", "Pavement")
        SceneType.STAIRS -> s("Concrete step", "Metal step", "Wooden step", "Stair landing", "Handrail edge")
        SceneType.GYM -> s("Yoga mat", "Rubber gym floor", "Workout bench", "Wooden studio floor", "Foam mat", "Locker bench")
        SceneType.BALLET -> s("Wooden dance floor", "Ballet barre", "Practice mat", "Mirror wall edge", "Studio bench")
        SceneType.NATURE -> s("Mossy ground", "Forest floor", "Tree root", "Flat rock", "Ferns", "Wooden log")
        SceneType.GARDEN -> s("Green grass", "Stone path", "Picnic blanket", "Garden bench", "Flower bed edge", "Wooden deck")
        SceneType.DESERT -> s("Sand dune", "Cracked earth", "Flat rock", "Warm sand", "Desert blanket")
        SceneType.SNOW -> s("Fresh snow", "Wooden porch", "Ice patch", "Cabin rug", "Snow-covered step")
        SceneType.RIVER -> s("Smooth river stones", "River bank", "Shallow water", "Wooden dock", "Flat rock")
        SceneType.WATERFALL -> s("Wet rock", "Mossy stone", "Shallow water", "River pebbles", "Wooden platform")
        SceneType.SPORTS_CAR -> s("Passenger footwell", "Dashboard", "Leather seat", "Pedals", "Door sill", "Center console")
        SceneType.VINTAGE_CAR -> s("Passenger footwell", "Vintage leather seat", "Dashboard", "Chrome door sill", "Pedals", "Bench seat")
        SceneType.LIMOUSINE -> s("Leather rear seat", "Carpeted footwell", "Center console", "Door sill", "Rear lounge seat")
        SceneType.TESLA -> s("Passenger footwell", "White seat", "Dashboard", "Pedals", "Door sill", "Center console")
        SceneType.PRIVATE_JET -> s("Cream carpet", "Leather jet seat", "Cabin floor", "Ottoman", "Window-side ledge")
        SceneType.YACHT_INTERIOR -> s("Teak floor", "Cream sofa", "Cabin bed edge", "Carpet", "Leather seat")
        else -> when (categoryOf(scene)) {
            SceneCategory.ABSTRACT -> abstractGeneric
            SceneCategory.HOME -> homeGeneric
            SceneCategory.LUXURY -> luxuryGeneric
            SceneCategory.URBAN -> urbanGeneric
            SceneCategory.NATURE -> natureGeneric
            SceneCategory.PROFESSIONAL -> professionalGeneric
            SceneCategory.VEHICLE -> vehicleGeneric
        }
    }

    fun defaultSurface(scene: SceneType): String =
        surfacesFor(scene).firstOrNull()?.prompt ?: "natural surface"

    private val allKnownSurfacePrompts: Set<String> by lazy {
        SceneType.entries.flatMap { surfacesFor(it) }.map { it.prompt.lowercase() }.toSet()
    }

    fun isSurfaceCompatible(scene: SceneType, surface: String): Boolean {
        val normalized = surface.trim().lowercase()
        if (normalized.isBlank()) return false
        if (normalized !in allKnownSurfacePrompts) return true
        return surfacesFor(scene).any { it.prompt.equals(normalized, ignoreCase = true) }
    }
}
