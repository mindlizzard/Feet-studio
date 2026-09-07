package com.mindlizzard.feetstudio.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindlizzard.feetstudio.StudioViewModel
import com.mindlizzard.feetstudio.domain.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeetStudioApp(viewModel: StudioViewModel) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var sheet by remember { mutableStateOf<StudioSection?>(null) }
    var promptInspector by remember { mutableStateOf(false) }
    var fixMenu by remember { mutableStateOf(false) }

    val refPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.take(5).forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        viewModel.addReferences(uris.take(5).map { ReferenceAsset(uri = it) })
    }

    val contract = remember(ui.workspace, ui.references) { viewModel.previewContract() }
    val bitmap = rememberFileBitmap(ui.active?.imagePath)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Feet Studio", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "v5 Native · ${contract.model}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::undo) { Icon(Icons.Default.Undo, "Undo") }
                    IconButton(onClick = viewModel::redo) { Icon(Icons.Default.Redo, "Redo") }
                    IconButton(onClick = { promptInspector = true }) { Icon(Icons.Default.Tune, "Prompt inspector") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                StudioSection.entries.forEach { section ->
                    val icon = when (section) {
                        StudioSection.STUDIO -> Icons.Default.AutoAwesome
                        StudioSection.HOSIERY -> Icons.Default.Texture
                        StudioSection.POSE -> Icons.Default.AccessibilityNew
                        StudioSection.CAMERA -> Icons.Default.PhotoCamera
                        StudioSection.SCENE -> Icons.Default.Landscape
                        StudioSection.RENDER -> Icons.Default.Settings
                    }
                    NavigationBarItem(
                        selected = sheet == section,
                        onClick = { sheet = section },
                        icon = { Icon(icon, null) },
                        label = { Text(section.label, maxLines = 1) }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::generate,
                expanded = true,
                icon = { Icon(Icons.Default.AutoAwesome, null) },
                text = { Text("Generate") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated render",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )

                    Row(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = { shareRender(context, ui.active?.imagePath) }
                        ) { Icon(Icons.Default.Share, null) }

                        SmallFloatingActionButton(
                            onClick = { fixMenu = !fixMenu }
                        ) { Icon(Icons.Default.Build, null) }
                    }

                    if (fixMenu) {
                        Card(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 70.dp, end = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                            )
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                FixTarget.entries.forEach { target ->
                                    TextButton(
                                        onClick = {
                                            fixMenu = false
                                            viewModel.targetedFix(target)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Fix ${target.name.lowercase()}")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))
                        Text("Native studio ready")
                        Text(
                            "Open een onderdeel onderaan en bouw je render.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (ui.loading) {
                    Surface(
                        Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.86f)
                    ) {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(14.dp))
                            Text(ui.progress.ifBlank { "Rendering…" })
                            Text(
                                "Kotlin + Compose · geen WebView",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            ReadinessBar(contract) { promptInspector = true }
            NativeGallery(ui.gallery, ui.active, viewModel::select)
        }
    }

    sheet?.let { section ->
        NativeControlSheet(
            section = section,
            viewModel = viewModel,
            workspace = ui.workspace,
            referenceCount = ui.references.size,
            onPickReferences = { refPicker.launch(arrayOf("image/*")) },
            onDismiss = { sheet = null }
        )
    }

    if (promptInspector) {
        PromptInspectorDialog(
            contract = contract,
            onDismiss = { promptInspector = false }
        )
    }

    ui.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) { Text("OK") }
            },
            title = { Text("Feet Studio") },
            text = { Text(error) }
        )
    }
}

@Composable
private fun ReadinessBar(contract: RenderContract, onClick: () -> Unit) {
    val warnings = contract.decisions.count { !it.applied }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (warnings == 0) "✓ Render ready" else "⚠ $warnings warning(s)",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "${contract.imageSize} · ${contract.aspectRatio}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NativeGallery(
    gallery: List<RenderRecord>,
    active: RenderRecord?,
    onSelect: (RenderRecord) -> Unit
) {
    if (gallery.isEmpty()) return
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(gallery.take(40), key = { it.id }) { record ->
            val thumb = rememberFileBitmap(record.imagePath, 240)
            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .clickable { onSelect(record) },
                shape = RoundedCornerShape(12.dp),
                tonalElevation = if (record.id == active?.id) 8.dp else 0.dp
            ) {
                if (thumb != null) {
                    Image(
                        bitmap = thumb.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptInspectorDialog(
    contract: RenderContract,
    onDismiss: () -> Unit
) {
    var fullPrompt by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = { fullPrompt = !fullPrompt }) {
                Text(if (fullPrompt) "Summary" else "Full prompt")
            }
        },
        title = { Text("Prompt Inspector") },
        text = {
            if (fullPrompt) {
                androidx.compose.foundation.lazy.LazyColumn(
                    Modifier.heightIn(max = 520.dp)
                ) {
                    item { Text(contract.prompt, style = MaterialTheme.typography.bodySmall) }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Model: ${contract.model}")
                    Text("Output: ${contract.imageSize} · ${contract.aspectRatio}")
                    Text("Pose: ${contract.effective.pose.label}")
                    Text("Camera: ${contract.effective.cameraAngle.label} · ${contract.effective.lens.label}")
                    Text("Hosiery: ${contract.effective.hosieryType.label} · ${contract.effective.denier.label}")
                    Text("Refs actually sent: ${contract.referenceManifest.size}")
                    if (contract.decisions.isNotEmpty()) {
                        HorizontalDivider()
                        contract.decisions.forEach {
                            Text(
                                "${if (it.applied) "✓" else "⚠"} ${it.title}: ${it.detail}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    )
}

private fun shareRender(context: android.content.Context, path: String?) {
    if (path == null) return
    val source = File(path)
    if (!source.exists()) return

    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    val extension = source.extension.lowercase().ifBlank { "jpg" }
    val target = File(shareDir, "feet-studio-v5.$extension")
    source.copyTo(target, overwrite = true)

    val mimeType = when (extension) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "image/jpeg"
    }

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.files",
        target
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Feet Studio render"))
}
