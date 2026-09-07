package com.mindlizzard.feetstudio.domain

object RenderEngine {
    fun buildContract(workspace: WorkspaceState, references: List<ReferenceAsset>): RenderContract {
        val requested = workspace.design
        val normalized = normalize(requested)
        val (effective, decisions) = resolve(normalized, workspace.settings)
        val facts = derive(effective)
        val manifest = references.filter { it.enabled }.sortedBy { it.strength.ordinal }.take(5)
            .map { "${it.role.name.lowercase()}=${it.strength.name.lowercase()}" }
        val prompt = compilePrompt(effective, workspace.settings, facts, decisions, manifest)
        val model = if (workspace.settings.renderMode == RenderMode.PRO) "gemini-3-pro-image" else "gemini-3.1-flash-image"
        val imageSize = when (workspace.settings.resolution) { Resolution.K1 -> "1K"; Resolution.K2 -> "2K"; Resolution.K4 -> "4K" }
        return RenderContract(requested, effective, workspace.settings, facts, decisions, manifest, prompt, model, imageSize, workspace.settings.aspectRatio.apiValue)
    }

    private fun normalize(state: DesignState): DesignState {
        val colorRegex = Regex("^#[0-9A-Fa-f]{6}$")
        return state.copy(
            modelAge = state.modelAge.coerceIn(18, 80), shoeSize = state.shoeSize.coerceIn(35f, 45f),
            footWidth = state.footWidth.coerceIn(0, 100), heelWidth = state.heelWidth.coerceIn(0, 100),
            instep = state.instep.coerceIn(0, 100), toeSpread = state.toeSpread.coerceIn(0, 100),
            toeLength = state.toeLength.coerceIn(0, 100), footAsymmetry = state.footAsymmetry.coerceIn(0, 100),
            skinPores = state.skinPores.coerceIn(0, 100), skinVeins = state.skinVeins.coerceIn(0, 100),
            skinDryness = state.skinDryness.coerceIn(0, 100), skinRedness = state.skinRedness.coerceIn(0, 100),
            skinMoisture = state.skinMoisture.coerceIn(0, 100), hosieryTension = state.hosieryTension.coerceIn(0, 100),
            hosieryCompression = state.hosieryCompression.coerceIn(0, 100), hosieryWrinkles = state.hosieryWrinkles.coerceIn(0, 100),
            meshThickness = state.meshThickness.coerceIn(0, 100), cameraDistance = state.cameraDistance.coerceIn(0, 100),
            cameraHeight = state.cameraHeight.coerceIn(0, 100), cameraTilt = state.cameraTilt.coerceIn(-45, 45),
            cameraRoll = state.cameraRoll.coerceIn(-45, 45), depthOfField = state.depthOfField.coerceIn(0, 100),
            lightSoftness = state.lightSoftness.coerceIn(0, 100), lightIntensity = state.lightIntensity.coerceIn(0, 100),
            lightTemperature = state.lightTemperature.coerceIn(0, 100), lightContrast = state.lightContrast.coerceIn(0, 100),
            nailColor = if (colorRegex.matches(state.nailColor)) state.nailColor else "#D7263D",
            hosieryColor = if (colorRegex.matches(state.hosieryColor)) state.hosieryColor else "#111111",
            footwearColor = if (colorRegex.matches(state.footwearColor)) state.footwearColor else "#111111"
        )
    }

