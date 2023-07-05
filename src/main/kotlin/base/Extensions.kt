package base

import kotlin.math.abs

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
        val q = left.q + (right.q - left.q) * i / distance
        val r = left.r + (right.r - left.r) * i / distance
        result.add(HexCoordinates(q, r).into())
    }
    return result
}