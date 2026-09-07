package com.mindlizzard.feetstudio.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

class DeviceExportRepository(private val context: Context) {

    fun exportJpg(path: String, displayName: String): String {
        val bytes = File(path).readBytes()
        return writeToPictures(
            displayName = displayName.removeSuffix(".jpg") + ".jpg",
            mimeType = "image/jpeg",
            writer = { out -> out.write(bytes) }
        )
    }

    fun exportPng(path: String, displayName: String): String {
        val bitmap = BitmapFactory.decodeFile(path)
            ?: error("Kon afbeelding niet openen.")
        return writeToPictures(
            displayName = displayName.removeSuffix(".png") + ".png",
            mimeType = "image/png",
            writer = { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    error("PNG export mislukt.")
                }
            }
        )
    }

    private fun writeToPictures(
        displayName: String,
        mimeType: String,
        writer: (java.io.OutputStream) -> Unit
    ): String {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Feet Studio")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: error("Kon MediaStore-item niet aanmaken.")

        resolver.openOutputStream(uri)?.use(writer)
            ?: error("Kon uitvoerstream niet openen.")

        if (Build.VERSION.SDK_INT >= 29) {
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        return uri.toString()
    }
}
