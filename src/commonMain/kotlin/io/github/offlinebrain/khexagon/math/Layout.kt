package io.github.offlinebrain.khexagon.math

import kotlin.math.sqrt

/**
 * Data class representing the orientation of a hexagonal grid.
 * Read more: [Redblobgames Layout](https://www.redblobgames.com/grids/hexagons/implementation.html#layout)
 *
 * @property fN The forward matrix elements for transforming from hexagonal to offset coordinates.
 * @property bN The backward matrix elements for transforming from offset to hexagonal coordinates.
 * @property startAngle The starting angle of the hexagonal grid.
 */
data class Orientation(
    val f0: Float = 0.0f, val f1: Float = 0.0f, val f2: Float = 0.0f, val f3: Float = 0.0f,
    val b0: Float = 0.0f, val b1: Float = 0.0f, val b2: Float = 0.0f, val b3: Float = 0.0f,
    val startAngle: Float = 0.0f
) {
    companion object {
        /**
         * Orientation for a pointy-top hexagonal grid.
         */
        val Pointy = Orientation(
            f0 = sqrt(3.0f), f1 = sqrt(3.0f) / 2.0f, f2 = 0.0f, f3 = 3.0f / 2.0f,
            b0 = sqrt(3.0f) / 3.0f, b1 = -1.0f / 3.0f, b2 = 0.0f, b3 = 2.0f / 3.0f,
            startAngle = 0.5f
        )

        /**
         * Orientation for a flat-top hexagonal grid.
         */
        val Flat = Orientation(
            f0 = 3.0f / 2.0f, f1 = 0.0f, f2 = sqrt(3.0f) / 2.0f, f3 = sqrt(3.0f),
            b0 = 2.0f / 3.0f, b1 = 0.0f, b2 = -1.0f / 3.0f, b3 = sqrt(3.0f) / 3.0f,
            startAngle = 0.0f
        )
    }
}

data class Point(val x: Float = 0.0f, val y: Float = 0.0f)

/**
 * Represents the layout of a hexagonal grid.
 * Read more: [Redblobgames Layout Examples](https://www.redblobgames.com/grids/hexagons/implementation.html#layout-examples)
 *
 * @property orientation The orientation of the hexagonal grid.
 * @property size The size of the hexagons in the grid. Equal values for width and height indicate regular hexagons.
 * @property origin The origin of the grid. (0, 0) is the center of (0, 0, 0) hexagon.
 */
data class Layout(val orientation: Orientation = Orientation(), val size: Point = Point(), val origin: Point = Point())