package com.example.myapplication3

import android.content.Intent // הוספנו את זה
import android.os.Bundle
import android.widget.Button // הוספנו את זה ליתר ביטחון
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HighScoreActivity : AppCompatActivity() {

    private lateinit var googleMap: GoogleMap
    private lateinit var scoreListFragment: ScoreListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_score)

        initMapFragment()
        initListFragment()

        // --- התיקון לכפתור החזרה ---
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, LobbyActivity::class.java)
            // השורות הבאות מנקות את הזיכרון כדי ש"חזור" בטלפון ייצא מהאפליקציה ולא יחזור למשחק
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun initListFragment() {
        scoreListFragment = ScoreListFragment()

        scoreListFragment.setCallBackList(object : ScoreListFragment.CallBack_List {
            override fun onRowClick(lat: Double, lon: Double) {
                zoomToLocation(lat, lon)
            }
        })

        supportFragmentManager.beginTransaction()
            .replace(R.id.layLst, scoreListFragment)
            .commit()
    }

    private fun initMapFragment() {
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.layMap, mapFragment)
            .commit()

        mapFragment.getMapAsync { map ->
            googleMap = map
            loadMarkers()
        }
    }

    private fun loadMarkers() {
        val scores = ScoreManager.getTop10Scores(this)

        if (scores.isNotEmpty()) {
            for (item in scores) {
                if (item.lat != 0.0 || item.lon != 0.0) {
                    val position = LatLng(item.lat, item.lon)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title("${item.name}: ${item.score}")
                    )
                }
            }

            if (scores[0].lat != 0.0) {
                zoomToLocation(scores[0].lat, scores[0].lon)
            }
        }
    }

    private fun zoomToLocation(lat: Double, lon: Double) {
        if (::googleMap.isInitialized && (lat != 0.0 || lon != 0.0)) {
            val loc = LatLng(lat, lon)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
        }
    }
}