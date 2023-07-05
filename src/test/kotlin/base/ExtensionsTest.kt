package base

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@DisplayName("Coordinates extension functions")
class ExtensionsTest : DescribeSpec({

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

        it("should handle conversion between coordinate systems") {
            val start = HexCoordinates(0, 0).toEvenQCoordinates()
            val end = HexCoordinates(2, -2).toEvenQCoordinates()
            val line = start lineTo end
            line shouldBe listOf(
                HexCoordinates(0, 0).toEvenQCoordinates(),
                HexCoordinates(1, -1).toEvenQCoordinates(),
                HexCoordinates(2, -2).toEvenQCoordinates(),
            )
        }
    }
})
