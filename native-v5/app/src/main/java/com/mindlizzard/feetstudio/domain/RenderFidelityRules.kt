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
                "foot_shape" -> "copy foot proportions, toe-order silhouette, arch and heel/forefoot geometry only"
                "skin" -> "copy visible skin tone, undertone and texture character only"
                "nails" -> "copy visible nail shape, polish placement, color and nail-art structure only"
                "hosiery" -> "copy garment construction, denier appearance, color, seams, transparency and material behavior only"
                "footwear" -> "copy shoe silhouette, construction, straps, heel/sole and material details only"
                "pose" -> "copy joint articulation, limb ordering and weight distribution only"
                "camera" -> "copy framing, viewpoint, focal-length feel and perspective only"
                "scene" -> "copy environment, surface and spatial arrangement only"
                "style" -> "copy photographic finish, grading, contrast and editorial feel only"
                else -> "use only the visual property assigned to this reference"
            }

            val strengthText = when (strength) {
                "exact" -> "EXACT: preserve this role as closely as physically possible; change it only to repair impossible anatomy or occlusion"
                "strong" -> "STRONG: keep a close visual match while adapting naturally to the resolved pose and camera"
                "guided" -> "GUIDED: follow its structure and character without forcing a literal copy"
                "inspiration" -> "INSPIRATION: use only as a loose cue; never override anatomy or higher-priority references"
                else -> "GUIDED: use as a role-limited structural cue"
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

    fun denierSemantics(state: DesignState, facts: DerivedFacts): String {
        if (!facts.wearingHosiery || facts.fishnet) return "Denier semantics are not used for this hosiery type."
        return when (state.denier.name) {
            "D5" -> "5 denier: ultra-sheer and almost invisible at normal viewing distance; the garment is perceived mainly through a faint tint, subtle nylon sheen and extremely fine structure."
            "D15" -> "15 denier: clear sheer pantyhose look; skin and toenail color remain visible through the fabric, with a delicate nylon veil and fine smooth surface."
            "D30" -> "30 denier: semi-sheer coverage with more even tint and softer skin visibility; the textile presence is obvious without becoming opaque."
            "D60" -> "60 denier: near-opaque coverage; emphasize fabric texture and folds while strongly suppressing visible skin pores and nail polish beneath."
            "D100" -> "100 denier: fully opaque fabric; no visible pores or nail polish through the material."
            else -> "Use the selected denier as a real coverage cue: lower denier stays sheer and subtle, higher denier becomes visibly more covering and less transparent."
        }
    }

    fun hosieryConstruction(state: DesignState): String = when (state.hosieryType.name) {
        "PANTYHOSE" -> "Construction: one continuous pantyhose garment worn from waist through hips, both legs, both heels and both toes with uninterrupted coverage."
        "THIGH_HIGH" -> "Construction: two separate thigh-high stockings, each ending at the upper thigh with a visible top band / welt; they do not continue through the pelvis."
        "STOCKINGS" -> "Construction: separate stockings with distinct top edges; treat them as individual leg garments rather than a full pantyhose piece."
        "KNEE_HIGH" -> "Construction: each sock stops below the knee with a clear cuff / top edge; the foot and ankle remain covered by the same continuous knit."
        "ANKLE_SOCKS" -> "Construction: short ankle socks ending near or just above the ankle with a visible opening edge."
        "TOE_SOCKS" -> "Construction: toe socks with individually separated toe sleeves; each toe sits in its own knit channel."
        "VINTAGE_FF" -> "Construction: vintage fully fashioned stockings with a straight rear seam and authentic fully-fashioned shaping along the leg."
        "VINTAGE_RHT" -> "Construction: vintage reinforced heel-and-toe stockings with visibly denser heel and toe reinforcement zones."
        else -> "Construction must match the selected hosiery type exactly, with real garment edges, openings and coverage boundaries."
    }

    fun finishSemantics(state: DesignState): String = when (state.hosieryFinish.name.uppercase()) {
        "GLOSSY" -> "Finish: directional nylon specular sheen with narrow soft highlights that follow the leg curvature; never wet, oily, latex-like, metallic or plastic."
        "MATTE" -> "Finish: restrained diffuse surface with minimal specular sparkle and no plastic glare."
        else -> "Finish: natural nylon response between matte and glossy, with subtle believable sheen only on curved tensioned areas."
    }

    fun tensionSemantics(state: DesignState): String {
        fun band(value: Int): String = when {
            value <= 25 -> "loose"
            value <= 55 -> "natural"
            value <= 80 -> "taut"
            else -> "strongly stretched"
        }
        return """
            Stretch semantics:
            tension ${state.hosieryTension}/100 = ${band(state.hosieryTension)};
            compression ${state.hosieryCompression}/100 = ${band(state.hosieryCompression)};
            wrinkles ${state.hosieryWrinkles}/100 = ${band(state.hosieryWrinkles)}.
            Higher tension means smoother fabric, subtler knit visibility and slightly increased transparency over protruding areas such as toes, heel curve and knee.
            Compression affects gentle contouring around ankle, arch and calf without changing the underlying skeleton.
            Wrinkles create localized folds only where fabric would naturally gather, such as behind the knee, around the ankle or near shoe openings.
        """.trimIndent()
    }

    fun materialLayering(state: DesignState, facts: DerivedFacts): String {
        val hosieryLayer = when {
            !facts.wearingHosiery -> "No hosiery layer exists. Do not invent nylon sheen, seams or mesh."
            facts.fishnet -> "Fishnet is a real open 3D thread network. Open cells reveal the layer below; threads cast tiny contact shadows and deform around curvature."
            facts.opaqueHosiery -> "Opaque hosiery is a continuous fabric shell over skin; hidden pores and nail polish must not leak through."
            else -> "Sheer hosiery is a continuous fiber layer over skin; transparency changes gradually with stretch and viewing angle, never as random holes."
        }
        val shoeLayer = when {
            !facts.wearingShoes -> "No footwear layer exists."
            facts.closedShoeWorn -> "Closed footwear sits outside skin/hosiery and fully occludes toes inside the shoe volume."
            else -> "Open or partially removed footwear sits outside skin/hosiery at real pressure/contact points; straps pass over the material instead of through it."
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

    fun opticalCapture(state: DesignState, settings: StudioSettings): String {
        val qualityText = when (settings.qualityProfile) {
            QualityProfile.STANDARD -> "natural consumer-camera sharpness with restrained microcontrast"
            QualityProfile.AURA -> "premium full-frame editorial capture with crisp local microdetail and smooth optical roll-off"
            QualityProfile.ULTRA -> "master-quality capture with maximum recoverable real texture for refinement"
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

    fun hosieryMasterBlock(state: DesignState, facts: DerivedFacts): String {
        if (!facts.wearingHosiery) return "No hosiery. Bare skin is visible where footwear does not cover the feet."
        if (facts.fishnet) {
            return """
                Hosiery: ${state.hosieryType.label}, color ${ColorCatalog.describe(state.hosieryColor)},
                pattern ${state.hosieryPattern.label}, ${state.meshSize.name.lowercase()} mesh,
                thread thickness ${state.meshThickness}/100.

                ${hosieryConstruction(state)}
                Fishnet physics: a real open thread network, never a printed pattern.
                Open cells remain physically open across toes, arch, heel and ankle.
                Threads wrap around curvature, cast tiny contact shadows and keep continuous line direction.
                ${tensionSemantics(state)}
                ${finishSemantics(state)}
                No smeared mesh, no painted-on lattice and no random broken cells.
            """.trimIndent()
        }
        return """
            Hosiery: ${state.hosieryType.label}, ${state.denier.label},
            pattern ${state.hosieryPattern.label}, color ${ColorCatalog.describe(state.hosieryColor)}.

            ${hosieryConstruction(state)}
            ${denierSemantics(state, facts)}
            ${tensionSemantics(state)}
            ${finishSemantics(state)}

            Fabric follows the exact 3D surface of thighs, knees, calves, ankles, heels, arches and toes.
            Use real knit continuity and natural contact shadows. Do not paint hosiery directly onto skin.
            ${if (facts.opaqueHosiery) "Because this material is opaque, skin pores, veins and toenail polish must stay hidden beneath the fabric." else "Because this material is sheer, visible skin and nail color are optically filtered through the knit instead of being sharply exposed."}
            ${if (state.wornKnit) "Include subtle believable wear while keeping the garment intact." else "Keep the garment clean and intact."}
        """.trimIndent()
    }
}
