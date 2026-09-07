package com.mindlizzard.feetstudio.ai

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import com.mindlizzard.feetstudio.domain.ReferenceAsset
import com.mindlizzard.feetstudio.domain.RenderContract
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient(
    private val contentResolver: ContentResolver,
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()
) {
    private val endpoint = "https://generativelanguage.googleapis.com/v1beta/interactions"

    fun generate(
        apiKey: String,
        contract: RenderContract,
        references: List<ReferenceAsset>,
        sourceImage: ByteArray? = null,
        editInstruction: String? = null
    ): ByteArray {
        require(apiKey.isNotBlank()) { "Geen Gemini API-key ingesteld." }

        val input = JSONArray()

        if (sourceImage != null) {
            input.put(
                JSONObject()
                    .put("type", "image")
                    .put("mime_type", "image/png")
                    .put("data", Base64.encodeToString(sourceImage, Base64.NO_WRAP))
            )
        }

        references
            .filter { it.enabled }
            .sortedBy { it.strength.ordinal }
            .take(5)
            .forEach { ref ->
                uriToBase64(ref.uri)?.let { (mime, data) ->
                    input.put(
                        JSONObject()
                            .put("type", "image")
                            .put("mime_type", mime)
                            .put("data", data)
                    )
                }
            }

        val text = if (editInstruction.isNullOrBlank()) {
            contract.prompt
        } else {
            """
            EDIT THE FIRST SUPPLIED IMAGE.
            $editInstruction
            Preserve all unrelated visible details and composition.
            The resolved render contract remains authoritative:

            ${contract.prompt}
            """.trimIndent()
        }

        input.put(JSONObject().put("type", "text").put("text", text))

        val body = JSONObject()
            .put("model", contract.model)
            .put("input", input)
            .put(
                "response_format",
                JSONObject()
                    .put("type", "image")
                    .put("mime_type", "image/png")
                    .put("aspect_ratio", contract.aspectRatio)
                    .put("image_size", contract.imageSize)
            )

        val request = Request.Builder()
            .url(endpoint)
            .header("x-goog-api-key", apiKey)
            .header("Api-Revision", "2026-05-20")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching {
                    JSONObject(raw).optJSONObject("error")?.optString("message")
                }.getOrNull()
                throw IllegalStateException(message?.takeIf { it.isNotBlank() } ?: "Gemini fout ${response.code}")
            }
            return extractImage(raw)
                ?: throw IllegalStateException("Gemini gaf geen afbeelding terug.")
        }
    }

    private fun extractImage(raw: String): ByteArray? {
        val root = JSONObject(raw)

        root.optJSONObject("output_image")
            ?.optString("data")
            ?.takeIf { it.isNotBlank() }
            ?.let { return Base64.decode(it, Base64.DEFAULT) }

        val steps = root.optJSONArray("steps") ?: return null
        for (i in 0 until steps.length()) {
            val step = steps.optJSONObject(i) ?: continue
            if (step.optString("type") != "model_output") continue
            val content = step.optJSONArray("content") ?: continue
            for (j in 0 until content.length()) {
                val block = content.optJSONObject(j) ?: continue
                if (block.optString("type") == "image") {
                    val data = block.optString("data")
                    if (data.isNotBlank()) return Base64.decode(data, Base64.DEFAULT)
                }
            }
        }
        return null
    }

    private fun uriToBase64(uri: Uri): Pair<String, String>? = runCatching {
        val mime = contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        mime to Base64.encodeToString(bytes, Base64.NO_WRAP)
    }.getOrNull()
}
