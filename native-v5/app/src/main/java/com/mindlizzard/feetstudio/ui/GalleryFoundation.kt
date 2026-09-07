package com.mindlizzard.feetstudio.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.mutableStateOf
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
        columns = GridCells.Adaptive(minSize = 140.dp),
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
                        Text("Preview", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraGalleryViewerSheet(
    record: RenderRecord,
    onDismiss: () -> Unit,
    onShare: (RenderRecord) -> Unit,
    onSavePng: (RenderRecord) -> Unit,
    onSaveJpg: (RenderRecord) -> Unit
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
        onDismissRequest = onDismiss,
        windowInsets = WindowInsets.systemBars
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

                Spacer(Modifier.size(8.dp))

                Text(
                    text = "${record.imageSize} • ${record.aspectRatio} • ${record.model}",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = { onSaveJpg(record) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Save JPG")
                }
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
    transformState: androidx.compose.foundation.gestures.TransformableState,
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
                detectTapGestures(
                    onDoubleTap = { onReset() }
                )
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
        val f = File(path)
        if (!f.exists()) return null
        BitmapFactory.decodeFile(f.absolutePath)
    }.getOrNull()
