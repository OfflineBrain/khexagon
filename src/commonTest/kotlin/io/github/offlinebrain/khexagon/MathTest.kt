package io.github.offlinebrain.khexagon

import io.github.offlinebrain.khexagon.math.flatHexHeight
import io.github.offlinebrain.khexagon.math.flatHexWidth
import io.github.offlinebrain.khexagon.math.pointyHexHeight
import io.github.offlinebrain.khexagon.math.pointyHexWidth
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
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
})