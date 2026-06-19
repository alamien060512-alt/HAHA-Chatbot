package com.imhungry.looiai.data

import android.content.Context
import androidx.preference.PreferenceManager

class Prefs(context: Context) {

    private val sp = PreferenceManager.getDefaultSharedPreferences(context)

    var apiKey: String
        get() = sp.getString("pref_api_key", "") ?: ""
        set(v) = sp.edit().putString("pref_api_key", v).apply()

    var model: String
        get() = sp.getString("pref_model", MODELS[0]) ?: MODELS[0]
        set(v) = sp.edit().putString("pref_model", v).apply()

    var personality: String
        get() = sp.getString("pref_personality", PERSONALITIES[0]) ?: PERSONALITIES[0]
        set(v) = sp.edit().putString("pref_personality", v).apply()

    var robotName: String
        get() = sp.getString("pref_robot_name", "Looi") ?: "Looi"
        set(v) = sp.edit().putString("pref_robot_name", v).apply()

    val isSetup: Boolean
        get() = apiKey.isNotBlank()

    companion object {
        val MODELS = listOf(
            "google/gemini-flash-1.5",
            "openai/gpt-4o-mini",
            "openai/gpt-4o",
            "anthropic/claude-3.5-sonnet",
            "anthropic/claude-3-haiku",
            "meta-llama/llama-3.1-8b-instruct:free",
            "mistralai/mistral-7b-instruct:free",
            "deepseek/deepseek-chat"
        )

        val PERSONALITIES = listOf(
            "Cheerful — warm, playful, uses light humour",
            "Curious — asks questions, loves to explore ideas",
            "Calm — measured, thoughtful, reassuring",
            "Sarcastic — witty, dry, a little cheeky",
            "Energetic — enthusiastic, hype, exclamation-happy"
        )

        fun systemPromptFor(name: String, personality: String): String {
            val trait = personality.substringBefore("—").trim().lowercase()
            return """
                You are $name, a small but clever desktop AI companion robot. 
                Your personality is $trait: ${personality.substringAfter("—").trim()}.
                Keep replies concise — 1-3 sentences unless the user needs detail.
                You are self-aware that you live inside a phone app, and you find that endearing.
                Never break character. Express emotion through your words.
            """.trimIndent()
        }
    }
}
