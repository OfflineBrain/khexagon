package io.github.offlinebrain.khexagon.algorythm

import io.github.offlinebrain.khexagon.coordinates.AxisPoint
import io.github.offlinebrain.khexagon.math.circle
import io.github.offlinebrain.khexagon.math.distanceTo
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.shouldBe

data class Point(
    override val q: Int,
    override val r: Int,
    val walkable: Boolean = true,
    val cost: Int = 1,
) : AxisPoint {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        if (q != other.q) return false
        if (r != other.r) return false

        return true
    }

    override fun hashCode(): Int {
        var result = q
        result = 31 * result + r
        return result
    }
}


@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@DisplayName("Pathfinding")
class PathfindingTest : DescribeSpec({
    context("A*") {
        data class TestCase(
            val name: String,
            val from: Point,
            val to: Point,
            val map: Set<Point>,
            val expected: List<Point>,
        )

        fun neighbors(point: Point): List<Point> {
            return listOf(
                Point(point.q - 1, point.r, true),
                Point(point.q + 1, point.r, true),
                Point(point.q, point.r - 1, true),
                Point(point.q, point.r + 1, true),
                Point(point.q - 1, point.r + 1, true),
                Point(point.q + 1, point.r - 1, true),
            )
        }

        fun neighborsProvider(map: Set<Point>) = { point: Point ->
            neighbors(point).mapNotNull { map.firstOrNull { p -> p == it } }
        }

        val testCases = listOf(
            // Case 1: Direct path between two points
            TestCase(
                name = "Direct path",
                from = Point(0, 0, walkable = true),
                to = Point(0, 2, walkable = true),
                map = setOf(
                    Point(0, 0, walkable = true),
                    Point(0, 1, walkable = true),
                    Point(0, 2, walkable = true)
                ),
                expected = listOf(
                    Point(0, 0, walkable = true),
                    Point(0, 1, walkable = true),
                    Point(0, 2, walkable = true)
                )
            ),
            // Case 2: Path around an obstacle
            TestCase(
                name = "Path around an obstacle",
                from = Point(0, 0, walkable = true),
                to = Point(0, 2, walkable = true),
                map = setOf(
                    Point(0, 0, walkable = true),
                    Point(0, 1, walkable = false), // Obstacle
                    Point(0, 2, walkable = true),
                    Point(1, 0, walkable = true), // Alternate path
                    Point(1, 1, walkable = true)  // Alternate path
                ),
                expected = listOf(
                    Point(0, 0, walkable = true),
                    Point(1, 0, walkable = true),
                    Point(1, 1, walkable = true),
                    Point(0, 2, walkable = true)
                )
            ),
            // Case 3: No path between two points
            TestCase(
                name = "No path",
                from = Point(0, 0, walkable = true),
                to = Point(0, 2, walkable = true),
                map = setOf(
                    Point(0, 0, walkable = true),
                    Point(0, 1, walkable = false), // Obstacle
                    Point(0, 2, walkable = true),
                    Point(1, 0, walkable = false), // Obstacle
                    Point(1, 1, walkable = false)  // Obstacle
                ),
                expected = emptyList() // There is no path
            ),
            TestCase(
                name = "Path around an complex obstacle",
                from = Point(5, 5, walkable = true),
                to = Point(13, 13, walkable = true),
                map = setOf(
                    // Place some obstacles
                    Point(8, 8, walkable = false),
                    Point(8, 9, walkable = false),
                    Point(9, 8, walkable = false),
                    Point(9, 9, walkable = false),
                    Point(8, 10, walkable = false),
                    Point(10, 8, walkable = false),
                    Point(9, 7, walkable = false),
                    Point(7, 9, walkable = false),
                    Point(7, 8, walkable = false),
                    Point(8, 7, walkable = false),
                    // Build a 20x20 grid
                    *(0 until 20).flatMap { i ->
                        (0 until 20).map { j ->
                            Point(i, j, walkable = true)
                        }
                    }.toTypedArray(),
                ),
                expected = listOf(
                    Point(5, 5, walkable = true),
                    Point(5, 6, walkable = true),
                    Point(5, 7, walkable = true),
                    Point(5, 8, walkable = true),
                    Point(5, 9, walkable = true),
                    Point(5, 10, walkable = true),
                    Point(6, 10, walkable = true),
                    Point(7, 10, walkable = true),
                    Point(7, 11, walkable = true),
                    Point(8, 11, walkable = true),
                    Point(9, 11, walkable = true),
                    Point(10, 11, walkable = true),
                    Point(10, 12, walkable = true),
                    Point(11, 12, walkable = true),
                    Point(11, 13, walkable = true),
                    Point(12, 13, walkable = true),
                    Point(13, 13, walkable = true)
                )
            ),
            TestCase(
                name = "Path around an field with high cost",
                from = Point(0, 0, walkable = true),
                to = Point(2, 0, walkable = true),
                map = setOf(
                    Point(0, 0, walkable = true, cost = 1),
                    Point(1, 0, walkable = true, cost = 3), // High cost path
                    Point(2, 0, walkable = true, cost = 1),
                    Point(0, 1, walkable = true, cost = 1), // Low cost alternative path
                    Point(1, 1, walkable = true, cost = 1), // Low cost alternative path
                    Point(2, 1, walkable = true, cost = 1)  // Low cost alternative path
                ),
                expected = listOf(
                    Point(0, 0, walkable = true, cost = 1),
                    Point(0, 1, walkable = true, cost = 1),
                    Point(1, 1, walkable = true, cost = 1),
                    Point(2, 0, walkable = true, cost = 1)
                )
            )
        )


        testCases.forEach { testCase ->
            it(testCase.name) {
                val result = aStar(
                    from = testCase.from,
                    to = testCase.to,
                    neighbors = neighborsProvider(testCase.map),
                    isWalkable = { it.walkable },
                    heuristic = { a, b -> a distanceTo b },
                    movementCost = { a, b -> if (a == b) 0.0 else b.cost.toDouble() }
                )
                result shouldBe testCase.expected
            }
        }
    }
})

