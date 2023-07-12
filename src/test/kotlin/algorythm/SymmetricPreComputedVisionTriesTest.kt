package algorythm

import Heavy
import base.coordinates.HexCoordinates
import base.math.circle
import base.math.distance
import base.distanceTo
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
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
class SymmetricPreComputedVisionTriesTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    val gen = Arb.int(-1_000..1_000)

    describe("Bresenham's line algorithm") {

        describe("one dimensional lines") {
            describe("q = 0") {
                it("should construct line in order") {
                    checkAll(gen) { r ->
                        val start = HexCoordinates.from(0, 0)
                        val end = HexCoordinates.from(0, r)
                        val line = mutableListOf<HexCoordinates>()
                        bresenhamsLine(start, end) { x, y ->
                            line.add(HexCoordinates.from(x, y))
                        }
                        line shouldBe List(r.absoluteValue + 1) { HexCoordinates.from(0, it * r.sign) }
                    }
                }
            }

            describe("r = 0") {
                it("should construct line in order") {
                    checkAll(gen) { q ->
                        val start = HexCoordinates.from(0, 0)
                        val end = HexCoordinates.from(q, 0)
                        val line = mutableListOf<HexCoordinates>()
                        bresenhamsLine(start, end) { x, y ->
                            line.add(HexCoordinates.from(x, y))
                        }
                        line shouldBe List(q.absoluteValue + 1) { HexCoordinates.from(it * q.sign, 0) }
                    }
                }
            }

            describe("s = 0") {
                it("should construct line in order") {
                    checkAll(gen) { q ->
                        val start = HexCoordinates.from(0, 0)
                        val end = HexCoordinates.from(q, -q)
                        val line = mutableListOf<HexCoordinates>()
                        bresenhamsLine(start, end) { x, y ->
                            line.add(HexCoordinates.from(x, y))
                        }
                        line shouldBe List(q.absoluteValue + 1) { HexCoordinates.from(it * q.sign, -(it * q.sign)) }
                    }
                }
            }
        }

        describe("diagonals") {
            val diagonals = Exhaustive.collection(HexCoordinates.diagonals)

            checkAll(diagonals) { (q, r) ->
                val start = HexCoordinates.from(0, 0)
                val end = HexCoordinates.from(q, r)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates.from(x, y))
                }
                val reversedLine = mutableListOf<HexCoordinates>()
                bresenhamsLine(end, start) { x, y ->
                    reversedLine.add(HexCoordinates.from(x, y))
                }
                println()
                it("${HexCoordinates.from(q, r)} should construct line in order") {
                    line should { l -> l.windowed(2).all { (a, b) -> a distanceTo b == 1 } }
                    line shouldHaveSize 3
                    line shouldBe reversedLine.reversed()
                }
            }
        }

        it("should be symmetric") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.from(q1, r1)
                val end = HexCoordinates.from(q2, r2)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates.from(x, y))
                }
                val reversedLine = mutableListOf<HexCoordinates>()
                bresenhamsLine(end, start) { x, y ->
                    reversedLine.add(HexCoordinates.from(x, y))
                }

                line shouldBe reversedLine.reversed()
            }
        }

        it("should not contain emptiness") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.from(q1, r1)
                val end = HexCoordinates.from(q2, r2)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates.from(x, y))
                }

                line should { l -> l.windowed(2).all { (a, b) -> a distanceTo b == 1 } }
            }
        }

        it("should contain start and end") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.from(q1, r1)
                val end = HexCoordinates.from(q2, r2)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates.from(x, y))
                }

                line should { l -> l.contains(start) && l.contains(end) }
            }
        }

        it("should not contain duplicates") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.from(q1, r1)
                val end = HexCoordinates.from(q2, r2)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates.from(x, y))
                }

                line.toSet().size shouldBe line.size
            }
        }
    }

    describe("vision tries").config(tags = setOf(Heavy)) {
        val radius = 100
        it("should be complete for a radius = $radius") {
            val spcvt = SPCVT(radius)

            spcvt.fastLoSMap.size shouldBe 3 * radius * (radius + 1)
        }

        describe("line of sight") {
            val nums = Arb.int(-100..100)
            val spcvt = SPCVT(100)

            it("should be symmetric") {

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
                            callback = { _, trie -> los.add(HexCoordinates.from(trie.q, trie.r) + start) }
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

        describe("field of view") {
            val spcvt = SPCVT(100)
            describe("non blocked") {

                it("should be a circle at origin") {

                    val center = HexCoordinates.from(0, 0)
                    val fov = mutableListOf<HexCoordinates>()

                    spcvt.fieldOfView(
                        from = center,
                        doesBlockVision = { _, _ -> false },
                        callback = { q, r -> fov.add(HexCoordinates.from(q, r)) }
                    )

                    fov.distinct() shouldBeSameSizeAs center.circle(100)
                }

                describe("restricted radius") {
                    val radius = Exhaustive.collection((1..100 step 5).toList())
                    checkAll(radius) { radius ->
                        it("should be a circle at origin with radius $radius") {

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

                describe("restricted radius without callback") {
                    val radius = Exhaustive.collection((1..100 step 5).toList())
                    checkAll(radius) { radius ->
                        it("should be a circle at origin with radius $radius") {

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
