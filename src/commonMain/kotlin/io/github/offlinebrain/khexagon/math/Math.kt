package io.github.offlinebrain.khexagon.math

import io.github.offlinebrain.khexagon.coordinates.AxisPoint
import io.github.offlinebrain.khexagon.coordinates.Coordinates
import io.github.offlinebrain.khexagon.coordinates.FractionalHexCoordinates
import io.github.offlinebrain.khexagon.coordinates.FromHexCoordinates
import io.github.offlinebrain.khexagon.coordinates.HexCoordinates
import kotlin.math.*


/**
 * Calculates the hexagonal distance between two points given by their (q, r) coordinates.
 *
 * @param fromQ The 'q' coordinate of the first point.
 * @param fromR The 'r' coordinate of the first point.
 * @param toQ The 'q' coordinate of the second point.
 * @param toR The 'r' coordinate of the second point.
 * @return The calculated distance.
 */
fun distance(fromQ: Int, fromR: Int, toQ: Int, toR: Int): Int {
    val diffQ = abs(fromQ - toQ)
    val diffR = abs(fromR - toR)
    val diffS = abs(-fromQ - fromR - (-toQ - toR))
    return maxOf(diffQ, diffR, diffS)
}

/**
 * Calculates the hexagonal distance from this coordinate to another.
 *
 * @param other The other coordinate.
 * @return The distance.
 *
 * @see [io.github.offlinebrain.khexagon.math.distance]
 */
fun <T : AxisPoint> T.distance(other: AxisPoint): Int = distance(this.q, this.r, other.q, other.r)


/**
 * Calls the provided callback on each point within a hexagonal area defined by provided origin and radius.
 *
 * @param originQ The 'q' coordinate of the origin point. Default is 0.
 * @param originR The 'r' coordinate of the origin point. Default is 0.
 * @param radius The radius of the hexagonal area to loop over.
 * @param callback A function to call on each point within the defined area.
 *
 */
fun circle(originQ: Int = 0, originR: Int = 0, radius: Int, callback: (Int, Int) -> Unit) {
    for (q in -radius..radius) {
        for (r in maxOf(-radius, -q - radius)..minOf(radius, -q + radius)) {
            callback(originQ + q, originR + r)
        }
    }
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
    circle(this.q, this.r, radius) { q, r ->
        result.add(HexCoordinates.cached(q, r).into())
    }
    return result
}


/**
 * Calls the provided callback on each point forming a ring in a hexagonal grid defined by provided origin and radius.
 *
 * @param originQ The 'q' coordinate of the origin point. Default is 0.
 * @param originR The 'r' coordinate of the origin point. Default is 0.
 * @param radius The radius of the ring to loop over.
 * @param callback A function to call on each point along the ring.
 *
 */
fun ring(originQ: Int = 0, originR: Int = 0, radius: Int, callback: (Int, Int) -> Unit) {
    if (radius == 0) {
        callback(originQ, originR)
        return
    } else if (radius < 0) {
        return
    }

    val opposite = HexCoordinates.directions[4]
    var hex = HexCoordinates.cached(originQ, originR) + HexCoordinates.cached(opposite.q * radius, opposite.r * radius)

    for (direction in HexCoordinates.directions) {
        (1..radius).forEach { j ->
            callback(hex.q, hex.r)
            hex += direction
        }
    }
}


/**
 * Implements Bresenham's line generation algorithm to plot a line between two points on a hexagonal grid.
 * Calls the provided process function for each point along the line. This function generates a symmetric line,
 * meaning swapping the start and end points won't affect the result.
 *
 * @param startQ The 'q' coordinate of the first point.
 * @param startR The 'r' coordinate of the first point.
 * @param endQ The 'q' coordinate of the second point.
 * @param endR The 'r' coordinate of the second point.
 * @param process A function that's called for each point on the line.
 */
