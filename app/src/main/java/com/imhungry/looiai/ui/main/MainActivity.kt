package com.imhungry.looiai.ui.main

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.imhungry.looiai.R
import com.imhungry.looiai.chat.ChatAdapter
import com.imhungry.looiai.data.Mood
import com.imhungry.looiai.data.Prefs
import com.imhungry.looiai.databinding.ActivityMainBinding
import com.imhungry.looiai.robot.RobotFaceView
import com.imhungry.looiai.robot.VoiceManager
import com.imhungry.looiai.ui.settings.SetupActivity
import io.noties.markwon.Markwon

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Prefs
    private lateinit var adapter: ChatAdapter
    private lateinit var robotFace: RobotFaceView
    private lateinit var voice: VoiceManager
    private val vm: MainViewModel by viewModels()

    private val blinkHandler = Handler(Looper.getMainLooper())
    private var floatAnimator: ValueAnimator? = null
    private var moodAnimator: AnimatorSet? = null

    private var micListening = false
    private var ttsOn = true

    private val micPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening() else
            Toast.makeText(this, "Microphone permission needed for voice input", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)

        setupRobotFace()
        setupVoice()
        setupChat()
        setupInput()
        observeViewModel()
        startIdleAnimations()
    }

    private fun setupVoice() {
        voice = VoiceManager(
            context = this,
            onSttResult = { text ->
                runOnUiThread {
                    binding.etMessage.setText(text)
                    binding.etMessage.setSelection(text.length)
                    setMicIdle()
                    sendMessage()
                }
            },
            onSttError = { msg ->
                runOnUiThread {
                    setMicIdle()
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onSttStart = { runOnUiThread { setMicActive() } },
            onSttEnd   = { runOnUiThread { setMicIdle() } },
            onTtsStart = { runOnUiThread { binding.tvStatus.text = "speaking" } },
            onTtsDone  = { runOnUiThread { binding.tvStatus.text = "online" } }
        )
        voice.ttsEnabled = ttsOn
    }

    private fun setupRobotFace() {
        robotFace = RobotFaceView(this)
        binding.robotPanel.addView(robotFace, 0,
            android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        robotFace.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val cx = robotFace.width / 2f
                val cy = robotFace.height / 2f
                robotFace.eyeOffsetX = ((event.x - cx) / cx).coerceIn(-1f, 1f)
                robotFace.eyeOffsetY = ((event.y - cy) / cy).coerceIn(-1f, 1f)
                Handler(Looper.getMainLooper()).postDelayed({
                    ObjectAnimator.ofFloat(0f, 1f).apply {
                        duration = 600
                        addUpdateListener {
                            val t = 1f - it.animatedFraction
                            robotFace.eyeOffsetX *= t
                            robotFace.eyeOffsetY *= t
                        }
                        start()
                    }
                }, 800)
            }
            true
        }

        binding.tvRobotName.text = prefs.robotName
    }

    private fun setupChat() {
        val markwon = Markwon.create(this)
        adapter = ChatAdapter(markwon)
        binding.recyclerChat.layoutManager =
            LinearLayoutManager(this).also { it.stackFromEnd = true }
        binding.recyclerChat.adapter = adapter
    }

    private fun setupInput() {
        binding.btnSend.setOnClickListener { sendMessage() }

        binding.etMessage.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                sendMessage(); true
            } else false
        }

        binding.btnMic.setOnClickListener {
            if (micListening) {
                voice.stopListening()
                setMicIdle()
            } else {
                requestMicAndListen()
            }
        }

        binding.btnTtsToggle.setOnClickListener {
            ttsOn = !ttsOn
            voice.ttsEnabled = ttsOn
            if (!ttsOn) voice.stopSpeaking()
            binding.btnTtsToggle.setImageResource(
                if (ttsOn) R.drawable.ic_speaker else R.drawable.ic_speaker_off
            )
            val msg = if (ttsOn) "Voice replies on" else "Voice replies off"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }

    private fun requestMicAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        } else {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        voice.stopSpeaking()
        voice.startListening()
    }

    private fun setMicActive() {
        micListening = true
        binding.btnMic.setBackgroundResource(R.drawable.bg_mic_button_active)
        binding.tvStatus.text = "listening…"
    }

    private fun setMicIdle() {
        micListening = false
        binding.btnMic.setBackgroundResource(R.drawable.bg_mic_button)
        if (binding.tvStatus.text == "listening…") binding.tvStatus.text = "online"
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isBlank()) return
        binding.etMessage.setText("")
        voice.stopSpeaking()

        val systemPrompt = Prefs.systemPromptFor(prefs.robotName, prefs.personality)
        vm.send(prefs.apiKey, prefs.model, systemPrompt, text)
    }

    private fun observeViewModel() {
        vm.newMessage.observe(this) { msg ->
            adapter.add(msg)
            binding.recyclerChat.scrollToPosition(adapter.itemCount - 1)
        }

        vm.streamToken.observe(this) { text ->
            adapter.updateLast(text)
            binding.recyclerChat.scrollToPosition(adapter.itemCount - 1)
        }

        vm.isStreaming.observe(this) { streaming ->
            binding.tvTyping.visibility = if (streaming) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !streaming
            binding.btnMic.isEnabled = !streaming
        }

        vm.error.observe(this) { err ->
            Toast.makeText(this, err, Toast.LENGTH_LONG).show()
        }

        vm.mood.observe(this) { mood ->
            binding.tvMood.text = mood.label
            applyMoodExpression(mood)
        }

        // Speak the final reply once streaming is done
        var lastSpoken = ""
        vm.isStreaming.observe(this) { streaming ->
            if (!streaming) {
                val last = vm.history.lastOrNull { it.role == "assistant" }?.content ?: return@observe
                if (last != lastSpoken && last.isNotBlank()) {
                    lastSpoken = last
                    voice.speak(last)
                }
            }
        }
    }

    private fun applyMoodExpression(mood: Mood) {
        moodAnimator?.cancel()
        when (mood) {
            Mood.THINKING -> {
                val blink = ObjectAnimator.ofFloat(robotFace, "blinkProgress", 1f, 0f, 1f).apply {
                    duration = 300
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.RESTART
                }
                moodAnimator = AnimatorSet().apply { play(blink); start() }
            }
            Mood.EXCITED -> {
                val bounce = ObjectAnimator.ofFloat(robotFace, "translationY", 0f, -12f, 0f).apply {
                    duration = 200; repeatCount = 3; repeatMode = ValueAnimator.REVERSE
                }
                moodAnimator = AnimatorSet().apply { play(bounce); start() }
            }
            Mood.HAPPY -> {
                val wiggle = ObjectAnimator.ofFloat(robotFace, "rotation", 0f, -5f, 5f, 0f).apply {
                    duration = 400; repeatCount = 1
                }
                moodAnimator = AnimatorSet().apply { play(wiggle); start() }
            }
            else -> robotFace.blinkProgress = 1f
        }
    }

    private fun startIdleAnimations() {
        scheduleNextBlink()
        floatAnimator = ValueAnimator.ofFloat(0f, -10f).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { robotFace.translationY = it.animatedValue as Float }
            start()
        }
    }

    private fun scheduleNextBlink() {
        val delay = (3000L..7000L).random()
        blinkHandler.postDelayed({
            if (vm.mood.value !in listOf(Mood.THINKING)) {
                ObjectAnimator.ofFloat(1f, 0.05f, 1f).apply {
                    duration = 160
                    addUpdateListener { robotFace.blinkProgress = it.animatedValue as Float }
                    start()
                }
            }
            scheduleNextBlink()
        }, delay)
    }

    override fun onDestroy() {
        super.onDestroy()
        blinkHandler.removeCallbacksAndMessages(null)
        floatAnimator?.cancel()
        moodAnimator?.cancel()
        voice.destroy()
    }
}
