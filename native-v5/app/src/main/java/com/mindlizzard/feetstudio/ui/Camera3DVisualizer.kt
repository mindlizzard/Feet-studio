package com.mindlizzard.feetstudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mindlizzard.feetstudio.domain.CameraAngle
import com.mindlizzard.feetstudio.domain.DesignState
import com.mindlizzard.feetstudio.domain.Lens
import com.mindlizzard.feetstudio.domain.PoseType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

fun azimuthForCameraAngle(angle: CameraAngle): Int =
    when (angle) {
        CameraAngle.SIDE,
        CameraAngle.LYING_SIDE -> 90
        CameraAngle.REAR_VIEW,
        CameraAngle.OVER_SHOULDER -> 180
        else -> 0
    }

@Composable
fun Camera3DVisualizer(
    state: DesignState,
    onChange: (DesignState) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
    val subjectColor = MaterialTheme.colorScheme.onSurface
    val cameraColor = MaterialTheme.colorScheme.primary
    val rayColor = MaterialTheme.colorScheme.secondary
    val scroll = rememberScrollState()

    val complexPose = state.pose in setOf(
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
    val wideLens = state.lens == Lens.MM16 || state.lens == Lens.MM24
    val risky = state.cameraDistance < 45 && (complexPose || wideLens)

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Camera 3D View", style = MaterialTheme.typography.titleMedium)
            Text(
                "Sleep de camera rond het model. Verder van het midden = grotere afstand. De positie wordt direct toegepast.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor
            ) {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(state.cameraHeight, state.cameraAzimuth, state.cameraDistance) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val center = orbitCenter(w, h)
                                val shift = heightShift(h, state.cameraHeight)
                                val dx = change.position.x - center.x
                                val dz = (change.position.y + shift - center.y) / 0.42f

                                var azimuth = Math.toDegrees(
                                    atan2(dx.toDouble(), dz.toDouble())
                                ).roundToInt()
                                if (azimuth < 0) azimuth += 360

                                val maxRadius = min(w, h) * 0.44f
                                val radius = sqrt(dx * dx + dz * dz)
                                val distance = (
                                    ((radius / maxRadius - 0.28f) / 0.72f) * 100f
                                ).roundToInt().coerceIn(0, 100)

                                onChange(
                                    state.copy(
                                        cameraAzimuth = azimuth % 360,
                                        cameraDistance = distance,
                                        customCamera = ""
                                    )
                                )
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val horizon = h * 0.28f
                    val floor = h * 0.82f

                    for (i in 0..8) {
                        drawLine(
                            gridColor,
                            Offset(cx, horizon),
                            Offset(w * i / 8f, h),
                            strokeWidth = 1.2f
                        )
                    }
                    for (i in 1..6) {
                        val t = i / 6f
                        val y = horizon + t * t * (h - horizon)
                        drawLine(
                            gridColor,
                            Offset(0f, y),
                            Offset(w, y),
                            strokeWidth = 1.2f
                        )
                    }

                    drawSubject(cx, floor, h, subjectColor)

                    val camera = cameraPoint(
                        w, h, state.cameraAzimuth, state.cameraDistance, state.cameraHeight
                    )
                    val cameraFloor = cameraPoint(
                        w, h, state.cameraAzimuth, state.cameraDistance, 50
                    )
                    val target = Offset(cx, h * 0.52f)

                    drawLine(gridColor, cameraFloor, camera, strokeWidth = 2f)
                    drawLine(rayColor.copy(alpha = 0.72f), camera, target, strokeWidth = 3f)

                    val half = lensTargetWidth(state.lens)
                    drawLine(
                        rayColor.copy(alpha = 0.35f),
                        camera,
                        target + Offset(-half, 0f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        rayColor.copy(alpha = 0.35f),
                        camera,
                        target + Offset(half, 0f),
                        strokeWidth = 2f
                    )

                    drawCameraGlyph(camera, cameraColor)
                }
            }

            Text(
                "Azimuth ${state.cameraAzimuth}° · afstand ${state.cameraDistance} · hoogte ${state.cameraHeight} · ${state.lens.label}",
                style = MaterialTheme.typography.labelMedium
            )

            Row(
                Modifier.fillMaxWidth().horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Preset("Front") {
                    onChange(state.copy(
                        cameraAngle = CameraAngle.FULL_BODY,
                        cameraAzimuth = 0,
                        cameraDistance = 64,
                        cameraHeight = 48,
                        cameraTilt = 0,
                        lens = Lens.MM50,
                        customCamera = ""
                    ))
                }
                Preset("Low feet") {
                    onChange(state.copy(
                        cameraAngle = CameraAngle.LOW,
                        cameraAzimuth = 0,
                        cameraDistance = 42,
                        cameraHeight = 14,
                        cameraTilt = 10,
                        lens = Lens.MM50,
                        customCamera = ""
                    ))
                }
                Preset("Side") {
                    onChange(state.copy(
                        cameraAngle = CameraAngle.SIDE,
                        cameraAzimuth = 90,
                        cameraDistance = 62,
                        cameraHeight = 46,
                        cameraTilt = 0,
                        lens = Lens.MM50,
                        customCamera = ""
                    ))
                }
                Preset("Rear") {
                    onChange(state.copy(
                        cameraAngle = CameraAngle.REAR_VIEW,
                        cameraAzimuth = 180,
                        cameraDistance = 64,
                        cameraHeight = 46,
                        cameraTilt = 0,
                        lens = Lens.MM50,
                        customCamera = ""
                    ))
                }
                Preset("Top") {
                    onChange(state.copy(
                        cameraAngle = CameraAngle.TOP,
                        cameraAzimuth = 0,
                        cameraDistance = 60,
                        cameraHeight = 90,
                        cameraTilt = -32,
                        lens = Lens.MM50,
                        customCamera = ""
                    ))
                }
            }

            Text("Camera height", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = state.cameraHeight.toFloat(),
                onValueChange = {
                    onChange(state.copy(cameraHeight = it.roundToInt()))
                },
                valueRange = 0f..100f
            )

            Text("Tilt ${state.cameraTilt}°", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = state.cameraTilt.toFloat(),
                onValueChange = {
                    onChange(state.copy(cameraTilt = it.roundToInt()))
                },
                valueRange = -45f..45f
            )

            if (risky) {
                Text(
                    "⚠ Deze camera staat erg dicht op het model. Bij een complexe pose of 16/24mm lens kan anatomie sneller vervormen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                "3D azimuth bepaalt de exacte front/side/rear positie. Angle blijft de framingstijl.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Preset(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) })
}

private fun orbitCenter(w: Float, h: Float) = Offset(w / 2f, h * 0.62f)

private fun heightShift(h: Float, cameraHeight: Int): Float =
    (cameraHeight - 50) / 100f * h * 0.22f

private fun cameraPoint(
    w: Float,
    h: Float,
    azimuth: Int,
    distance: Int,
    cameraHeight: Int
): Offset {
    val center = orbitCenter(w, h)
    val maxRadius = min(w, h) * 0.44f
    val radius = maxRadius * (0.28f + distance.coerceIn(0, 100) / 100f * 0.72f)
    val radians = Math.toRadians(azimuth.toDouble())
    val x = center.x + sin(radians).toFloat() * radius
    val z = cos(radians).toFloat() * radius
    val y = center.y + z * 0.42f - heightShift(h, cameraHeight)
    return Offset(x, y)
}

private fun lensTargetWidth(lens: Lens): Float = when (lens) {
    Lens.MM16 -> 78f
    Lens.MM24 -> 65f
    Lens.MM35 -> 52f
    Lens.MM50 -> 42f
    Lens.MM85 -> 32f
    Lens.MM105 -> 26f
    Lens.MACRO -> 22f
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSubject(
    centerX: Float,
    floorY: Float,
    height: Float,
    color: Color
) {
    val head = Offset(centerX, floorY - height * 0.36f)
    val neck = Offset(centerX, floorY - height * 0.30f)
    val pelvis = Offset(centerX, floorY - height * 0.15f)
    val leftKnee = Offset(centerX - height * 0.04f, floorY - height * 0.075f)
    val rightKnee = Offset(centerX + height * 0.04f, floorY - height * 0.075f)
    val leftFoot = Offset(centerX - height * 0.06f, floorY)
    val rightFoot = Offset(centerX + height * 0.06f, floorY)

    drawCircle(color, radius = height * 0.028f, center = head)
    drawLine(color, neck, pelvis, strokeWidth = 6f)
    drawLine(
        color,
        Offset(centerX - height * 0.055f, neck.y + height * 0.035f),
        Offset(centerX + height * 0.055f, neck.y + height * 0.035f),
        strokeWidth = 5f
    )
    drawLine(color, pelvis, leftKnee, strokeWidth = 6f)
    drawLine(color, leftKnee, leftFoot, strokeWidth = 6f)
    drawLine(color, pelvis, rightKnee, strokeWidth = 6f)
    drawLine(color, rightKnee, rightFoot, strokeWidth = 6f)
    drawLine(color, leftFoot, leftFoot + Offset(height * 0.035f, 0f), strokeWidth = 5f)
    drawLine(color, rightFoot, rightFoot + Offset(height * 0.035f, 0f), strokeWidth = 5f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCameraGlyph(
    center: Offset,
    color: Color
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - 18f, center.y - 12f),
        size = Size(36f, 24f),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.92f),
        radius = 6f,
        center = center
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - 8f, center.y - 18f),
        size = Size(16f, 7f),
        cornerRadius = CornerRadius(3f, 3f)
    )
}
