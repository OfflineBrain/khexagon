import io.github.offlinebrain.khexagon.algorythm.AccessibilityTrie
import io.github.offlinebrain.khexagon.algorythm.aStar
import io.github.offlinebrain.khexagon.coordinates.AxisPoint
import io.github.offlinebrain.khexagon.math.circle
import io.github.offlinebrain.khexagon.math.distanceTo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.shouldBe

data class TestAxisPoint(
    override val q: Int,
    override val r: Int,
    val cost: Int = 1,
) : AxisPoint {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestAxisPoint) return false

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

class AccessibilityTrieTest : StringSpec({
    val origin = TestAxisPoint(0, 0)
    val heuristic: (TestAxisPoint, TestAxisPoint) -> Int = { a, b ->
        a distanceTo b
    }

    val neighbors: (TestAxisPoint) -> List<TestAxisPoint> = { point ->
        listOf(
            TestAxisPoint(point.q + 1, point.r),
            TestAxisPoint(point.q + 1, point.r - 1),
            TestAxisPoint(point.q, point.r - 1),
            TestAxisPoint(point.q - 1, point.r),
            TestAxisPoint(point.q - 1, point.r + 1),
            TestAxisPoint(point.q, point.r + 1)
        )
    }

    "Builds correct AccessMap" {
        val walkable = mutableSetOf<TestAxisPoint>()
        circle(radius = 3) { q, r ->
            walkable.add(TestAxisPoint(q, r))
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

    "Retrieves path correctly" {
        val trie = AccessibilityTrie(
            origin = origin,
            maxMoveCost = 2,
            neighbors = neighbors,
            isWalkable = { true },
            heuristic = heuristic
        )
        val point = TestAxisPoint(1, 1)
        trie[point] shouldBe listOf(TestAxisPoint(0, 0), TestAxisPoint(0, 1), point)
    }

    "Finds shortest path considering move cost" {
        val highCostPoints = setOf(TestAxisPoint(0, 1, 10), TestAxisPoint(1, 0, 10))
        val target = TestAxisPoint(1, 1)

        val walkable = mutableSetOf<TestAxisPoint>()
        circle(radius = 3) { q, r ->
            val element = TestAxisPoint(q, r)
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
            TestAxisPoint(0, 0),  // Original starting point
            TestAxisPoint(-1, 1),
            TestAxisPoint(-1, 2),
            TestAxisPoint(0, 2),
            target // Final destination
        )

        path shouldBe expectedPath
    }
})

data class PathTestPoint(
    override val q: Int,
    override val r: Int,
    val walkable: Boolean,
    val cost: Int = 1,
) : AxisPoint {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathTestPoint) return false

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

class PathfindingTest : StringSpec({
    data class TestCase(
        val from: PathTestPoint,
        val to: PathTestPoint,
        val map: Set<PathTestPoint>,
        val expected: List<PathTestPoint>,
    )

    fun neighbors(point: PathTestPoint): List<PathTestPoint> {
        return listOf(
            PathTestPoint(point.q - 1, point.r, true),
            PathTestPoint(point.q + 1, point.r, true),
            PathTestPoint(point.q, point.r - 1, true),
            PathTestPoint(point.q, point.r + 1, true),
            PathTestPoint(point.q - 1, point.r + 1, true),
            PathTestPoint(point.q + 1, point.r - 1, true),
        )
    }

    fun neighborsProvider(map: Set<PathTestPoint>) = { point: PathTestPoint ->
        neighbors(point).mapNotNull { map.firstOrNull { p -> p == it } }
    }

    val testCases = listOf(
        // Case 1: Direct path between two points
        TestCase(
            from = PathTestPoint(0, 0, walkable = true),
            to = PathTestPoint(0, 2, walkable = true),
            map = setOf(
                PathTestPoint(0, 0, walkable = true),
                PathTestPoint(0, 1, walkable = true),
                PathTestPoint(0, 2, walkable = true)
            ),
            expected = listOf(
                PathTestPoint(0, 0, walkable = true),
                PathTestPoint(0, 1, walkable = true),
                PathTestPoint(0, 2, walkable = true)
            )
        ),
        // Case 2: Path around an obstacle
        TestCase(
            from = PathTestPoint(0, 0, walkable = true),
            to = PathTestPoint(0, 2, walkable = true),
            map = setOf(
                PathTestPoint(0, 0, walkable = true),
                PathTestPoint(0, 1, walkable = false), // Obstacle
                PathTestPoint(0, 2, walkable = true),
                PathTestPoint(1, 0, walkable = true), // Alternate path
                PathTestPoint(1, 1, walkable = true)  // Alternate path
            ),
            expected = listOf(
                PathTestPoint(0, 0, walkable = true),
                PathTestPoint(1, 0, walkable = true),
                PathTestPoint(1, 1, walkable = true),
                PathTestPoint(0, 2, walkable = true)
            )
        ),
        // Case 3: No path between two points
        TestCase(
            from = PathTestPoint(0, 0, walkable = true),
            to = PathTestPoint(0, 2, walkable = true),
            map = setOf(
                PathTestPoint(0, 0, walkable = true),
                PathTestPoint(0, 1, walkable = false), // Obstacle
                PathTestPoint(0, 2, walkable = true),
                PathTestPoint(1, 0, walkable = false), // Obstacle
                PathTestPoint(1, 1, walkable = false)  // Obstacle
            ),
            expected = emptyList() // There is no path
        ),
        TestCase(
            from = PathTestPoint(5, 5, walkable = true),
            to = PathTestPoint(13, 13, walkable = true),
            map = setOf(
                // Build a 20x20 grid
                *(0 until 20).flatMap { i ->
                    (0 until 20).map { j ->
                        if ((i in 7..10) && (j in 7..10))
                            PathTestPoint(i, j, walkable = false)
                        else
                            PathTestPoint(i, j, walkable = true)
                    }
                }.toTypedArray(),
            ),
            expected = listOf(
                PathTestPoint(5, 5, walkable = true),
                PathTestPoint(5, 6, walkable = true),
                PathTestPoint(5, 7, walkable = true),
                PathTestPoint(5, 8, walkable = true),
                PathTestPoint(5, 9, walkable = true),
                PathTestPoint(5, 10, walkable = true),
                PathTestPoint(5, 11, walkable = true),
                PathTestPoint(6, 11, walkable = true),
                PathTestPoint(7, 11, walkable = true),
                PathTestPoint(8, 11, walkable = true),
                PathTestPoint(9, 11, walkable = true),
                PathTestPoint(10, 11, walkable = true),
                PathTestPoint(11, 11, walkable = true),
                PathTestPoint(11, 12, walkable = true),
                PathTestPoint(12, 12, walkable = true),
                PathTestPoint(12, 13, walkable = true),
                PathTestPoint(13, 13, walkable = true)
            )
        ),
        TestCase(
            from = PathTestPoint(0, 0, walkable = true),
            to = PathTestPoint(2, 0, walkable = true),
            map = setOf(
                PathTestPoint(0, 0, walkable = true, cost = 1),
                PathTestPoint(1, 0, walkable = true, cost = 3), // High cost path
                PathTestPoint(2, 0, walkable = true, cost = 1),
                PathTestPoint(0, 1, walkable = true, cost = 1), // Low cost alternative path
                PathTestPoint(1, 1, walkable = true, cost = 1), // Low cost alternative path
                PathTestPoint(2, 1, walkable = true, cost = 1)  // Low cost alternative path
            ),
            expected = listOf(
                PathTestPoint(0, 0, walkable = true, cost = 1),
                PathTestPoint(0, 1, walkable = true, cost = 1),
                PathTestPoint(1, 1, walkable = true, cost = 1),
                PathTestPoint(2, 0, walkable = true, cost = 1)
            )
        )
    )

    testCases.forEachIndexed { idx, testCase ->
        "[$idx] should find path from ${testCase.from} to ${testCase.to}".config(
            invocations = 1,
            threads = 1,
            enabled = true
        ) {
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
})