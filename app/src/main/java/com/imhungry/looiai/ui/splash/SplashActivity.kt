package com.imhungry.looiai.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.imhungry.looiai.R
import com.imhungry.looiai.data.Prefs
import com.imhungry.looiai.databinding.ActivitySplashBinding
import com.imhungry.looiai.ui.main.MainActivity
import com.imhungry.looiai.ui.settings.SetupActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val floatAnim = AnimationUtils.loadAnimation(this, R.anim.float_idle)
        binding.robotView.startAnimation(floatAnim)

        val prefs = Prefs(this)
        binding.root.postDelayed({
            val target = if (prefs.isSetup) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, SetupActivity::class.java)
            }
            startActivity(target)
            finish()
        }, 1600)
    }
}
