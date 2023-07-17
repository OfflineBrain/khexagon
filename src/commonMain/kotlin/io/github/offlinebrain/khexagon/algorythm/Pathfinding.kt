package io.github.offlinebrain.khexagon.algorythm

import io.github.offlinebrain.khexagon.coordinates.AxisPoint

/**
 * Applies the A* pathfinding algorithm on a given graph to find the shortest path from the start point [from] to the destination [to].
 *
 * @param [from] The starting point in the graph.
 * @param [to] The destination point in the graph.
 * @param [neighbors] A function that takes a point and returns a list of its neighboring points.
 * @param [isWalkable] A function that takes a point and returns whether the point can be traversed or not.
 * @param [heuristic] A function that estimates the distance between two points.
 * @return a list of points representing the shortest path from [from] to [to] based on the provided functions. Returns an empty list if no path is found.
 */
fun <T> aStar(
    from: T,
    to: T,
    neighbors: (T) -> List<T>,
    isWalkable: (T) -> Boolean,
    heuristic: (T, T) -> Int
): List<T> where T : AxisPoint {
    if (!isWalkable(from) || !isWalkable(to)) return emptyList()
    if (from.q == to.q && from.r == to.r) return listOf(from)

    val moveCost = 1

    val openSet = mutableListOf(from to moveCost)
    val costs = mutableMapOf(from to moveCost)
    val path = mutableMapOf<T, T>()

    while (openSet.isNotEmpty()) {
        val current = openSet.removeLast()
        if (current.first == to) {
            break
        }

        neighbors(current.first).filter { isWalkable(it) }.forEach { neighbor ->
            val previousCost = costs[neighbor]
            val newCost = (costs[current.first] ?: 0) + moveCost
            if (previousCost == null || newCost < previousCost) {
                costs[neighbor] = newCost
                val priority = newCost + heuristic(current.first, neighbor)
                openSet.apply {
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

/**
 * A data class that implements a pathfinding algorithm on a graph represented by points of type `T` extending [AxisPoint].
 *
 * @property origin The initial point or node.
 * @property maxMoveCost The maximum allowed movement cost.
 * @property neighbors A function that takes a point and returns a list of its neighboring points.
 * @property isWalkable A function that takes a point and returns whether it's traversable.
 * @property heuristic A heuristic function to estimate the distance between two points.
 */
data class AccessibilityTrie<T>(
    val origin: T,
    val maxMoveCost: Int,
    val neighbors: (T) -> List<T>,
    val isWalkable: (T) -> Boolean,
    val heuristic: (T, T) -> Int,
) where T : AxisPoint {
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

        val openSet = mutableListOf(origin to 0)
        val costs = mutableMapOf(origin to 0)

        while (openSet.isNotEmpty()) {
            val current = openSet.removeLast()
            if (current.second > maxMoveCost) {
                continue
            }

            neighbors(current.first).filter { isWalkable(it) }.forEach { neighbor ->
                val previousCost = costs[neighbor]
                val newCost = (costs[current.first] ?: 0) + 1
                if (previousCost == null || newCost < previousCost) {
                    costs[neighbor] = newCost
                    val priority = newCost + heuristic(current.first, neighbor)
                    openSet.apply {
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
        while (backward != null) {
            result.add(backward)
            backward = accessMap[backward]
        }
        return result.reversed()
    }
}