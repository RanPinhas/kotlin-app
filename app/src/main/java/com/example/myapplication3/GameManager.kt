package com.example.myapplication3

import android.view.View
import android.widget.ImageView

class GameManager(private val callback: GameCallback) {

    private var score = 0
    private var lives = 3
    private var carPosition = 2 // 0..4 (התחלה באמצע)

    // מערכים של התצוגה (Views)
    private var rockCols: List<List<ImageView>>? = null
    private var cigarCols: List<List<ImageView>>? = null
    private var carLanes: List<ImageView>? = null
    private var lifeHearts: List<ImageView>? = null


    private val gridRows = 5
    private val gridCols = 5
    private val logicalGrid = Array(gridCols) { IntArray(gridRows) }

    fun initGameGrid(rocks: List<List<ImageView>>, cigars: List<List<ImageView>>) {
        this.rockCols = rocks
        this.cigarCols = cigars
        clearGrid()
    }

    fun initCar(cars: List<ImageView>) {
        this.carLanes = cars
        updateCarUI()
    }

    fun initLife(hearts: List<ImageView>) {
        this.lifeHearts = hearts
        updateLivesUI()
    }

    fun resetGame() {
        score = 0
        lives = 3
        carPosition = 2
        clearGrid()
        updateCarUI()
        updateLivesUI()
    }

    private fun clearGrid() {
        for (i in 0 until gridCols) {
            for (j in 0 until gridRows) {
                logicalGrid[i][j] = 0
                rockCols?.get(i)?.get(j)?.visibility = View.INVISIBLE
                cigarCols?.get(i)?.get(j)?.visibility = View.INVISIBLE
            }
        }
    }

    // --- תנועת המכונית ---

    fun moveLeft() {
        if (carPosition > 0) {
            carPosition--
            updateCarUI()
        }
    }

    fun moveRight() {
        if (carPosition < 4) {
            carPosition++
            updateCarUI()
        }
    }

    fun canMoveLeft(): Boolean = carPosition > 0
    fun canMoveRight(): Boolean = carPosition < 4

    private fun updateCarUI() {
        carLanes?.forEachIndexed { index, imageView ->
            imageView.visibility = if (index == carPosition) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun updateLivesUI() {
        lifeHearts?.forEachIndexed { index, imageView ->
            imageView.visibility = if (index < lives) View.VISIBLE else View.INVISIBLE
        }
    }

    // --- לוגיקת נפילת עצמים ---

    fun spawnOneItem() {
        val randomCol = (0 until gridCols).random()
        // בחירה רנדומלית: 1 = אבן, 2 = סיגר
        // נניח 20% סיכוי לסיגר
        val type = if ((1..5).random() == 1) 2 else 1

        logicalGrid[randomCol][0] = type
        updateCellUI(randomCol, 0, type)
    }

    fun moveItemStep(
        onCollision: () -> Unit,
        onPass: () -> Unit,
        onBonus: () -> Unit
    ) {
        // רצים מלמטה למעלה כדי לא לדרוס נתונים
        for (col in 0 until gridCols) {
            // בדיקת השורה האחרונה (התחתונה)
            val itemAtBottom = logicalGrid[col][gridRows - 1]

            if (itemAtBottom != 0) {
                // בדיקה אם המכונית נמצאת בעמודה הזו
                if (col == carPosition) {
                    if (itemAtBottom == 1) {
                        // התנגשות באבן!
                        callback.collisionDetected() // הודעה ל-SignalManager
                        onCollision() // לוגיקה נוספת של ה-MainActivity
                    } else if (itemAtBottom == 2) {
                        // תפיסת סיגר!
                        onBonus()
                    }
                } else {
                    onPass()
                }
                // מחיקה מהלוגיקה ומהמסך
                logicalGrid[col][gridRows - 1] = 0
                updateCellUI(col, gridRows - 1, 0)
            }

            // הזזת שאר השורות למטה
            for (row in gridRows - 1 downTo 1) {
                val itemAbove = logicalGrid[col][row - 1]
                logicalGrid[col][row] = itemAbove
                updateCellUI(col, row, itemAbove)
            }

            // ניקוי השורה העליונה
            logicalGrid[col][0] = 0
            updateCellUI(col, 0, 0)
        }
    }

    private fun updateCellUI(col: Int, row: Int, type: Int) {
        // הסתרת הכל בתא הזה
        rockCols?.get(col)?.get(row)?.visibility = View.INVISIBLE
        cigarCols?.get(col)?.get(row)?.visibility = View.INVISIBLE

        // הצגת החדש
        if (type == 1) {
            rockCols?.get(col)?.get(row)?.visibility = View.VISIBLE
        } else if (type == 2) {
            cigarCols?.get(col)?.get(row)?.visibility = View.VISIBLE
        }
    }

    fun decreaseLife(): Int {
        lives--
        updateLivesUI()
        callback.livesUpdated(lives)
        return lives
    }
}