    private fun derive(state: DesignState): DerivedFacts {
        val hosiery = state.hosieryType != HosieryType.NONE
        val fishnet = state.hosieryType == HosieryType.FISHNET
        val sockLike = state.hosieryType == HosieryType.ANKLE_SOCKS || state.hosieryType == HosieryType.KNEE_HIGH || state.hosieryType == HosieryType.TOE_SOCKS
        val opaque = hosiery && !fishnet && (sockLike || state.denier == Denier.D60 || state.denier == Denier.D100)
        val shoes = state.footwearType != FootwearType.NONE
        val closedShoeWorn = shoes && state.footwearType.closedToe && state.footwearState == FootwearState.WORN
        val toesVisible = !closedShoeWorn
        val nailsVisible = toesVisible && !opaque
        val skinVisible = !closedShoeWorn && !opaque
        val soleFocused = state.cameraAngle == CameraAngle.MACRO_SOLE || state.pose == PoseType.SOLE_FOCUS
        val wideCamera = state.lens == Lens.MM16 || state.lens == Lens.MM24 || state.cameraAngle == CameraAngle.FISH_EYE || state.cameraAngle == CameraAngle.DRONE
        return DerivedFacts(hosiery, fishnet, opaque, shoes, closedShoeWorn, toesVisible, nailsVisible, skinVisible, hosiery, soleFocused, wideCamera)
    }

