package com.mindlizzard.feetstudio.domain

object HosieryFluxPromptCompiler {
    fun compile(
        contract: RenderContract,
        preset: HosieryLoraPreset
    ): String {
        val d = contract.effective
        val s = contract.settings

        val footwear = if (d.footwearType == FootwearType.NONE) {
            "no footwear"
        } else {
            "${d.footwearType.label}, ${d.footwearState.label}, ${ColorCatalog.describe(d.footwearColor)}"
        }

        val hosiery = if (d.hosieryType == HosieryType.FISHNET) {
            "${d.hosieryType.label}, ${d.meshSize.name.lowercase()} mesh, " +
                "thread thickness ${d.meshThickness}/100, tension ${d.hosieryTension}/100"
        } else {
            "${d.hosieryType.label}, ${d.denier.label}, ${d.hosieryPattern.label}, " +
                "${d.hosieryFinish.name.lowercase()} finish, " +
                "tension ${d.hosieryTension}/100, compression ${d.hosieryCompression}/100"
        }

        return """
            Photorealistic adult fashion editorial photograph.
            One coherent adult subject, age ${d.modelAge}. ${preset.triggerWord}.
            Primary material study: $hosiery, color ${ColorCatalog.describe(d.hosieryColor)}.
            Preserve realistic nylon / hosiery fiber structure, transparency, weave direction,
            stretch, compression, tiny wrinkles, contact shadows and natural highlight roll-off.
            Fabric must wrap continuously around thighs, knees, calves, ankles, heels, toes and soles
            according to the selected garment. Do not paint hosiery as a flat texture.

            Anatomy: both legs belong to one body and trace continuously
            pelvis -> hip -> thigh -> knee -> shin/calf -> ankle -> heel -> foot.
            Exactly five toes per visible foot. Natural left/right orientation.
            No duplicate knees, detached feet, twisted shins or impossible ankle rotation.

            Foot geometry: ${d.footShape.label}, ${d.archType.label}, EU ${d.shoeSize}.
            Footwear: $footwear.
            If footwear is worn, keep one mechanically coherent shoe per intended foot.
            Preserve toe box, heel cup, sole, straps and shoe opening without warping.

            Pose: ${d.pose.label}.
            Camera: ${d.cameraAngle.label}, ${d.lens.label},
            orbit azimuth ${d.cameraAzimuth} degrees around the subject,
            distance ${d.cameraDistance}/100, height ${d.cameraHeight}/100,
            tilt ${d.cameraTilt} degrees, roll ${d.cameraRoll} degrees.
            Scene: ${d.scene.label}. Surface/contact: ${d.surface}.
            Lighting: ${d.lighting.label}, ${d.filmStock.label}.

            Quality: ${s.qualityProfile.label}. Premium camera realism,
            crisp natural microdetail, realistic skin texture, sharp hosiery threads,
            clean shoe seams, plausible depth of field, no waxy smoothing,
            no watercolor blur, no fake HDR, no AI-melted anatomy.

            Avoid: duplicated limbs, extra toes, fused toes, extra shoes, melted shoes,
            random transparency holes, broken hosiery continuity, painted-on mesh,
            watermark, logos or text artifacts unless specifically requested.
        """.trimIndent().take(2950)
    }
}
