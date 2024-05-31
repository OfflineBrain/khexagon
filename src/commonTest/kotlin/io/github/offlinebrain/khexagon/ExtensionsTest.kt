package io.github.offlinebrain.khexagon

import io.github.offlinebrain.khexagon.coordinates.HexCoordinates
import io.github.offlinebrain.khexagon.coordinates.toDoubleWidthCoordinates
import io.github.offlinebrain.khexagon.coordinates.toEvenQCoordinates
import io.github.offlinebrain.khexagon.math.line
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@DisplayName("Coordinates extension functions")
class ExtensionsTest : DescribeSpec({
    val gen = Arb.int(-100..100)

    describe("line calculation") {
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
})
