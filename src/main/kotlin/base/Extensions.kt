package base

import kotlin.math.abs
import kotlin.math.round

infix fun <T : Coordinates<T>> T.distanceTo(other: Coordinates<*>): Int {
    val left = this.hex
    val right = other.hex
    return maxOf(
        abs(left.q - right.q),
        abs(left.q + left.r - right.q - right.r),
        abs(left.r - right.r),
    )
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
    var sInt = round(-q - r).toInt()

    val qDiff = abs(qInt - q)
    val rDiff = abs(rInt - r)
    val sDiff = abs(sInt - (-q - r))

    if (qDiff > rDiff && qDiff > sDiff) {
        qInt = -rInt - sInt
    } else if (rDiff > sDiff) {
        rInt = -qInt - sInt
    } else {
        sInt = -qInt - rInt
    }

    return HexCoordinates(qInt, rInt, sInt)
}