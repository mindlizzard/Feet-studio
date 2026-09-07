package com.mindlizzard.feetstudio.data

import android.content.Context
import com.mindlizzard.feetstudio.domain.RenderContract
import com.mindlizzard.feetstudio.domain.RenderRecord
import org.json.JSONObject
import java.io.File
import java.util.UUID

class GalleryRepository(private val context: Context) {
    private val dir = File(context.filesDir, "renders").apply { mkdirs() }

    fun save(
        imageBytes: ByteArray,
        contract: RenderContract,
        parentId: String? = null,
        fixTarget: String? = null
    ): RenderRecord {
        val id = UUID.randomUUID().toString()
        val image = File(dir, "$id.png")
        image.writeBytes(imageBytes)

        val record = RenderRecord(
            id = id,
            imagePath = image.absolutePath,
            createdAt = System.currentTimeMillis(),
            prompt = contract.prompt,
            model = contract.model,
            imageSize = contract.imageSize,
            aspectRatio = contract.aspectRatio,
            parentId = parentId,
            fixTarget = fixTarget
        )
        writeMeta(record)
        return record
    }

    fun list(): List<RenderRecord> =
        dir.listFiles { file -> file.extension == "json" }
            ?.mapNotNull { readMeta(it) }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()

    private fun writeMeta(record: RenderRecord) {
        val json = JSONObject()
            .put("id", record.id)
            .put("imagePath", record.imagePath)
            .put("createdAt", record.createdAt)
            .put("prompt", record.prompt)
            .put("model", record.model)
            .put("imageSize", record.imageSize)
            .put("aspectRatio", record.aspectRatio)
            .put("parentId", record.parentId)
            .put("fixTarget", record.fixTarget)
        File(dir, "${record.id}.json").writeText(json.toString())
    }

    private fun readMeta(file: File): RenderRecord? = runCatching {
        val json = JSONObject(file.readText())
        RenderRecord(
            id = json.getString("id"),
            imagePath = json.getString("imagePath"),
            createdAt = json.getLong("createdAt"),
            prompt = json.getString("prompt"),
            model = json.getString("model"),
            imageSize = json.getString("imageSize"),
            aspectRatio = json.getString("aspectRatio"),
            parentId = json.optString("parentId").takeIf { it.isNotBlank() && it != "null" },
            fixTarget = json.optString("fixTarget").takeIf { it.isNotBlank() && it != "null" }
        )
    }.getOrNull()
}