    private fun resolve(state: DesignState, settings: StudioSettings): Pair<DesignState, List<ResolverDecision>> {
        var effective = state
        val decisions = mutableListOf<ResolverDecision>()
        val soleConflict = (state.cameraAngle == CameraAngle.MACRO_SOLE || state.pose == PoseType.SOLE_FOCUS) && state.pose == PoseType.STANDING
        if (soleConflict) {
            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    effective = effective.copy(pose = PoseType.RECLINED)
                    decisions += ResolverDecision("Pose adjusted", "Standing was changed to reclined for physically plausible sole visibility.", true)
                }
                ResolverMode.ASK -> decisions += ResolverDecision("Sole camera conflict", "Standing + sole macro may produce unnatural ankle rotation.", false)
                ResolverMode.CREATIVE -> Unit
            }
        }

        val vehicleIntent = effective.cameraAngle == CameraAngle.DRIVER || effective.pose == PoseType.DRIVING || effective.pose == PoseType.ACTION_PEDAL || effective.pose == PoseType.DASHBOARD
        if (vehicleIntent && !effective.scene.vehicle) {
            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    effective = effective.copy(scene = SceneType.SPORTS_CAR, surface = SceneCatalog.defaultSurface(SceneType.SPORTS_CAR))
                    decisions += ResolverDecision("Vehicle context restored", "Driving or dashboard framing requires a vehicle scene.", true)
                }
                ResolverMode.ASK -> decisions += ResolverDecision("Vehicle context mismatch", "Driving framing does not match the selected scene.", false)
                ResolverMode.CREATIVE -> Unit
            }
        }

        if (!SceneCatalog.isSurfaceCompatible(effective.scene, effective.surface)) {
            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    effective = effective.copy(surface = SceneCatalog.defaultSurface(effective.scene))
                    decisions += ResolverDecision("Surface matched to scene", "The previous surface belonged to another environment, so a compatible surface was selected.", true)
                }
                ResolverMode.ASK -> decisions += ResolverDecision("Surface / scene mismatch", "The selected surface normally belongs to another environment.", false)
                ResolverMode.CREATIVE -> Unit
            }
        }

        val facts = derive(effective)
        if (facts.closedShoeWorn && settings.detailPriority == DetailPriority.NAILS) {
            decisions += ResolverDecision("Nails are hidden", "Closed worn footwear physically covers the toenails.", false, settings.resolverMode == ResolverMode.STRICT)
        }
        if (facts.opaqueHosiery && settings.detailPriority == DetailPriority.SKIN) {
            decisions += ResolverDecision("Skin detail is covered", "Opaque hosiery hides pores and veins. Material texture takes priority.", true)
        }
        return effective to decisions
    }

    private fun compilePrompt(state: DesignState, settings: StudioSettings, facts: DerivedFacts, decisions: List<ResolverDecision>, refs: List<String>): String {
        val anatomy = """
            Adult model age ${state.modelAge}. EU shoe size ${state.shoeSize}.
            Foot geometry: ${state.footShape.label}. Arch: ${state.archType.label}.
            Geometry tuning: width ${state.footWidth}/100, heel width ${state.heelWidth}/100,
            instep ${state.instep}/100, toe spread ${state.toeSpread}/100, toe length ${state.toeLength}/100,
            natural asymmetry ${state.footAsymmetry}/100. Exactly five toes per visible foot.
        """.trimIndent()

        val skin = if (facts.skinVisible) """
            Visible skin: ${state.skinTone.label}, ${state.skinTexture.label}, ${state.skinUndertone.label} undertone.
            Pores ${state.skinPores}/100, veins ${state.skinVeins}/100, dryness ${state.skinDryness}/100,
            redness ${state.skinRedness}/100, moisture/gloss ${state.skinMoisture}/100.
            Preserve believable microtexture without plastic beauty smoothing.
        """.trimIndent() else "Skin is physically hidden by opaque material. Do not render pores, veins or skin gradients through it."

        val nails = if (facts.nailsVisible) """
            Visible toenails: ${state.nailShape.label}, ${state.nailLength.label}, ${state.nailStyle.label},
            ${state.nailFinish.label}, color ${ColorCatalog.describe(state.nailColor)}.
            Polish and decoration stay on nail plates only.
        """.trimIndent() else "Toenails are hidden. Do not hallucinate nail color through opaque hosiery or closed footwear."

        val hosiery = when {
            !facts.wearingHosiery -> "No hosiery. Bare skin is visible where footwear does not cover the feet."
            facts.fishnet -> """
                Fishnet hosiery, color ${ColorCatalog.describe(state.hosieryColor)}, pattern ${state.hosieryPattern.label},
                ${state.meshSize.name.lowercase()} mesh, thread thickness ${state.meshThickness}/100,
                tension ${state.hosieryTension}/100, compression ${state.hosieryCompression}/100,
                wrinkles ${state.hosieryWrinkles}/100. Open mesh remains open over toes, heel and sole.
                Threads have physical thickness, stretch and tiny contact shadows.
                ${if (state.wornKnit) "Fabric shows subtle realistic wear." else "Fabric is clean and intact."}
            """.trimIndent()
            else -> """
                Hosiery: ${state.hosieryType.label}, ${state.denier.label}, pattern ${state.hosieryPattern.label},
                color ${ColorCatalog.describe(state.hosieryColor)}, ${state.hosieryFinish.name.lowercase()} finish,
                tension ${state.hosieryTension}/100, compression ${state.hosieryCompression}/100, wrinkles ${state.hosieryWrinkles}/100.
                ${if (facts.opaqueHosiery) "Opaque layer: show fibers and folds; hide skin pores, veins and nail polish." else "Sheer layer: skin and nail color are optically filtered through stretched fibers."}
                ${if (state.wornKnit) "Include subtle believable worn fabric texture." else ""}
            """.trimIndent()
        }

        val footwear = when {
            !facts.wearingShoes -> "No footwear present."
            facts.closedShoeWorn -> "Footwear: ${state.footwearType.label}, color ${ColorCatalog.describe(state.footwearColor)}, worn. Closed opaque footwear contains the toes completely."
            else -> "Footwear: ${state.footwearType.label}, color ${ColorCatalog.describe(state.footwearColor)}, state ${state.footwearState.label}. Keep straps, soles and contact points physically plausible."
        }
        val accessory = if (state.accessory == AccessoryType.NONE) "No ankle, toe-ring or tattoo accessory." else "Accessory: ${state.accessory.label}. Keep placement anatomically correct and do not duplicate it."
        val pose = buildString {
            append("${state.pose.label}. Weight distribution, toes, ankles and knees remain physically plausible.")
            if (state.customPose.isNotBlank()) append("\nCustom pose instruction: ${state.customPose.trim()}")
        }
        val camera = buildString {
            append("${state.cameraAngle.label}, ${state.lens.label}. Distance ${state.cameraDistance}/100, height ${state.cameraHeight}/100, tilt ${state.cameraTilt}°, roll ${state.cameraRoll}°, depth of field ${state.depthOfField}/100.")
            if (facts.wideCamera) append(" Preserve foot proportions and avoid wide-angle stretching.")
            if (state.customCamera.isNotBlank()) append("\nCustom camera instruction: ${state.customCamera.trim()}")
        }
        val lighting = "${state.lighting.label}. Film look: ${state.filmStock.label}. Intensity ${state.lightIntensity}/100, softness ${state.lightSoftness}/100, temperature ${state.lightTemperature}/100, contrast ${state.lightContrast}/100."
        val priority = if (settings.detailPriority == DetailPriority.BALANCED) "Balance all visible requirements." else "Give extra fidelity to ${settings.detailPriority.name.lowercase()} without violating physical visibility."
        val refText = if (refs.isEmpty()) "No reference images are supplied." else refs.joinToString("; ")
        val resolverText = if (decisions.isEmpty()) "No resolver adjustments." else decisions.joinToString("\n") { "- ${it.title}: ${it.detail}${if (it.applied) " [applied]" else ""}" }

        return """
            Create ONE photorealistic adult editorial beauty/fashion photograph with premium Aura-grade realism, tack-sharp subject detail, natural optics and crisp material fidelity, focused on feet, legwear, footwear and pose fidelity.

            AUTHORITY ORDER:
            1. Physical visibility and occlusion.
            2. Actually supplied reference images.
            3. Resolved anatomy, pose and camera.
            4. Styling and atmosphere.

            OUTPUT:
            ${settings.resolution.name} / ${settings.aspectRatio.apiValue}. $priority
            Variation strength: ${settings.variationStrength}/100.

            REFERENCE MANIFEST:
            $refText

            SUBJECT / ANATOMY:
            $anatomy

            VISIBILITY:
            toes=${facts.toesVisible}; nails=${facts.nailsVisible}; skin=${facts.skinVisible};
            soleFocus=${facts.soleFocused}; solesCoveredByHosiery=${facts.solesCoveredByHosiery}.

            SKIN:
            $skin

            NAILS:
            $nails

            HOSIERY:
            $hosiery

            FOOTWEAR:
            $footwear

            ACCESSORY:
            $accessory

            POSE:
            $pose

            CAMERA:
            $camera

            SCENE:
            ${state.scene.label}. Surface/contact: ${state.surface}.
            Feet, hosiery and footwear must contact the selected surface with believable pressure and contact shadows.

            LIGHTING / FILM:
            $lighting

            RESOLVER:
            $resolverText

            NEGATIVE / AVOID:
            extra toes, missing toes, fused toes, duplicated feet, detached feet, impossible ankle rotation,
            nail polish on skin, toes through closed shoes, painted-on hosiery, mismatched hosiery,
            bare sole leaking through foot-covering hosiery, plastic skin, CGI look, random duplicate limbs,
            duplicated accessories, watermark, text artifacts.

            
            FINAL OUTPUT LOOK:
            High-end editorial DSLR / premium-camera look.
            Strong perceived sharpness without oversharpening.
            Clean edge definition on hosiery threads, toes, nails, straps and seams.
            Natural skin texture, realistic lens depth, no muddy blur and no cheap AI softness.

            AURA FIDELITY TARGET:
            High micro-contrast but natural skin texture. Crisp focus on the intended subject plane.
            Preserve fine hosiery weave, thread edges, footwear seams, pores, natural specular highlights
            and realistic contact shadows. Avoid mushy edges, waxy skin, watercolor blur, over-smoothed AI look,
            haloing and fake HDR. Render as if captured on a good camera with believable lens behavior.
            If the selected output is 4K or Pro, make it suitable for later 8K upscale without adding fake details.

FINAL CHECK:
            Exactly five toes per visible foot; coherent left/right anatomy; no material clipping;
            no contradiction with opaque coverage.
        """.trimIndent()
    }
}
