package base

import kotlin.math.abs
import kotlin.math.round

infix fun <T : Coordinates<T>> T.distanceTo(other: Coordinates<*>): Int {
    val left = this.hex
    val right = other.hex
    return distance(left.q, left.r, right.q, right.r)
}


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

private fun hexRound(q: Float, r: Float): HexCoordinates {
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

fun <T : Coordinates<T>> T.circle(radius: Int): List<T> {
    val result = mutableListOf<T>()
    for (q in -radius..radius) {
        for (r in maxOf(-radius, -q - radius)..minOf(radius, -q + radius)) {
            result.add(HexCoordinates.from(q, r).into())
        }
    }
    return result
}
