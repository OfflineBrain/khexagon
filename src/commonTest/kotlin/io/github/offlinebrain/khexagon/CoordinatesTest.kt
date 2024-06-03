package io.github.offlinebrain.khexagon

import io.github.offlinebrain.khexagon.coordinates.Coordinates
import io.github.offlinebrain.khexagon.coordinates.HexCoordinates
import io.github.offlinebrain.khexagon.coordinates.toDoubleHeightCoordinates
import io.github.offlinebrain.khexagon.coordinates.toDoubleWidthCoordinates
import io.github.offlinebrain.khexagon.coordinates.toEvenQCoordinates
import io.github.offlinebrain.khexagon.coordinates.toEvenRCoordinates
import io.github.offlinebrain.khexagon.coordinates.toOddQCoordinates
import io.github.offlinebrain.khexagon.coordinates.toOddRCoordinates
import io.github.offlinebrain.khexagon.math.distance
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.ints
import kotlin.math.abs

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@DisplayName("Coordinates")
class CoordinatesTest : DescribeSpec({
    val gen = Arb.int(-1_000_000..1_000_000)
    val directions = Exhaustive.ints(0..5)

    describe("hexagonal coordinates") {
        it("should construct correctly") {
            checkAll<Int, Int> { q, r ->
                val s = -q - r
                val coordinates = HexCoordinates.cached(q, r)
                coordinates.q shouldBe q
                coordinates.r shouldBe r
                coordinates.s shouldBe s
            }
        }

        describe("distance") {

            it("should be 0 for the same coordinates") {
                checkAll(gen) { q ->
                    val coordinates = HexCoordinates.cached(q, q)
                    (coordinates.distance(coordinates)) shouldBe 0
                }
            }

            it("should be 1 for adjacent coordinates") {
                checkAll(gen) { q ->
                    val coordinates = HexCoordinates.cached(q, q)
                    (coordinates.distance(coordinates + HexCoordinates.cached(1, 0))) shouldBe 1
                    (coordinates.distance(coordinates + HexCoordinates.cached(0, 1))) shouldBe 1
                    (coordinates.distance(coordinates + HexCoordinates.cached(-1, 1))) shouldBe 1
                    (coordinates.distance(coordinates + HexCoordinates.cached(-1, 0))) shouldBe 1
                    (coordinates.distance(coordinates + HexCoordinates.cached(0, -1))) shouldBe 1
                    (coordinates.distance(coordinates + HexCoordinates.cached(1, -1))) shouldBe 1
                }
            }

            it("should be max of abs(q), abs(r), abs(s) for non-adjacent coordinates") {
                checkAll(gen, gen, gen, gen) { q1, r1, q2, r2 ->
                    val coordinates1 = HexCoordinates.cached(q1, r1)
                    val coordinates2 = HexCoordinates.cached(q2, r2)
                    (coordinates1.distance(coordinates2)) shouldBe maxOf(
                        abs(q1 - q2),
                        abs(r1 - r2),
                        abs(coordinates1.s - coordinates2.s)
                    )
                }
            }
        }

        describe("neighbors") {
            it("should be 6") {
                checkAll(gen, gen) { q, r ->
                    val coordinates = HexCoordinates.cached(q, r)
                    coordinates.neighbors.size shouldBe 6
                }
            }

            it("should be unique") {
                checkAll(gen, gen) { q, r ->
                    val coordinates = HexCoordinates.cached(q, r)
                    coordinates.neighbors.toSet().size shouldBe 6
                }
            }

            it("should be adjacent") {
                checkAll(gen, gen) { q, r ->
                    val coordinates = HexCoordinates.cached(q, r)
                    coordinates.neighbors.forEach {
                        (coordinates.distance(it)) shouldBe 1
                    }
                }
            }

            it("should return the correct neighbor") {
                checkAll(gen, gen) { q, r ->
                    val coordinates = HexCoordinates.cached(q, r)
                    coordinates[Coordinates.Flat.RightBottom] shouldBe coordinates + HexCoordinates.cached(1, 0)
                    coordinates[Coordinates.Flat.RightTop] shouldBe coordinates + HexCoordinates.cached(1, -1)
                    coordinates[Coordinates.Flat.Top] shouldBe coordinates + HexCoordinates.cached(0, -1)
                    coordinates[Coordinates.Flat.LeftTop] shouldBe coordinates + HexCoordinates.cached(-1, 0)
                    coordinates[Coordinates.Flat.LeftBottom] shouldBe coordinates + HexCoordinates.cached(-1, 1)
                    coordinates[Coordinates.Flat.Bottom] shouldBe coordinates + HexCoordinates.cached(0, 1)
                }
            }
        }
    }


    describe("offset coordinates") {
        describe("even-q") {
            describe("neighbors") {
                it("should return the correct neighbor") {
                    checkAll(gen, gen, directions) { q, r, direction ->
                        val hex = HexCoordinates.cached(q, r)
                        val coordinates = hex.toEvenQCoordinates()
                        coordinates[direction] shouldBe hex[direction].toEvenQCoordinates()
                    }
                }
            }
        }

        describe("even-r") {
            describe("neighbors") {
                it("should return the correct neighbor") {
                    checkAll(gen, gen, directions) { q, r, direction ->
                        val hex = HexCoordinates.cached(q, r)
                        val coordinates = hex.toEvenRCoordinates()
                        coordinates[direction] shouldBe hex[direction].toEvenRCoordinates()
                    }
                }
            }
        }

        describe("odd-q") {
            describe("neighbors") {
                it("should return the correct neighbor") {
                    checkAll(gen, gen, directions) { q, r, direction ->
                        val hex = HexCoordinates.cached(q, r)
                        val coordinates = hex.toOddQCoordinates()
                        coordinates[direction] shouldBe hex[direction].toOddQCoordinates()
                    }
                }
            }
        }

        describe("odd-r") {
            describe("neighbors") {
                it("should return the correct neighbor") {
                    checkAll(gen, gen, directions) { q, r, direction ->
                        val hex = HexCoordinates.cached(q, r)
                        val coordinates = hex.toOddRCoordinates()
                        coordinates[direction] shouldBe hex[direction].toOddRCoordinates()
                    }
                }
            }
        }
    }

    describe("doubled coordinates") {
        describe("width") {
            describe("neighbors") {
                it("should be 6") {
                    checkAll(gen, gen) { q, r ->
                        val coordinates = HexCoordinates.cached(q, r).toDoubleWidthCoordinates()
                        coordinates.neighbors.size shouldBe 6
                    }
                }

                it("should be unique") {
                    checkAll(gen, gen) { q, r ->
                        val coordinates = HexCoordinates.cached(q, r).toDoubleWidthCoordinates()
                        coordinates.neighbors.toSet().size shouldBe 6
                    }
                }

                it("should be adjacent") {
                    checkAll(gen, gen) { q, r ->
                        val coordinates = HexCoordinates.cached(q, r).toDoubleWidthCoordinates()
                        coordinates.neighbors.forEach {
                            (coordinates.toHexCoordinates().distance(it.toHexCoordinates())) shouldBe 1
                        }
                    }
                }

                it("should return the correct neighbor") {
                    checkAll(gen, gen) { q, r ->
                        val hex = HexCoordinates.cached(q, r)
                        val coordinates = hex.toDoubleWidthCoordinates()
                        coordinates[Coordinates.Flat.RightBottom].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(1, 0)
                        coordinates[Coordinates.Flat.RightTop].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(1, -1)
                        coordinates[Coordinates.Flat.Top].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(0, -1)
                        coordinates[Coordinates.Flat.LeftTop].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(-1, 0)
                        coordinates[Coordinates.Flat.LeftBottom].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(-1, 1)
                        coordinates[Coordinates.Flat.Bottom].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(0, 1)
                    }
                }
            }
        }

        describe("height") {
            describe("neighbors") {
                it("should be 6") {
                    checkAll(gen, gen) { q, r ->
                        val coordinates = HexCoordinates.cached(q, r).toDoubleHeightCoordinates()
                        coordinates.neighbors.size shouldBe 6
                    }
                }

                it("should be unique") {
                    checkAll(gen, gen) { q, r ->
                        val coordinates = HexCoordinates.cached(q, r).toDoubleHeightCoordinates()
                        coordinates.neighbors.toSet().size shouldBe 6
                    }
                }

                it("should be adjacent") {
                    checkAll(gen, gen) { q, r ->
                        val coordinates = HexCoordinates.cached(q, r).toDoubleHeightCoordinates()
                        coordinates.neighbors.forEach {
                            (coordinates.toHexCoordinates().distance(it.toHexCoordinates())) shouldBe 1
                        }
                    }
                }

                it("should return the correct neighbor") {
                    checkAll(gen, gen) { q, r ->
                        val hex = HexCoordinates.cached(q, r)
                        val coordinates = hex.toDoubleHeightCoordinates()
                        coordinates[Coordinates.Flat.RightBottom].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(1, 0)
                        coordinates[Coordinates.Flat.RightTop].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(1, -1)
                        coordinates[Coordinates.Flat.Top].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(0, -1)
                        coordinates[Coordinates.Flat.LeftTop].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(-1, 0)
                        coordinates[Coordinates.Flat.LeftBottom].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(-1, 1)
                        coordinates[Coordinates.Flat.Bottom].toHexCoordinates() shouldBe
                                hex + HexCoordinates.cached(0, 1)
                    }
                }
            }
        }
    }

    describe("conversion") {
        describe("hexagonal - even-q") {
            it("should be reversible") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val evenQCoordinates = hexCoordinates.toEvenQCoordinates()
                    (evenQCoordinates.toHexCoordinates()) shouldBe hexCoordinates
                }
            }

            it("should convert correctly") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val evenQCoordinates = hexCoordinates.toEvenQCoordinates()
                    evenQCoordinates.col shouldBe q
                    evenQCoordinates.row shouldBe r + (q + (q and 1)) / 2
                }
            }
        }

        describe("hexagonal - odd-q") {
            it("should be reversible") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val oddQCoordinates = hexCoordinates.toOddQCoordinates()
                    (oddQCoordinates.toHexCoordinates()) shouldBe hexCoordinates
                }
            }

            it("should convert correctly") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val oddQCoordinates = hexCoordinates.toOddQCoordinates()
                    oddQCoordinates.col shouldBe q
                    oddQCoordinates.row shouldBe r + (q - (q and 1)) / 2
                }
            }
        }

        describe("hexagonal - even-r") {
            it("should be reversible") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val evenRCoordinates = hexCoordinates.toEvenRCoordinates()
                    (evenRCoordinates.toHexCoordinates()) shouldBe hexCoordinates
                }
            }

            it("should convert correctly") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val evenRCoordinates = hexCoordinates.toEvenRCoordinates()
                    evenRCoordinates.col shouldBe q + (r + (r and 1)) / 2
                    evenRCoordinates.row shouldBe r
                }
            }
        }

        describe("hexagonal - odd-r") {
            it("should be reversible") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val oddRCoordinates = hexCoordinates.toOddRCoordinates()
                    (oddRCoordinates.toHexCoordinates()) shouldBe hexCoordinates
                }
            }

            it("should convert correctly") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val oddRCoordinates = hexCoordinates.toOddRCoordinates()
                    oddRCoordinates.col shouldBe q + (r - (r and 1)) / 2
                    oddRCoordinates.row shouldBe r
                }
            }
        }

        describe("hexagonal - double-height") {
            it("should be reversible") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val doubleHeightCoordinates = hexCoordinates.toDoubleHeightCoordinates()
                    (doubleHeightCoordinates.toHexCoordinates()) shouldBe hexCoordinates
                }
            }

            it("should convert correctly") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val doubleHeightCoordinates = hexCoordinates.toDoubleHeightCoordinates()
                    doubleHeightCoordinates.col shouldBe q
                    doubleHeightCoordinates.row shouldBe r * 2 + q
                }
            }
        }

        describe("hexagonal - double-width") {
            it("should be reversible") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val doubleWidthCoordinates = hexCoordinates.toDoubleWidthCoordinates()
                    (doubleWidthCoordinates.toHexCoordinates()) shouldBe hexCoordinates
                }
            }

            it("should convert correctly") {
                checkAll(gen, gen) { q, r ->
                    val hexCoordinates = HexCoordinates.cached(q, r)
                    val doubleWidthCoordinates = hexCoordinates.toDoubleWidthCoordinates()
                    doubleWidthCoordinates.col shouldBe q * 2 + r
                    doubleWidthCoordinates.row shouldBe r
                }
            }
        }
    }
})
