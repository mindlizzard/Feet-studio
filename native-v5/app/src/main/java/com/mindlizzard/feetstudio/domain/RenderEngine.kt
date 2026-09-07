package com.mindlizzard.feetstudio.domain

import kotlin.math.roundToInt

object RenderEngine {

    fun buildContract(
        workspace: WorkspaceState,
        references: List<ReferenceAsset>
    ): RenderContract {
        val requested = workspace.design
        val normalized = normalize(requested)
        val (effective, decisions) = resolve(normalized, workspace.settings)
        val facts = derive(effective)
        val manifest = references
            .filter { it.enabled }
            .sortedBy { it.strength.ordinal }
            .take(5)
            .map { "${it.role.name.lowercase()}=${it.strength.name.lowercase()}" }

        val prompt = compilePrompt(effective, workspace.settings, facts, decisions, manifest)
        val model = if (workspace.settings.renderMode == RenderMode.PRO) {
            "gemini-3-pro-image"
        } else {
            "gemini-3.1-flash-image"
        }
        val imageSize = when (workspace.settings.resolution) {
            Resolution.K1 -> "1K"
            Resolution.K2 -> "2K"
            Resolution.K4 -> "4K"
        }

        return RenderContract(
            requested = requested,
            effective = effective,
            settings = workspace.settings,
            facts = facts,
            decisions = decisions,
            referenceManifest = manifest,
            prompt = prompt,
            model = model,
            imageSize = imageSize,
            aspectRatio = workspace.settings.aspectRatio.apiValue
        )
    }

    private fun normalize(state: DesignState): DesignState {
        val colorRegex = Regex("^#[0-9A-Fa-f]{6}$")
        return state.copy(
            modelAge = state.modelAge.coerceIn(18, 80),
            shoeSize = state.shoeSize.coerceIn(35f, 45f),
            footWidth = state.footWidth.coerceIn(0, 100),
            heelWidth = state.heelWidth.coerceIn(0, 100),
            instep = state.instep.coerceIn(0, 100),
            toeSpread = state.toeSpread.coerceIn(0, 100),
            toeLength = state.toeLength.coerceIn(0, 100),
            nailColor = if (colorRegex.matches(state.nailColor)) state.nailColor else "#D9466F",
            hosieryColor = if (colorRegex.matches(state.hosieryColor)) state.hosieryColor else "#111111",
            footwearColor = if (colorRegex.matches(state.footwearColor)) state.footwearColor else "#111111"
        )
    }

    private fun derive(state: DesignState): DerivedFacts {
        val hosiery = state.hosieryType != HosieryType.NONE
        val fishnet = state.hosieryType == HosieryType.FISHNET
        val opaque = hosiery && !fishnet && (
            state.hosieryType == HosieryType.SOCKS ||
                state.hosieryType == HosieryType.KNEE_HIGH ||
                state.denier == Denier.D60 ||
                state.denier == Denier.D100
            )
        val shoes = state.footwearType != FootwearType.NONE
        val closedShoeWorn = shoes &&
            state.footwearType.closedToe &&
            state.footwearState == FootwearState.WORN
        val toesVisible = !closedShoeWorn
        val nailsVisible = toesVisible && !opaque
        val skinVisible = !closedShoeWorn && !opaque
        val soleFocused = state.cameraAngle == CameraAngle.MACRO_SOLE ||
            state.pose == PoseType.SOLE_FOCUS

        return DerivedFacts(
            wearingHosiery = hosiery,
            fishnet = fishnet,
            opaqueHosiery = opaque,
            wearingShoes = shoes,
            closedShoeWorn = closedShoeWorn,
            toesVisible = toesVisible,
            nailsVisible = nailsVisible,
            skinVisible = skinVisible,
            soleFocused = soleFocused
        )
    }

