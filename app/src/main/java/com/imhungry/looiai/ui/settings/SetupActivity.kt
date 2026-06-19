package com.imhungry.looiai.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.imhungry.looiai.R
import com.imhungry.looiai.data.Prefs
import com.imhungry.looiai.databinding.ActivitySetupBinding
import com.imhungry.looiai.ui.main.MainActivity

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)

        val modelAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Prefs.MODELS
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerModel.adapter = modelAdapter

        val personalityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Prefs.PERSONALITIES
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerPersonality.adapter = personalityAdapter

        if (prefs.isSetup) {
            binding.etApiKey.setText(prefs.apiKey)
            binding.etRobotName.setText(prefs.robotName)
            val modelIdx = Prefs.MODELS.indexOf(prefs.model).coerceAtLeast(0)
            binding.spinnerModel.setSelection(modelIdx)
            val persIdx = Prefs.PERSONALITIES.indexOf(prefs.personality).coerceAtLeast(0)
            binding.spinnerPersonality.setSelection(persIdx)
            binding.btnStart.text = getString(R.string.title_settings)
        }

        binding.btnStart.setOnClickListener {
            val key = binding.etApiKey.text.toString().trim()
            if (key.isBlank()) {
                Toast.makeText(this, getString(R.string.error_no_key), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.apiKey = key
            prefs.robotName = binding.etRobotName.text.toString().trim()
                .ifBlank { getString(R.string.default_robot_name) }
            prefs.model = binding.spinnerModel.selectedItem.toString()
            prefs.personality = binding.spinnerPersonality.selectedItem.toString()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
