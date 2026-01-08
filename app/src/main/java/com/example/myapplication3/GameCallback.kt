package com.example.myapplication3

interface GameCallback {
    fun updateScore(score: Int)
    fun livesUpdated(lives: Int)
    fun collisionDetected()
    fun gameOver(score: Int)
}