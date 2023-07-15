package algorythm

import base.coordinates.AxisPoint

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