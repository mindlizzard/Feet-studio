package com.mindlizzard.feetstudio.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mindlizzard.feetstudio.domain.RenderRecord
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuraGalleryGrid(
    items: List<RenderRecord>,
    onOpen: (RenderRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Nog geen renders",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 138.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.id }) { item ->
            val bitmap = remember(item.imagePath) { decodeBitmap(item.imagePath) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .combinedClickable(
                        onClick = { onOpen(item) },
                        onLongClick = { onOpen(item) }
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Box {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Preview")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                RoundedCornerShape(topEnd = 12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${item.imageSize} • ${item.aspectRatio}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AuraGalleryViewerSheet(
    record: RenderRecord,
    onDismiss: () -> Unit,
    onShare: (RenderRecord) -> Unit,
    onSavePng: (RenderRecord) -> Unit,
    onSaveJpg: (RenderRecord) -> Unit,
    onSave8kPng: (RenderRecord) -> Unit,
    onSave8kJpg: (RenderRecord) -> Unit,
    onUseAsReference: (RenderRecord) -> Unit
) {
    var scale by remember(record.id) { mutableFloatStateOf(1f) }
    var offsetX by remember(record.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(record.id) { mutableFloatStateOf(0f) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 6f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.navigationBars,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gallery viewer") },
                    navigationIcon = {
                        FilledTonalIconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    },
                    actions = {
                        FilledTonalIconButton(onClick = { onSavePng(record) }) {
                            Icon(Icons.Default.Download, contentDescription = null)
                        }
                        FilledTonalIconButton(onClick = { onShare(record) }) {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                    }
                )
            }
        ) { inner ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(inner)
            ) {
                ZoomableGalleryImage(
                    path = record.imagePath,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    transformState = transformState,
                    onReset = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                )

                Text(
                    text = "${record.imageSize} • ${record.aspectRatio} • ${record.model}",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.size(8.dp))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedAssistChip(
                        onClick = { onUseAsReference(record) },
                        label = { Text("Use as reference") },
                        leadingIcon = {
                            Icon(Icons.Default.ZoomIn, contentDescription = null)
                        }
                    )
                    ElevatedAssistChip(
                        onClick = { onSaveJpg(record) },
                        label = { Text("Save JPG") }
                    )
                    ElevatedAssistChip(
                        onClick = { onSavePng(record) },
                        label = { Text("Save PNG") }
                    )
                    ElevatedAssistChip(
                        onClick = { onSave8kJpg(record) },
                        label = { Text("Export 8K JPG") },
                        leadingIcon = {
                            Icon(Icons.Default.HighQuality, contentDescription = null)
                        }
                    )
                    ElevatedAssistChip(
                        onClick = { onSave8kPng(record) },
                        label = { Text("Export 8K PNG") },
                        leadingIcon = {
                            Icon(Icons.Default.HighQuality, contentDescription = null)
                        }
                    )
                }

                Spacer(Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun ZoomableGalleryImage(
    path: String,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    transformState: TransformableState,
    onReset: () -> Unit
) {
    val bitmap = remember(path) { decodeBitmap(path) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onReset() })
            },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }
                    .transformable(transformState)
            )
        } else {
            Text("Preview unavailable", modifier = Modifier.padding(32.dp))
        }
    }
}

private fun decodeBitmap(path: String): Bitmap? =
    runCatching {
        val file = File(path)
        if (!file.exists()) return null
        BitmapFactory.decodeFile(file.absolutePath)
    }.getOrNull()
