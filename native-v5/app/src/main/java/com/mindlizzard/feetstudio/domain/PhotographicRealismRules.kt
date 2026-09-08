package com.mindlizzard.feetstudio.domain

object PhotographicRealismRules {

    fun prompt(
        state: DesignState,
        settings: StudioSettings,
        facts: DerivedFacts
    ): String {
        val quality = when (settings.qualityProfile) {
            QualityProfile.STANDARD ->
                "clean natural photography with restrained processing"
            QualityProfile.AURA ->
                "premium editorial photography with refined material detail and natural tonal depth"
            QualityProfile.ULTRA ->
                "master-quality photographic capture with maximum believable texture and tonal separation"
        }

        val hosieryResponse = when {
            !facts.wearingHosiery ->
                "No hosiery: preserve normal skin reflectance and avoid invented nylon sheen."
            facts.fishnet ->
                "Fishnet: resolve real thread thickness, open cells and tiny contact shadows without printing the mesh onto skin."
            facts.opaqueHosiery ->
                "Opaque hosiery: emphasize fabric surface, folds and directional light response while keeping hidden skin detail suppressed."
            else ->
                "Sheer hosiery: keep denier-appropriate transparency, subtle fiber presence and gentle directional nylon sheen; never turn it into latex, oil or plastic."
        }

        val footwearResponse = if (facts.wearingShoes) {
            "Footwear: preserve real upper/sole construction, edge stitching, material roughness, pressure points and contact shadows."
        } else {
            "No footwear: do not invent soles, straps, shoe shadows or accessory edges."
        }

        val focus = when {
            state.cameraFocusY >= 79 -> "feet / footwear"
            state.cameraFocusY >= 53 -> "legs / knees"
            state.cameraFocusY >= 25 -> "torso / hips"
            else -> "head / upper body"
        }

        return """
            PHOTOGRAPHIC REALISM CORE:
            Target $quality.

            EXPOSURE / TONALITY:
            Use natural exposure with smooth highlight roll-off and readable shadow detail.
            Preserve realistic local contrast instead of global HDR or crushed blacks.
            White balance must stay coherent with ${state.lighting.label} and ${state.filmStock.label}.
            Avoid clipped skin highlights, grey lifted blacks, oversaturated color and artificial glow.

            MATERIAL RESPONSE:
            Skin remains skin: subtle pores, fine tonal variation and soft subsurface character,
            never waxy, airbrushed, porcelain, oily or plastic.
            $hosieryResponse
            $footwearResponse
            Every material gets its own roughness, highlight width and edge behavior.
            Do not apply one identical glossy response to skin, nylon and footwear.

            MICRODETAIL DISCIPLINE:
            Resolve true texture only where the optics can actually support it.
            Primary focus plane: $focus.
            Fine detail should be strongest near that plane and fall off naturally with depth.
            Avoid repeated AI texture, checkerboard fabric, fake micro-scratches, crunchy clarity,
            sharpening halos, painterly denoise and random high-frequency noise.

            LENS / DEPTH REALISM:
            Respect ${state.lens.label} perspective and depth behavior.
            Background separation comes from optics and distance, not cut-out blur around the subject.
            Keep transition zones around legs, feet, hosiery and shoes clean and physically believable.

            LIGHT / CONTACT:
            Light direction, shadow softness and material highlights must agree with one another.
            Feet and footwear must cast contact shadows exactly where they touch the surface.
            Preserve tiny occlusion shadows at toe gaps, hosiery folds, ankle creases and shoe openings
            only when those details are actually visible.

            FINAL QUALITY RULE:
            Prefer coherent photographic realism over decorative detail.
            If a texture cue conflicts with anatomy, material physics, focus depth or the reference roles,
            keep the physically coherent interpretation and discard the decorative artifact.
        """.trimIndent()
    }
}
