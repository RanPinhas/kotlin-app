package com.example.myapplication3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication3.databinding.ActivityScoreBinding

class ScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // קריאת הנתונים מה-Bundle
        if (intent.hasExtra("BUNDLE")) {
            val bundle = intent.getBundleExtra("BUNDLE")
            val name = bundle?.getString("KEY_NAME") ?: "Player"
            val score = bundle?.getInt("KEY_SCORE") ?: 0

            // אפשר לקרוא גם את המיקום אם רוצים להציג אותו פה
            // val lat = bundle?.getDouble("KEY_LAT") ?: 0.0
            // val lon = bundle?.getDouble("KEY_LON") ?: 0.0

            binding.lblPlayerName.text = name
            binding.lblScore.text = "$score"
        }

        binding.btnLeaderboard.setOnClickListener {
            val intent = Intent(this, HighScoreActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}