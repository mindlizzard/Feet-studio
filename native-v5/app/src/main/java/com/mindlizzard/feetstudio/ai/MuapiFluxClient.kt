package com.mindlizzard.feetstudio.ai

import com.mindlizzard.feetstudio.domain.AspectRatio
import com.mindlizzard.feetstudio.domain.HosieryFluxPromptCompiler
import com.mindlizzard.feetstudio.domain.HosieryLoraPreset
import com.mindlizzard.feetstudio.domain.RenderContract
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MuapiFluxClient(
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()
) {
    private val api = "https://api.muapi.ai/api/v1"

    fun generate(
        apiKey: String,
        contract: RenderContract,
        preset: HosieryLoraPreset,
        weight: Int
    ): ByteArray {
        require(apiKey.isNotBlank()) { "Geen MuAPI key ingesteld." }

        val (width, height) = dimensions(contract.settings.aspectRatio)
        val prompt = HosieryFluxPromptCompiler.compile(contract, preset)

        val modelList = JSONArray().put(
            JSONObject()
                .put("model", preset.modelId)
                .put("weight", weight.coerceIn(0, 100) / 100.0)
        )

        val body = JSONObject()
            .put("prompt", prompt)
            .put("model_id", modelList)
            .put("width", width)
            .put("height", height)
            .put("num_images", 1)

        val submit = Request.Builder()
            .url("$api/flux_dev_lora_image")
            .header("x-api-key", apiKey)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val submitRaw = http.newCall(submit).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    muapiError(raw) ?: "MuAPI fout ${response.code}"
                )
            }
            raw
        }

        val requestId = requestIdFrom(JSONObject(submitRaw))
            ?: throw IllegalStateException("MuAPI gaf geen request_id terug.")

        repeat(180) {
            Thread.sleep(3000)

            val poll = Request.Builder()
                .url("$api/predictions/$requestId/result")
                .header("x-api-key", apiKey)
                .get()
                .build()

            val raw = http.newCall(poll).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException(
                        muapiError(text) ?: "MuAPI polling fout ${response.code}"
                    )
                }
                text
            }

            val root = JSONObject(raw)
            val data = root.optJSONObject("data")
            val status = root.optString("status")
                .ifBlank { data?.optString("status").orEmpty() }

            if (status.equals("failed", true)) {
                val message = root.optString("error")
                    .ifBlank { data?.optString("error").orEmpty() }
                throw IllegalStateException(
                    message.ifBlank { "MuAPI generatie mislukt." }
                )
            }

            if (status.equals("completed", true)) {
                val outputUrl =
                    firstOutputUrl(root.optJSONArray("outputs"))
                        ?: firstOutputUrl(data?.optJSONArray("outputs"))
                        ?: throw IllegalStateException(
                            "MuAPI voltooide de taak zonder afbeeldings-URL."
                        )

                return downloadImage(outputUrl)
            }
        }

        throw IllegalStateException(
            "MuAPI timeout: de FLUX render duurde langer dan 9 minuten."
        )
    }

    private fun requestIdFrom(root: JSONObject): String? {
        return root.optString("request_id").takeIf { it.isNotBlank() }
            ?: root.optString("id").takeIf { it.isNotBlank() }
            ?: root.optJSONObject("data")
                ?.optString("id")
                ?.takeIf { it.isNotBlank() }
    }

    private fun firstOutputUrl(array: JSONArray?): String? {
        if (array == null || array.length() == 0) return null

        val first = array.opt(0)
        if (first is String && first.isNotBlank()) return first

        if (first is JSONObject) {
            return first.optString("url").takeIf { it.isNotBlank() }
                ?: first.optString("output").takeIf { it.isNotBlank() }
        }

        return null
    }

    private fun downloadImage(url: String): ByteArray {
        val request = Request.Builder().url(url).get().build()

        return http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Kon MuAPI afbeelding niet downloaden (${response.code})."
                )
            }

            response.body?.bytes()
                ?.takeIf { it.isNotEmpty() }
                ?: throw IllegalStateException("MuAPI afbeelding was leeg.")
        }
    }

    private fun muapiError(raw: String): String? =
        runCatching {
            val root = JSONObject(raw)
            root.optString("message").takeIf { it.isNotBlank() }
                ?: root.optString("error").takeIf { it.isNotBlank() }
                ?: root.optJSONObject("data")
                    ?.optString("error")
                    ?.takeIf { it.isNotBlank() }
        }.getOrNull()

    private fun dimensions(aspect: AspectRatio): Pair<Int, Int> =
        when (aspect) {
            AspectRatio.SQUARE -> 1024 to 1024
            AspectRatio.SOCIAL -> 1024 to 1280
            AspectRatio.PORTRAIT -> 960 to 1280
            AspectRatio.CLASSIC -> 896 to 1344
            AspectRatio.TALL -> 768 to 1344
            AspectRatio.LANDSCAPE -> 1344 to 768
        }
}
