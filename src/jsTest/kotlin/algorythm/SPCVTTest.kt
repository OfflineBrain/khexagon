import algorythm.SPCVT
import base.coordinates.HexCoordinates
import base.math.bresenhamsLine
import base.math.circle
import base.math.distance
import base.math.distanceTo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.collection
import kotlin.math.absoluteValue
import kotlin.math.sign

class SPCVTTest : StringSpec({
    val gen = Arb.int(-1_000..1_000)

    "Bresenham's line algorithm - one dimensional lines - q = 0 - line construction" {
        checkAll(gen) { r ->
            val start = HexCoordinates.from(0, 0)
            val end = HexCoordinates.from(0, r)
            val line = start.bresenhamsLine(end)

            line shouldBe List(r.absoluteValue + 1) { HexCoordinates.from(0, it * r.sign) }
        }
    }

    "Bresenham's line algorithm - one dimensional lines - r = 0 - line construction" {
        checkAll(gen) { q ->
            val start = HexCoordinates.from(0, 0)
            val end = HexCoordinates.from(q, 0)
            val line = start.bresenhamsLine(end)

            line shouldBe List(q.absoluteValue + 1) { HexCoordinates.from(it * q.sign, 0) }
        }
    }

    "Bresenham's line algorithm - one dimensional lines - s = 0 - line construction" {
        checkAll(gen) { q ->
            val start = HexCoordinates.from(0, 0)
            val end = HexCoordinates.from(q, -q)
            val line = start.bresenhamsLine(end)

            line shouldBe List(q.absoluteValue + 1) { HexCoordinates.from(it * q.sign, -(it * q.sign)) }
        }
    }

    "Bresenham's line algorithm - diagonals - line construction" {
        val diagonals = Exhaustive.collection(HexCoordinates.diagonals)
        checkAll(diagonals) { (q, r) ->
            val start = HexCoordinates.from(0, 0)
            val end = HexCoordinates.from(q, r)
            val line = start.bresenhamsLine(end)
            val reversedLine = end.bresenhamsLine(start)
            line should { l -> l.windowed(2).all { (a, b) -> a distanceTo b == 1 } }
            line shouldHaveSize 3
            line shouldBe reversedLine.reversed()
        }
    }

    "Bresenham's line algorithm - should be symmetric" {
        checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
            val start = HexCoordinates.from(q1, r1)
            val end = HexCoordinates.from(q2, r2)
            val line = start.bresenhamsLine(end)

            val reversedLine = end.bresenhamsLine(start)

            line shouldBe reversedLine.reversed()
        }
    }

    "Bresenham's line algorithm - should not contain emptiness" {
        checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
            val start = HexCoordinates.from(q1, r1)
            val end = HexCoordinates.from(q2, r2)
            val line = start.bresenhamsLine(end)

            line should { l -> l.windowed(2).all { (a, b) -> a distanceTo b == 1 } }
        }
    }

    "Bresenham's line algorithm - should contain start and end" {
        checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
            val start = HexCoordinates.from(q1, r1)
            val end = HexCoordinates.from(q2, r2)
            val line = start.bresenhamsLine(end)

            line should { l -> l.contains(start) && l.contains(end) }
        }
    }

    "Bresenham's line algorithm - should not contain duplicates" {
        checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
            val start = HexCoordinates.from(q1, r1)
            val end = HexCoordinates.from(q2, r2)
            val line = start.bresenhamsLine(end)

            line.toSet().size shouldBe line.size
        }
    }

    "Symmetric pre-computed vision tries - should be complete for a radius = 35" {
        val radius = 35
        val spcvt = SPCVT(radius)

        spcvt.fastLoSMap.size shouldBe 3 * radius * (radius + 1)
    }

    "Symmetric pre-computed vision tries - line of sight - should be symmetric" {
        val nums = Arb.int(-35..35)
        val spcvt = SPCVT(35)
        var single = 0
        var outOfRange = 0
        var complete = 0

        checkAll(PropTestConfig(iterations = 10_000), nums, nums, nums, nums) { q1, r1, q2, r2 ->
            val start = HexCoordinates.from(q1, r1)
            val end = HexCoordinates.from(q2, r2)

            val los = mutableListOf<HexCoordinates>()
            val visible =
                spcvt.lineOfSight(
                    from = start,
                    to = end,
                    doesBlockVision = { _, _ -> false },
                    callback = { q, r -> los.add(HexCoordinates.from(q, r) + start) }
                )
            val reverseVisible = spcvt.lineOfSight(
                from = end,
                to = start,
                doesBlockVision = { _, _ -> false },
                callback = { _, _ -> }
            )

            visible shouldBe reverseVisible

            if (start == end) {
                single++
                los.distinct() shouldHaveSize 0
            } else if (start distanceTo end > 35) {
                outOfRange++
                visible shouldBe false
            } else {
                complete++
                los.distinct() shouldHaveSize ((start distanceTo end) + 1)
            }
        }
    }

    "Symmetric pre-computed vision tries - field of view at origin - should return a circle" {
        val center = HexCoordinates.from(0, 0)
        val fov = mutableListOf<HexCoordinates>()
        val spcvt = SPCVT(35)

        spcvt.fieldOfView(
            from = center,
            doesBlockVision = { _, _ -> false },
            callback = { q, r -> fov.add(HexCoordinates.from(q, r)) }
        )

        fov.distinct() shouldBeSameSizeAs center.circle(35)
    }

    "Symmetric pre-computed vision tries - field of view at origin with restricted radius - should return a circle" {
        val radius = Exhaustive.collection((1..35 step 5).toList())
        val spcvt = SPCVT(35)

        checkAll(radius) { radius ->
            val center = HexCoordinates.from(0, 0)
            val fov = mutableListOf<HexCoordinates>()

            spcvt.fieldOfView(
                from = center,
                doesBlockVision = { q, r -> distance(q, r, 0, 0) > radius },
                callback = { q, r -> fov.add(HexCoordinates.from(q, r)) },
            )
            fov.distinct() shouldBeSameSizeAs center.circle(radius)
        }
    }

    "Symmetric pre-computed vision tries - field of view at origin with restricted radius without callback - should return a circle" {
        val radius = Exhaustive.collection((1..35 step 5).toList())
        val spcvt = SPCVT(35)

        checkAll(radius) { radius ->
            val center = HexCoordinates.from(0, 0)

            val fov = spcvt.fieldOfView(
                from = center,
                doesBlockVision = { q, r -> distance(q, r, 0, 0) > radius }
            )

            fov shouldBeSameSizeAs center.circle(radius)
        }
    }
})