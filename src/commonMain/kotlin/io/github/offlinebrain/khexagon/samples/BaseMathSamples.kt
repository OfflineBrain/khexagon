package io.github.offlinebrain.khexagon.samples

import io.github.offlinebrain.khexagon.coordinates.HexCoordinates
import io.github.offlinebrain.khexagon.math.circle
import io.github.offlinebrain.khexagon.math.ring


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