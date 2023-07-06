package algorythm

import base.HexCoordinates
import base.circle
import base.distance

typealias SPCVT = SymmetricPreComputedVisionTries

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
        callback: (Int, TrieNode) -> Unit = { _, _ -> },
    ): Boolean {
        return lineOfSight(from.q, from.r, to.q, to.r, radius, doesBlockVision, callback)
    }

    fun lineOfSight(
        fromQ: Int, fromR: Int, toQ: Int, toR: Int, radius: Int = this.radius,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, TrieNode) -> Unit = { _, _ -> }
    ): Boolean {
        val distance = distance(fromQ, fromR, toQ, toR)
        val diffQ = toQ - fromQ
        val diffR = toR - fromR

        if (distance > radius) {
            return false
        }

        fun losKetgen(x: Int, y: Int) = radius + x + (2 * radius + 1) * (y + radius)

        var trace: TrieNode? = null
        for (it in fastLoSMap[losKetgen(diffQ, diffR)] ?: emptyList()) {
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
                callback(losKetgen(cur.q, cur.r), cur)
                cur = cur.parent
            }
        }
        return trace != null
    }

    fun fieldOfView(
        from: HexCoordinates,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, Int) -> Unit = { _, _ -> }
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
    val children: MutableMap<Int, TrieNode> = mutableMapOf(),
    val q: Int,
    val r: Int,
) {

    fun add(coordinates: HexCoordinates, radius: Int, callback: (Int, TrieNode) -> Unit = { _, _ -> }) =
        add(coordinates.q, coordinates.r, radius, callback)

    fun add(Q: Int, R: Int, radius: Int, callback: (Int, TrieNode) -> Unit = { _, _ -> }) {
        fun losKetgen(x: Int, y: Int) = radius + x + (2 * radius + 1) * (y + radius)

        var q = Q
        var r = R
        var current = this

        val cb = { newQ: Int, newR: Int ->

            if (distance(newQ, newR, 0, 0) <= radius) {
                val dq = newQ - q
                val dr = newR - r

                if (!(dq == 0 && dr == 0)) {
                    q = newQ
                    r = newR

                    val key = dq + 1 + (dr + 1) * 3

                    if (current.children[key] == null) {
                        val child = TrieNode(
                            q = q,
                            r = r,
                            parent = current,
                        )
                        callback(losKetgen(q, r), child)
                        current.children[key] = child
                    }
                    current = current.children[key]!!
                }
            }
        }

        bresenhamsLine(0, 0, Q, R, cb)
    }

    fun preOrder(shouldStop: (Int, Int) -> Boolean) {
        if (shouldStop(q, r)) {
            return
        }
        children.values.forEach { it.preOrder(shouldStop) }
    }
}

private fun diff(a: Int, b: Int) = if (a < b) (b - a) to 1 else (a - b) to -1

fun bresenhamsLine(start: HexCoordinates, end: HexCoordinates, process: (x: Int, y: Int) -> Unit) =
    bresenhamsLine(start.q, start.r, end.q, end.r, process)

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