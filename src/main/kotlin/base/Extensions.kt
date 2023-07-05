package base

import kotlin.math.abs

infix fun <T : Coordinates> T.distanceTo(other: T): Int {
    val left = this.hex
    val right = other.hex
    return maxOf(
        abs(left.q - right.q),
        abs(left.q + left.r - right.q - right.r),
        abs(left.r - right.r),
    )
}