class AccessibilityTrieTest : DescribeSpec({
    describe("AccessibilityTrie") {

        val origin = Point(0, 0)
        val heuristic: (Point, Point) -> Int = { a, b ->
            a distanceTo b
        }

        val neighbors: (Point) -> List<Point> = { point ->
            listOf(
                Point(point.q + 1, point.r),       // Right
                Point(point.q + 1, point.r - 1),  // Top Right
                Point(point.q, point.r - 1),      // Top Left
                Point(point.q - 1, point.r),      // Left
                Point(point.q - 1, point.r + 1),  // Bottom Left
                Point(point.q, point.r + 1)       // Bottom Right
            )
        }
        it("Builds correct AccessMap") {
            val walkable = mutableSetOf<Point>()
            circle(radius = 3) { q, r ->
                walkable.add(Point(q, r))
            }
            val expectedAccessibility = walkable - origin

            val trie = AccessibilityTrie(
                origin = origin,
                maxMoveCost = 3,
                neighbors = neighbors,
                isWalkable = { walkable.contains(it) },
                heuristic = heuristic
            )
            trie.accessible shouldBeSameSizeAs expectedAccessibility
            trie.accessible shouldBe expectedAccessibility
        }

        it("Retrieves path correctly") {
            val trie = AccessibilityTrie(
                origin = origin,
                maxMoveCost = 2,
                neighbors = neighbors,
                isWalkable = { true },
                heuristic = heuristic
            )
            val point = Point(1, 1)
            trie[point] shouldBe listOf(Point(0, 0), Point(0, 1), point)
        }

        it("Finds shortest path considering move cost") {
            val highCostPoints = setOf(Point(0, 1, true, 10), Point(1, 0, true, 10))
            val target = Point(1, 1)

            val walkable = mutableSetOf<Point>()
            circle(radius = 3) { q, r ->
                val element = Point(q, r)
                if (highCostPoints.contains(element).not()) {
                    print(element)
                    walkable.add(element)
                }
            }
            walkable.addAll(highCostPoints)


            val trie = AccessibilityTrie(
                origin = origin,
                maxMoveCost = 5,
                neighbors = { point ->
                    neighbors(point).mapNotNull { n -> walkable.find { n == it } }
                },
                isWalkable = { walkable.contains(it) },
                heuristic = heuristic,
                movementCost = { a, b -> if (a == b) 0.0 else b.cost.toDouble() }
            )

            val path = trie[target]
            val expectedPath = listOf(
                Point(0, 0),  // Original starting point
                Point(-1, 1),
                Point(-1, 2),
                Point(0, 2),
                target // Final destination
            )

            path shouldBe expectedPath
        }
    }
})