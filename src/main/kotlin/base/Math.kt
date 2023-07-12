package base

import kotlin.math.*

fun distance(fromQ: Int, fromR: Int, toQ: Int, toR: Int): Int {
    val diffQ = abs(fromQ - toQ)
    val diffR = abs(fromR - toR)
    val diffS = abs(-fromQ - fromR - (-toQ - toR))
    return maxOf(diffQ, diffR, diffS)
}

fun circle(originQ: Int = 0, originR: Int = 0, radius: Int, callback: (Int, Int) -> Unit) {
    for (q in -radius..radius) {
        for (r in maxOf(-radius, -q - radius)..minOf(radius, -q + radius)) {
            callback(originQ + q, originR + r)
        }
    }
}

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
