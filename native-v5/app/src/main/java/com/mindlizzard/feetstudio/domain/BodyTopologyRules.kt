package com.mindlizzard.feetstudio.domain

object BodyTopologyRules {

    fun singleBodyTopologyLock(
        state: DesignState,
        complexPose: Boolean
    ): String {
        val poseMode = if (complexPose) {
            "This is a complex / overlapping pose. Solve skeletal ownership and depth order before clothing, hosiery pattern, footwear or styling."
        } else {
            "Keep the same single-body ownership even if framing hides part of a limb."
        }

        return """
            SINGLE BODY TOPOLOGY LOCK:
            There is exactly ONE adult human body.
            Exactly ONE pelvis.
            Exactly TWO hip joints attached to that pelvis.
            Exactly TWO thighs.
            Exactly TWO knees.
            Exactly TWO lower legs.
            Exactly TWO ankles.
            Exactly TWO feet.

            BODY OWNERSHIP MAP:
            LEFT PELVIS SOCKET -> LEFT THIGH -> LEFT KNEE -> LEFT SHIN/CALF -> LEFT ANKLE -> LEFT HEEL -> LEFT FOOT.
            RIGHT PELVIS SOCKET -> RIGHT THIGH -> RIGHT KNEE -> RIGHT SHIN/CALF -> RIGHT ANKLE -> RIGHT HEEL -> RIGHT FOOT.

            These two chains may cross visually but they may NEVER merge, duplicate, swap ownership,
            branch into extra limbs, create a second pelvis, create an extra hip/buttock mass, or form a repeated lower body.

            $poseMode

            TOPOLOGY FIRST PASS:
            Before rendering fabric or styling, internally solve a simple skeleton with one pelvis and two complete leg chains.
            Only after that skeleton is coherent may skin, hosiery, footwear, pattern, lighting and photographic detail be applied.

            OVERLAP RULE:
            At every crossing, explicitly choose which limb is in front and which is behind.
            An occluded segment still exists continuously behind the foreground limb; do not replace it with a second hip, second pelvis, duplicate thigh or detached calf.

            PELVIS RULE:
            Both femurs originate from the same pelvis. The waist, abdomen, hips and buttocks describe one continuous torso-to-pelvis volume.
            Never generate two waistlines, two pelvises, stacked buttocks, duplicated groins or a second lower torso to satisfy a crossed-leg pose.

            Pose intent: ${state.pose.label}.
        """.trimIndent()
    }

    fun repairInstruction(): String = """
        SINGLE BODY REPAIR:
        Preserve exactly one pelvis and exactly two hip sockets.
        LEFT HIP -> LEFT THIGH -> LEFT KNEE -> LEFT SHIN/CALF -> LEFT ANKLE -> LEFT FOOT.
        RIGHT HIP -> RIGHT THIGH -> RIGHT KNEE -> RIGHT SHIN/CALF -> RIGHT ANKLE -> RIGHT FOOT.
        Crossing legs may overlap in the image, but the hidden portions must remain continuous behind the foreground limb.
        Remove any second pelvis, duplicate hip/buttock mass, extra thigh, repeated lower torso, duplicate knee, extra ankle or extra foot.
        Do not redesign clothing, hosiery, scene or lighting unless needed to reveal correct anatomy.
    """.trimIndent()

    fun negativeTerms(): String = """
        second pelvis, duplicate pelvis, double pelvis, extra hip joint, duplicate hip,
        stacked buttocks, duplicate buttock structure, second lower torso, repeated waist,
        duplicated groin, extra thigh, duplicate femur, third leg, fourth leg,
        merged leg chains, swapped leg ownership, branching limb, duplicate knee,
        detached calf, extra ankle, extra foot, body split, two lower bodies
    """.trimIndent()
}
