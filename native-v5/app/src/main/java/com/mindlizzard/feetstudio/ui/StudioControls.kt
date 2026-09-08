package com.mindlizzard.feetstudio.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mindlizzard.feetstudio.StudioViewModel
import com.mindlizzard.feetstudio.domain.*

enum class StudioSection(val label: String) {
    STUDIO("Studio"), HOSIERY("Hosiery"), POSE("Pose"), CAMERA("Camera"), SCENE("Scene"), RENDER("Render")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeControlSheet(
    section: StudioSection,
    viewModel: StudioViewModel,
    workspace: WorkspaceState,
    references: List<ReferenceAsset>,
    onPickReferences: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(section.label, style = MaterialTheme.typography.titleMedium)
                    ModeSwitch(workspace.settings.mode) { viewModel.updateSettings { s -> s.copy(mode = it) } }
                }
                if (section != StudioSection.RENDER) {
                    TextButton(onClick = { shuffleSection(section, viewModel, workspace) }) { Text("↻ Shuffle this section") }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 650.dp),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (section) {
                    StudioSection.STUDIO -> studioItems(viewModel, workspace)
                    StudioSection.HOSIERY -> hosieryItems(viewModel, workspace)
                    StudioSection.POSE -> poseItems(viewModel, workspace)
                    StudioSection.CAMERA -> cameraItems(viewModel, workspace)
                    StudioSection.SCENE -> sceneItems(viewModel, workspace)
                    StudioSection.RENDER -> renderItems(viewModel, workspace, references, onPickReferences)
                }
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }
}

private fun shuffleSection(section: StudioSection, vm: StudioViewModel, workspace: WorkspaceState) {
    val d = workspace.design
    val s = workspace.settings
    when (section) {
        StudioSection.STUDIO -> vm.updateDesign {
            it.copy(
                footShape = if (s.lockFeet) d.footShape else FootShape.entries.random(),
                archType = if (s.lockFeet) d.archType else ArchType.entries.random(),
                skinTone = SkinTone.entries.random(), nailShape = if (s.lockNails) d.nailShape else NailShape.entries.random(),
                nailStyle = if (s.lockNails) d.nailStyle else NailStyle.entries.random(),
                footwearType = if (s.lockShoes) d.footwearType else SceneCatalog.recommendedFootwear(d.scene).random(),
                lighting = LightingPreset.entries.random(), filmStock = FilmStock.entries.random()
            )
        }
        StudioSection.HOSIERY -> if (!s.lockHosiery) vm.updateDesign {
            it.copy(hosieryType = HosieryType.entries.random(), denier = Denier.entries.random(), hosieryPattern = HosieryPattern.entries.random(), hosieryFinish = HosieryFinish.entries.random())
        }
        StudioSection.POSE -> if (!s.lockPose) vm.updateDesign { it.copy(pose = SceneCatalog.recommendedPoses(d.scene).random(), customPose = "") }
        StudioSection.CAMERA -> if (!s.lockCamera) vm.updateDesign { it.copy(cameraAngle = CameraAngle.entries.random(), lens = Lens.entries.random(), customCamera = "") }
        StudioSection.SCENE -> if (!s.lockScene) {
            val scene = SceneType.entries.random()
            vm.updateDesign { it.copy(scene = scene, surface = SceneCatalog.defaultSurface(scene), customPose = "", customCamera = "") }
        }
        StudioSection.RENDER -> Unit
    }
}

