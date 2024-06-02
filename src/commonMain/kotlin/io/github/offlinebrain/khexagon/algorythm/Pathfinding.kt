package io.github.offlinebrain.khexagon.algorythm

/**
 * Represents a path tile with generic type `T`.
 *
 * @param T the type of objects that can be used for movement cost and heuristic calculations.
 */
interface PathTile<T : PathTile<T>> {

    /**
     * Calculates the movement cost to another tile.
     *
     * This method is used by the pathfinding algorithm to determine the cost of moving from this tile to another tile.
     *
     * @param to the tile to move to.
     * @return the cost of moving to the specified tile.
     */
    infix fun moveCostTo(to: T): Double

    /**
     * Checks if the tile is walkable.
     *
     * This method is used by the pathfinding algorithm to determine if a tile can be traversed.
     *
     * @return `true` if the tile is walkable, `false` otherwise.
     */
    fun isWalkable(): Boolean

    /**
     * Calculates the heuristic to another tile.
     *
     * This method is used by the pathfinding algorithm to estimate the distance from this tile to another tile.
     * It should return 0 if the tiles are the same.
     *
     * @param to the tile to calculate the heuristic to.
     * @return the heuristic to the specified tile.
     */
    infix fun distanceTo(to: T): Int
}

/**
 * Applies the A* pathfinding algorithm on a given graph to find the shortest path from the start point [from] to the destination [to].
 *
 * @param [from] The starting point in the graph.
 * @param [to] The destination point in the graph.
 * @param [neighbors] A function that takes a point and returns a list of its neighboring points.
 * @param [isWalkable] A function that takes a point and returns whether the point can be traversed or not.
 * @param [distance] A function that estimates the distance between two points.
 * @param [movementCost] A function that calculates the exact movement cost between two points. By default, it returns a constant cost of 1.0 for any pair of points or 0.0 if points are equal.
 * @return a list of points representing the shortest path from [from] to [to] based on the provided functions. Returns an empty list if no path is found.
 */
fun <T> aStar(
    from: T,
    to: T,
    neighbors: (T) -> List<T>,
    isWalkable: (T) -> Boolean,
    distance: (T, T) -> Int,
    movementCost: (T, T) -> Double
): List<T> {
    if (!isWalkable(from) || !isWalkable(to)) return emptyList()
    if (distance(from, to) == 0) return listOf(from)

    val moveCost = movementCost(from, from)
    val candidates = mutableListOf(from to moveCost)
    val costs = mutableMapOf(from to moveCost)
    val path = mutableMapOf<T, T>()

    while (candidates.isNotEmpty()) {
        val current = candidates.removeLast()
        if (current.first == to) {
            break
        }

        neighbors(current.first).filter { isWalkable(it) }.forEach { neighbor ->
            val previousCost = costs[neighbor]
            val newCost = (costs[current.first] ?: 0.0) + movementCost(current.first, neighbor)
            if (previousCost == null || newCost < previousCost) {
                costs[neighbor] = newCost
                val priority = newCost + distance(current.first, neighbor)
                candidates.apply {
                    add(neighbor to priority)
                    sortByDescending { it.second }
                }
                path[neighbor] = current.first
            }
        }
    }

    var backward: T? = path[to] ?: return emptyList()
    val result = mutableListOf(to)
    while (backward != null) {
        result.add(backward)
        backward = path[backward]
    }

    return result.reversed()
}

fun <T> aStar(
    from: T,
    to: T,
    neighbors: (T) -> List<T>,
): List<T> where T : PathTile<T> =
    aStar(from, to, neighbors, PathTile<T>::isWalkable, PathTile<T>::distanceTo, PathTile<T>::moveCostTo)

/**
 * A data class that implements a pathfinding algorithm on a graph represented by points of type `T`.
 *
 * @property origin The initial point or node.
 * @property maxMoveCost The maximum allowed movement cost.
 * @property neighbors A function that takes a point and returns a list of its neighboring points.
 * @property isWalkable A function that takes a point and returns whether it's traversable.
 * @property distance A heuristic function to estimate the distance between two points.
 * @property movementCost A function that calculates the exact movement cost between two points. By default, it returns a constant cost of 1.0 for any pair of points or 0.0 if points are equal.
 */
data class AccessibilityTrie<T>(
    val origin: T,
    val maxMoveCost: Int,
    val neighbors: (T) -> List<T>,
    val isWalkable: (T) -> Boolean,
    val distance: (T, T) -> Int,
    val movementCost: (T, T) -> Double
) {
    private val accessMap: MutableMap<T, T> = mutableMapOf()

    /**
     * The set of all points accessible from the origin within [maxMoveCost].
     */
    val accessible: Set<T>
        get() = accessMap.keys

    init {
        build()
    }

    /**
     * Builds the accessMap considering the walkable nodes, neighbors, and costs.
     * Called automatically on initialization.
     */
    fun build() {
        accessMap.clear()

        if (!isWalkable(origin)) return

        val moveCost = movementCost(origin, origin)
        val candidates = mutableListOf(origin to moveCost)
        val costs = mutableMapOf(origin to moveCost)

        while (candidates.isNotEmpty()) {
            val current = candidates.removeLast()
            if (current.second > maxMoveCost) {
                continue
            }

            neighbors(current.first).filter { isWalkable(it) }.forEach { neighbor ->
                val previousCost = costs[neighbor]
                val newCost = (costs[current.first] ?: 0.0) + movementCost(current.first, neighbor)
                if (previousCost == null || newCost < previousCost) {
                    costs[neighbor] = newCost
                    val priority = newCost + distance(current.first, neighbor)
                    candidates.apply {
                        add(neighbor to priority)
                        sortByDescending { it.second }
                    }
                    accessMap[neighbor] = current.first
                }
            }
        }
    }

    /**
     * Retrieves a list representing the path from the origin to the specified point using the predecessors stored in the access map.
     *
     * @param point The point to which you need the path from the origin.
     * @return a list of points forming the path from the origin to the specified point.
     * An empty list is returned if no path exists.
     */
    operator fun get(point: T): List<T> {
        if (!isWalkable(point)) return emptyList()
        if (point == origin) return listOf(origin)

        val result = mutableListOf<T>()
        var backward: T? = accessMap[point] ?: return result
        result.add(point)
        while (backward != null) {
            result.add(backward)
            backward = accessMap[backward]
        }
        return result.reversed()
    }

    companion object {
        operator fun <T> invoke(
            origin: T,
            maxMoveCost: Int,
            neighbors: (T) -> List<T>,
        ): AccessibilityTrie<T> where T : PathTile<T> =
            AccessibilityTrie(
                origin,
                maxMoveCost,
                neighbors,
                isWalkable = PathTile<T>::isWalkable,
                distance = PathTile<T>::distanceTo,
                movementCost = PathTile<T>::moveCostTo
            )
    }
}