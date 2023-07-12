package algorythm

import base.coordinates.HexCoordinates
import base.math.bresenhamsLine
import base.math.circle
import base.math.distance

typealias SPCVT = SymmetricPreComputedVisionTries

fun losKeygen(x: Int, y: Int, radius: Int) = radius + x + (2 * radius + 1) * (y + radius)

class SymmetricPreComputedVisionTries(private val radius: Int) {
    val root: TrieNode
    val fastLoSMap: MutableMap<Int, MutableList<TrieNode>> = mutableMapOf()

    init {
        root = TrieNode(
            q = 0,
            r = 0,
        )
        circle(
            radius = radius,
            callback = { q, r ->
                root.add(q, r, radius) { key, trie -> fastLoSMap.getOrPut(key) { mutableListOf() }.add(trie) }
            }
        )
    }

    fun lineOfSight(
        from: HexCoordinates,
        to: HexCoordinates,
        radius: Int = this.radius,
        doesBlockVision: (Int, Int) -> Boolean,
    ): Set<HexCoordinates> {
        val result = mutableSetOf<HexCoordinates>()

        lineOfSight(from, to, radius, doesBlockVision) { _, trie ->
            result.add(HexCoordinates.from(trie.q, trie.r))
        }

        return result
    }

    fun lineOfSight(
        from: HexCoordinates,
        to: HexCoordinates,
        radius: Int = this.radius,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, TrieNode) -> Unit,
    ): Boolean {
        return lineOfSight(from.q, from.r, to.q, to.r, radius, doesBlockVision, callback)
    }

    fun lineOfSight(
        fromQ: Int, fromR: Int, toQ: Int, toR: Int, radius: Int = this.radius,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, TrieNode) -> Unit
    ): Boolean {
        val distance = distance(fromQ, fromR, toQ, toR)
        val diffQ = toQ - fromQ
        val diffR = toR - fromR

        if (distance > radius) {
            return false
        }


        var trace: TrieNode? = null
        for (it in fastLoSMap[losKeygen(diffQ, diffR, radius)] ?: emptyList()) {
            trace = it
            var cur: TrieNode? = trace
            while (cur != null) {
                if (doesBlockVision(cur.q, cur.r)) {
                    trace = null
                    break
                }

                cur = cur.parent
            }
            if (trace != null) {
                break
            }
        }
        if (callback != { _: Int, _: TrieNode -> }) {
            var cur: TrieNode? = trace
            while (cur != null) {
                callback(losKeygen(cur.q, cur.r, radius), cur)
                cur = cur.parent
            }
        }
        return trace != null
    }

    fun fieldOfView(
        from: HexCoordinates,
        doesBlockVision: (Int, Int) -> Boolean,
    ): Set<HexCoordinates> {
        val result = mutableSetOf<HexCoordinates>()

        fieldOfView(from, doesBlockVision) { q, r ->
            result.add(HexCoordinates.from(q, r))
        }

        return result
    }

    fun fieldOfView(
        from: HexCoordinates,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, Int) -> Unit
    ) = fieldOfView(from.q, from.r, doesBlockVision, callback)

    fun fieldOfView(
        fromQ: Int, fromR: Int,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, Int) -> Unit = { _, _ -> }
    ) {
        fun run(q: Int, r: Int): Boolean {
            val currentQ = q + fromQ
            val currentR = r + fromR
            if (doesBlockVision(currentQ, currentR)) {
                return true
            }
            callback(currentQ, currentR)
            return false
        }


        root.preOrder(::run)
    }
}

data class TrieNode(
    val parent: TrieNode? = null,
    val q: Int,
    val r: Int,
    val childrenIndices: MutableList<Int?> = MutableList(10) { null },
) {
    private val childrenNodes: MutableList<TrieNode> = mutableListOf()

    fun add(coordinates: HexCoordinates, radius: Int, callback: (Int, TrieNode) -> Unit = { _, _ -> }) =
        add(coordinates.q, coordinates.r, radius, callback)

    fun add(Q: Int, R: Int, radius: Int, callback: (Int, TrieNode) -> Unit = { _, _ -> }) {
        fun losKetgen(x: Int, y: Int) = radius + x + (2 * radius + 1) * (y + radius)

        var q = q
        var r = r
        var current = this

        val cb = { newQ: Int, newR: Int ->

            if (distance(newQ, newR, 0, 0) <= radius) {
                val dq = newQ - q
                val dr = newR - r

                if (!(dq == 0 && dr == 0)) {
                    q = newQ
                    r = newR

                    val key = dq + 1 + (dr + 1) * 3

                    if (key < 0 || key > 8) {
                        println("key: $key, dq: $dq, dr: $dr")
                    }

                    var index = current.childrenIndices[key]
                    if (index == null) {
                        val child = TrieNode(
                            q = q,
                            r = r,
                            parent = current,
                        )
                        callback(losKetgen(q, r), child)
                        current.childrenNodes.add(child)
                        index = current.childrenNodes.lastIndex
                        current.childrenIndices[key] = index
                    }
                    current = current.childrenNodes[index]
                }
            }
        }

        bresenhamsLine(0, 0, Q, R, cb)
    }

    fun preOrder(shouldStop: (Int, Int) -> Boolean) {
        if (shouldStop(q, r)) {
            return
        }
        childrenNodes.forEach { it.preOrder(shouldStop) }
    }
}
