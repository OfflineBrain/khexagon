package algorythm

import base.coordinates.AxisPoint
import base.math.distanceTo
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@DisplayName("Pathfinding")
class PathfindingTest : DescribeSpec({
    context("A*") {
        data class Point(override val q: Int, override val r: Int, val walkable: Boolean) : AxisPoint {
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

        data class TestCase(
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
            )
        )


        testCases.forEach { testCase ->
            it("find path from ${testCase.from} to ${testCase.to}") {
                val result = aStar(
                    from = testCase.from,
                    to = testCase.to,
                    neighbors = neighborsProvider(testCase.map),
                    isWalkable = { it.walkable },
                    heuristic = { a, b -> a distanceTo b }
                )
                result shouldBe testCase.expected
            }
        }
    }
})