package algorythm

import Heavy
import base.coordinates.HexCoordinates
import base.math.bresenhamsLine
import base.math.circle
import base.math.distance
import base.math.distanceTo
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.ShouldSpec
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

@OptIn(ExperimentalKotest::class)
@DisplayName("Symmetric Pre-Computed Vision Tries")
class SymmetricPreComputedVisionTriesTest : ShouldSpec({
    isolationMode = IsolationMode.InstancePerTest

    val gen = Arb.int(-1_000..1_000)

    context("Bresenham's line algorithm") {

        context("one dimensional lines") {
            context("q = 0") {
                should("should construct line in order") {
                    checkAll(gen) { r ->
                        val start = HexCoordinates.from(0, 0)
                        val end = HexCoordinates.from(0, r)
                        val line = start.bresenhamsLine(end)

                        line shouldBe List(r.absoluteValue + 1) { HexCoordinates.from(0, it * r.sign) }
                    }
                }
            }

            context("r = 0") {
                should("should construct line in order") {
                    checkAll(gen) { q ->
                        val start = HexCoordinates.from(0, 0)
                        val end = HexCoordinates.from(q, 0)
                        val line = start.bresenhamsLine(end)

                        line shouldBe List(q.absoluteValue + 1) { HexCoordinates.from(it * q.sign, 0) }
                    }
                }
            }

            context("s = 0") {
                should("should construct line in order") {
                    checkAll(gen) { q ->
                        val start = HexCoordinates.from(0, 0)
                        val end = HexCoordinates.from(q, -q)
                        val line = start.bresenhamsLine(end)

                        line shouldBe List(q.absoluteValue + 1) { HexCoordinates.from(it * q.sign, -(it * q.sign)) }
                    }
                }
            }
        }

        context("diagonals") {
            val diagonals = Exhaustive.collection(HexCoordinates.diagonals)

            checkAll(diagonals) { (q, r) ->
                val start = HexCoordinates.from(0, 0)
                val end = HexCoordinates.from(q, r)
                val line = start.bresenhamsLine(end)

                val reversedLine = end.bresenhamsLine(start)
                println()
                should("${HexCoordinates.from(q, r)} should construct line in order") {
                    line should { l -> l.windowed(2).all { (a, b) -> a distanceTo b == 1 } }
                    line shouldHaveSize 3
                    line shouldBe reversedLine.reversed()
                }
            }
        }

        should("should be symmetric") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.from(q1, r1)
                val end = HexCoordinates.from(q2, r2)
                val line = start.bresenhamsLine(end)

                val reversedLine = end.bresenhamsLine(start)

                line shouldBe reversedLine.reversed()
            }
        }

        should("should not contain emptiness") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.from(q1, r1)
                val end = HexCoordinates.from(q2, r2)
                val line = start.bresenhamsLine(end)


                line should { l -> l.windowed(2).all { (a, b) -> a distanceTo b == 1 } }
            }
        }

        should("should contain start and end") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.from(q1, r1)
                val end = HexCoordinates.from(q2, r2)
                val line = start.bresenhamsLine(end)

                line should { l -> l.contains(start) && l.contains(end) }
            }
        }

        should("should not contain duplicates") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.from(q1, r1)
                val end = HexCoordinates.from(q2, r2)
                val line = start.bresenhamsLine(end)

                line.toSet().size shouldBe line.size
            }
        }
    }

    context("vision tries").config(tags = setOf(Heavy)) {
        val radius = 100
        should("should be complete for a radius = $radius") {
            val spcvt = SPCVT(radius)

            spcvt.fastLoSMap.size shouldBe 3 * radius * (radius + 1)
        }

        context("line of sight") {
            val nums = Arb.int(-100..100)
            val spcvt = SPCVT(100)

            should("should be symmetric") {

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
                    } else if (start distanceTo end > 100) {
                        outOfRange++
                        visible shouldBe false
                    } else {
                        complete++
                        los.distinct() shouldHaveSize ((start distanceTo end) + 1)
                    }
                }
            }
        }

        context("field of view") {
            val spcvt = SPCVT(100)
            context("non blocked") {

                should("should be a circle at origin") {

                    val center = HexCoordinates.from(0, 0)
                    val fov = mutableListOf<HexCoordinates>()

                    spcvt.fieldOfView(
                        from = center,
                        doesBlockVision = { _, _ -> false },
                        callback = { q, r -> fov.add(HexCoordinates.from(q, r)) }
                    )

                    fov.distinct() shouldBeSameSizeAs center.circle(100)
                }

                context("restricted radius") {
                    val radius = Exhaustive.collection((1..100 step 5).toList())
                    checkAll(radius) { radius ->
                        should("should be a circle at origin with radius $radius") {

                            val center = HexCoordinates.from(0, 0)
                            val fov = mutableListOf<HexCoordinates>()

                            spcvt.fieldOfView(
                                from = center,
                                doesBlockVision = { q, r -> distance(q, r, 0, 0) > radius },
                                callback = { q, r -> fov.add(HexCoordinates.from(q, r)) },
                            )

                            println("fov: ${fov.size}, distinct: ${fov.distinct().size}")

                            fov.distinct() shouldBeSameSizeAs center.circle(radius)
                        }
                    }
                }

                context("restricted radius without callback") {
                    val radius = Exhaustive.collection((1..100 step 5).toList())
                    checkAll(radius) { radius ->
                        should("should be a circle at origin with radius $radius") {

                            val center = HexCoordinates.from(0, 0)

                            val fov = spcvt.fieldOfView(
                                from = center,
                                doesBlockVision = { q, r -> distance(q, r, 0, 0) > radius }
                            )


                            fov shouldBeSameSizeAs center.circle(radius)
                        }
                    }
                }
            }
        }
    }
})
