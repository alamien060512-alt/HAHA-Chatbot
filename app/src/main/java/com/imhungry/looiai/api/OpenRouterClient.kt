package com.imhungry.looiai.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.imhungry.looiai.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenRouterClient {

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun chat(
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        onToken: suspend (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val body = JsonObject().apply {
            addProperty("model", model)
            addProperty("stream", true)
            add("messages", gson.toJsonTree(messages))
        }.toString()

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "com.imhungry.looiai")
            .addHeader("X-Title", "Looi AI")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: "Unknown error"
            throw Exception("API error ${response.code}: $errBody")
        }

        response.body?.source()?.let { source ->
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("data: ")) continue
                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break
                try {
                    val json = gson.fromJson(data, JsonObject::class.java)
                    val token = json
                        .getAsJsonArray("choices")
                        ?.get(0)?.asJsonObject
                        ?.getAsJsonObject("delta")
                        ?.get("content")?.asString
                        ?: continue
                    withContext(Dispatchers.Main) { onToken(token) }
                } catch (_: Exception) {}
            }
        }
    }
}
