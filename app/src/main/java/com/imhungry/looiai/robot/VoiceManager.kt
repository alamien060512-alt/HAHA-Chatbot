package com.imhungry.looiai.robot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSttResult: (String) -> Unit,
    private val onSttError: (String) -> Unit,
    private val onSttStart: () -> Unit,
    private val onSttEnd: () -> Unit,
    private val onTtsStart: () -> Unit,
    private val onTtsDone: () -> Unit
) {

    private var tts: TextToSpeech? = null
    private var stt: SpeechRecognizer? = null
    private var ttsReady = false

    var ttsEnabled = true

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(1.1f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { onTtsStart() }
                    override fun onDone(utteranceId: String?) { onTtsDone() }
                    override fun onError(utteranceId: String?) { onTtsDone() }
                })
            }
        }
    }

    fun speak(text: String) {
        if (!ttsEnabled || !ttsReady) return
        val clean = text
            .replace(Regex("```[\\s\\S]*?```"), "code block")
            .replace(Regex("`[^`]+`"), "")
            .replace(Regex("[*_#>~]"), "")
            .trim()
        if (clean.isBlank()) return
        tts?.stop()
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "looi_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onSttError("Speech recognition not available on this device")
            return
        }
        stt?.destroy()
        stt = SpeechRecognizer.createSpeechRecognizer(context)
        stt?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { onSttStart() }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { onSttEnd() }
            override fun onError(error: Int) {
                onSttEnd()
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that — try again"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    else -> "Speech error ($error)"
                }
                onSttError(msg)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: return
                onSttResult(text)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        stt?.startListening(intent)
    }

    fun stopListening() {
        stt?.stopListening()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        stt?.destroy()
    }
}
