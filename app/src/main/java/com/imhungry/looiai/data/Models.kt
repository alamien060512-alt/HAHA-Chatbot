package com.imhungry.looiai.data

data class ChatMessage(
    val role: String,
    val content: String
)

enum class Mood(val label: String) {
    IDLE("✦ idle"),
    THINKING("◈ thinking"),
    HAPPY("✿ happy"),
    CURIOUS("⟡ curious"),
    EXCITED("★ excited")
}
