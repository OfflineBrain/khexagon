package base.math

import base.coordinates.FractionalHexCoordinates
import base.coordinates.HexCoordinates
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
 * Calls the provided callback on each point within a hexagonal area defined by provided origin and radius.
 *
 * @param originQ The 'q' coordinate of the origin point. Default is 0.
 * @param originR The 'r' coordinate of the origin point. Default is 0.
 * @param radius The radius of the hexagonal area to loop over.
 * @param callback A function to call on each point within the defined area.
 *
 * @sample [samples.mathCircleCollect]
 */
fun circle(originQ: Int = 0, originR: Int = 0, radius: Int, callback: (Int, Int) -> Unit) {
    for (q in -radius..radius) {
        for (r in maxOf(-radius, -q - radius)..minOf(radius, -q + radius)) {
            callback(originQ + q, originR + r)
        }
    }
}


/**
 * Calls the provided callback on each point forming a ring in a hexagonal grid defined by provided origin and radius.
 *
 * @param originQ The 'q' coordinate of the origin point. Default is 0.
 * @param originR The 'r' coordinate of the origin point. Default is 0.
 * @param radius The radius of the ring to loop over.
 * @param callback A function to call on each point along the ring.
 *
 * @sample [samples.mathRingCollect]
 */
fun ring(originQ: Int = 0, originR: Int = 0, radius: Int, callback: (Int, Int) -> Unit) {
    val opposite = HexCoordinates.directions[4]
    var hex = HexCoordinates.from(originQ, originR) + HexCoordinates.from(opposite.q * radius, opposite.r * radius)

    for (direction in HexCoordinates.directions) {
        for (j in 0..radius) {
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

        for (i in 0 until dq) {
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

        for (i in 0 until ds) {
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

        for (i in 0 until dr) {
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

private fun diff(a: Int, b: Int) = if (a < b) (b - a) to 1 else (a - b) to -1


fun flatHexWidth(radius: Int) = radius * 2
fun flatHexHeight(radius: Int) = (radius * cos(PI / 6) * 2).toInt()

fun pointyHexWidth(radius: Int) = flatHexHeight(radius)

fun pointyHexHeight(radius: Int) = flatHexWidth(radius)

fun pixelToHex(layout: Layout, point: Point): FractionalHexCoordinates {
    val m = layout.orientation
    val size = layout.size
    val origin = layout.origin
    val pt = Point((point.x - origin.x) / size.x, (point.y - origin.y) / size.y)
    val q = m.b0 * pt.x + m.b1 * pt.y
    val r = m.b2 * pt.x + m.b3 * pt.y
    return FractionalHexCoordinates(q, r, -q - r)
}

fun hexToPixel(layout: Layout, hex: HexCoordinates): Point {
    val m = layout.orientation
    val size = layout.size
    val origin = layout.origin
    val x = (m.f0 * hex.q + m.f1 * hex.r) * size.x
    val y = (m.f2 * hex.q + m.f3 * hex.r) * size.y
    return Point(x + origin.x, y + origin.y)
}

fun hexCornerOffset(layout: Layout, corner: Int): Point {
    val m = layout.orientation
    val size = layout.size
    val angle = 2.0 * PI * (m.startAngle - corner) / 6
    return Point(size.x * cos(angle).toFloat(), size.y * sin(angle).toFloat())
}

fun polygonCorners(layout: Layout, hex: HexCoordinates): List<Point> {
    val corners = mutableListOf<Point>()
    val center = hexToPixel(layout, hex)
    for (i in 0..5) {
        val offset = hexCornerOffset(layout, i)
        corners.add(Point(center.x + offset.x, center.y + offset.y))
    }
    return corners
}

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

    return HexCoordinates.from(qInt, rInt)
}
