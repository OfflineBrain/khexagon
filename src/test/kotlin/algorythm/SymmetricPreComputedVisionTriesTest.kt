package algorythm

import base.HexCoordinates
import base.circle
import base.distanceTo
import io.kotest.core.annotation.DisplayName
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

@DisplayName("Symmetric Pre-Computed Vision Tries")
class SymmetricPreComputedVisionTriesTest : DescribeSpec({
    val gen = Arb.int(-1_000..1_000)

    context("Bresenham's line algorithm") {

        context("one dimensional lines") {
            context("q = 0") {
                it("should construct line in order") {
                    checkAll(gen) { r ->
                        val start = HexCoordinates(0, 0)
                        val end = HexCoordinates(0, r)
                        val line = mutableListOf<HexCoordinates>()
                        bresenhamsLine(start, end) { x, y ->
                            line.add(HexCoordinates(x, y))
                        }
                        line shouldBe List(r.absoluteValue + 1) { HexCoordinates(0, it * r.sign) }
                    }
                }
            }

            context("r = 0") {
                it("should construct line in order") {
                    checkAll(gen) { q ->
                        val start = HexCoordinates(0, 0)
                        val end = HexCoordinates(q, 0)
                        val line = mutableListOf<HexCoordinates>()
                        bresenhamsLine(start, end) { x, y ->
                            line.add(HexCoordinates(x, y))
                        }
                        line shouldBe List(q.absoluteValue + 1) { HexCoordinates(it * q.sign, 0) }
                    }
                }
            }

            context("s = 0") {
                it("should construct line in order") {
                    checkAll(gen) { q ->
                        val start = HexCoordinates(0, 0)
                        val end = HexCoordinates(q, -q)
                        val line = mutableListOf<HexCoordinates>()
                        bresenhamsLine(start, end) { x, y ->
                            line.add(HexCoordinates(x, y))
                        }
                        line shouldBe List(q.absoluteValue + 1) { HexCoordinates(it * q.sign, -(it * q.sign)) }
                    }
                }
            }
        }

        context("diagonals") {
            val diagonals = Exhaustive.collection(HexCoordinates.diagonals)

            checkAll(diagonals) { (q, r) ->
                val start = HexCoordinates(0, 0)
                val end = HexCoordinates(q, r)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    println("$x : $y : ${-x - y}")
                    line.add(HexCoordinates(x, y))
                }
                val reversedLine = mutableListOf<HexCoordinates>()
                bresenhamsLine(end, start) { x, y ->
                    reversedLine.add(HexCoordinates(x, y))
                }
                println()
                it("${HexCoordinates(q, r)} should construct line in order") {
                    line should { l -> l.windowed(2).all { (a, b) -> a distanceTo b == 1 } }
                    line shouldHaveSize 3
                    line shouldBe reversedLine.reversed()
                }
            }
        }

        checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
            val start = HexCoordinates(q1, r1)
            val end = HexCoordinates(q2, r2)
            val line = mutableListOf<HexCoordinates>()
            bresenhamsLine(start, end) { x, y ->
                line.add(HexCoordinates(x, y))
            }
            val reversedLine = mutableListOf<HexCoordinates>()
            bresenhamsLine(end, start) { x, y ->
                reversedLine.add(HexCoordinates(x, y))
            }
        }

        it("should be symmetric") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates(q1, r1)
                val end = HexCoordinates(q2, r2)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates(x, y))
                }
                val reversedLine = mutableListOf<HexCoordinates>()
                bresenhamsLine(end, start) { x, y ->
                    reversedLine.add(HexCoordinates(x, y))
                }

                line shouldBe reversedLine.reversed()
            }
        }

        it("should not contain emptiness") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates(q1, r1)
                val end = HexCoordinates(q2, r2)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates(x, y))
                }

                line should { l -> l.windowed(2).all { (a, b) -> a distanceTo b == 1 } }
            }
        }

        it("should contain start and end") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates(q1, r1)
                val end = HexCoordinates(q2, r2)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates(x, y))
                }

                line should { l -> l.contains(start) && l.contains(end) }
            }
        }

        it("should not contain duplicates") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates(q1, r1)
                val end = HexCoordinates(q2, r2)
                val line = mutableListOf<HexCoordinates>()
                bresenhamsLine(start, end) { x, y ->
                    line.add(HexCoordinates(x, y))
                }

                line.toSet().size shouldBe line.size
            }
        }
    }

    context("vision tries") {
        context("compute") {
            val positives = Arb.int(50..150)
            checkAll(PropTestConfig(iterations = 10), positives) { radius ->
                it("should be complete for a radius = $radius") {
                    val spcvt = SPCVT(radius)

                    spcvt.fastLoSMap.size shouldBe 1 + 3 * radius * (radius + 1)
                }
            }
        }

        context("line of sight") {
            val nums = Arb.int(-100..100)
            val spcvt = SPCVT(100)

            it("should be symmetric") {

                var single = 0
                var outOfRange = 0
                var complete = 0

                val attempts = checkAll(PropTestConfig(iterations = 10_000), nums, nums, nums, nums) { q1, r1, q2, r2 ->
                    val start = HexCoordinates(q1, r1)
                    val end = HexCoordinates(q2, r2)

                    val los = mutableListOf<HexCoordinates>()
                    val visible =
                        spcvt.lineOfSight(
                            from = start,
                            to = end,
                            doesBlockVision = { false },
                            callback = { _, trie -> los.add(trie.coordinates.hex + start) }
                        )
                    val reverseVisible = spcvt.lineOfSight(
                        from = end,
                        to = start,
                        doesBlockVision = { false },
                        callback = { _, _ -> }
                    )



                    visible shouldBe reverseVisible

                    if (start == end) {
                        single++
                        los.distinct() shouldHaveSize 1
                        los.first() shouldBe start
                    } else if (start distanceTo end > 100) {
                        outOfRange++
                        visible shouldBe false
                    } else {
                        complete++
                        los.distinct() shouldHaveSize ((start distanceTo end) + 1)
                    }
                }.attempts()

                println("attempts: $attempts")
                println("stats: single: $single, outOfRange: $outOfRange, complete: $complete")
            }
        }

        context("field of view") {
            val spcvt = SPCVT(100)
            context("non blocked") {

                it("should be a circle at origin") {

                    val center = HexCoordinates(0, 0)
                    val fov = mutableListOf<HexCoordinates>()

                    spcvt.fieldOfView(
                        from = center,
                        doesBlockVision = { false },
                        callback = { _, trie -> fov.add(trie.hex) }
                    )

                    fov.distinct() shouldBeSameSizeAs center.circle(100)
                }

                context("restricted radius") {
                    val radius = Exhaustive.collection((1..100 step 5).toList())
                    checkAll(radius) { r ->
                        it("should be a circle at origin with radius $r") {

                            val center = HexCoordinates(0, 0)
                            val fov = mutableListOf<HexCoordinates>()

                            spcvt.fieldOfView(
                                from = center,
                                doesBlockVision = { it.hex distanceTo center > r },
                                callback = { _, trie -> fov.add(trie.hex) },
                            )

                            fov.distinct() shouldBeSameSizeAs center.circle(r)
                        }
                    }
                }
            }
        }
    }
})
