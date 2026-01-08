package com.example.myapplication3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication3.databinding.ActivityLobbyBinding

class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding
    private var isFastMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateFastModeUI()

        binding.btnStartGameFast.setOnClickListener {
            isFastMode = !isFastMode
            updateFastModeUI()
        }

        binding.btnStartGame.setOnClickListener {
            startGame(isSensor = false)
        }

        binding.btnLeaderboard.setOnClickListener {
            startGame(isSensor = true)
        }

        binding.btnMap.setOnClickListener {
            val intent = Intent(this, HighScoreActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateFastModeUI() {
        if (isFastMode) {
            binding.btnStartGameFast.text = "Fast Mode: ON"
            binding.btnStartGameFast.alpha = 1.0f
        } else {
            binding.btnStartGameFast.text = "Fast Mode: OFF"
            binding.btnStartGameFast.alpha = 0.5f
        }
    }

    private fun startGame(isSensor: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()

        bundle.putString("KEY_NAME", binding.etName.text.toString())
        bundle.putBoolean("KEY_SENSOR_MODE", isSensor)
        bundle.putBoolean("KEY_FAST_MODE", isFastMode)

        intent.putExtra("BUNDLE", bundle)
        startActivity(intent)
    }
}