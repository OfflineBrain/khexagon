package io.github.offlinebrain.khexagon.algorythm

import io.github.offlinebrain.khexagon.coordinates.AxisPoint
import io.github.offlinebrain.khexagon.coordinates.HexCoordinates
import io.github.offlinebrain.khexagon.math.bresenhamsLine
import io.github.offlinebrain.khexagon.math.circle
import io.github.offlinebrain.khexagon.math.distance

private fun losKeygen(x: Int, y: Int, radius: Int) = radius + x + (2 * radius + 1) * (y + radius)


/**
 *  A provider for pre-computed tries for the line of sight (LoS) on a symmetric grid.
 *
 *  This class provides you with the ability to pre-compute the tries in order to enhance the speed of common operations e.g., line-of-sight (LoS), field-of-view (FoV), etc.
 *
 *  @param radius the vision radius for the tries.
 */
class SymmetricPreComputedVisionTries(private val radius: Int) {
    val root: TrieNode = TrieNode(
        q = 0,
        r = 0,
    )

    val fastLoSMap: MutableMap<Int, MutableList<TrieNode>> = mutableMapOf()

    init {
        circle(
            radius = radius,
            callback = { q, r ->
                root.add(q, r, radius) { key, trie -> fastLoSMap.getOrPut(key) { mutableListOf() }.add(trie) }
            }
        )
    }

    /**
     *  Converts provided coordinates into line-of-sight data.
     *
     *  @param from The starting point of the line-of-sight.
     *  @param to The ending point of the line-of-sight.
     *  @param radius The vision radius (default is the class's instance radius).
     *  @param doesBlockVision A function that checks whether the line-of-sight is blocked.
     *
     *  @return A set of [HexCoordinates] that represents all visible coordinates in the line-of-sight from the start to the end point.
     */
    fun lineOfSight(
        from: AxisPoint,
        to: AxisPoint,
        radius: Int = this.radius,
        doesBlockVision: (Int, Int) -> Boolean,
    ): Set<HexCoordinates> {
        val result = mutableSetOf<HexCoordinates>()

        lineOfSight(from, to, radius, doesBlockVision) { q: Int, r: Int ->
            result.add(HexCoordinates.cached(q, r))
        }

        return result
    }

    /**
     *  Converts provided coordinates into line-of-sight data.
     *
     *  @param from The starting point of the line-of-sight.
     *  @param to The ending point of the line-of-sight.
     *  @param radius The vision radius (default is the class's instance radius).
     *  @param doesBlockVision A function that checks whether the line-of-sight is blocked.
     *  @param callback A callback function that is called for each visible coordinate.
     *
     *  @return true if the line-of-sight is not blocked, false otherwise.
     */
    fun lineOfSight(
        from: AxisPoint,
        to: AxisPoint,
        radius: Int = this.radius,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, Int) -> Unit
    ): Boolean {
        return lineOfSight(from.q, from.r, to.q, to.r, radius, doesBlockVision) { _: Int, trie: TrieNode ->
            callback(trie.q, trie.r)
        }
    }


    /**
     *  Converts provided coordinates into line-of-sight data.
     *
     *  @param fromQ The starting point of the line-of-sight (q).
     *  @param fromR The starting point of the line-of-sight (r).
     *  @param toQ The ending point of the line-of-sight (q).
     *  @param toR The ending point of the line-of-sight (r).
     *  @param radius The vision radius (default is the class's instance radius).
     *  @param doesBlockVision A function that checks whether the line-of-sight is blocked.
     *  @param callback A callback function that is called for each visible coordinate.
     *
     *  @return true if the line-of-sight is not blocked, false otherwise.
     */
    fun lineOfSight(
        fromQ: Int, fromR: Int, toQ: Int, toR: Int,
        radius: Int = this.radius,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, Int) -> Unit
    ): Boolean {
        return lineOfSight(fromQ, fromR, toQ, toR, radius, doesBlockVision) { _: Int, trie: TrieNode ->
            callback(trie.q, trie.r)
        }
    }

    /**
     *  Converts provided coordinates into line-of-sight data.
     *
     *  @param fromQ The starting point of the line-of-sight (q).
     *  @param fromR The starting point of the line-of-sight (r).
     *  @param toQ The ending point of the line-of-sight (q).
     *  @param toR The ending point of the line-of-sight (r).
     *  @param radius The vision radius (default is the class's instance radius).
     *  @param doesBlockVision A function that checks whether the line-of-sight is blocked.
     *  @param callback A callback function that is called for each visible coordinate.
     *
     *  @return true if the line-of-sight is not blocked, false otherwise.
     */
    internal fun lineOfSight(
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


    /**
     *  Generates a field of view from a specified point.
     *
     *  @param from The starting point of the field of view.
     *  @param doesBlockVision A function to indicate whether a coordinate blocks vision.
     *
     *  @return A set of [HexCoordinates] that represents all visible coordinates in the field of view.
     */
    fun fieldOfView(
        from: AxisPoint,
        doesBlockVision: (Int, Int) -> Boolean,
    ): Set<HexCoordinates> {
        val result = mutableSetOf<HexCoordinates>()

        fieldOfView(from, doesBlockVision) { q, r ->
            result.add(HexCoordinates.cached(q, r))
        }

        return result
    }

    /**
     *  Generates a field of view from a specified point.
     *
     *  @param from The starting point of the field of view.
     *  @param doesBlockVision A function to indicate whether a coordinate blocks vision.
     *  @param callback An optional callback function that is called for every visible coordinate in the field of view.
     */
    fun fieldOfView(
        from: AxisPoint,
        doesBlockVision: (Int, Int) -> Boolean,
        callback: (Int, Int) -> Unit
    ) = fieldOfView(from.q, from.r, doesBlockVision, callback)

    /**
     *  Generates a field of view from a specified point.
     *
     *  @param fromQ The starting point (q) of the field of view.
     *  @param fromR The starting point (r) of the field of view.
     *  @param doesBlockVision A function to indicate whether a coordinate blocks vision.
     *  @param callback An optional callback function that is called for every visible coordinate in the field of view.
     */
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

/**
 *  Data class defining a trie node for the SymmetricPreComputedVisionTries.
 *
 *  @param parent The parent node.
 *  @param q The (q) component of the coordinate in the hexagonal grid.
 *  @param r The (r) component of the coordinate in the hexagonal grid.
 *  @param childrenIndices The indices of the children nodes.
 */
data class TrieNode(
    val parent: TrieNode? = null,
    val q: Int,
    val r: Int,
    val childrenIndices: MutableList<Int?> = MutableList(10) { null },
) {
    private val childrenNodes: MutableList<TrieNode> = mutableListOf()

    internal fun add(coordinates: AxisPoint, radius: Int, callback: (Int, TrieNode) -> Unit = { _, _ -> }) =
        add(coordinates.q, coordinates.r, radius, callback)

    internal fun add(
        destinationQ: Int,
        destinationR: Int,
        radius: Int,
        callback: (Int, TrieNode) -> Unit = { _, _ -> }
    ) {
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

        bresenhamsLine(0, 0, destinationQ, destinationR, cb)
    }

    /**
     * Performs a pre-order traversal of the tries or its sub-tries.
     *
     * @param shouldStop the function to determine whether to stop the traversal.
     */
    internal fun preOrder(shouldStop: (Int, Int) -> Boolean) {
        if (shouldStop(q, r)) {
            return
        }
        childrenNodes.forEach { it.preOrder(shouldStop) }
    }
}
