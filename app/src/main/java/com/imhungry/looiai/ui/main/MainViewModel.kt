package com.imhungry.looiai.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.looiai.api.OpenRouterClient
import com.imhungry.looiai.data.ChatMessage
import com.imhungry.looiai.data.Mood
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val history = mutableListOf<ChatMessage>()
    val newMessage = MutableLiveData<ChatMessage>()
    val streamToken = MutableLiveData<String>()
    val error = MutableLiveData<String>()
    val isStreaming = MutableLiveData(false)
    val mood = MutableLiveData(Mood.IDLE)

    private val client = OpenRouterClient()

    private var streamBuffer = StringBuilder()

    fun send(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userText: String
    ) {
        if (isStreaming.value == true) return

        val userMsg = ChatMessage("user", userText)
        history.add(userMsg)
        newMessage.value = userMsg

        mood.value = Mood.THINKING
        isStreaming.value = true
        streamBuffer.clear()

        val messages = buildList {
            add(ChatMessage("system", systemPrompt))
            addAll(history)
        }

        val placeholderMsg = ChatMessage("assistant", "")
        newMessage.value = placeholderMsg

        viewModelScope.launch {
            try {
                client.chat(apiKey, model, messages) { token ->
                    streamBuffer.append(token)
                    streamToken.value = streamBuffer.toString()
                }
                val finalMsg = ChatMessage("assistant", streamBuffer.toString())
                history.add(finalMsg)
                mood.value = pickMoodFromReply(streamBuffer.toString())
            } catch (e: Exception) {
                error.value = e.message ?: "Unknown error"
                mood.value = Mood.IDLE
            } finally {
                isStreaming.value = false
            }
        }
    }

    private fun pickMoodFromReply(text: String): Mood {
        val lower = text.lowercase()
        return when {
            lower.contains("!") && lower.length < 80 -> Mood.EXCITED
            lower.contains("?") -> Mood.CURIOUS
            lower.contains("haha") || lower.contains("lol") || lower.contains("hehe") -> Mood.HAPPY
            else -> Mood.IDLE
        }
    }
}
