package io.github.offlinebrain.khexagon

import io.github.offlinebrain.khexagon.coordinates.HexCoordinates
import io.github.offlinebrain.khexagon.coordinates.toDoubleWidthCoordinates
import io.github.offlinebrain.khexagon.coordinates.toEvenQCoordinates
import io.github.offlinebrain.khexagon.math.circle
import io.github.offlinebrain.khexagon.math.flatHexHeight
import io.github.offlinebrain.khexagon.math.flatHexWidth
import io.github.offlinebrain.khexagon.math.hexRound
import io.github.offlinebrain.khexagon.math.line
import io.github.offlinebrain.khexagon.math.pointyHexHeight
import io.github.offlinebrain.khexagon.math.pointyHexWidth
import io.github.offlinebrain.khexagon.math.ring
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.collection

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@DisplayName("Math")
class MathTest : DescribeSpec({
    describe("Dimensions") {
        val flatWidth = Exhaustive.collection(
            listOf(
                1 to 2,
                2 to 4,
                3 to 6,
                4 to 8,
                10 to 20,
                20 to 40,
                30 to 60,
            )
        )

        it("should calculate the width of a flat hex") {
            checkAll(flatWidth) { (radius, width) ->
                flatHexWidth(radius) shouldBe width
            }
        }

        val flatHeight = Exhaustive.collection(
            listOf(
                25 to 43,
                50 to 86,
                75 to 129,
                10 to 17,
                20 to 34,
                5 to 8,
            )
        )

        it("should calculate the height of a flat hex") {
            checkAll(flatHeight) { (radius, height) ->
                flatHexHeight(radius) shouldBe height
            }
        }

        it("should calculate the width of a pointy hex") {
            checkAll(flatWidth) { (radius, width) ->
                pointyHexHeight(radius) shouldBe width
            }
        }

        it("should calculate the height of a pointy hex") {
            checkAll(flatHeight) { (radius, height) ->
                pointyHexWidth(radius) shouldBe height
            }
        }
    }

    describe("line calculation") {
        val gen = Arb.int(-100..100)
        it("should calculate a line between two points") {
            val start = HexCoordinates.cached(0, 0)
            val end = HexCoordinates.cached(2, -2)
            val line = start.line(end)
            line shouldBe listOf(
                HexCoordinates.cached(0, 0),
                HexCoordinates.cached(1, -1),
                HexCoordinates.cached(2, -2),
            )
        }

        it("should handle conversion between coordinate representations") {
            val start = HexCoordinates.cached(0, 0).toEvenQCoordinates()
            val end = HexCoordinates.cached(2, -2).toEvenQCoordinates()
            val line = start.line(end)
            line shouldBe listOf(
                HexCoordinates.cached(0, 0).toEvenQCoordinates(),
                HexCoordinates.cached(1, -1).toEvenQCoordinates(),
                HexCoordinates.cached(2, -2).toEvenQCoordinates(),
            )
        }

        it("should handle different coordinate representations") {
            val start = HexCoordinates.cached(0, 0).toEvenQCoordinates()
            val end = HexCoordinates.cached(2, -2).toDoubleWidthCoordinates()
            val line = start.line(end)
            line shouldBe listOf(
                HexCoordinates.cached(0, 0).toEvenQCoordinates(),
                HexCoordinates.cached(1, -1).toEvenQCoordinates(),
                HexCoordinates.cached(2, -2).toEvenQCoordinates(),
            )
        }

        it("should be symmetric") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates.cached(q1, r1)
                val end = HexCoordinates.cached(q2, r2)
                val line = start.line(end)
                val line2 = end.line(start)
                line.toSet() shouldBe line2.toSet()
            }
        }
    }


    describe("circle function") {
        it("should return correct points for radius 0") {
            val points = mutableListOf<Pair<Int, Int>>()
            circle(0, 0, 0) { q, r -> points.add(q to r) }
            points shouldBe listOf(0 to 0)
        }

        it("should return correct points for radius 1") {
            val points = mutableListOf<Pair<Int, Int>>()
            circle(0, 0, 1) { q, r -> points.add(q to r) }
            points.size shouldBe 7
            points shouldContainExactlyInAnyOrder listOf(
                0 to 0,
                1 to 0,
                1 to -1,
                0 to -1,
                -1 to 0,
                -1 to 1,
                0 to 1,
            )
        }

        it("should return correct points for radius 2") {
            val points = mutableListOf<Pair<Int, Int>>()
            circle(0, 0, 2) { q, r -> points.add(q to r) }
            points.size shouldBe 19
        }
    }

    describe("circle extension function") {
        it("should return correct points for radius 0") {
            val hex = HexCoordinates.cached(0, 0)
            val points = hex.circle(0)
            points.size shouldBe 1
            points[0] shouldBe hex
        }

        it("should return correct points for radius 1") {
            val hex = HexCoordinates.cached(0, 0)
            val points = hex.circle(1)
            points.size shouldBe 7
        }

        it("should return correct points for radius 2") {
            val hex = HexCoordinates.cached(0, 0)
            val points = hex.circle(2)
            points.size shouldBe 19
        }
    }

    describe("ring function") {
        it("should call the callback for each point in the ring") {
            var points = mutableListOf<Pair<Int, Int>>()
            ring(0, 0, 2) { q, r -> points.add(Pair(q, r)) }

            points.size shouldBe 12
        }

        it("should not call the callback when radius is zero") {
            var points = mutableListOf<Pair<Int, Int>>()
            ring(0, 0, 0) { q, r -> points.add(Pair(q, r)) }

            points shouldBe listOf(0 to 0)
        }

        it("should work with non-zero origin") {
            var points = mutableListOf<Pair<Int, Int>>()
            ring(1, 1, 1) { q, r -> points.add(Pair(q, r)) }

            points.size shouldBe 6
            points shouldContainExactlyInAnyOrder listOf(
                Pair(1, 0),
                Pair(2, 0),
                Pair(2, 1),
                Pair(1, 2),
                Pair(0, 2),
                Pair(0, 1),
            )
        }
    }

    describe("hexRound function") {
        it("should round to the nearest integer coordinates") {
            val hex = hexRound(1.1f, 2.9f)
            hex shouldBe HexCoordinates.cached(1, 3)
        }

        it("should handle negative coordinates") {
            val hex = hexRound(-1.1f, -2.9f)
            hex shouldBe HexCoordinates.cached(-1, -3)
        }

        it("should handle zero coordinates") {
            val hex = hexRound(0.0f, 0.0f)
            hex shouldBe HexCoordinates.cached(0, 0)
        }

        it("should handle fractional coordinates") {
            val hex = hexRound(1.5f, 2.5f)
            hex shouldBe HexCoordinates.cached(2, 2)
        }
    }
})