fun bresenhamsLine(startQ: Int, startR: Int, endQ: Int, endR: Int, process: (x: Int, y: Int) -> Unit) {
    fun diff(a: Int, b: Int) = if (a < b) (b - a) to 1 else (a - b) to -1

    process(startQ, startR)

    val (dq, sq) = diff(startQ, endQ)
    val (dr, sr) = diff(startR, endR)
    val (ds, ss) = diff(-startQ - startR, -endQ - endR)

    var test = if (sr == -1) -1 else 0

    var q = startQ
    var r = startR
    var s = -startQ - startR

    if (dq >= dr && dq >= ds) {
        test = (dq + test) shr 1

        (0 until dq).forEach { i ->
            test -= dr
            q += sq
            if (test < 0) {
                r += sr
                test += dq
            }
            process(q, r)
        }
    } else if (ds >= dr) {
        test = (ds + test) shr 1

        (0 until ds).forEach { i ->
            test -= dr
            s += ss
            if (test < 0) {
                r += sr
                test += ds
            }
            q = -s - r
            process(q, r)
        }
    } else {
        test = (dr + test) shr 1

        (0 until dr).forEach { i ->
            test -= dq
            r += sr
            if (test < 0) {
                q += sq
                test += dr
            }
            process(q, r)
        }
    }
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
fun <T> T.bresenhamsLine(end: AxisPoint): List<T>
        where T : AxisPoint, T : FromHexCoordinates<T> {
    val line = mutableListOf<T>()
    bresenhamsLine(this.q, this.r, end.q, end.r) { q, r ->
        line.add(HexCoordinates(q, r).into())
    }
    return line
}

/**
 * Generates a line of points between two given points in a hexagonal grid.
 * The line is generated by calculating the distance between the start and end points,
 * and then interpolating the coordinates for each step along the line.
 * The provided process function is called for each point along the line.
 *
 * @param startQ The 'q' coordinate of the start point.
 * @param startR The 'r' coordinate of the start point.
 * @param endQ The 'q' coordinate of the end point.
 * @param endR The 'r' coordinate of the end point.
 * @param process A function that's called for each point on the line.
 */
fun line(startQ: Int, startR: Int, endQ: Int, endR: Int, process: (x: Int, y: Int) -> Unit) {
    val distance = distance(startQ, startR, endQ, endR)
    for (i in 0..distance) {
        val q = startQ + (endQ - startQ) * i * 1.0f / distance
        val r = startR + (endR - startR) * i * 1.0f / distance
        process(q.roundToInt(), r.roundToInt())
    }
}


/**
 * Generates a line of coordinates from this coordinate to another.
 *
 * @param other The destination coordinate.
 * @return A list of coordinates marking a path between the two points.
 */
fun <T> T.line(other: Coordinates<*>): List<T>
        where T : Coordinates<T>, T : FromHexCoordinates<T> {
    val result = mutableListOf<T>()
    line(this.q, this.r, other.q, other.r) { q, r ->
        result.add(HexCoordinates.cached(q, r).into())
    }
    return result
}

fun flatHexWidth(radius: Int) = radius * 2
fun flatHexHeight(radius: Int) = (radius * cos(PI / 6) * 2).toInt()

fun pointyHexWidth(radius: Int) = flatHexHeight(radius)

fun pointyHexHeight(radius: Int) = flatHexWidth(radius)

/**
 * Converts a point in pixel coordinates to hexagonal coordinates.
 * This is done by first normalizing the point's coordinates relative to the layout's origin and size,
 * then applying the layout's orientation matrix to the normalized coordinates.
 *
 * @param layout The layout of the hexagonal grid.
 * @param point The point in pixel coordinates.
 * @return The point in hexagonal coordinates.
 */
fun pixelToHex(layout: Layout, point: Point): FractionalHexCoordinates {
    val m = layout.orientation
    val size = layout.size
    val origin = layout.origin
    val pt = Point((point.x - origin.x) / size.x, (point.y - origin.y) / size.y)
    val q = m.b0 * pt.x + m.b1 * pt.y
    val r = m.b2 * pt.x + m.b3 * pt.y
    return FractionalHexCoordinates(q, r, -q - r)
}

/**
 * Converts a point in hexagonal coordinates to pixel coordinates.
 * This is done by applying the layout's orientation matrix to the hexagonal coordinates,
 * then scaling and translating the result by the layout's size and origin.
 *
 * @param layout The layout of the hexagonal grid.
 * @param hex The point in hexagonal coordinates.
 * @return The point in pixel coordinates.
 */
fun hexToPixel(layout: Layout, hex: HexCoordinates): Point {
    val m = layout.orientation
    val size = layout.size
    val origin = layout.origin
    val x = (m.f0 * hex.q + m.f1 * hex.r) * size.x
    val y = (m.f2 * hex.q + m.f3 * hex.r) * size.y
    return Point(x + origin.x, y + origin.y)
}

/**
 * Calculates the offset of a corner of a hexagon.
 * This is done by first calculating the angle of the corner relative to the layout's start angle,
 * then scaling a unit vector at that angle by the layout's size.
 *
 * @param layout The layout of the hexagonal grid.
 * @param corner The index of the corner (0-5).
 * @return The offset of the corner.
 */
fun hexCornerOffset(layout: Layout, corner: Int): Point {
    val m = layout.orientation
    val size = layout.size
    val angle = 2.0 * PI * (m.startAngle - corner) / 6
    return Point(size.x * cos(angle).toFloat(), size.y * sin(angle).toFloat())
}

/**
 * Calculates the corners of a hexagon in pixel coordinates.
 *
 * @param layout The layout of the hexagonal grid.
 * @param hex The hexagon.
 * @return A list of points representing the corners of the hexagon.
 */
fun polygonCorners(layout: Layout, hex: HexCoordinates): List<Point> {
    val corners = mutableListOf<Point>()
    val center = hexToPixel(layout, hex)
    for (i in 0..5) {
        val offset = hexCornerOffset(layout, i)
        corners.add(Point(center.x + offset.x, center.y + offset.y))
    }
    return corners
}

/**
 * Rounds a point in fractional hexagonal coordinates to the nearest integer coordinates.
 *
 * @param q The 'q' coordinate of the point.
 * @param r The 'r' coordinate of the point.
 * @return The point in integer coordinates.
 */
fun hexRound(q: Float, r: Float): HexCoordinates {
    var qInt = round(q).toInt()
    var rInt = round(r).toInt()
    val sInt = round(-q - r).toInt()

    val qDiff = abs(qInt - q)
    val rDiff = abs(rInt - r)
    val sDiff = abs(sInt - (-q - r))

    if (qDiff > rDiff && qDiff > sDiff) {
        qInt = -rInt - sInt
    } else if (rDiff > sDiff) {
        rInt = -qInt - sInt
    }

    return HexCoordinates.cached(qInt, rInt)
}
