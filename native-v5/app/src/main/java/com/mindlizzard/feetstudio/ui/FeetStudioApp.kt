package com.mindlizzard.feetstudio.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindlizzard.feetstudio.StudioViewModel
import com.mindlizzard.feetstudio.domain.FixTarget
import com.mindlizzard.feetstudio.domain.ReferenceAsset
import com.mindlizzard.feetstudio.domain.ReferenceRole
import com.mindlizzard.feetstudio.domain.ReferenceStrength
import com.mindlizzard.feetstudio.domain.RenderRecord
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeetStudioApp(vm: StudioViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    var section by remember { mutableStateOf(StudioSection.STUDIO) }
    var showControls by remember { mutableStateOf(false) }
    var viewerRecord by remember { mutableStateOf<RenderRecord?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    val pickReferences = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            vm.addReferences(uris.take(5).map {
                ReferenceAsset(uri = it)
            })
            notice = "${uris.size.coerceAtMost(5)} referentie(s) toegevoegd"
        }
    }

    LaunchedEffect(notice) {
        notice?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            notice = null
        }
    }

    ui.error?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::dismissError,
            confirmButton = {
                TextButton(onClick = vm::dismissError) { Text("OK") }
            },
            title = { Text("Fout") },
            text = { Text(msg) }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Feet Studio v5.5") },
                navigationIcon = {
                    IconButton(onClick = { viewerRecord = ui.active }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showControls = true }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                },
                windowInsets = WindowInsets.statusBars
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallFloatingActionButton(onClick = {
                    ui.active?.let {
                        vm.targetedFix(FixTarget.REALISM)
                        notice = "Realism fix gestart"
                    } ?: run {
                        notice = "Kies eerst een render uit de gallery"
                    }
                }) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                }

                FloatingActionButton(onClick = {
                    vm.generate()
                    notice = "Render gestart"
                }) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            SectionBar(
                selected = section,
                onChange = {
                    section = it
                    showControls = true
                }
            )

            ActiveRenderPanel(
                active = ui.active,
                loading = ui.loading,
                progress = ui.progress,
                onOpenViewer = {
                    ui.active?.let { viewerRecord = it }
                },
                onRealismFix = {
                    ui.active?.let {
                        vm.targetedFix(FixTarget.REALISM)
                        notice = "Realism fix gestart"
                    } ?: run {
                        notice = "Kies eerst een render"
                    }
                },
                onUseAsReference = {
                    ui.active?.let {
                        vm.addReferences(
                            listOf(
                                ReferenceAsset(
                                    uri = Uri.fromFile(File(it.imagePath)),
                                    role = ReferenceRole.STYLE,
                                    strength = ReferenceStrength.GUIDED
                                )
                            )
                        )
                        notice = "Actieve render toegevoegd als referentie"
                    }
                }
            )

            AuraGalleryGrid(
                items = ui.gallery,
                onOpen = {
                    vm.select(it)
                    viewerRecord = it
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showControls) {
        NativeControlSheet(
            section = section,
            viewModel = vm,
            workspace = ui.workspace,
            references = ui.references,
            onPickReferences = { pickReferences.launch("image/*") },
            onDismiss = { showControls = false }
        )
    }

    viewerRecord?.let { record ->
        AuraGalleryViewerSheet(
            record = record,
            onDismiss = { viewerRecord = null },
            onShare = { shareImage(context, File(it.imagePath)) },
            onSavePng = {
                notice = "Opgeslagen: " + vm.exportRecordAsPng(it)
            },
            onSaveJpg = {
                notice = "Opgeslagen: " + vm.exportRecordAsJpg(it)
            },
            onSave8kPng = {
                notice = "8K PNG opgeslagen: " + vm.exportRecordAs8kPng(it)
            },
            onSave8kJpg = {
                notice = "8K JPG opgeslagen: " + vm.exportRecordAs8kJpg(it)
            },
            onUseAsReference = {
                vm.addReferences(
                    listOf(
                        ReferenceAsset(
                            uri = Uri.fromFile(File(it.imagePath)),
                            role = ReferenceRole.STYLE,
                            strength = ReferenceStrength.GUIDED
                        )
                    )
                )
                notice = "Toegevoegd als referentie"
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SectionBar(
    selected: StudioSection,
    onChange: (StudioSection) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StudioSection.entries.forEach { item ->
            AssistChip(
                onClick = { onChange(item) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun ActiveRenderPanel(
    active: RenderRecord?,
    loading: Boolean,
    progress: String,
    onOpenViewer: () -> Unit,
    onRealismFix: () -> Unit,
    onUseAsReference: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Active render", style = MaterialTheme.typography.titleSmall)

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (progress.isNotBlank()) {
                    Text(
                        progress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (active == null) {
                Text(
                    "Nog geen actieve render. Genereer iets of open een item uit de gallery.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "${active.imageSize} • ${active.aspectRatio} • ${active.model}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpenViewer, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                        Text("Open")
                    }
                    TextButton(onClick = onRealismFix) { Text("Realism fix") }
                    TextButton(onClick = onUseAsReference) { Text("Use as ref") }
                }
            }
        }
    }
}

private fun shareImage(context: Context, source: File) {
    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    val extension = source.extension.lowercase().ifBlank { "jpg" }
    val target = File(shareDir, "feet-studio-share.$extension")
    source.copyTo(target, overwrite = true)

    val mimeType = when (extension) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "image/jpeg"
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.files",
        target
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share render"))
}
