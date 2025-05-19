import android.graphics.PointF

class Circle(private val startX: Float, private val startY: Float) {
    // Точки траектории пальца
    private val points = mutableListOf(PointF(startX, startY))

    // Направления движения (1-4: вправо-вверх, вправо-вниз, влево-вниз, влево-вверх)
    private var lastDirection = 0
    private var directionChanges = 0

    /** Добавляет новую точку в траекторию */
    fun addPoint(x: Float, y: Float) {
        points.add(PointF(x, y))
        updateDirection(x, y)
    }

    /** Обновляет текущее направление движения */
    private fun updateDirection(x: Float, y: Float) {
        val newDir = when {
            x > points.last().x && y < points.last().y -> 1 // Вправо-вверх
            x > points.last().x && y > points.last().y -> 2 // Вправо-вниз
            x < points.last().x && y > points.last().y -> 3 // Влево-вниз
            x < points.last().x && y < points.last().y -> 4 // Влево-вверх
            else -> lastDirection
        }

        if (newDir != lastDirection) directionChanges++
        lastDirection = newDir
    }

    /** Проверяет, является ли траектория завершенным кругом */
    fun isComplete(): Boolean {
        // Критерии круга:
        // 1. Минимум 50 точек траектории
        // 2. Не менее 8 смен направления
        // 3. Расстояние между начальной и конечной точкой < 50px
        return points.size > 50 &&
                directionChanges >= 8 &&
                distance(points.first(), points.last()) < 50f
    }

    /** Вычисляет расстояние между точками */
    private fun distance(p1: PointF, p2: PointF): Float {
        return Math.sqrt(
            (p1.x - p2.x).toDouble().pow(2) +
                    (p1.y - p2.y).toDouble().pow(2)
        ).toFloat()
    }

    /** Возвращает все точки для отрисовки */
    fun getPoints(): List<PointF> = points
}