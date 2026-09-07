package com.mindlizzard.feetstudio.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mindlizzard.feetstudio.StudioViewModel
import com.mindlizzard.feetstudio.domain.*

enum class StudioSection(val label: String) {
    STUDIO("Studio"), HOSIERY("Hosiery"), POSE("Pose"),
    CAMERA("Camera"), SCENE("Scene"), RENDER("Render")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeControlSheet(
    section: StudioSection,
    viewModel: StudioViewModel,
    workspace: WorkspaceState,
    referenceCount: Int,
    onPickReferences: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(section.label, style = MaterialTheme.typography.titleMedium)
                ModeSwitch(workspace.settings.mode) {
                    viewModel.updateSettings { s -> s.copy(mode = it) }
                }
            }

            // Critical difference versus the old WebView UI:
            // every menu is a native LazyColumn and scrolls independently.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 620.dp),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (section) {
                    StudioSection.STUDIO -> studioItems(viewModel, workspace)
                    StudioSection.HOSIERY -> hosieryItems(viewModel, workspace)
                    StudioSection.POSE -> poseItems(viewModel, workspace)
                    StudioSection.CAMERA -> cameraItems(viewModel, workspace)
                    StudioSection.SCENE -> sceneItems(viewModel, workspace)
                    StudioSection.RENDER -> renderItems(viewModel, workspace, referenceCount, onPickReferences)
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun ModeSwitch(mode: StudioMode, onChange: (StudioMode) -> Unit) {
    Row {
        FilterChip(
            selected = mode == StudioMode.SIMPLE,
            onClick = { onChange(StudioMode.SIMPLE) },
            label = { Text("Simple") }
        )
        Spacer(Modifier.width(6.dp))
        FilterChip(
            selected = mode == StudioMode.PRO,
            onClick = { onChange(StudioMode.PRO) },
            label = { Text("Pro") }
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.studioItems(
    vm: StudioViewModel,
    workspace: WorkspaceState
) {
    val d = workspace.design
    val pro = workspace.settings.mode == StudioMode.PRO

    item { SectionTitle("Feet") }
    item { EnumChips("Foot shape", FootShape.entries, d.footShape, { it.label }) { v -> vm.updateDesign { it.copy(footShape = v) } } }
    item { EnumChips("Arch", ArchType.entries, d.archType, { it.label }) { v -> vm.updateDesign { it.copy(archType = v) } } }
    item { FloatSlider("EU shoe size", d.shoeSize, 35f, 45f, 0.5f) { v -> vm.updateDesign { it.copy(shoeSize = v) } } }
    if (pro) {
        item { IntSlider("Foot width", d.footWidth) { v -> vm.updateDesign { it.copy(footWidth = v) } } }
        item { IntSlider("Heel width", d.heelWidth) { v -> vm.updateDesign { it.copy(heelWidth = v) } } }
        item { IntSlider("Instep", d.instep) { v -> vm.updateDesign { it.copy(instep = v) } } }
        item { IntSlider("Toe spread", d.toeSpread) { v -> vm.updateDesign { it.copy(toeSpread = v) } } }
        item { IntSlider("Toe length", d.toeLength) { v -> vm.updateDesign { it.copy(toeLength = v) } } }
    }

    item { SectionTitle("Skin") }
    item { EnumChips("Tone", SkinTone.entries, d.skinTone, { it.label }) { v -> vm.updateDesign { it.copy(skinTone = v) } } }
    item { EnumChips("Texture", SkinTexture.entries, d.skinTexture, { it.label }) { v -> vm.updateDesign { it.copy(skinTexture = v) } } }
    item { IntSlider("Age", d.modelAge, 18, 80) { v -> vm.updateDesign { it.copy(modelAge = v) } } }
    if (pro) {
        item { IntSlider("Pores", d.skinPores) { v -> vm.updateDesign { it.copy(skinPores = v) } } }
        item { IntSlider("Veins", d.skinVeins) { v -> vm.updateDesign { it.copy(skinVeins = v) } } }
        item { IntSlider("Dryness", d.skinDryness) { v -> vm.updateDesign { it.copy(skinDryness = v) } } }
    }

    item { SectionTitle("Nails") }
    item { EnumChips("Shape", NailShape.entries, d.nailShape, { it.label }) { v -> vm.updateDesign { it.copy(nailShape = v) } } }
    item { EnumChips("Style", NailStyle.entries, d.nailStyle, { it.label }) { v -> vm.updateDesign { it.copy(nailStyle = v) } } }
    item { TextEntry("Nail color", d.nailColor) { v -> vm.updateDesign { it.copy(nailColor = v) } } }

    item { SectionTitle("Shoes") }
    item { EnumChips("Footwear", FootwearType.entries, d.footwearType, { it.label }) { v -> vm.updateDesign { it.copy(footwearType = v) } } }
    if (d.footwearType != FootwearType.NONE) {
        item { EnumChips("State", FootwearState.entries, d.footwearState, { it.label }) { v -> vm.updateDesign { it.copy(footwearState = v) } } }
        item { TextEntry("Shoe color", d.footwearColor) { v -> vm.updateDesign { it.copy(footwearColor = v) } } }
    }

    item { SectionTitle("Light") }
    item { EnumChips("Preset", LightingPreset.entries, d.lighting, { it.label }) { v -> vm.updateDesign { it.copy(lighting = v) } } }
    if (pro) {
        item { IntSlider("Softness", d.lightSoftness) { v -> vm.updateDesign { it.copy(lightSoftness = v) } } }
        item { IntSlider("Intensity", d.lightIntensity) { v -> vm.updateDesign { it.copy(lightIntensity = v) } } }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.hosieryItems(
    vm: StudioViewModel,
    workspace: WorkspaceState
) {
    val d = workspace.design
    val pro = workspace.settings.mode == StudioMode.PRO

    item { EnumChips("Type", HosieryType.entries, d.hosieryType, { it.label }) { v -> vm.updateDesign { it.copy(hosieryType = v) } } }
    if (d.hosieryType != HosieryType.NONE) {
        if (d.hosieryType != HosieryType.FISHNET) {
            item { EnumChips("Denier", Denier.entries, d.denier, { it.label }) { v -> vm.updateDesign { it.copy(denier = v) } } }
        }
        item { TextEntry("Color", d.hosieryColor) { v -> vm.updateDesign { it.copy(hosieryColor = v) } } }
        item { EnumChips("Finish", HosieryFinish.entries, d.hosieryFinish, { it.name.lowercase() }) { v -> vm.updateDesign { it.copy(hosieryFinish = v) } } }

        if (d.hosieryType == HosieryType.FISHNET) {
            item { EnumChips("Mesh", MeshSize.entries, d.meshSize, { it.name.lowercase() }) { v -> vm.updateDesign { it.copy(meshSize = v) } } }
            item { IntSlider("Thread thickness", d.meshThickness) { v -> vm.updateDesign { it.copy(meshThickness = v) } } }
        }
        if (pro) {
            item { IntSlider("Tension", d.hosieryTension) { v -> vm.updateDesign { it.copy(hosieryTension = v) } } }
            item { IntSlider("Wrinkles", d.hosieryWrinkles) { v -> vm.updateDesign { it.copy(hosieryWrinkles = v) } } }
        }
    }
    item { LockToggle("Lock hosiery", workspace.settings.lockHosiery) { v -> vm.updateSettings { it.copy(lockHosiery = v) } } }
}

private fun androidx.compose.foundation.lazy.LazyListScope.poseItems(
    vm: StudioViewModel,
    workspace: WorkspaceState
) {
    item { EnumChips("Pose", PoseType.entries, workspace.design.pose, { it.label }) { v -> vm.updateDesign { it.copy(pose = v) } } }
    item { LockToggle("Lock pose", workspace.settings.lockPose) { v -> vm.updateSettings { it.copy(lockPose = v) } } }
    item {
        Text(
            "Pose locks are part of the native workspace and are preserved when later variation tools are added.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.cameraItems(
    vm: StudioViewModel,
    workspace: WorkspaceState
) {
    val d = workspace.design
    val pro = workspace.settings.mode == StudioMode.PRO

    item { EnumChips("Angle", CameraAngle.entries, d.cameraAngle, { it.label }) { v -> vm.updateDesign { it.copy(cameraAngle = v) } } }
    item { EnumChips("Lens", Lens.entries, d.lens, { it.label }) { v -> vm.updateDesign { it.copy(lens = v) } } }
    item { EnumChips("Aspect ratio", AspectRatio.entries, workspace.settings.aspectRatio, { it.apiValue }) { v -> vm.updateSettings { it.copy(aspectRatio = v) } } }
    if (pro) {
        item { IntSlider("Distance", d.cameraDistance) { v -> vm.updateDesign { it.copy(cameraDistance = v) } } }
        item { IntSlider("Height", d.cameraHeight) { v -> vm.updateDesign { it.copy(cameraHeight = v) } } }
        item { IntSlider("Tilt", d.cameraTilt, -45, 45) { v -> vm.updateDesign { it.copy(cameraTilt = v) } } }
        item { IntSlider("Depth of field", d.depthOfField) { v -> vm.updateDesign { it.copy(depthOfField = v) } } }
    }
    item { LockToggle("Lock camera", workspace.settings.lockCamera) { v -> vm.updateSettings { it.copy(lockCamera = v) } } }
}

private fun androidx.compose.foundation.lazy.LazyListScope.sceneItems(
    vm: StudioViewModel,
    workspace: WorkspaceState
) {
    item { EnumChips("Environment", SceneType.entries, workspace.design.scene, { it.label }) { v -> vm.updateDesign { it.copy(scene = v) } } }
    item { TextEntry("Surface / interaction", workspace.design.surface) { v -> vm.updateDesign { it.copy(surface = v) } } }
    item { LockToggle("Lock scene", workspace.settings.lockScene) { v -> vm.updateSettings { it.copy(lockScene = v) } } }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderItems(
    vm: StudioViewModel,
    workspace: WorkspaceState,
    referenceCount: Int,
    onPickReferences: () -> Unit
) {
    item { EnumChips("Mode", RenderMode.entries, workspace.settings.renderMode, { it.name.lowercase() }) { v ->
        vm.updateSettings {
            it.copy(
                renderMode = v,
                resolution = if (v == RenderMode.PRO) Resolution.K4 else it.resolution
            )
        }
    } }
    item { EnumChips("Resolution", Resolution.entries, workspace.settings.resolution, { it.name.replace("K", "") + "K" }) { v -> vm.updateSettings { it.copy(resolution = v) } } }
    item { EnumChips("Resolver", ResolverMode.entries, workspace.settings.resolverMode, { it.name.lowercase() }) { v -> vm.updateSettings { it.copy(resolverMode = v) } } }
    item { EnumChips("Priority", DetailPriority.entries, workspace.settings.detailPriority, { it.name.lowercase() }) { v -> vm.updateSettings { it.copy(detailPriority = v) } } }
    item { EnumChips("Batch", listOf(1, 2, 4), workspace.settings.batchCount, { "×$it" }) { v -> vm.updateSettings { it.copy(batchCount = v) } } }

    item { SectionTitle("References") }
    item {
        OutlinedButton(onClick = onPickReferences, modifier = Modifier.fillMaxWidth()) {
            Text("Add references ($referenceCount/5)")
        }
    }
    if (referenceCount > 0) {
        item {
            TextButton(onClick = { vm.clearReferences() }, modifier = Modifier.fillMaxWidth()) {
                Text("Clear references")
            }
        }
    }

    item { SectionTitle("Gemini API key") }
    item { ApiKeyEditor(vm) }
}

@Composable
private fun ApiKeyEditor(vm: StudioViewModel) {
    var value by remember { mutableStateOf(vm.apiKeyForEditor()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("API key") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { vm.saveApiKey(value) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save in Android Keystore")
        }
        Text(
            "De key wordt versleuteld op dit toestel en zit niet hardcoded in de APK.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun IntSlider(
    label: String,
    value: Int,
    min: Int = 0,
    max: Int = 100,
    onChange: (Int) -> Unit
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value.toString(), style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat()
        )
    }
}

@Composable
private fun FloatSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    step: Float,
    onChange: (Float) -> Unit
) {
    val steps = (((max - min) / step).toInt() - 1).coerceAtLeast(0)
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("%.1f".format(value), style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = min..max,
            steps = steps
        )
    }
}

@Composable
private fun TextEntry(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun LockToggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    FilledTonalButton(onClick = { onChange(!value) }, modifier = Modifier.fillMaxWidth()) {
        Icon(if (value) Icons.Default.Lock else Icons.Default.LockOpen, null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun <T> EnumChips(
    label: String,
    values: List<T>,
    selected: T,
    text: (T) -> String,
    onChange: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            values.forEach { value ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onChange(value) },
                    label = { Text(text(value)) }
                )
            }
        }
    }
}
