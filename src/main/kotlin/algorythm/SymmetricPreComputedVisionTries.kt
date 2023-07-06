package algorythm

import base.Coordinates
import base.HexCoordinates
import base.circle
import base.distanceTo

typealias SPCVT = SymmetricPreComputedVisionTries

class SymmetricPreComputedVisionTries(private val radius: Int) {
    val root: TrieNode
    val fastLoSMap: MutableMap<Int, MutableList<TrieNode>> = mutableMapOf()

    init {
        root = TrieNode(
            coordinates = HexCoordinates(0, 0)
        )
        HexCoordinates(0, 0).circle(radius)
            .forEach { root.add(it, radius) { key, trie -> fastLoSMap.getOrPut(key) { mutableListOf() }.add(trie) } }
    }

    fun lineOfSight(
        from: HexCoordinates, to: HexCoordinates, radius: Int = this.radius,
        doesBlockVision: (HexCoordinates) -> Boolean,
        callback: (Int, TrieNode) -> Unit = { _, _ -> }
    ): Boolean {
        val distance = from distanceTo to
        val diff = to - from

        if (distance > radius) {
            return false
        }

        fun losKetgen(x: Int, y: Int) = radius + x + (2 * radius + 1) * (y + radius)

        var trace: TrieNode? = null
        for (it in fastLoSMap[losKetgen(diff.q, diff.r)] ?: emptyList()) {
            trace = it
            var cur: TrieNode? = trace
            while (cur != null) {
                if (doesBlockVision(cur.coordinates)) {
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
                callback(losKetgen(cur.coordinates.q, cur.coordinates.r), cur)
                cur = cur.parent
            }
        }
        return trace != null
    }

    fun fieldOfView(
        from: HexCoordinates,
        doesBlockVision: (HexCoordinates) -> Boolean,
        callback: (Int, HexCoordinates) -> Unit = { _, _ -> }
    ) {
        fun run(hex: HexCoordinates): Boolean {
            val current = hex + from
            if (doesBlockVision(current)) {
                return true
            }
            callback(0, current)
            return false
        }

        root.preOrder(::run)
    }
}

data class TrieNode(
    val parent: TrieNode? = null,
    val children: MutableMap<Int, TrieNode> = mutableMapOf(),
    val coordinates: HexCoordinates
) {

    fun add(coordinates: HexCoordinates, radius: Int, callback: (Int, TrieNode) -> Unit = { _, _ -> }) {
        fun losKetgen(x: Int, y: Int) = radius + x + (2 * radius + 1) * (y + radius)

        var q = coordinates.q
        var r = coordinates.r
        var current = this

        val callback = { newQ: Int, newR: Int ->
            val hex = HexCoordinates(newQ, newR)
            if (hex distanceTo HexCoordinates(0, 0) <= radius) {
                var dq = newQ - q
                var dr = newR - r

                if (!(dq == 0 && dr == 0)) {
                    q = newQ
                    r = newR

                    val key = dq + 1 + (dr + 1) * 3

                    if (current.children[key] == null) {
                        val child = TrieNode(
                            coordinates = HexCoordinates(q, r),
                            parent = current,
                        )
                        callback(losKetgen(q, r), child)
                        current.children[key] = child
                    }
                    current = current.children[key]!!
                }
            }
        }

        bresenhamsLine(HexCoordinates(0, 0), coordinates, callback)
    }

    fun preOrder(shouldStop: (HexCoordinates) -> Boolean) {
        if (shouldStop(coordinates.hex)) {
            return
        }
        children.values.forEach { it.preOrder(shouldStop) }
    }
}

fun bresenhamsLine(start: Coordinates<*>, end: Coordinates<*>, process: (x: Int, y: Int) -> Unit) {
    fun diff(a: Int, b: Int) = if (a < b) (b - a) to 1 else (a - b) to -1
    process(start.hex.q, start.hex.r)

    val (dq, sq) = diff(start.hex.q, end.hex.q)
    val (dr, sr) = diff(start.hex.r, end.hex.r)
    val (ds, ss) = diff(start.hex.s, end.hex.s)

    var test = if (sr == -1) -1 else 0

    var q = start.hex.q
    var r = start.hex.r
    var s = start.hex.s

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