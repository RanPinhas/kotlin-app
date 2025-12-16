package com.example.myapplication3

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var gameActive = false
    private lateinit var binding: ActivityMainBinding
    private val gameManager = GameManager()
    private val handler = Handler(Looper.getMainLooper())
    private val fallDelay: Long = 550
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val col0 = listOf(binding.imgRock00, binding.imgRock10, binding.imgRock20)
        val col1 = listOf(binding.imgRock01, binding.imgRock11, binding.imgRock21)
        val col2 = listOf(binding.imgRock02, binding.imgRock12, binding.imgRock22)
        val rockCols = listOf(col0, col1, col2)
        gameManager.initRocks(rockCols)

        val lanes = listOf(binding.imgCar40, binding.imgCar41, binding.imgCar42)
        gameManager.initCar(lanes)

        val life_list = listOf(binding.imglif1, binding.imglif2, binding.imglif3)
        gameManager.initLife(life_list)

        binding.btnArrow.setOnClickListener {
            if (gameActive) {
                gameManager.moveLeft()
                updateControls()
            }
        }

        binding.btnArrow2.setOnClickListener {
            if (gameActive) {
                gameManager.moveRight()
                updateControls()
            }
        }

        binding.btnStart.setOnClickListener {
            if (!gameActive) {
                startGame()
            }
        }

        stopGame(initialSetup = true)
    }

    private fun startGame() {
        gameActive = true
        gameManager.resetGame()
        binding.btnStart.visibility = View.INVISIBLE
        updateControls()

        gameManager.startNewRocks()
        handler.postDelayed({ startRockFall() }, fallDelay)

        Toast.makeText(this, "המשחק התחיל!", Toast.LENGTH_SHORT).show()
    }

    private fun updateControls() {
        if (!gameActive) {
            binding.btnArrow.isEnabled = false
            binding.btnArrow2.isEnabled = false
            return
        }
        binding.btnArrow.isEnabled = gameManager.canMoveLeft()
        binding.btnArrow2.isEnabled = gameManager.canMoveRight()
    }

    private fun startRockFall() {
        if (!gameActive) return

        val shouldSpawnNew = gameManager.moveRockStep(
            onCollision = { handleCollision() }
        )

        if (!gameActive) return

        if (shouldSpawnNew || gameManager.activeRocks.isEmpty()) {
            gameManager.startNewRocks()
        }

        handler.postDelayed({ startRockFall() }, fallDelay)
    }

    private fun handleCollision() {
        val remainingLives = gameManager.decreaseLife()

        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val collisionEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(collisionEffect)
            } else {
                vibrator.vibrate(200)
            }
        }

        if (remainingLives <= 0) {
            Toast.makeText(this, "החיים נגמרו! המשחק הסתיים.", Toast.LENGTH_LONG).show()
            stopGame()
        } else {
            Toast.makeText(this, "פגיעה! נותרו $remainingLives חיים", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopGame(initialSetup: Boolean = false) {
        gameActive = false
        handler.removeCallbacksAndMessages(null)

        binding.btnStart.visibility = View.VISIBLE

        if (!initialSetup) {
            gameManager.resetGame()
        }

        updateControls()
    }
}