    private fun resolve(
        state: DesignState,
        settings: StudioSettings
    ): Pair<DesignState, List<ResolverDecision>> {
        var effective = state
        val decisions = mutableListOf<ResolverDecision>()

        if (state.modelAge < 18) {
            decisions += ResolverDecision(
                "Adult age enforced",
                "Model age is clamped to 18+.",
                applied = true
            )
        }

        if (state.hosieryType == HosieryType.FISHNET &&
            (state.denier == Denier.D60 || state.denier == Denier.D100)
        ) {
            decisions += ResolverDecision(
                "Fishnet stays open mesh",
                "Denier does not turn fishnet into an opaque fabric. Mesh physics has priority.",
                applied = true
            )
        }

        val soleConflict = (state.cameraAngle == CameraAngle.MACRO_SOLE ||
            state.pose == PoseType.SOLE_FOCUS) &&
            state.pose == PoseType.STANDING

        if (soleConflict) {
            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    effective = effective.copy(pose = PoseType.RECLINED)
                    decisions += ResolverDecision(
                        "Pose adjusted",
                        "Standing was changed to reclined for physically plausible sole visibility.",
                        applied = true
                    )
                }
                ResolverMode.ASK -> decisions += ResolverDecision(
                    "Sole camera conflict",
                    "Standing + sole macro may produce unnatural ankle rotation.",
                    applied = false
                )
                ResolverMode.CREATIVE -> Unit
            }
        }

        val vehicleIntent = effective.cameraAngle == CameraAngle.DRIVER ||
            effective.pose == PoseType.DRIVING ||
            effective.pose == PoseType.DASHBOARD

        if (vehicleIntent && !effective.scene.vehicle) {
            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    effective = effective.copy(scene = SceneType.SPORTS_CAR, surface = "car interior / pedals")
                    decisions += ResolverDecision(
                        "Vehicle context restored",
                        "Driver or dashboard framing requires a vehicle scene.",
                        applied = true
                    )
                }
                ResolverMode.ASK -> decisions += ResolverDecision(
                    "Vehicle context mismatch",
                    "Driver framing does not match the selected scene.",
                    applied = false
                )
                ResolverMode.CREATIVE -> Unit
            }
        }

        val facts = derive(effective)
        if (facts.closedShoeWorn && settings.detailPriority == DetailPriority.NAILS) {
            decisions += ResolverDecision(
                "Nails are hidden",
                "Closed worn footwear physically covers the toenails.",
                applied = false,
                blocking = settings.resolverMode == ResolverMode.STRICT
            )
        }
        if (facts.opaqueHosiery && settings.detailPriority == DetailPriority.SKIN) {
            decisions += ResolverDecision(
                "Skin detail is covered",
                "Opaque hosiery hides pores and veins. Material texture takes priority.",
                applied = true
            )
        }

        return effective to decisions
    }

    private fun compilePrompt(
        state: DesignState,
        settings: StudioSettings,
        facts: DerivedFacts,
        decisions: List<ResolverDecision>,
        refs: List<String>
    ): String {
        val anatomy = """
            Adult model age ${state.modelAge}. EU shoe size ${state.shoeSize}.
            Foot geometry: ${state.footShape.label}. Arch: ${state.archType.label}.
            Geometry tuning: width ${state.footWidth}/100, heel width ${state.heelWidth}/100,
            instep ${state.instep}/100, toe spread ${state.toeSpread}/100, toe length ${state.toeLength}/100.
            Exactly five toes per visible foot. Natural left/right asymmetry and plausible ankle joints.
        """.trimIndent()

        val skin = if (facts.skinVisible) {
            """
            Visible skin: ${state.skinTone.label}, ${state.skinTexture.label}.
            Natural pores ${state.skinPores}/100, veins ${state.skinVeins}/100, dryness ${state.skinDryness}/100.
            Preserve real microtexture without plastic beauty-filter smoothing.
            """.trimIndent()
        } else {
            "Skin is physically hidden by opaque material. Do not render pores, veins or skin gradients through it."
        }

        val nails = if (facts.nailsVisible) {
            "Visible toenails: ${state.nailShape.label}, ${state.nailStyle.label}, color ${state.nailColor}. Polish stays on nail plates only."
        } else {
            "Toenails are hidden. Do not hallucinate nail color through opaque hosiery or closed footwear."
        }

        val hosiery = when {
            !facts.wearingHosiery -> "No hosiery. Bare skin is visible where shoes do not cover the feet."
            facts.fishnet -> """
                Fishnet hosiery, color ${state.hosieryColor}, ${state.meshSize.name.lowercase()} mesh,
                thread thickness ${state.meshThickness}/100, tension ${state.hosieryTension}/100.
                Open mesh remains open over toes, heel and sole. Skin is visible only through mesh openings.
                Threads have physical thickness, stretch and tiny contact shadows. Never turn fishnet into opaque tights.
            """.trimIndent()
            else -> """
                Hosiery: ${state.hosieryType.label}, ${state.denier.label}, color ${state.hosieryColor},
                ${state.hosieryFinish.name.lowercase()} finish, tension ${state.hosieryTension}/100,
                wrinkles ${state.hosieryWrinkles}/100.
                ${if (facts.opaqueHosiery) "Opaque layer: show fibers and folds; hide skin pores, veins and nail polish." else "Sheer layer: skin and nail color are optically filtered through stretched fibers."}
                Material coverage must be coherent on both legs, including toes, heels and soles when the garment covers the foot.
            """.trimIndent()
        }

        val footwear = when {
            !facts.wearingShoes -> "No footwear present."
            facts.closedShoeWorn -> """
                Footwear: ${state.footwearType.label}, color ${state.footwearColor}, worn.
                Closed opaque footwear contains the toes completely. No toes or nail polish visible through the shoe.
            """.trimIndent()
            else -> "Footwear: ${state.footwearType.label}, color ${state.footwearColor}, state ${state.footwearState.label}. Keep straps and shoe geometry physically plausible."
        }

        val priority = if (settings.detailPriority == DetailPriority.BALANCED) {
            "Balance all visible requirements."
        } else {
            "Give extra fidelity to ${settings.detailPriority.name.lowercase()} without violating physical visibility."
        }

        val refText = if (refs.isEmpty()) "No reference images are supplied." else refs.joinToString("; ")
        val resolverText = if (decisions.isEmpty()) "No resolver adjustments." else
            decisions.joinToString("\n") { "- ${it.title}: ${it.detail}${if (it.applied) " [applied]" else ""}" }

        return """
            Create ONE photorealistic adult fashion/editorial photograph focused on feet, legwear, footwear and pose fidelity.

            AUTHORITY ORDER:
            1. Physical visibility and occlusion.
            2. Actually supplied reference images.
            3. Resolved anatomy, pose and camera.
            4. Styling and atmosphere.
            Never resurrect a detail hidden by a higher-priority physical layer.

            OUTPUT:
            ${settings.resolution.name} / ${settings.aspectRatio.apiValue}. $priority

            REFERENCE MANIFEST:
            $refText

            SUBJECT / ANATOMY:
            $anatomy

            VISIBILITY:
            toes=${facts.toesVisible}; nails=${facts.nailsVisible}; skin=${facts.skinVisible}; soleFocus=${facts.soleFocused}

            SKIN:
            $skin

            NAILS:
            $nails

            HOSIERY:
            $hosiery

            FOOTWEAR:
            $footwear

            POSE:
            ${state.pose.label}. Weight distribution, toes, ankles and knees remain physically plausible.

            CAMERA:
            ${state.cameraAngle.label}, ${state.lens.label}. Distance ${state.cameraDistance}/100,
            camera height ${state.cameraHeight}/100, tilt ${state.cameraTilt} degrees, depth of field ${state.depthOfField}/100.
            Avoid perspective stretching that changes foot proportions.

            SCENE:
            ${state.scene.label}. Surface/contact: ${state.surface}.
            Feet, hosiery and footwear must contact the surface with believable pressure and contact shadows.

            LIGHTING:
            ${state.lighting.label}, intensity ${state.lightIntensity}/100, softness ${state.lightSoftness}/100.
            Natural shadows, reflections and material response.

            RESOLVER:
            $resolverText

            NEGATIVE / AVOID:
            extra toes, missing toes, fused toes, duplicated feet, detached feet, impossible ankle rotation,
            nail polish on skin, toes through closed shoes, painted-on hosiery, mismatched hosiery,
            bare sole leaking through foot-covering hosiery, plastic skin, CGI look, random duplicate limbs,
            watermark, text artifacts.

            FINAL CHECK:
            Exactly five toes per visible foot; coherent left/right anatomy; no material clipping;
            no contradiction with opaque coverage.
        """.trimIndent()
    }
}
