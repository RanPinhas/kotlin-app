package com.example.myapplication3

import android.view.View
import android.widget.ImageView
import kotlin.random.Random

data class Rock(var row: Int, val lane: Int)

class GameManager {

    private lateinit var carLanes: List<ImageView>
    private var carPos = 0

    fun initCar(lanes: List<ImageView>, startPos: Int = 1) {
        carLanes = lanes
        carPos = startPos
        showCarAt(carPos)
    }

    private fun showCarAt(pos: Int) {
        for (i in carLanes.indices) {
            carLanes[i].visibility = if (i == pos) View.VISIBLE else View.INVISIBLE
            carLanes[i].alpha = 1f
        }
    }

    private fun animateCarMove(oldPos: Int, newPos: Int) {
        val oldCar = carLanes[oldPos]
        val newCar = carLanes[newPos]

        val direction = if (newPos > oldPos) 1f else -1f
        val rotationAngle = direction * 5f

        newCar.alpha = 0f
        newCar.visibility = View.VISIBLE
        newCar.rotation = rotationAngle

        oldCar.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                oldCar.visibility = View.INVISIBLE
                oldCar.alpha = 1f
                oldCar.rotation = 0f
            }.start()

        newCar.animate()
            .alpha(1f)
            .rotation(0f)
            .setDuration(300)
            .start()
    }

    fun moveLeft() {
        if (carPos <= 0) return
        val oldPos = carPos
        carPos--
        animateCarMove(oldPos, carPos)
    }

    fun moveRight() {
        if (carPos >= carLanes.size - 1) return
        val oldPos = carPos
        carPos++
        animateCarMove(oldPos, carPos)
    }

    fun canMoveLeft() = carPos > 0
    fun canMoveRight() = carPos < carLanes.size - 1

    private lateinit var lifeLanes: List<ImageView>
    private var lives = 3

    fun initLife(life: List<ImageView>) {
        lifeLanes = life
        updateLifeUI()
    }

    fun decreaseLife(): Int {
        if (lives > 0) {
            lives--
            updateLifeUI()
        }
        return lives
    }

    private fun updateLifeUI() {
        for (i in lifeLanes.indices) {
            lifeLanes[i].visibility = if (i < lives) View.VISIBLE else View.INVISIBLE
        }
    }

    fun resetGame(startPos: Int = 1) {
        lives = 3
        updateLifeUI()

        if (::rockCols.isInitialized) {
            rockCols.flatten().forEach { it.visibility = View.INVISIBLE }
        }
        activeRocks.clear()

        carPos = startPos
        showCarAt(carPos)
    }

    private lateinit var rockCols: List<List<ImageView>>
    val activeRocks = mutableListOf<Rock>()

    fun initRocks(cols: List<List<ImageView>>) {
        rockCols = cols
        rockCols.flatten().forEach { it.visibility = View.INVISIBLE }
        activeRocks.clear()
    }

    fun startNewRocks() {
        if (!::rockCols.isInitialized) return

        val lanesAvailable = (0 until rockCols.size).toMutableList()
        val numRocksToStart = 1

        activeRocks.removeAll { it.row == 0 }

        for (i in 0 until numRocksToStart) {
            if (lanesAvailable.isEmpty()) break

            val laneIndex = Random.nextInt(lanesAvailable.size)
            val newLane = lanesAvailable.removeAt(laneIndex)

            val newRock = Rock(0, newLane)
            activeRocks.add(newRock)
            rockCols[newRock.lane][newRock.row].visibility = View.VISIBLE
        }
    }

    fun moveRockStep(onCollision: () -> Unit): Boolean {
        if (!::rockCols.isInitialized || activeRocks.isEmpty()) {
            return false
        }

        var spawnNeeded = false
        val rocksToMove = activeRocks.toList()
        val rocksToRemove = mutableListOf<Rock>()

        for (rock in rocksToMove) {
            if (!activeRocks.contains(rock)) continue

            rockCols[rock.lane][rock.row].visibility = View.INVISIBLE

            if (rock.row == rockCols[rock.lane].size - 1) {
                if (rock.lane == carPos) {
                    onCollision()
                }
                rocksToRemove.add(rock)
            } else {
                rock.row++
                rockCols[rock.lane][rock.row].visibility = View.VISIBLE
                if (rock.row == rockCols[rock.lane].size - 1) {
                    spawnNeeded = true
                }
            }
        }

        activeRocks.removeAll(rocksToRemove)
        return spawnNeeded
    }
}