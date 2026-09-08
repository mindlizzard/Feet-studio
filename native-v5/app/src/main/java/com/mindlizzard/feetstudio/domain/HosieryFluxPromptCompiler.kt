package com.mindlizzard.feetstudio.domain

object HosieryFluxPromptCompiler {
    fun compile(contract: RenderContract, preset: HosieryLoraPreset): String {
        val d = contract.effective
        val s = contract.settings
        val facts = contract.facts

        val footwear = if (d.footwearType == FootwearType.NONE) {
            "no footwear"
        } else {
            "${d.footwearType.label}, ${d.footwearState.label}, ${ColorCatalog.describe(d.footwearColor)}"
        }

        val hosiery = RenderFidelityRules.hosieryMasterBlock(d, facts)
        val harmony = RenderFidelityRules.sceneCameraHarmony(d, facts)
        val auraIntent = RenderFidelityRules.auraPromptIntent(s, d, facts)

        return """
            Photorealistic adult fashion editorial photograph. One coherent adult subject, age ${d.modelAge}.
            ${preset.triggerWord}.

            HOSIERY MASTER:
            $hosiery

            LAYER ORDER:
            skin -> hosiery -> footwear where present. Footwear: $footwear.
            Straps/openings sit over hosiery at real pressure points. Closed footwear occludes
            toes inside one coherent shoe volume. No fabric through soles, no shoe through toes,
            no floating straps, doubled heels or melted toe boxes.

            ANATOMY:
            One pelvis and two coherent legs. Trace each leg pelvis -> hip -> thigh -> knee ->
            shin/calf -> ankle -> heel -> foot. Exactly five toes per visible foot.
            Each foot is continuous ankle -> heel -> arch -> forefoot -> five toes.
            Natural left/right orientation. No duplicate knees, detached feet, twisted shins,
            fused toes or impossible ankle rotation.

            Foot geometry: ${d.footShape.label}, ${d.archType.label}, EU ${d.shoeSize}.
            Pose: ${d.pose.label}.
            Camera: ${d.cameraAngle.label}, ${d.lens.label}, azimuth ${d.cameraAzimuth} degrees,
            focus ${d.cameraFocusY}/100, distance ${d.cameraDistance}/100,
            height ${d.cameraHeight}/100, tilt ${d.cameraTilt}, roll ${d.cameraRoll}.
            Scene: ${d.scene.label}; contact surface: ${d.surface}.
            Lighting: ${d.lighting.label}; film: ${d.filmStock.label}.

            $harmony

            $auraIntent

            QUALITY ${s.qualityProfile.label}:
            crisp real microdetail at the intended focus plane, distinct hosiery fibers,
            believable skin texture, clean shoe seams, real contact shadows, optical depth falloff.
            No waxy smoothing, fake HDR, sharpening halos, watercolor blur, CGI sheen,
            duplicated limbs, extra toes, text or watermark.
        """.trimIndent().take(3400)
    }
}
