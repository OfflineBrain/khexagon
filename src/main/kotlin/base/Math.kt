package base

import kotlin.math.abs

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