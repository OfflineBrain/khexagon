package samples

import base.math.circle
import base.coordinates.HexCoordinates
import base.math.ring


fun mathCircleCollect() {
    val radius = 2
    val originQ = 0
    val originR = 0
    val result = mutableListOf<HexCoordinates>()
    circle(originQ, originR, radius) { q, r ->
        result.add(HexCoordinates(q, r))
    }
    println(result)
}


fun mathRingCollect() {
    val radius = 2
    val originQ = 0
    val originR = 0
    val result = mutableListOf<HexCoordinates>()
    ring(originQ, originR, radius) { q, r ->
        result.add(HexCoordinates(q, r))
    }
    println(result)
}