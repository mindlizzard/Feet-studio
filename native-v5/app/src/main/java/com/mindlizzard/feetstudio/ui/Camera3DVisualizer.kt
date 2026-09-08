package com.mindlizzard.feetstudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mindlizzard.feetstudio.domain.CameraAngle
import com.mindlizzard.feetstudio.domain.DesignState
import com.mindlizzard.feetstudio.domain.Lens
import com.mindlizzard.feetstudio.domain.PoseType
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

fun azimuthForCameraAngle(angle: CameraAngle): Int =
    when (angle) {
        CameraAngle.SIDE, CameraAngle.LYING_SIDE -> 90
        CameraAngle.REAR_VIEW, CameraAngle.OVER_SHOULDER -> 180
        else -> 0
    }

@Composable
fun Camera3DVisualizer(
    state: DesignState,
    onChange: (DesignState) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val orbitColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
    val subjectColor = MaterialTheme.colorScheme.onSurface
    val cameraColor = MaterialTheme.colorScheme.primary
    val rayColor = MaterialTheme.colorScheme.secondary
    val focusColor = MaterialTheme.colorScheme.tertiary

    val complexPose = state.pose in setOf(
        PoseType.CROSSED, PoseType.LOTUS, PoseType.DRIVING,
        PoseType.ACTION_PEDAL, PoseType.DASHBOARD, PoseType.RECLINED,
        PoseType.WALL_LEGS, PoseType.KNEELING, PoseType.PINUP_KNEEL,
        PoseType.PINUP_CROSS, PoseType.PINUP_RECLINE
    )
    val wideLens = state.lens == Lens.MM16 || state.lens == Lens.MM24
    val criticalRisk = state.cameraDistance < 34 && (complexPose || wideLens)
    val warningRisk = !criticalRisk && (
        state.cameraDistance < 46 && (complexPose || wideLens) ||
            abs(state.cameraTilt) > 32 ||
            (wideLens && state.cameraDistance < 56)
    )

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text("Camera Studio", style = MaterialTheme.typography.titleMedium)
            Text(
                "Sleep camera of focusvizier. Rechtsboven zie je live ongeveer wat de camera kadert.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier.fillMaxWidth().height(380.dp),
                shape = RoundedCornerShape(18.dp),
                color = surfaceColor
            ) {
                Box(Modifier.fillMaxSize()) {
                    Canvas(
                        Modifier
                            .fillMaxSize()
                            .pointerInput(
                                state.cameraHeight,
                                state.cameraAzimuth,
                                state.cameraDistance,
                                state.cameraFocusY
                            ) {
                                var draggingFocus = false
                                detectDragGestures(
                                    onDragStart = { start ->
                                        val target = focusPoint(
                                            size.width.toFloat(),
                                            size.height.toFloat(),
                                            state.cameraFocusY
                                        )
                                        draggingFocus = pointDistance(start, target) < 58f
                                    },
                                    onDragEnd = { draggingFocus = false },
                                    onDragCancel = { draggingFocus = false }
                                ) { change, _ ->
                                    change.consume()
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()

                                    if (draggingFocus) {
                                        onChange(
                                            state.copy(
                                                cameraFocusY = focusYFromScreen(
                                                    change.position.y, h
                                                ),
                                                customCamera = ""
                                            )
                                        )
                                    } else {
                                        val center = orbitCenter(w, h)
                                        val shift = heightShift(h, state.cameraHeight)
                                        val dx = change.position.x - center.x
                                        val dz = (
                                            change.position.y + shift - center.y
                                        ) / 0.42f

                                        var azimuth = Math.toDegrees(
                                            atan2(dx.toDouble(), dz.toDouble())
                                        ).roundToInt()
                                        if (azimuth < 0) azimuth += 360

                                        val maxRadius = min(w, h) * 0.46f
                                        val radius = sqrt(dx * dx + dz * dz)
                                        val distance = (
                                            ((radius / maxRadius - 0.30f) / 0.70f) * 100f
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
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val horizon = h * 0.24f
                        val floor = h * 0.87f

                        drawStudioGrid(w, h, horizon, cx, gridColor)
                        drawOrbitRing(w, h, orbitColor)
                        drawRotatingSubject(
                            cx, floor, h, state.cameraAzimuth, subjectColor
                        )

                        val target = focusPoint(w, h, state.cameraFocusY)
                        drawFocusTarget(target, focusColor)

                        val cameraPoint = cameraPoint(
                            w, h, state.cameraAzimuth,
                            state.cameraDistance, state.cameraHeight
                        )
                        val cameraFloor = cameraPoint(
                            w, h, state.cameraAzimuth,
                            state.cameraDistance, 50
                        )

                        drawHeightRail(
                            w, h, state.cameraHeight, orbitColor, cameraColor
                        )
                        drawLine(
                            orbitColor, cameraFloor, cameraPoint, strokeWidth = 2f
                        )
                        drawLine(
                            rayColor.copy(alpha = 0.86f),
                            cameraPoint, target, strokeWidth = 3.4f
                        )

                        val half = lensTargetWidth(state.lens)
                        drawLine(
                            rayColor.copy(alpha = 0.44f),
                            cameraPoint,
                            target + Offset(-half, 0f),
                            strokeWidth = 2.4f
                        )
                        drawLine(
                            rayColor.copy(alpha = 0.44f),
                            cameraPoint,
                            target + Offset(half, 0f),
                            strokeWidth = 2.4f
                        )
                        drawCameraGlyph(cameraPoint, cameraColor)
                    }

                    FramingPreview(
                        state = state,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(width = 118.dp, height = 154.dp)
                    )
                }
            }

            Text(
                "Azimuth ${state.cameraAzimuth}° · ${directionLabel(state.cameraAzimuth)} · " +
                    "${distanceLabel(state.cameraDistance)} · hoogte ${state.cameraHeight}% · " +
                    "focus ${focusLabel(state.cameraFocusY)} · ${state.lens.label}",
                style = MaterialTheme.typography.labelMedium
            )

            CameraPresetGrid(state = state, onChange = onChange)

            Text("Focus target", style = MaterialTheme.typography.labelMedium)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FocusChip(
                    "Head", state.cameraFocusY <= 24, Modifier.weight(1f)
                ) { onChange(state.copy(cameraFocusY = 15)) }

                FocusChip(
                    "Torso", state.cameraFocusY in 25..52, Modifier.weight(1f)
                ) { onChange(state.copy(cameraFocusY = 42)) }

                FocusChip(
                    "Legs", state.cameraFocusY in 53..78, Modifier.weight(1f)
                ) { onChange(state.copy(cameraFocusY = 68)) }

                FocusChip(
                    "Feet", state.cameraFocusY >= 79, Modifier.weight(1f)
                ) { onChange(state.copy(cameraFocusY = 94)) }
            }

            Text(
                "Camera height · ${state.cameraHeight}% · ${heightLabel(state.cameraHeight)}",
                style = MaterialTheme.typography.labelMedium
            )
            Slider(
                value = state.cameraHeight.toFloat(),
                onValueChange = {
                    onChange(state.copy(cameraHeight = it.roundToInt()))
                },
                valueRange = 0f..100f
            )

            Text(
                "Tilt ${state.cameraTilt}°",
                style = MaterialTheme.typography.labelMedium
            )
            Slider(
                value = state.cameraTilt.toFloat(),
                onValueChange = {
                    onChange(state.copy(cameraTilt = it.roundToInt()))
                },
                valueRange = -45f..45f
            )

            when {
                criticalRisk -> Text(
                    "🔴 Hoog anatomierisico: camera erg dichtbij met complexe pose of groothoek.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                warningRisk -> Text(
                    "🟡 Perspectiefwaarschuwing: deze combinatie kan benen/voeten sterker vervormen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                else -> Text(
                    "🟢 Camerageometrie is rustig. Goede basis voor stabiele anatomie.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun FramingPreview(
    state: DesignState,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
    val frame = MaterialTheme.colorScheme.outline
    val subject = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = frame.copy(alpha = 0.65f),
                topLeft = Offset(1f, 1f),
                size = Size(w - 2f, h - 2f),
                cornerRadius = CornerRadius(9f, 9f),
                style = Stroke(width = 2f)
            )

            val crop = previewScale(state.cameraDistance, state.lens)
            val focusShift = (state.cameraFocusY - 50) / 100f * h * 0.52f
            val cameraHeightShift =
                (state.cameraHeight - 50) / 100f * h * 0.16f
            val tiltShift = state.cameraTilt / 45f * h * 0.10f

            val centerY =
                h * 0.54f - focusShift - cameraHeightShift + tiltShift

            val az = Math.toRadians(state.cameraAzimuth.toDouble())
            val widthFactor =
                0.42f + abs(cos(az)).toFloat() * 0.58f

            val silhouetteHeight = h * 0.64f * crop
            val silhouetteWidth = w * 0.34f * crop * widthFactor
            val top = centerY - silhouetteHeight * 0.42f
            val bottom = centerY + silhouetteHeight * 0.58f
            val cx = w / 2f

            drawCircle(
                color = subject,
                radius = min(silhouetteWidth, silhouetteHeight) * 0.11f,
                center = Offset(cx, top + silhouetteHeight * 0.08f)
            )

            val shoulderY = top + silhouetteHeight * 0.24f
            val pelvisY = top + silhouetteHeight * 0.50f
            val kneeY = top + silhouetteHeight * 0.73f
            val footY = bottom
            val halfShoulder = silhouetteWidth * 0.48f
            val halfLeg = silhouetteWidth * 0.24f

            drawLine(
                subject,
                Offset(cx, shoulderY - silhouetteHeight * 0.08f),
                Offset(cx, pelvisY),
                strokeWidth = 4f
            )
            drawLine(
                subject,
                Offset(cx - halfShoulder, shoulderY),
                Offset(cx + halfShoulder, shoulderY),
                strokeWidth = 3.6f
            )
            drawLine(
                subject,
                Offset(cx, pelvisY),
                Offset(cx - halfLeg, kneeY),
                strokeWidth = 4f
            )
            drawLine(
                subject,
                Offset(cx - halfLeg, kneeY),
                Offset(cx - halfLeg * 1.15f, footY),
                strokeWidth = 4f
            )
            drawLine(
                subject,
                Offset(cx, pelvisY),
                Offset(cx + halfLeg, kneeY),
                strokeWidth = 4f
            )
            drawLine(
                subject,
                Offset(cx + halfLeg, kneeY),
                Offset(cx + halfLeg * 1.15f, footY),
                strokeWidth = 4f
            )

            val focusY = (
                top + silhouetteHeight *
                    state.cameraFocusY.coerceIn(0, 100) / 100f
            ).coerceIn(8f, h - 8f)

            drawCircle(
                color = accent.copy(alpha = 0.20f),
                radius = 11f,
                center = Offset(cx, focusY)
            )
            drawCircle(
                color = accent,
                radius = 4f,
                center = Offset(cx, focusY),
                style = Stroke(width = 2f)
            )

            val safeInset = lensSafeInset(state.lens)
            drawRoundRect(
                color = accent.copy(alpha = 0.45f),
                topLeft = Offset(safeInset, safeInset),
                size = Size(
                    w - safeInset * 2f,
                    h - safeInset * 2f
                ),
                cornerRadius = CornerRadius(5f, 5f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

@Composable
private fun CameraPresetGrid(
    state: DesignState,
    onChange: (DesignState) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Preset("Front", Modifier.weight(1f)) {
                onChange(state.copy(
                    cameraAngle = CameraAngle.FULL_BODY,
                    cameraAzimuth = 0, cameraDistance = 64,
                    cameraHeight = 48, cameraFocusY = 48,
                    cameraTilt = 0, lens = Lens.MM50,
                    customCamera = ""
                ))
            }
            Preset("¾ left", Modifier.weight(1f)) {
                onChange(state.copy(
                    cameraAngle = CameraAngle.FULL_BODY,
                    cameraAzimuth = 315, cameraDistance = 62,
                    cameraHeight = 48, cameraFocusY = 48,
                    cameraTilt = 0, lens = Lens.MM50,
                    customCamera = ""
                ))
            }
            Preset("Side", Modifier.weight(1f)) {
                onChange(state.copy(
                    cameraAngle = CameraAngle.SIDE,
                    cameraAzimuth = 90, cameraDistance = 62,
                    cameraHeight = 46, cameraFocusY = 50,
                    cameraTilt = 0, lens = Lens.MM50,
                    customCamera = ""
                ))
            }
            Preset("Rear", Modifier.weight(1f)) {
                onChange(state.copy(
                    cameraAngle = CameraAngle.REAR_VIEW,
                    cameraAzimuth = 180, cameraDistance = 64,
                    cameraHeight = 46, cameraFocusY = 48,
                    cameraTilt = 0, lens = Lens.MM50,
                    customCamera = ""
                ))
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Preset("Ground", Modifier.weight(1f)) {
                onChange(state.copy(
                    cameraAngle = CameraAngle.LOW,
                    cameraAzimuth = 0, cameraDistance = 48,
                    cameraHeight = 6, cameraFocusY = 78,
                    cameraTilt = 12, lens = Lens.MM50,
                    customCamera = ""
                ))
            }
            Preset("Feet macro", Modifier.weight(1f)) {
                onChange(state.copy(
                    cameraAngle = CameraAngle.MACRO_TOES,
                    cameraAzimuth = 0, cameraDistance = 24,
                    cameraHeight = 10, cameraFocusY = 94,
                    cameraTilt = 8, lens = Lens.MACRO,
                    customCamera = ""
                ))
            }
            Preset("Top", Modifier.weight(1f)) {
                onChange(state.copy(
                    cameraAngle = CameraAngle.TOP,
                    cameraAzimuth = 0, cameraDistance = 62,
                    cameraHeight = 92, cameraFocusY = 50,
                    cameraTilt = -34, lens = Lens.MM50,
                    customCamera = ""
                ))
            }
            Preset("Eye level", Modifier.weight(1f)) {
                onChange(state.copy(
                    cameraAngle = CameraAngle.FULL_BODY,
                    cameraAzimuth = 0, cameraDistance = 68,
                    cameraHeight = 58, cameraFocusY = 42,
                    cameraTilt = 0, lens = Lens.MM85,
                    customCamera = ""
                ))
            }
        }
    }
}

@Composable
private fun Preset(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun FocusChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(if (selected) "● $label" else label) }
    )
}

private fun pointDistance(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

private fun focusYFromScreen(y: Float, h: Float): Int =
    (((y - h * 0.20f) / (h * 0.70f)) * 100f)
        .roundToInt().coerceIn(0, 100)

private fun focusPoint(w: Float, h: Float, focusY: Int): Offset =
    Offset(
        w / 2f,
        h * 0.20f + h * 0.70f * focusY.coerceIn(0, 100) / 100f
    )

private fun focusLabel(focusY: Int): String = when {
    focusY <= 24 -> "hoofd"
    focusY <= 52 -> "torso"
    focusY <= 78 -> "benen"
    else -> "voeten"
}

private fun directionLabel(degrees: Int): String {
    val d = ((degrees % 360) + 360) % 360
    return when {
        d < 23 || d >= 338 -> "front"
        d < 68 -> "¾ rechts"
        d < 113 -> "rechts"
        d < 158 -> "¾ achter"
        d < 203 -> "achter"
        d < 248 -> "¾ achter"
        d < 293 -> "links"
        else -> "¾ links"
    }
}

private fun heightLabel(value: Int): String = when {
    value < 20 -> "floor"
    value < 42 -> "low"
    value < 66 -> "eye/waist"
    value < 84 -> "high"
    else -> "overhead"
}

private fun distanceLabel(value: Int): String = when {
    value < 28 -> "Macro/close"
    value < 48 -> "Close"
    value < 70 -> "Medium"
    else -> "Full body"
}

private fun previewScale(distance: Int, lens: Lens): Float {
    val distanceScale =
        1.36f - distance.coerceIn(0, 100) / 100f * 0.58f
    val lensScale = when (lens) {
        Lens.MM16 -> 0.72f
        Lens.MM24 -> 0.80f
        Lens.MM35 -> 0.90f
        Lens.MM50 -> 1.00f
        Lens.MM85 -> 1.14f
        Lens.MM105 -> 1.22f
        Lens.MACRO -> 1.34f
    }
    return (distanceScale * lensScale).coerceIn(0.62f, 1.55f)
}

private fun lensSafeInset(lens: Lens): Float = when (lens) {
    Lens.MM16 -> 4f
    Lens.MM24 -> 6f
    Lens.MM35 -> 9f
    Lens.MM50 -> 12f
    Lens.MM85 -> 16f
    Lens.MM105 -> 19f
    Lens.MACRO -> 22f
}

private fun orbitCenter(w: Float, h: Float) =
    Offset(w / 2f, h * 0.64f)

private fun heightShift(h: Float, cameraHeight: Int): Float =
    (cameraHeight - 50) / 100f * h * 0.24f

private fun cameraPoint(
    w: Float,
    h: Float,
    azimuth: Int,
    distance: Int,
    cameraHeight: Int
): Offset {
    val center = orbitCenter(w, h)
    val maxRadius = min(w, h) * 0.46f
    val radius = maxRadius * (
        0.30f + distance.coerceIn(0, 100) / 100f * 0.70f
    )
    val radians = Math.toRadians(azimuth.toDouble())
    val x = center.x + sin(radians).toFloat() * radius
    val z = cos(radians).toFloat() * radius
    val y = center.y + z * 0.42f - heightShift(h, cameraHeight)
    return Offset(x, y)
}

private fun lensTargetWidth(lens: Lens): Float = when (lens) {
    Lens.MM16 -> 108f
    Lens.MM24 -> 90f
    Lens.MM35 -> 70f
    Lens.MM50 -> 54f
    Lens.MM85 -> 38f
    Lens.MM105 -> 30f
    Lens.MACRO -> 22f
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStudioGrid(
    w: Float,
    h: Float,
    horizon: Float,
    centerX: Float,
    color: Color
) {
    for (i in 0..10) {
        drawLine(
            color,
            Offset(centerX, horizon),
            Offset(w * i / 10f, h),
            strokeWidth = 1f
        )
    }
    for (i in 1..7) {
        val t = i / 7f
        val y = horizon + t * t * (h - horizon)
        drawLine(color, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrbitRing(
    w: Float,
    h: Float,
    color: Color
) {
    val width = min(w * 0.86f, h * 0.78f)
    drawOval(
        color = color,
        topLeft = Offset(w / 2f - width / 2f, h * 0.49f),
        size = Size(width, width * 0.34f),
        style = Stroke(width = 2f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeightRail(
    w: Float,
    h: Float,
    cameraHeight: Int,
    color: Color,
    accent: Color
) {
    val x = w - 28f
    val top = h * 0.18f
    val bottom = h * 0.88f
    drawLine(color, Offset(x, top), Offset(x, bottom), strokeWidth = 3f)
    val y =
        bottom - (bottom - top) * cameraHeight.coerceIn(0, 100) / 100f
    drawCircle(accent, radius = 8f, center = Offset(x, y))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFocusTarget(
    center: Offset,
    color: Color
) {
    drawCircle(color.copy(alpha = 0.20f), radius = 24f, center = center)
    drawCircle(
        color,
        radius = 8f,
        center = center,
        style = Stroke(width = 3f)
    )
    drawLine(
        color,
        center + Offset(-18f, 0f),
        center + Offset(18f, 0f),
        strokeWidth = 2.5f
    )
    drawLine(
        color,
        center + Offset(0f, -18f),
        center + Offset(0f, 18f),
        strokeWidth = 2.5f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRotatingSubject(
    centerX: Float,
    floorY: Float,
    height: Float,
    azimuth: Int,
    color: Color
) {
    val radians = Math.toRadians(azimuth.toDouble())
    val frontFactor = abs(cos(radians)).toFloat()
    val sideFactor = abs(sin(radians)).toFloat()
    val bodyScale = 0.42f + frontFactor * 0.58f
    val depthShift = sin(radians).toFloat() * height * 0.018f

    val head = Offset(
        centerX + depthShift * 0.25f,
        floorY - height * 0.53f
    )
    val shoulderY = floorY - height * 0.43f
    val pelvisY = floorY - height * 0.25f
    val kneeY = floorY - height * 0.115f
    val shoulderHalf = height * 0.075f * bodyScale
    val hipHalf = height * 0.045f * bodyScale
    val legHalf = height * (
        0.045f * bodyScale + 0.010f * sideFactor
    )
    val neck = Offset(
        centerX + depthShift * 0.2f,
        floorY - height * 0.46f
    )
    val pelvis = Offset(centerX, pelvisY)
    val leftKnee = Offset(centerX - legHalf + depthShift, kneeY)
    val rightKnee = Offset(centerX + legHalf - depthShift, kneeY)
    val leftFoot = Offset(
        centerX - legHalf * 1.25f + depthShift * 1.2f,
        floorY
    )
    val rightFoot = Offset(
        centerX + legHalf * 1.25f - depthShift * 1.2f,
        floorY
    )

    drawCircle(color, radius = height * 0.038f, center = head)
    drawLine(color, neck, pelvis, strokeWidth = 8f)
    drawLine(
        color,
        Offset(centerX - shoulderHalf, shoulderY),
        Offset(
            centerX + shoulderHalf,
            shoulderY + depthShift * 0.30f
        ),
        strokeWidth = 7f
    )
    drawLine(
        color,
        Offset(centerX - hipHalf, pelvisY),
        Offset(
            centerX + hipHalf,
            pelvisY + depthShift * 0.18f
        ),
        strokeWidth = 7f
    )
    drawLine(color, pelvis, leftKnee, strokeWidth = 8f)
    drawLine(color, leftKnee, leftFoot, strokeWidth = 8f)
    drawLine(color, pelvis, rightKnee, strokeWidth = 8f)
    drawLine(color, rightKnee, rightFoot, strokeWidth = 8f)
    drawLine(
        color,
        leftFoot,
        leftFoot + Offset(height * 0.050f, 0f),
        strokeWidth = 7f
    )
    drawLine(
        color,
        rightFoot,
        rightFoot + Offset(height * 0.050f, 0f),
        strokeWidth = 7f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCameraGlyph(
    center: Offset,
    color: Color
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - 20f, center.y - 14f),
        size = Size(40f, 28f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.94f),
        radius = 7f,
        center = center
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - 9f, center.y - 21f),
        size = Size(18f, 8f),
        cornerRadius = CornerRadius(3f, 3f)
    )
}
