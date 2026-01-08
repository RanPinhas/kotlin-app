package com.example.myapplication3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication3.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : AppCompatActivity() {

    private var gameActive = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var gameManager: GameManager

    private val handler = Handler(Looper.getMainLooper())
    private var fallDelay: Long = 550

    private lateinit var lblCount: TextView
    private var count = 0
    private var tickCounter = 0
    private var playerName: String? = ""

    private var bgmPlayer: MediaPlayer? = null
    private var crashPlayer: MediaPlayer? = null

    private lateinit var accSensorApi: AccSensorApi
    private var isSensorMode = false
    private var lastSensorMoveTime: Long = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        SignalManager.init(this)

        gameManager = GameManager(object : GameCallback {
            override fun updateScore(score: Int) { }
            override fun livesUpdated(lives: Int) { }
            override fun collisionDetected() {
                SignalManager.getInstance().vibrate()
                SignalManager.getInstance().toast("Hit!")
                playCrashSound()
            }
            override fun gameOver(score: Int) {
                this@MainActivity.gameOver()
            }
        })

        askForLocationPermission()

        if (intent.hasExtra("BUNDLE")) {
            val bundle = intent.getBundleExtra("BUNDLE")
            playerName = bundle?.getString("KEY_NAME")
            val startWithSensor = bundle?.getBoolean("KEY_SENSOR_MODE", false) ?: false
            if (startWithSensor) isSensorMode = true

            val isFastMode = bundle?.getBoolean("KEY_FAST_MODE", false) ?: false
            if (isFastMode) {
                fallDelay = (fallDelay * 0.75).toLong()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initSensor()
        initViews()
        initSounds()

        lblCount = findViewById(R.id.lblCount3)

        binding.btnArrow.setOnClickListener {
            if (gameActive && !isSensorMode) {
                gameManager.moveLeft()
                updateControls()
            }
        }

        binding.btnArrow2.setOnClickListener {
            if (gameActive && !isSensorMode) {
                gameManager.moveRight()
                updateControls()
            }
        }

        startGame()

        if (isSensorMode) {
            toggleSensorMode(forceOn = true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (gameActive) {
            if (isSensorMode) accSensorApi.start()
            startMusic()
            handler.removeCallbacksAndMessages(null)
            startRockFall()
        }
    }

    override fun onPause() {
        super.onPause()
        if (gameActive) {
            accSensorApi.stop()
            stopMusic()
            handler.removeCallbacksAndMessages(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseSounds()
        handler.removeCallbacksAndMessages(null)
    }

    private fun askForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Location permission is required for High Score map", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initViews() {
        val colRock0 = listOf(binding.imgRock00, binding.imgRock10, binding.imgRock20, binding.imgRock30, binding.imgRock40)
        val colRock1 = listOf(binding.imgRock01, binding.imgRock11, binding.imgRock21, binding.imgRock31, binding.imgRock41)
        val colRock2 = listOf(binding.imgRock02, binding.imgRock12, binding.imgRock22, binding.imgRock32, binding.imgRock42)
        val colRock3 = listOf(binding.imgRock03, binding.imgRock13, binding.imgRock23, binding.imgRock33, binding.imgRock43)
        val colRock4 = listOf(binding.imgRock04, binding.imgRock14, binding.imgRock24, binding.imgRock34, binding.imgRock44)

        val colCigar0 = listOf(binding.imgCigar00, binding.imgCigar10, binding.imgCigar20, binding.imgCigar30, binding.imgCigar40)
        val colCigar1 = listOf(binding.imgCigar01, binding.imgCigar11, binding.imgCigar21, binding.imgCigar31, binding.imgCigar41)
        val colCigar2 = listOf(binding.imgCigar02, binding.imgCigar12, binding.imgCigar22, binding.imgCigar32, binding.imgCigar42)
        val colCigar3 = listOf(binding.imgCigar03, binding.imgCigar13, binding.imgCigar23, binding.imgCigar33, binding.imgCigar43)
        val colCigar4 = listOf(binding.imgCigar04, binding.imgCigar14, binding.imgCigar24, binding.imgCigar34, binding.imgCigar44)

        gameManager.initGameGrid(listOf(colRock0, colRock1, colRock2, colRock3, colRock4),
            listOf(colCigar0, colCigar1, colCigar2, colCigar3, colCigar4))

        gameManager.initCar(listOf(binding.imgCar50, binding.imgCar51, binding.imgCar52, binding.imgCar53, binding.imgCar54))
        gameManager.initLife(listOf(binding.imglif1, binding.imglif2, binding.imglif3))
    }

    private fun initSensor() {
        accSensorApi = AccSensorApi(this, object : AccSensorCallBack {
            override fun data(x: Float, y: Float, z: Float) {
                if (isSensorMode && gameActive) {
                    if (System.currentTimeMillis() - lastSensorMoveTime > 300) {
                        if (x > 3.0) {
                            gameManager.moveLeft()
                            lastSensorMoveTime = System.currentTimeMillis()
                        } else if (x < -3.0) {
                            gameManager.moveRight()
                            lastSensorMoveTime = System.currentTimeMillis()
                        }
                    }
                }
            }
        })
    }

    private fun initSounds() {
        try {
            bgmPlayer = MediaPlayer.create(this, R.raw.game_music)
            bgmPlayer?.isLooping = true
            bgmPlayer?.setVolume(0.5f, 0.5f)

            crashPlayer = MediaPlayer.create(this, R.raw.crash_sound)
            crashPlayer?.setVolume(1.0f, 1.0f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playCrashSound() {
        try {
            if (crashPlayer != null) {
                if (crashPlayer!!.isPlaying) crashPlayer!!.seekTo(0)
                crashPlayer!!.start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun startMusic() { bgmPlayer?.start() }
    private fun stopMusic() { bgmPlayer?.pause() }

    private fun releaseSounds() {
        bgmPlayer?.release()
        bgmPlayer = null
        crashPlayer?.release()
        crashPlayer = null
    }

    private fun startGame() {
        gameActive = true
        gameManager.resetGame()
        count = 0
        updateCount()
        tickCounter = 0
        updateControls()
        startMusic()

        gameManager.spawnOneItem()
        // לא קוראים ל-startRockFall כאן ישירות כדי למנוע כפילות עם onResume
        // onResume ייקרא מיד אחרי onCreate והוא יתחיל את המשחק
    }

    private fun updateCount() { lblCount.text = "%02d".format(count) }

    private fun toggleSensorMode(forceOn: Boolean = false) {
        if (!forceOn) isSensorMode = !isSensorMode

        if (isSensorMode) {
            binding.btnArrow.visibility = View.INVISIBLE
            binding.btnArrow2.visibility = View.INVISIBLE
            accSensorApi.start()
            Toast.makeText(this, "Sensor Mode ON", Toast.LENGTH_SHORT).show()
        } else {
            binding.btnArrow.visibility = View.VISIBLE
            binding.btnArrow2.visibility = View.VISIBLE
            accSensorApi.stop()
            Toast.makeText(this, "Buttons Mode ON", Toast.LENGTH_SHORT).show()
        }
        updateControls()
    }

    private fun updateControls() {
        if (!gameActive) {
            binding.btnArrow.isEnabled = false
            binding.btnArrow2.isEnabled = false
            return
        }
        if (!isSensorMode) {
            binding.btnArrow.isEnabled = gameManager.canMoveLeft()
            binding.btnArrow2.isEnabled = gameManager.canMoveRight()
        }
    }

    private fun startRockFall() {
        // מונע ריצה אם המשחק לא פעיל
        if (!gameActive) return

        count++
        updateCount()

        gameManager.moveItemStep(
            onCollision = { handleCollision() },
            onPass = { },
            onBonus = {
                count++
                updateCount()
                Toast.makeText(this, "+1 point !", Toast.LENGTH_SHORT).show()
            }
        )

        // בדיקה כפולה לפני קריאה ללופ הבא
        if (!gameActive) return

        tickCounter++
        if (tickCounter % 2 == 0) gameManager.spawnOneItem()

        handler.postDelayed({ startRockFall() }, fallDelay)
    }

    private fun handleCollision() {
        playCrashSound()
        SignalManager.getInstance().vibrate()

        val remainingLives = gameManager.decreaseLife()

        if (remainingLives <= 0) {
            gameOver()
        } else {
            count--
            updateCount()
            SignalManager.getInstance().toast("Hit! Lives left: $remainingLives")
        }
    }

    private fun gameOver() {
        gameActive = false
        stopMusic()
        accSensorApi.stop()

        handler.removeCallbacksAndMessages(null)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            saveScoreAndMoveToScoreActivity(0.0, 0.0)
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    saveScoreAndMoveToScoreActivity(location.latitude, location.longitude)
                } else {
                    saveScoreAndMoveToScoreActivity(0.0, 0.0)
                }
            }
            .addOnFailureListener {
                saveScoreAndMoveToScoreActivity(0.0, 0.0)
            }
    }

    private fun saveScoreAndMoveToScoreActivity(lat: Double, lon: Double) {
        val finalName = if (!playerName.isNullOrEmpty()) playerName!! else "Player"

        ScoreManager.saveScore(this, finalName, count, lat, lon, isSensorMode)

        val intent = Intent(this, ScoreActivity::class.java)
        val bundle = Bundle()
        bundle.putString("KEY_NAME", finalName)
        bundle.putInt("KEY_SCORE", count)
        bundle.putDouble("KEY_LAT", lat)
        bundle.putDouble("KEY_LON", lon)
        bundle.putBoolean("KEY_SENSOR_MODE", isSensorMode)

        intent.putExtra("BUNDLE", bundle)
        startActivity(intent)
        finish()
    }
}