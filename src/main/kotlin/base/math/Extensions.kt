package base.math

import base.coordinates.Coordinates
import base.coordinates.HexCoordinates

/**
 * Calculates the hexagonal distance from this coordinate to another.
 *
 * @param other The other coordinate.
 * @return The distance.
 *
 * @see [base.math.distance]
 */
infix fun <T : Coordinates<T>> T.distanceTo(other: Coordinates<*>): Int {
    val left = this.hex
    val right = other.hex
    return distance(left.q, left.r, right.q, right.r)
}


/**
 * Generates a line of coordinates from this coordinate to another.
 *
 * @param other The destination coordinate.
 * @return A list of coordinates marking a path between the two points.
 */
infix fun <T : Coordinates<T>> T.lineTo(other: Coordinates<*>): List<T> {
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
 * @see [base.math.bresenhamsLine]
 */
infix fun <T : Coordinates<T>> T.bresenhamsLine(end: Coordinates<*>): List<T> {
    val left = this.hex
    val right = end.hex
    val line = mutableListOf<T>()
    bresenhamsLine(left.q, left.r, right.q, right.r) { q, r ->
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
 * @see [base.math.circle]
 */
fun <T : Coordinates<T>> T.circle(radius: Int): List<T> {
    val result = mutableListOf<T>()
    for (q in -radius..radius) {
        for (r in maxOf(-radius, -q - radius)..minOf(radius, -q + radius)) {
            result.add(HexCoordinates.from(q, r).into())
        }
    }
    return result
}