package base.coordinates.doubled

import base.coordinates.Coordinates
import base.coordinates.FromHexCoordinates
import base.coordinates.HexCoordinates


/**
 * Represents a double width coordinate in the hexagonal grid layout, which doubles the column step size.
 * Use in a pointy top hex layout, the column is increased by 2 for each hex.
 *
 * Constraint: (col + row) % 2 == 0
 *
 * @property col Column of the coordinate.
 * @property row Row of the coordinate.
 */
data class DoubleWidthCoordinates(val col: Int, val row: Int) : Coordinates<DoubleWidthCoordinates>,
    FromHexCoordinates<DoubleWidthCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): DoubleWidthCoordinates = toDoubleWidthCoordinates()

    val neighbors by lazy { directions.map { this + it } }

    operator fun get(int: Int): DoubleWidthCoordinates {
        return this + directions[int]
    }

    operator fun plus(other: DoubleWidthCoordinates): DoubleWidthCoordinates {
        return DoubleWidthCoordinates(col + other.col, row + other.row)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = (col - row) / 2
        val r = row
        return HexCoordinates.from(q, r)
    }

    companion object {
        private val directions = listOf(
            DoubleWidthCoordinates(2, 0),
            DoubleWidthCoordinates(1, -1),
            DoubleWidthCoordinates(-1, -1),
            DoubleWidthCoordinates(-2, 0),
            DoubleWidthCoordinates(-1, 1),
            DoubleWidthCoordinates(1, 1),
        )
    }
}


/**
 * Represents a double height coordinate in the hexagonal grid layout, which doubles the row step size.
 * Use in a flat top hex layout, the row is increased by 2 for each hex.
 *
 * Constraint: (col + row) % 2 == 0
 *
 * @property col Column of the coordinate.
 * @property row Row of the coordinate.
 */
data class DoubleHeightCoordinates(val col: Int, val row: Int) : Coordinates<DoubleHeightCoordinates>,
    FromHexCoordinates<DoubleHeightCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): DoubleHeightCoordinates = toDoubleHeightCoordinates()

    val neighbors by lazy { directions.map { this + it } }

    operator fun get(int: Int): DoubleHeightCoordinates {
        return this + directions[int]
    }

    operator fun plus(other: DoubleHeightCoordinates): DoubleHeightCoordinates {
        return DoubleHeightCoordinates(col + other.col, row + other.row)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col
        val r = (row - col) / 2
        return HexCoordinates.from(q, r)
    }

    companion object {
        private val directions = listOf(
            DoubleHeightCoordinates(1, 1),
            DoubleHeightCoordinates(1, -1),
            DoubleHeightCoordinates(0, -2),
            DoubleHeightCoordinates(-1, -1),
            DoubleHeightCoordinates(-1, 1),
            DoubleHeightCoordinates(0, 2),
        )
    }
}