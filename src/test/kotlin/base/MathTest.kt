package base

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.collection

@DisplayName("Math")
class MathTest : DescribeSpec({
    describe("Dimensions") {
        val width = Exhaustive.collection(
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
            checkAll(width) { (radius, width) ->
                flatHexWidth(radius) shouldBe width
            }
        }

        val height = Exhaustive.collection(
            listOf(
                25 to 43,
                50 to 86,
                75 to 129,
                10 to 17,
                20 to 34,
            )
        )

        it("should calculate the height of a flat hex") {
            checkAll(height) { (radius, height) ->
                flatHexHeight(radius) shouldBe height
            }
        }
    }

    describe("Pixel to Hex") {

    }
})