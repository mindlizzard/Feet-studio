package com.mindlizzard.feetstudio.domain

import kotlin.math.abs

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

        val prompt = compilePrompt(
            effective,
            workspace.settings,
            facts,
            decisions,
            manifest
        )

        val forceUltra = workspace.settings.qualityProfile == QualityProfile.ULTRA

        val model = if (
            workspace.settings.renderMode == RenderMode.PRO || forceUltra
        ) {
            "gemini-3-pro-image"
        } else {
            "gemini-3.1-flash-image"
        }

        val imageSize = if (forceUltra) {
            "4K"
        } else {
            when (workspace.settings.resolution) {
                Resolution.K1 -> "1K"
                Resolution.K2 -> "2K"
                Resolution.K4 -> "4K"
            }
        }

        return RenderContract(
            requested,
            effective,
            workspace.settings,
            facts,
            decisions,
            manifest,
            prompt,
            model,
            imageSize,
            workspace.settings.aspectRatio.apiValue
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
            footAsymmetry = state.footAsymmetry.coerceIn(0, 100),
            skinPores = state.skinPores.coerceIn(0, 100),
            skinVeins = state.skinVeins.coerceIn(0, 100),
            skinDryness = state.skinDryness.coerceIn(0, 100),
            skinRedness = state.skinRedness.coerceIn(0, 100),
            skinMoisture = state.skinMoisture.coerceIn(0, 100),
            hosieryTension = state.hosieryTension.coerceIn(0, 100),
            hosieryCompression = state.hosieryCompression.coerceIn(0, 100),
            hosieryWrinkles = state.hosieryWrinkles.coerceIn(0, 100),
            meshThickness = state.meshThickness.coerceIn(0, 100),
            cameraAzimuth = ((state.cameraAzimuth % 360) + 360) % 360,
            cameraDistance = state.cameraDistance.coerceIn(0, 100),
            cameraHeight = state.cameraHeight.coerceIn(0, 100),
            cameraTilt = state.cameraTilt.coerceIn(-45, 45),
            cameraRoll = state.cameraRoll.coerceIn(-45, 45),
            depthOfField = state.depthOfField.coerceIn(0, 100),
            lightSoftness = state.lightSoftness.coerceIn(0, 100),
            lightIntensity = state.lightIntensity.coerceIn(0, 100),
            lightTemperature = state.lightTemperature.coerceIn(0, 100),
            lightContrast = state.lightContrast.coerceIn(0, 100),
            nailColor = if (colorRegex.matches(state.nailColor)) state.nailColor else "#D7263D",
            hosieryColor = if (colorRegex.matches(state.hosieryColor)) state.hosieryColor else "#111111",
            footwearColor = if (colorRegex.matches(state.footwearColor)) state.footwearColor else "#111111"
        )
    }

    private fun derive(state: DesignState): DerivedFacts {
        val hosiery = state.hosieryType != HosieryType.NONE
        val fishnet = state.hosieryType == HosieryType.FISHNET
        val sockLike =
            state.hosieryType == HosieryType.ANKLE_SOCKS ||
                state.hosieryType == HosieryType.KNEE_HIGH ||
                state.hosieryType == HosieryType.TOE_SOCKS

        val opaque =
            hosiery && !fishnet &&
                (
                    sockLike ||
                        state.denier == Denier.D60 ||
                        state.denier == Denier.D100
                    )

        val shoes = state.footwearType != FootwearType.NONE
        val closedShoeWorn =
            shoes &&
                state.footwearType.closedToe &&
                state.footwearState == FootwearState.WORN

        val toesVisible = !closedShoeWorn
        val nailsVisible = toesVisible && !opaque
        val skinVisible = !closedShoeWorn && !opaque
        val soleFocused =
            state.cameraAngle == CameraAngle.MACRO_SOLE ||
                state.pose == PoseType.SOLE_FOCUS

        val wideCamera =
            state.lens == Lens.MM16 ||
                state.lens == Lens.MM24 ||
                state.cameraAngle == CameraAngle.FISH_EYE ||
                state.cameraAngle == CameraAngle.DRONE

        return DerivedFacts(
            hosiery,
            fishnet,
            opaque,
            shoes,
            closedShoeWorn,
            toesVisible,
            nailsVisible,
            skinVisible,
            hosiery,
            soleFocused,
            wideCamera
        )
    }

    private fun isComplexPose(pose: PoseType): Boolean =
        pose in setOf(
            PoseType.CROSSED,
            PoseType.LOTUS,
            PoseType.DRIVING,
            PoseType.ACTION_PEDAL,
            PoseType.DASHBOARD,
            PoseType.RECLINED,
            PoseType.WALL_LEGS,
            PoseType.KNEELING,
            PoseType.PINUP_KNEEL,
            PoseType.PINUP_CROSS,
            PoseType.PINUP_RECLINE
        )

    private fun isUnstableCamera(angle: CameraAngle): Boolean =
        angle in setOf(
            CameraAngle.FISH_EYE,
            CameraAngle.KEYHOLE,
            CameraAngle.BETWEEN_LEGS,
            CameraAngle.UPSIDE_DOWN,
            CameraAngle.BOTTOM_UP_GLASS
        )

    private fun resolve(
        state: DesignState,
        settings: StudioSettings
    ): Pair<DesignState, List<ResolverDecision>> {
        var effective = state
        val decisions = mutableListOf<ResolverDecision>()

        val soleConflict =
            (
                state.cameraAngle == CameraAngle.MACRO_SOLE ||
                    state.pose == PoseType.SOLE_FOCUS
                ) &&
                state.pose == PoseType.STANDING

        if (soleConflict) {
            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    effective = effective.copy(pose = PoseType.RECLINED)
                    decisions += ResolverDecision(
                        "Pose adjusted",
                        "Standing was changed to reclined for physically plausible sole visibility.",
                        true
                    )
                }

                ResolverMode.ASK -> decisions += ResolverDecision(
                    "Sole camera conflict",
                    "Standing + sole macro may produce unnatural ankle rotation.",
                    false
                )

                ResolverMode.CREATIVE -> Unit
            }
        }

        val vehicleIntent =
            effective.cameraAngle == CameraAngle.DRIVER ||
                effective.pose == PoseType.DRIVING ||
                effective.pose == PoseType.ACTION_PEDAL ||
                effective.pose == PoseType.DASHBOARD

        if (vehicleIntent && !effective.scene.vehicle) {
            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    effective = effective.copy(
                        scene = SceneType.SPORTS_CAR,
                        surface = SceneCatalog.defaultSurface(SceneType.SPORTS_CAR)
                    )
                    decisions += ResolverDecision(
                        "Vehicle context restored",
                        "Driving or dashboard framing requires a vehicle scene.",
                        true
                    )
                }

                ResolverMode.ASK -> decisions += ResolverDecision(
                    "Vehicle context mismatch",
                    "Driving framing does not match the selected scene.",
                    false
                )

                ResolverMode.CREATIVE -> Unit
            }
        }

        if (!SceneCatalog.isSurfaceCompatible(effective.scene, effective.surface)) {
            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    effective = effective.copy(
                        surface = SceneCatalog.defaultSurface(effective.scene)
                    )
                    decisions += ResolverDecision(
                        "Surface matched to scene",
                        "The previous surface belonged to another environment, so a compatible surface was selected.",
                        true
                    )
                }

                ResolverMode.ASK -> decisions += ResolverDecision(
                    "Surface / scene mismatch",
                    "The selected surface normally belongs to another environment.",
                    false
                )

                ResolverMode.CREATIVE -> Unit
            }
        }

        if (settings.anatomyGuard && isComplexPose(effective.pose)) {
            val unstableOptics =
                effective.lens == Lens.MM16 ||
                    effective.lens == Lens.MM24 ||
                    isUnstableCamera(effective.cameraAngle) ||
                    abs(effective.cameraRoll) > 20

            when (settings.resolverMode) {
                ResolverMode.AUTO, ResolverMode.STRICT -> {
                    if (unstableOptics) {
                        effective = effective.copy(
                            cameraAngle = CameraAngle.FULL_BODY,
                            lens = Lens.MM50,
                            cameraDistance = maxOf(effective.cameraDistance, 68),
                            cameraRoll = 0,
                            depthOfField = maxOf(effective.depthOfField, 58)
                        )
                        decisions += ResolverDecision(
                            "Anatomy guard stabilized camera",
                            "A complex pose with extreme perspective was changed to a safer 50mm full-body framing.",
                            true
                        )
                    } else if (effective.cameraDistance < 58) {
                        effective = effective.copy(cameraDistance = 64)
                        decisions += ResolverDecision(
                            "More body context",
                            "Camera distance was increased so hips, knees, ankles and feet remain connected in a complex pose.",
                            true
                        )
                    }
                }

                ResolverMode.ASK -> {
                    if (unstableOptics || effective.cameraDistance < 58) {
                        decisions += ResolverDecision(
                            "Complex pose may warp anatomy",
                            "Use a 50–85mm lens and wider body context for more reliable knees, ankles and footwear.",
                            false
                        )
                    }
                }

                ResolverMode.CREATIVE -> Unit
            }
        }

        val facts = derive(effective)

        if (
            facts.closedShoeWorn &&
            settings.detailPriority == DetailPriority.NAILS
        ) {
            decisions += ResolverDecision(
                "Nails are hidden",
                "Closed worn footwear physically covers the toenails.",
                false,
                settings.resolverMode == ResolverMode.STRICT
            )
        }

        if (
            facts.opaqueHosiery &&
            settings.detailPriority == DetailPriority.SKIN
        ) {
            decisions += ResolverDecision(
                "Skin detail is covered",
                "Opaque hosiery hides pores and veins. Material texture takes priority.",
                true
            )
        }

        return effective to decisions
    }

    private fun azimuthLabel(degrees: Int): String {
        val d = ((degrees % 360) + 360) % 360
        return when {
            d < 45 || d >= 315 -> "front"
            d < 135 -> "subject-right side"
            d < 225 -> "rear"
            else -> "subject-left side"
        }
    }

    private fun compilePrompt(
        state: DesignState,
        settings: StudioSettings,
        facts: DerivedFacts,
        decisions: List<ResolverDecision>,
        refs: List<String>
    ): String {
        val complexPose = isComplexPose(state.pose)

        val anatomy = """
            One coherent adult subject, age ${state.modelAge}. EU shoe size ${state.shoeSize}.
            Foot geometry: ${state.footShape.label}. Arch: ${state.archType.label}.
            Geometry tuning: width ${state.footWidth}/100, heel width ${state.heelWidth}/100,
            instep ${state.instep}/100, toe spread ${state.toeSpread}/100,
            toe length ${state.toeLength}/100, natural asymmetry ${state.footAsymmetry}/100.

            ANATOMICAL CHAIN:
            Both legs belong to the same body.
            Each leg must trace continuously from pelvis -> hip -> thigh -> knee -> shin/calf -> ankle -> heel -> foot.
            Knees bend only at the knee joint. Ankles remain attached and correctly oriented.
            No duplicated joint, missing joint, detached limb, telescoped leg or impossible overlap.
            Exactly five toes per visible foot.
        """.trimIndent()

        val bodyContext = if (complexPose && settings.anatomyGuard) {
            """
            BODY CONTEXT GUARD:
            This is a complex pose. Include enough lower torso / pelvis context to make both hip origins clear.
            Keep BOTH knees, BOTH ankles and BOTH feet spatially readable.
            Do not crop exactly through a knee, ankle or hip joint.
            If limbs cross, preserve clear front/back ordering and continuous limb paths.
            Pose plausibility is more important than dramatic cropping.
            """.trimIndent()
        } else {
            """
            BODY CONTEXT:
            Preserve one coherent body and plausible leg proportions.
            Do not crop or overlap limbs in a way that makes joints ambiguous.
            """.trimIndent()
        }

        val skin = if (facts.skinVisible) {
            """
            Visible skin: ${state.skinTone.label}, ${state.skinTexture.label}, ${state.skinUndertone.label} undertone.
            Pores ${state.skinPores}/100, veins ${state.skinVeins}/100,
            dryness ${state.skinDryness}/100, redness ${state.skinRedness}/100,
            moisture/gloss ${state.skinMoisture}/100.
            Preserve believable microtexture without plastic beauty smoothing.
            """.trimIndent()
        } else {
            "Skin is physically hidden by opaque material. Do not render pores, veins or skin gradients through it."
        }

        val nails = if (facts.nailsVisible) {
            """
            Visible toenails: ${state.nailShape.label}, ${state.nailLength.label},
            ${state.nailStyle.label}, ${state.nailFinish.label},
            color ${ColorCatalog.describe(state.nailColor)}.
            Polish and decoration stay on nail plates only.
            """.trimIndent()
        } else {
            "Toenails are hidden. Do not hallucinate nail color through opaque hosiery or closed footwear."
        }

        val hosiery = when {
            !facts.wearingHosiery ->
                "No hosiery. Bare skin is visible where footwear does not cover the feet."

            facts.fishnet -> """
                Fishnet hosiery, color ${ColorCatalog.describe(state.hosieryColor)},
                pattern ${state.hosieryPattern.label},
                ${state.meshSize.name.lowercase()} mesh,
                thread thickness ${state.meshThickness}/100,
                tension ${state.hosieryTension}/100,
                compression ${state.hosieryCompression}/100,
                wrinkles ${state.hosieryWrinkles}/100.

                Open mesh remains open over toes, heel and sole.
                Threads have physical thickness, stretch and tiny contact shadows.
                Mesh direction follows the 3D curvature of thighs, knees, calves, ankles and feet.
                Do not smear or paint mesh over joints.
                ${if (state.wornKnit) "Fabric shows subtle realistic wear." else "Fabric is clean and intact."}
            """.trimIndent()

            else -> """
                Hosiery: ${state.hosieryType.label}, ${state.denier.label},
                pattern ${state.hosieryPattern.label},
                color ${ColorCatalog.describe(state.hosieryColor)},
                ${state.hosieryFinish.name.lowercase()} finish,
                tension ${state.hosieryTension}/100,
                compression ${state.hosieryCompression}/100,
                wrinkles ${state.hosieryWrinkles}/100.

                ${if (facts.opaqueHosiery)
                    "Opaque layer: show fibers and folds; hide skin pores, veins and nail polish."
                else
                    "Sheer layer: skin and nail color are optically filtered through stretched fibers."
                }

                Fabric tension changes naturally around knees, ankles, toes and shoe openings.
                No discontinuous fabric, random transparency holes or painted-on texture.
                ${if (state.wornKnit) "Include subtle believable worn fabric texture." else ""}
            """.trimIndent()
        }

        val footwear = when {
            !facts.wearingShoes ->
                "No footwear present."

            facts.closedShoeWorn -> """
                Footwear: ${state.footwearType.label},
                color ${ColorCatalog.describe(state.footwearColor)}, worn.
                Closed opaque footwear contains the toes completely.

                SHOE GEOMETRY:
                Exactly one foot inside each worn shoe.
                Heel sits in heel cup. Forefoot points through the toe box.
                Sole, upper and opening follow one consistent shoe volume.
                No doubled toe box, melted sole, sideways shoe, duplicated heel or shoe fused into ankle.
            """.trimIndent()

            else -> """
                Footwear: ${state.footwearType.label},
                color ${ColorCatalog.describe(state.footwearColor)},
                state ${state.footwearState.label}.

                SHOE GEOMETRY:
                Keep one coherent shoe per intended foot.
                Sole, heel, straps and opening remain mechanically connected.
                Match pressure/contact points to the pose and selected surface.
                No warped toe boxes, doubled heels, floating straps or merged shoes.
            """.trimIndent()
        }

        val accessory =
            if (state.accessory == AccessoryType.NONE) {
                "No ankle, toe-ring or tattoo accessory."
            } else {
                "Accessory: ${state.accessory.label}. Keep placement anatomically correct and do not duplicate it."
            }

        val pose = buildString {
            append(
                "${state.pose.label}. Preserve a believable pelvis-to-foot joint chain, " +
                    "balanced weight distribution, plausible knee bend and anatomically possible ankle rotation."
            )
            if (state.customPose.isNotBlank()) {
                append("\nCustom pose instruction: ${state.customPose.trim()}")
            }
        }

        val camera = buildString {
            append(
                "${state.cameraAngle.label}, ${state.lens.label}. " +
                    "3D camera orbit azimuth ${state.cameraAzimuth}° (${azimuthLabel(state.cameraAzimuth)}). " +
                    "Use numeric orbit for exact front/side/rear placement and angle preset as framing intent. " +
                    "Distance ${state.cameraDistance}/100, height ${state.cameraHeight}/100, " +
                    "tilt ${state.cameraTilt}°, roll ${state.cameraRoll}°, " +
                    "depth of field ${state.depthOfField}/100."
            )

            if (facts.wideCamera) {
                append(
                    " Keep the subject away from stretched frame edges. " +
                        "Do not enlarge feet or compress legs with perspective distortion."
                )
            }

            append(
                " Focus plane must cover the important foot/hosiery/shoe detail. " +
                    "Do not hide bad geometry behind blur."
            )

            if (state.customCamera.isNotBlank()) {
                append("\nCustom camera instruction: ${state.customCamera.trim()}")
            }
        }

        val lighting =
            "${state.lighting.label}. Film look: ${state.filmStock.label}. " +
                "Intensity ${state.lightIntensity}/100, softness ${state.lightSoftness}/100, " +
                "temperature ${state.lightTemperature}/100, contrast ${state.lightContrast}/100."

        val priority =
            if (settings.detailPriority == DetailPriority.BALANCED) {
                "Balance all visible requirements."
            } else {
                "Give extra fidelity to ${settings.detailPriority.name.lowercase()} without violating physical visibility."
            }

        val quality = when (settings.qualityProfile) {
            QualityProfile.STANDARD -> """
                QUALITY PROFILE: STANDARD
                Natural photographic output. Avoid obvious AI artifacts and excessive smoothing.
            """.trimIndent()

            QualityProfile.AURA -> """
                QUALITY PROFILE: AURA
                Premium editorial DSLR / high-end mirrorless look.
                Crisp subject detail with natural optical roll-off.
                Fine hosiery weave, pores, nail edges, leather seams and contact shadows must survive normal zoom.
                High perceived sharpness without halos, fake HDR or brittle oversharpening.
                Resolve small real details instead of painting smooth approximations.
                No waxy skin, watercolor texture, mushy fabric or cheap CGI sheen.
            """.trimIndent()

            QualityProfile.ULTRA -> """
                QUALITY PROFILE: ULTRA
                Treat this as a master-quality 4K source intended for a second refinement pass.
                Prioritize structural correctness first, then microdetail.
                Maximum clean edge definition on hosiery threads, shoe seams, nails and skin texture.
                Maintain realistic optics and local texture without inventing crunchy fake detail.
                Avoid beauty-filter smoothing, haloing, ringing, fake sharpening, synthetic skin and painterly blur.
            """.trimIndent()
        }

        val refText =
            if (refs.isEmpty()) "No reference images are supplied."
            else refs.joinToString("; ")

        val resolverText =
            if (decisions.isEmpty()) {
                "No resolver adjustments."
            } else {
                decisions.joinToString("\n") {
                    "- ${it.title}: ${it.detail}${if (it.applied) " [applied]" else ""}"
                }
            }

        return """
            Create ONE photorealistic adult editorial fashion photograph focused on feet, legwear, footwear and pose fidelity.

            MASTER RULE:
            Produce one coherent real human body first. Styling, dramatic pose and close framing must never override correct body structure.

            AUTHORITY ORDER:
            1. Physical body continuity and plausible anatomy.
            2. Physical visibility and material occlusion.
            3. Actually supplied reference images.
            4. Resolved pose, camera and scene.
            5. Styling and atmosphere.

            OUTPUT:
            ${settings.resolution.name} / ${settings.aspectRatio.apiValue}. $priority
            Variation strength: ${settings.variationStrength}/100.

            $quality

            REFERENCE MANIFEST:
            $refText

            SUBJECT / ANATOMY:
            $anatomy

            $bodyContext

            VISIBILITY:
            toes=${facts.toesVisible};
            nails=${facts.nailsVisible};
            skin=${facts.skinVisible};
            soleFocus=${facts.soleFocused};
            solesCoveredByHosiery=${facts.solesCoveredByHosiery}.

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
            Feet, hosiery and footwear contact the selected surface with believable pressure,
            gravity, compression and contact shadows.

            LIGHTING / FILM:
            $lighting

            RESOLVER:
            $resolverText

            NEGATIVE / AVOID:
            extra toes, missing toes, fused toes, duplicated feet, detached feet,
            extra knees, missing knees, duplicated joints, impossible ankle rotation,
            broken pelvis connection, disconnected legs, telescoped limbs,
            impossible crossing, hidden malformed joints, twisted shins,
            one leg emerging from another leg,
            warped shoes, doubled shoes, doubled heels, melted toe boxes,
            shoe fused into ankle, sideways footwear,
            nail polish on skin, toes through closed shoes,
            painted-on hosiery, discontinuous hosiery, random transparency holes,
            bare sole leaking through foot-covering hosiery,
            plastic skin, waxy skin, watercolor detail, CGI look,
            random duplicate limbs, duplicated accessories,
            artificial sharpening halos, fake HDR, watermark, text artifacts.

            FINAL ANATOMY TRACE:
            Before finalizing, mentally trace BOTH legs continuously:
            pelvis -> hip -> thigh -> knee -> calf/shin -> ankle -> heel -> foot.
            If any segment cannot be traced cleanly, correct the pose/composition before rendering.

            FINAL DETAIL CHECK:
            Exactly five toes per visible foot.
            Correct left/right anatomy.
            Correct footwear orientation.
            Coherent hosiery coverage.
            Natural material texture.
            Crisp intended focus without hiding anatomy in blur.
        """.trimIndent()
    }
}
