package com.mindlizzard.feetstudio.ai

import com.mindlizzard.feetstudio.domain.AspectRatio
import com.mindlizzard.feetstudio.domain.RenderContract
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HuggingFaceFreeClient(
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()
) {
    companion object {
        const val MODEL =
            "stabilityai/stable-diffusion-3-medium-diffusers"

        private const val ENDPOINT =
            "https://router.huggingface.co/hf-inference/models/" +
                MODEL
    }

    fun generate(
        token: String,
        contract: RenderContract
    ): ByteArray {
        require(token.isNotBlank()) {
            "Geen Hugging Face token ingesteld."
        }

        val (width, height) =
            dimensions(contract.settings.aspectRatio)

        val body = JSONObject()
            .put(
                "inputs",
                contract.prompt.take(6000)
            )
            .put(
                "parameters",
                JSONObject()
                    .put("width", width)
                    .put("height", height)
                    .put("num_inference_steps", 28)
                    .put("guidance_scale", 4.0)
            )

        val request = Request.Builder()
            .url(ENDPOINT)
            .header(
                "Authorization",
                "Bearer $token"
            )
            .header(
                "Content-Type",
                "application/json"
            )
            .header(
                "Accept",
                "image/*"
            )
            .post(
                body.toString()
                    .toRequestBody(
                        "application/json".toMediaType()
                    )
            )
            .build()

        return http.newCall(request).execute().use { response ->
            val bytes =
                response.body?.bytes()
                    ?: ByteArray(0)

            if (!response.isSuccessful) {
                val message =
                    runCatching {
                        val raw =
                            bytes.toString(Charsets.UTF_8)

                        val json =
                            JSONObject(raw)

                        json.optString("error")
                            .ifBlank {
                                json.optString("message")
                            }
                    }.getOrNull()

                throw IllegalStateException(
                    message?.takeIf {
                        it.isNotBlank()
                    }
                        ?: "Hugging Face fout ${response.code}."
                )
            }

            val contentType =
                response.header("Content-Type")
                    .orEmpty()
                    .lowercase()

            if (
                contentType.contains("application/json") ||
                bytes.isEmpty()
            ) {
                val raw =
                    bytes.toString(Charsets.UTF_8)

                throw IllegalStateException(
                    runCatching {
                        val json =
                            JSONObject(raw)

                        json.optString("error")
                            .ifBlank {
                                json.optString("message")
                            }
                            .ifBlank {
                                "Hugging Face gaf geen afbeelding terug."
                            }
                    }.getOrDefault(
                        "Hugging Face gaf geen afbeelding terug."
                    )
                )
            }

            bytes
        }
    }

    private fun dimensions(
        aspect: AspectRatio
    ): Pair<Int, Int> =
        when (aspect) {
            AspectRatio.SQUARE ->
                1024 to 1024

            AspectRatio.SOCIAL ->
                896 to 1152

            AspectRatio.PORTRAIT ->
                896 to 1152

            AspectRatio.CLASSIC ->
                832 to 1216

            AspectRatio.TALL ->
                768 to 1344

            AspectRatio.LANDSCAPE ->
                1216 to 832
        }
}
