package com.mindlizzard.feetstudio.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun decodeSampledBitmap(path: String, maxDimension: Int): Bitmap? {
    val file = File(path)
    if (!file.exists()) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > maxDimension * 2 ||
        bounds.outHeight / sample > maxDimension * 2
    ) {
        sample *= 2
    }
    return BitmapFactory.decodeFile(
        path,
        BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
    )
}

@Composable
fun rememberFileBitmap(path: String?, maxDimension: Int = 1800): Bitmap? {
    val state = produceState<Bitmap?>(initialValue = null, path, maxDimension) {
        value = if (path == null) null else withContext(Dispatchers.IO) {
            decodeSampledBitmap(path, maxDimension)
        }
    }
    return state.value
}
