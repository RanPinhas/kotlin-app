package com.example.myapplication3

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ScoreManager {
    private const val PREFS_NAME = "game_scores"
    private const val KEY_SCORES = "top_scores"

    fun saveScore(context: Context, name: String, score: Int, lat: Double, lon: Double, isSensor: Boolean) {
        val scores = getTop10Scores(context).toMutableList()
        scores.add(ScoreItem(name, score, lat, lon, isSensor))

        // מיון לפי ניקוד יורד
        scores.sortByDescending { it.score }

        // שמירת ה-10 הטובים ביותר
        val top10 = scores.take(10)

        val gson = Gson()
        val json = gson.toJson(top10)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCORES, json).apply()
    }

    fun getTop10Scores(context: Context): List<ScoreItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SCORES, null) ?: return emptyList()

        val type = object : TypeToken<List<ScoreItem>>() {}.type
        return Gson().fromJson(json, type)
    }
}