@Composable
private fun ModeSwitch(mode: StudioMode, onChange: (StudioMode) -> Unit) {
    Row {
        FilterChip(selected = mode == StudioMode.SIMPLE, onClick = { onChange(StudioMode.SIMPLE) }, label = { Text("Simple") })
        Spacer(Modifier.width(6.dp))
        FilterChip(selected = mode == StudioMode.PRO, onClick = { onChange(StudioMode.PRO) }, label = { Text("Pro") })
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.studioItems(vm: StudioViewModel, workspace: WorkspaceState) {
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
        item { IntSlider("Natural asymmetry", d.footAsymmetry) { v -> vm.updateDesign { it.copy(footAsymmetry = v) } } }
    }
    item { LockToggle("Lock feet", workspace.settings.lockFeet) { v -> vm.updateSettings { it.copy(lockFeet = v) } } }

    item { SectionTitle("Skin") }
    item { EnumChips("Tone", SkinTone.entries, d.skinTone, { it.label }) { v -> vm.updateDesign { it.copy(skinTone = v) } } }
    item { EnumChips("Texture", SkinTexture.entries, d.skinTexture, { it.label }) { v -> vm.updateDesign { it.copy(skinTexture = v) } } }
    item { EnumChips("Undertone", SkinUndertone.entries, d.skinUndertone, { it.label }) { v -> vm.updateDesign { it.copy(skinUndertone = v) } } }
    item { IntSlider("Model age", d.modelAge, 18, 80) { v -> vm.updateDesign { it.copy(modelAge = v) } } }
    if (pro) {
        item { IntSlider("Pores", d.skinPores) { v -> vm.updateDesign { it.copy(skinPores = v) } } }
        item { IntSlider("Veins", d.skinVeins) { v -> vm.updateDesign { it.copy(skinVeins = v) } } }
        item { IntSlider("Dryness", d.skinDryness) { v -> vm.updateDesign { it.copy(skinDryness = v) } } }
        item { IntSlider("Redness", d.skinRedness) { v -> vm.updateDesign { it.copy(skinRedness = v) } } }
        item { IntSlider("Moisture / shine", d.skinMoisture) { v -> vm.updateDesign { it.copy(skinMoisture = v) } } }
    }

    item { SectionTitle("Nails") }
    item { EnumChips("Shape", NailShape.entries, d.nailShape, { it.label }) { v -> vm.updateDesign { it.copy(nailShape = v) } } }
    item { EnumChips("Nail art", NailStyle.entries, d.nailStyle, { it.label }) { v -> vm.updateDesign { it.copy(nailStyle = v) } } }
    item { EnumChips("Finish", NailFinish.entries, d.nailFinish, { it.label }) { v -> vm.updateDesign { it.copy(nailFinish = v) } } }
    item { EnumChips("Length", NailLength.entries, d.nailLength, { it.label }) { v -> vm.updateDesign { it.copy(nailLength = v) } } }
    item { NamedColorPicker("Nail color", d.nailColor, ColorCatalog.nails, pro) { v -> vm.updateDesign { it.copy(nailColor = v) } } }
    item { LockToggle("Lock nails", workspace.settings.lockNails) { v -> vm.updateSettings { it.copy(lockNails = v) } } }

    item { SectionTitle("Shoes & accessories") }
    item { EnumChips("Footwear", FootwearType.entries, d.footwearType, { it.label }) { v -> vm.updateDesign { it.copy(footwearType = v) } } }
    if (d.footwearType != FootwearType.NONE) {
        item { EnumChips("State", FootwearState.entries, d.footwearState, { it.label }) { v -> vm.updateDesign { it.copy(footwearState = v) } } }
        item { NamedColorPicker("Shoe color", d.footwearColor, ColorCatalog.footwear, pro) { v -> vm.updateDesign { it.copy(footwearColor = v) } } }
    }
    item { EnumChips("Accessory / tattoo", AccessoryType.entries, d.accessory, { it.label }) { v -> vm.updateDesign { it.copy(accessory = v) } } }
    item { LockToggle("Lock shoes", workspace.settings.lockShoes) { v -> vm.updateSettings { it.copy(lockShoes = v) } } }

    item { SectionTitle("Light & film") }
    item { EnumChips("Lighting preset", LightingPreset.entries, d.lighting, { it.label }) { v -> vm.updateDesign { it.copy(lighting = v) } } }
    item { EnumChips("Film look", FilmStock.entries, d.filmStock, { it.label }) { v -> vm.updateDesign { it.copy(filmStock = v) } } }
    if (pro) {
        item { IntSlider("Intensity", d.lightIntensity) { v -> vm.updateDesign { it.copy(lightIntensity = v) } } }
        item { IntSlider("Softness", d.lightSoftness) { v -> vm.updateDesign { it.copy(lightSoftness = v) } } }
        item { IntSlider("Temperature", d.lightTemperature) { v -> vm.updateDesign { it.copy(lightTemperature = v) } } }
        item { IntSlider("Contrast", d.lightContrast) { v -> vm.updateDesign { it.copy(lightContrast = v) } } }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.hosieryItems(vm: StudioViewModel, workspace: WorkspaceState) {
    val d = workspace.design
    val pro = workspace.settings.mode == StudioMode.PRO
    item { EnumChips("Type", HosieryType.entries, d.hosieryType, { it.label }) { v -> vm.updateDesign { it.copy(hosieryType = v) } } }
    if (d.hosieryType != HosieryType.NONE) {
        if (d.hosieryType != HosieryType.FISHNET) item { EnumChips("Denier", Denier.entries, d.denier, { it.label }) { v -> vm.updateDesign { it.copy(denier = v) } } }
        item { EnumChips("Pattern", HosieryPattern.entries, d.hosieryPattern, { it.label }) { v -> vm.updateDesign { it.copy(hosieryPattern = v) } } }
        item { NamedColorPicker("Color", d.hosieryColor, ColorCatalog.hosiery, pro) { v -> vm.updateDesign { it.copy(hosieryColor = v) } } }
        item { EnumChips("Finish", HosieryFinish.entries, d.hosieryFinish, { it.name.lowercase() }) { v -> vm.updateDesign { it.copy(hosieryFinish = v) } } }
        if (d.hosieryType == HosieryType.FISHNET) {
            item { SectionTitle("Fishnet physics") }
            item { EnumChips("Mesh", MeshSize.entries, d.meshSize, { it.name.lowercase() }) { v -> vm.updateDesign { it.copy(meshSize = v) } } }
            item { IntSlider("Thread thickness", d.meshThickness) { v -> vm.updateDesign { it.copy(meshThickness = v) } } }
        }
        if (pro) {
            item { SectionTitle("Fabric physics") }
            item { IntSlider("Tension", d.hosieryTension) { v -> vm.updateDesign { it.copy(hosieryTension = v) } } }
            item { IntSlider("Compression", d.hosieryCompression) { v -> vm.updateDesign { it.copy(hosieryCompression = v) } } }
            item { IntSlider("Wrinkles", d.hosieryWrinkles) { v -> vm.updateDesign { it.copy(hosieryWrinkles = v) } } }
            item { SwitchRow("Worn fabric", d.wornKnit) { v -> vm.updateDesign { it.copy(wornKnit = v) } } }
        }
    }
    item { LockToggle("Lock hosiery", workspace.settings.lockHosiery) { v -> vm.updateSettings { it.copy(lockHosiery = v) } } }
}

private fun androidx.compose.foundation.lazy.LazyListScope.poseItems(vm: StudioViewModel, workspace: WorkspaceState) {
    val d = workspace.design
    item { EnumChips("Pose", PoseType.entries, d.pose, { it.label }) { v -> vm.updateDesign { it.copy(pose = v, customPose = "") } } }
    if (workspace.settings.mode == StudioMode.PRO) item { MultiLineTextEntry("Custom pose instruction", d.customPose, "Optional precise pose instruction…") { v -> vm.updateDesign { it.copy(customPose = v) } } }
    item { LockToggle("Lock pose", workspace.settings.lockPose) { v -> vm.updateSettings { it.copy(lockPose = v) } } }
}

private fun androidx.compose.foundation.lazy.LazyListScope.cameraItems(vm: StudioViewModel, workspace: WorkspaceState) {
    val d = workspace.design
    val pro = workspace.settings.mode == StudioMode.PRO
    item { EnumChips("Angle", CameraAngle.entries, d.cameraAngle, { it.label }) { v -> vm.updateDesign { it.copy(cameraAngle = v, customCamera = "") } } }
    item { EnumChips("Lens", Lens.entries, d.lens, { it.label }) { v -> vm.updateDesign { it.copy(lens = v) } } }
    item { EnumChips("Aspect ratio", AspectRatio.entries, workspace.settings.aspectRatio, { it.apiValue }) { v -> vm.updateSettings { it.copy(aspectRatio = v) } } }
    if (pro) {
        item { IntSlider("Distance", d.cameraDistance) { v -> vm.updateDesign { it.copy(cameraDistance = v) } } }
        item { IntSlider("Height", d.cameraHeight) { v -> vm.updateDesign { it.copy(cameraHeight = v) } } }
        item { IntSlider("Tilt", d.cameraTilt, -45, 45) { v -> vm.updateDesign { it.copy(cameraTilt = v) } } }
        item { IntSlider("Roll", d.cameraRoll, -45, 45) { v -> vm.updateDesign { it.copy(cameraRoll = v) } } }
        item { IntSlider("Depth of field", d.depthOfField) { v -> vm.updateDesign { it.copy(depthOfField = v) } } }
        item { MultiLineTextEntry("Custom camera instruction", d.customCamera, "Optional framing / lens instruction…") { v -> vm.updateDesign { it.copy(customCamera = v) } } }
    }
    item { LockToggle("Lock camera", workspace.settings.lockCamera) { v -> vm.updateSettings { it.copy(lockCamera = v) } } }
}

private fun androidx.compose.foundation.lazy.LazyListScope.sceneItems(vm: StudioViewModel, workspace: WorkspaceState) {
    val pro = workspace.settings.mode == StudioMode.PRO
    item { ScenePicker(workspace.design.scene) { selected -> vm.updateDesign { it.copy(scene = selected, surface = SceneCatalog.defaultSurface(selected), customPose = "", customCamera = "") } } }
    item { SurfacePicker(workspace.design.scene, workspace.design.surface, pro) { v -> vm.updateDesign { it.copy(surface = v) } } }
    item { LockToggle("Lock scene", workspace.settings.lockScene) { v -> vm.updateSettings { it.copy(lockScene = v) } } }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderItems(vm: StudioViewModel, workspace: WorkspaceState, references: List<ReferenceAsset>, onPickReferences: () -> Unit) {
    item {
        EnumChips(
            "Quality profile",
            QualityProfile.entries,
            workspace.settings.qualityProfile,
            { it.label }
        ) { value ->
            vm.updateSettings {
                it.copy(qualityProfile = value)
            }
        }
    }

    item {
        SwitchRow(
            "Anatomy guard",
            workspace.settings.anatomyGuard
        ) { value ->
            vm.updateSettings {
                it.copy(anatomyGuard = value)
            }
        }
    }

    item {
        EnumChips(
            "Image engine",
            ImageEngine.entries,
            workspace.settings.imageEngine,
            { it.label }
        ) { value ->
            vm.updateSettings {
                it.copy(imageEngine = value)
            }
        }
    }

    if (workspace.settings.imageEngine == ImageEngine.HF_FREE) {
        item { SectionTitle("Hugging Face Free") }

        item {
            Text(
                "Gebruikt Hugging Face Inference Providers met de gratis maandcredits van je HF-account. " +
                    "Om je gratis saldo niet per ongeluk op te eten, maakt deze engine altijd één afbeelding per druk op Generate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item { HuggingFaceTokenEditor(vm) }

        item { ZeroGpuLoraLabLink() }
    }

    if (workspace.settings.imageEngine == ImageEngine.FLUX_HOSIERY) {
        item { SectionTitle("FLUX Hosiery Lab") }

        item {
            EnumChips(
                "Hosiery LoRA",
                HosieryLoraPreset.entries,
                workspace.settings.hosieryLoraPreset,
                { it.label }
            ) { value ->
                vm.updateSettings {
                    it.copy(hosieryLoraPreset = value)
                }
            }
        }

        item {
            IntSlider(
                "LoRA strength",
                workspace.settings.hosieryLoraWeight,
                0,
                100
            ) { value ->
                vm.updateSettings {
                    it.copy(hosieryLoraWeight = value)
                }
            }
        }

        item {
            Text(
                "Experimenteel: FLUX.1-dev + gespecialiseerde hosiery LoRA via MuAPI. References worden in deze eerste versie niet meegestuurd.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item { MuapiKeyEditor(vm) }
    }

    if (workspace.settings.qualityProfile == QualityProfile.ULTRA) {
        item {
            Text(
                "Ultra gebruikt Pro + 4K en daarna een tweede refinement-pass. Dit kost ongeveer twee image-calls per render.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else if (workspace.settings.qualityProfile == QualityProfile.AURA) {
        item {
            Text(
                "Aura geeft strengere anatomie, scherper materiaal-detail en natuurlijkere fotografie zonder extra tweede pass.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    item { EnumChips("Mode", RenderMode.entries, workspace.settings.renderMode, { it.name.lowercase() }) { v -> vm.updateSettings { it.copy(renderMode = v, resolution = if (v == RenderMode.PRO) Resolution.K4 else it.resolution) } } }
    item { EnumChips("Resolution", Resolution.entries, workspace.settings.resolution, { it.name.replace("K", "") + "K" }) { v -> vm.updateSettings { it.copy(resolution = v) } } }
    item { EnumChips("Resolver", ResolverMode.entries, workspace.settings.resolverMode, { it.name.lowercase() }) { v -> vm.updateSettings { it.copy(resolverMode = v) } } }
    item { EnumChips("Priority", DetailPriority.entries, workspace.settings.detailPriority, { it.name.lowercase() }) { v -> vm.updateSettings { it.copy(detailPriority = v) } } }
    item { EnumChips("Batch", listOf(1, 2, 4), workspace.settings.batchCount, { "×$it" }) { v -> vm.updateSettings { it.copy(batchCount = v) } } }
    if (workspace.settings.mode == StudioMode.PRO) item { IntSlider("Variation strength", workspace.settings.variationStrength) { v -> vm.updateSettings { it.copy(variationStrength = v) } } }

    item { SectionTitle("References") }
    item { OutlinedButton(onClick = onPickReferences, modifier = Modifier.fillMaxWidth()) { Text("Add references (${references.size}/5)") } }
    if (references.isNotEmpty()) {
        items(references.indices.toList()) { index ->
            ReferenceEditor(index, references[index],
                onRole = { vm.updateReferenceRole(index, it) },
                onStrength = { vm.updateReferenceStrength(index, it) },
                onEnabled = { vm.updateReferenceEnabled(index, it) },
                onRemove = { vm.removeReference(index) })
        }
        item { TextButton(onClick = vm::clearReferences, modifier = Modifier.fillMaxWidth()) { Text("Clear all references") } }
    }
    item { SectionTitle("Gemini API key / Targeted Fix") }
    item { ApiKeyEditor(vm) }
}

@Composable
private fun ReferenceEditor(index: Int, asset: ReferenceAsset, onRole: (ReferenceRole) -> Unit, onStrength: (ReferenceStrength) -> Unit, onEnabled: (Boolean) -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Reference ${index + 1}", style = MaterialTheme.typography.titleSmall)
                Switch(checked = asset.enabled, onCheckedChange = onEnabled)
            }
            Text(asset.uri.lastPathSegment ?: asset.uri.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            EnumChips("Role", ReferenceRole.entries, asset.role, { it.label }, onRole)
            EnumChips("Strength", ReferenceStrength.entries, asset.strength, { it.label }, onStrength)
            TextButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) { Text("Remove") }
        }
    }
}

@Composable
private fun HuggingFaceTokenEditor(vm: StudioViewModel) {
    var value by remember {
        mutableStateOf(
            vm.huggingFaceTokenForEditor()
        )
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = {
                Text("HF token")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                vm.saveHuggingFaceToken(value)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save HF token")
        }

        Text(
            "Maak op Hugging Face een token met Inference Providers-toegang. " +
                "De token wordt encrypted opgeslagen in Android Keystore.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ZeroGpuLoraLabLink() {
    val uriHandler =
        LocalUriHandler.current

    Column(
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        SectionTitle(
            "Echt gratis ZeroGPU panty-LoRA"
        )

        Text(
            "Voor de gespecialiseerde Sheer 15D LoRA kun je de openbare ZeroGPU FLUX LoRA Lab gebruiken. " +
                "Custom LoRA: Muapi/sheer-tights-pantyhose. Trigger: 15tights. " +
                "Deze route draait buiten Feet Studio en gebruikt je dagelijkse ZeroGPU-quota.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = {
                uriHandler.openUri(
                    "https://huggingface.co/spaces/multimodalart/flux-lora-lab"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open gratis ZeroGPU LoRA Lab")
        }
    }
}

@Composable
private fun MuapiKeyEditor(vm: StudioViewModel) {
    var value by remember { mutableStateOf(vm.muapiKeyForEditor()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("MuAPI key") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.saveMuapiKey(value) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save MuAPI key")
        }

        Text(
            "Encrypted lokaal. Nodig voor FLUX Hosiery Lab via MuAPI.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ApiKeyEditor(vm: StudioViewModel) {
    var value by remember { mutableStateOf(vm.apiKeyForEditor()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("API key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.saveApiKey(value) }, modifier = Modifier.fillMaxWidth()) { Text("Save in Android Keystore") }
        Text("Encrypted locally. Plak alleen de Gemini API-key zelf, op één regel.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun SectionTitle(text: String) { Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }

@Composable
private fun IntSlider(label: String, value: Int, min: Int = 0, max: Int = 100, onChange: (Int) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, style = MaterialTheme.typography.labelMedium); Text(value.toString(), style = MaterialTheme.typography.labelMedium) }
        Slider(value = value.toFloat(), onValueChange = { onChange(it.toInt()) }, valueRange = min.toFloat()..max.toFloat())
    }
}

@Composable
private fun FloatSlider(label: String, value: Float, min: Float, max: Float, step: Float, onChange: (Float) -> Unit) {
    val steps = (((max - min) / step).toInt() - 1).coerceAtLeast(0)
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, style = MaterialTheme.typography.labelMedium); Text("%.1f".format(value), style = MaterialTheme.typography.labelMedium) }
        Slider(value = value, onValueChange = onChange, valueRange = min..max, steps = steps)
    }
}

@Composable private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Switch(checked = checked, onCheckedChange = onChange) }
}

@Composable
private fun MultiLineTextEntry(label: String, value: String, placeholder: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, placeholder = { Text(placeholder) }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6)
}

@Composable
private fun LockToggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    FilledTonalButton(onClick = { onChange(!value) }, modifier = Modifier.fillMaxWidth()) {
        Icon(if (value) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = null); Spacer(Modifier.width(8.dp)); Text(label)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun NamedColorPicker(label: String, value: String, colors: List<NamedColor>, showCustom: Boolean, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            colors.forEach { option ->
                val swatch = runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(option.hex)) }.getOrDefault(MaterialTheme.colorScheme.surfaceVariant)
                FilterChip(selected = option.hex.equals(value, true), onClick = { onChange(option.hex) },
                    leadingIcon = { Surface(modifier = Modifier.size(16.dp), shape = CircleShape, color = swatch, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))) {} },
                    label = { Text(option.label) })
            }
        }
        val selected = colors.firstOrNull { it.hex.equals(value, true) }
        Text("Selected: ${selected?.label ?: "Custom"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (showCustom) OutlinedTextField(value = value, onValueChange = onChange, label = { Text("Custom HEX (advanced)") }, supportingText = { Text("Example: #111111") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ScenePicker(scene: SceneType, onChange: (SceneType) -> Unit) {
    var category by remember(scene) { mutableStateOf(SceneCatalog.categoryOf(scene)) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Environment", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(end = 12.dp)) {
            items(SceneCategory.entries) { item -> FilterChip(selected = item == category, onClick = { category = item }, label = { Text(item.label) }) }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            SceneCatalog.scenesIn(category).forEach { option -> FilterChip(selected = option == scene, onClick = { onChange(option) }, label = { Text(option.label) }) }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SurfacePicker(scene: SceneType, surface: String, pro: Boolean, onChange: (String) -> Unit) {
    val choices = SceneCatalog.surfacesFor(scene)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Surface / interaction", style = MaterialTheme.typography.labelMedium)
        Text("Suggestions automatically match ${scene.label}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            choices.forEach { option -> FilterChip(selected = option.prompt.equals(surface, true), onClick = { onChange(option.prompt) }, label = { Text(option.label) }) }
        }
        if (pro) OutlinedTextField(value = surface, onValueChange = onChange, label = { Text("Custom surface / interaction") }, supportingText = { Text("Pro only. Presets are safer for scene consistency.") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun <T> EnumChips(label: String, values: List<T>, selected: T, text: (T) -> String, onChange: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            values.forEach { value -> FilterChip(selected = value == selected, onClick = { onChange(value) }, label = { Text(text(value)) }) }
        }
    }
}
