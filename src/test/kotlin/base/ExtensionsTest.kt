package base

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

@DisplayName("Coordinates extension functions")
class ExtensionsTest : DescribeSpec({
    val gen = Arb.int(-1_000..1_000)

    describe("line calculation") {
        it("should calculate a line between two points") {
            val start = HexCoordinates(0, 0)
            val end = HexCoordinates(2, -2)
            val line = start lineTo end
            line shouldBe listOf(
                HexCoordinates(0, 0),
                HexCoordinates(1, -1),
                HexCoordinates(2, -2),
            )
        }

        it("should handle conversion between coordinate representations") {
            val start = HexCoordinates(0, 0).toEvenQCoordinates()
            val end = HexCoordinates(2, -2).toEvenQCoordinates()
            val line = start lineTo end
            line shouldBe listOf(
                HexCoordinates(0, 0).toEvenQCoordinates(),
                HexCoordinates(1, -1).toEvenQCoordinates(),
                HexCoordinates(2, -2).toEvenQCoordinates(),
            )
        }

        it("should handle different coordinate representations") {
            val start = HexCoordinates(0, 0).toEvenQCoordinates()
            val end = HexCoordinates(2, -2).toDoubleWidthCoordinates()
            val line = start lineTo end
            line shouldBe listOf(
                HexCoordinates(0, 0).toEvenQCoordinates(),
                HexCoordinates(1, -1).toEvenQCoordinates(),
                HexCoordinates(2, -2).toEvenQCoordinates(),
            )
        }

        it("should be symmetric") {
            checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                val start = HexCoordinates(q1, r1)
                val end = HexCoordinates(q2, r2)
                val line = start lineTo end
                val line2 = end lineTo start
                line.toSet() shouldBe line2.toSet()
            }
        }
    }
})
