package com.mindlizzard.feetstudio.domain

object RenderFidelityRules {

    fun referencePolicy(refs: List<String>): String {
        if (refs.isEmpty()) {
            return """
                REFERENCE FIDELITY:
                No reference images supplied. Follow the resolved design state only.
            """.trimIndent()
        }

        val rules = refs.map { item ->
            val role = item.substringBefore("=").trim()
            val strength = item.substringAfter("=", "guided").trim()

            val roleText = when (role) {
                "foot_shape" ->
                    "copy foot proportions, toe-order silhouette, arch and heel/forefoot geometry only"
                "skin" ->
                    "copy visible skin tone, undertone and texture character only"
                "nails" ->
                    "copy visible nail shape, polish placement, color and nail-art structure only"
                "hosiery" ->
                    "copy garment weave, denier/mesh character, color, seams, transparency and material behavior only"
                "footwear" ->
                    "copy shoe silhouette, construction, straps, heel/sole and material details only"
                "pose" ->
                    "copy joint articulation, limb ordering and weight distribution only"
                "camera" ->
                    "copy framing, viewpoint, focal-length feel and perspective only"
                "scene" ->
                    "copy environment, surface and spatial arrangement only"
                "style" ->
                    "copy photographic finish, grading, contrast and editorial feel only"
                else ->
                    "use only the visual property assigned to this reference"
            }

            val strengthText = when (strength) {
                "exact" ->
                    "EXACT: preserve this role as closely as physically possible; change it only to repair impossible anatomy or occlusion"
                "strong" ->
                    "STRONG: keep a close visual match while adapting naturally to the resolved pose and camera"
                "guided" ->
                    "GUIDED: follow its structure and character without forcing a literal copy"
                "inspiration" ->
                    "INSPIRATION: use only as a loose cue; never override anatomy or higher-priority references"
                else ->
                    "GUIDED: use as a role-limited structural cue"
            }

            "- $role=$strength: $strengthText; $roleText."
        }

        return """
            REFERENCE FIDELITY CONTRACT:
            References are role-isolated. Never let a STYLE reference silently change pose,
            footwear or anatomy. Never let a POSE reference replace hosiery, footwear or scene.
            Never let a CAMERA reference alter body shape. Never import unrelated details from
            a reference just because they are visually prominent.
            ${rules.joinToString("\n")}
        """.trimIndent()
    }

    fun footTopology(state: DesignState): String =
        """
            FOOT TOPOLOGY CHECK:
            Each visible foot is one continuous volume from ankle mortise -> heel -> arch ->
            metatarsal forefoot -> five toes. The big toe and four lesser toes originate from
            one believable forefoot arc; no toe may originate from the sole, side wall or another toe.
            Preserve left/right handedness, natural toe-length progression and believable soft-tissue
            compression. Shoe size ${state.shoeSize} and ${state.footShape.label} are proportion cues,
            not permission to stretch or shrink individual toes unnaturally.
        """.trimIndent()

    fun materialLayering(
        state: DesignState,
        facts: DerivedFacts
    ): String {
        val hosieryLayer = when {
            !facts.wearingHosiery ->
                "No hosiery layer exists. Do not invent nylon sheen, seams or mesh."
            facts.fishnet ->
                "Fishnet is a real open 3D thread network. Open cells reveal the layer below; threads cast tiny contact shadows and deform around curvature."
            facts.opaqueHosiery ->
                "Opaque hosiery is a continuous fabric shell over skin; hidden pores and nail polish must not leak through."
            else ->
                "Sheer hosiery is a continuous fiber layer over skin; transparency changes gradually with stretch and viewing angle, never as random holes."
        }

        val shoeLayer = when {
            !facts.wearingShoes ->
                "No footwear layer exists."
            facts.closedShoeWorn ->
                "Closed footwear sits outside skin/hosiery and fully occludes toes inside the shoe volume."
            else ->
                "Open or partially removed footwear sits outside skin/hosiery at real pressure/contact points; straps pass over the material instead of through it."
        }

        return """
            MATERIAL / OCCLUSION ORDER:
            Treat visible materials as physical layers with a stable depth order.
            $hosieryLayer
            $shoeLayer
            At every overlap use one clear front/back order. No fabric through soles, no shoe
            geometry through toes, no nail polish floating above fabric, and no contact shadow
            detached from the actual contact edge.
            Hosiery tension ${state.hosieryTension}/100 and wrinkles ${state.hosieryWrinkles}/100
            affect local fiber spacing and folds, not the underlying skeleton.
        """.trimIndent()
    }

    fun opticalCapture(
        state: DesignState,
        settings: StudioSettings
    ): String {
        val qualityText = when (settings.qualityProfile) {
            QualityProfile.STANDARD ->
                "natural consumer-camera sharpness with restrained microcontrast"
            QualityProfile.AURA ->
                "premium full-frame editorial capture with crisp local microdetail and smooth optical roll-off"
            QualityProfile.ULTRA ->
                "master-quality capture with maximum recoverable real texture for refinement"
        }

        return """
            OPTICAL CAPTURE DISCIPLINE:
            $qualityText.
            Lens intent: ${state.lens.label}; depth of field ${state.depthOfField}/100.
            Resolve texture at the focus plane first. Let sharpness fall off optically with depth
            instead of applying global blur or global sharpening.
            Keep skin pores, hosiery fibers, shoe seams and nail edges locally distinct where in focus.
            Preserve natural sensor noise / fine grain if appropriate to ${state.filmStock.label}.
            No edge halos, ringing, fake clarity, brittle HDR, smeared denoise, beauty-filter skin,
            watercolor microtexture or uniformly sharp background.
        """.trimIndent()
    }
}
