package base.math

import kotlin.math.sqrt

data class Orientation(
    val f0: Float = 0.0f, val f1: Float = 0.0f, val f2: Float = 0.0f, val f3: Float = 0.0f,
    val b0: Float = 0.0f, val b1: Float = 0.0f, val b2: Float = 0.0f, val b3: Float = 0.0f,
    val startAngle: Float = 0.0f
) {
    companion object {
        val Pointy = Orientation(
            f0 = sqrt(3.0f), f1 = sqrt(3.0f) / 2.0f, f2 = 0.0f, f3 = 3.0f / 2.0f,
            b0 = sqrt(3.0f) / 3.0f, b1 = -1.0f / 3.0f, b2 = 0.0f, b3 = 2.0f / 3.0f,
            startAngle = 0.5f
        )

        val Flat = Orientation(
            f0 = 3.0f / 2.0f, f1 = 0.0f, f2 = sqrt(3.0f) / 2.0f, f3 = sqrt(3.0f),
            b0 = 2.0f / 3.0f, b1 = 0.0f, b2 = -1.0f / 3.0f, b3 = sqrt(3.0f) / 3.0f,
            startAngle = 0.0f
        )
    }
}

data class Point(val x: Float = 0.0f, val y: Float = 0.0f) {
}

data class Layout(val orientation: Orientation = Orientation(), val size: Point = Point(), val origin: Point = Point())