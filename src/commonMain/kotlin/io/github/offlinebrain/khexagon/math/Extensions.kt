package io.github.offlinebrain.khexagon.math

import io.github.offlinebrain.khexagon.coordinates.AxisPoint
import io.github.offlinebrain.khexagon.coordinates.Coordinates
import io.github.offlinebrain.khexagon.coordinates.FromHexCoordinates
import io.github.offlinebrain.khexagon.coordinates.HexCoordinates

/**
 * Calculates the hexagonal distance from this coordinate to another.
 *
 * @param other The other coordinate.
 * @return The distance.
 *
 * @see [io.github.offlinebrain.khexagon.math.distance]
 */
infix fun <T : AxisPoint> T.distanceTo(other: AxisPoint): Int {
    return distance(this.q, this.r, other.q, other.r)
}


/**
 * Generates a line of coordinates from this coordinate to another.
 *
 * @param other The destination coordinate.
 * @return A list of coordinates marking a path between the two points.
 */
infix fun <T> T.lineTo(other: Coordinates<*>): List<T>
        where T : Coordinates<T>, T : FromHexCoordinates<T> {
    val left = this.hex
    val right = other.hex
    val distance = this distanceTo other
    val result = mutableListOf<T>()
    for (i in 0..distance) {
        val q = left.q + (right.q - left.q) * i * 1.0f / distance
        val r = left.r + (right.r - left.r) * i * 1.0f / distance
        result.add(hexRound(q, r).into())
    }
    return result
}


/**
 * Uses Bresenham's line generation algorithm to create a path from this coordinate to another.
 *
 * This function generates a symmetric line - meaning swapping the start and end points won't affect the result.
 *
 * @param end The destination coordinate.
 * @return A list containing coordinates of each point on the line.
 *
 * @see [io.github.offlinebrain.khexagon.math.bresenhamsLine]
 */
infix fun <T> T.bresenhamsLine(end: AxisPoint): List<T>
        where T : AxisPoint, T : FromHexCoordinates<T> {
    val line = mutableListOf<T>()
    bresenhamsLine(this.q, this.r, end.q, end.r) { q, r ->
        line.add(HexCoordinates(q, r).into())
    }
    return line
}


/**
 * Generates a list of coordinates forming a circular region at this coordinate with a given radius.
 *
 * @param radius The radius of the circular region.
 * @return A list of coordinates within the circular region.
 *
 * @see [io.github.offlinebrain.khexagon.math.circle]
 */
fun <T> T.circle(radius: Int): List<T>
        where T : AxisPoint, T : FromHexCoordinates<T> {
    val result = mutableListOf<T>()
    for (q in -radius..radius) {
        for (r in maxOf(-radius, -q - radius)..minOf(radius, -q + radius)) {
            result.add(HexCoordinates.cached(q + this.q, r + this.r).into())
        }
    }
    return result
}
