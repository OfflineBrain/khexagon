package io.github.offlinebrain.khexagon.coordinates

interface OffsetCoordinates<T> : Coordinates<T> where T : OffsetCoordinates<T> {
    val col: Int
    val row: Int
}

/**
 * Data class representing even-Q coordinates in a flat-topped hexagonal grid layout.
 * The Even-Q coordinate system makes use of a 2D grid representation (column and row), which differs from the cube coordinates system used by the HexCoordinates:
 * - In the [HexCoordinates] class, the hexagonal grid coordinates are denoted by three axes [q, r, s], hence forming a cube structure.
 * - The [EvenQCoordinates] class, on the other hand, interprets the hexagonal grid using a vertical column system aligned with even-Q. Here, the column ('q') has its origin at the top-left.
 * - This layout causes every other column to be offset. Consequently, when progressing vertically in the positive direction, the even-numbered columns are slightly to the left of odd-numbered columns.
 *
 * The [col] field denotes the column (equivalent to the q-axis in cube coordinates), while the [row] denotes the row.
 *
 * The [EvenQCoordinates] class can be particularly useful when dealing with UI elements or graphical representations where Cartesian coordinates (column and row) are more intuitive to work with.
 */
data class EvenQCoordinates(override val col: Int, override val row: Int) : OffsetCoordinates<EvenQCoordinates>,
    FromHexCoordinates<EvenQCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): EvenQCoordinates = toEvenQCoordinates()

    /**
     * Retrieves neighbouring grid element in the particular direction.
     * @param direction Direction of neighbour. Should be in range [0, 5].
     * @return The neighbouring EvenQCoordinates in the specified direction.
     */
    operator fun get(direction: Int): EvenQCoordinates {
        val (qDiff, rDiff) = directionDiffs[col and 1][direction]
        return EvenQCoordinates(col + qDiff, row + rDiff)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col
        val r = row - (col + (col and 1)) / 2
        return HexCoordinates.cached(q, r)
    }

    companion object {
        /**
         * Predefined offset differences for transitioning between [EvenQCoordinates]
         * based on the current column and the direction.
         * The primary array represents column parity (even/odd), and secondary array represents direction.
         */
        private val directionDiffs: Array<Array<Pair<Int, Int>>> = arrayOf(
            arrayOf(Pair(1, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, 1)),
            arrayOf(Pair(1, 0), Pair(1, -1), Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(0, 1)),
        )
    }
}

/**
 * Data class representing even-R coordinates in a pointy-topped hexagonal grid layout.
 * The Even-R coordinate system uses a 2D grid representation (column and row) differing from the cube coordinate system utilized in the [HexCoordinates] class:
 * - The [HexCoordinates] class maps hexagonal grid coordinates using three axes [q, r, s], forming a cube structure.
 * - Conversely, the [EvenRCoordinates] class interprets the hexagonal grid using a horizontal row system consistent with even-R. Here, 'R' aligns with the row, with the origin placed at the top-left.
 * - In this layout, even rows are offset by half a column value. As one moves vertically in the positive direction, each even row appears shifted right to the midway of the adjacent columns.
 *
 * The [col] attribute denotes the column, whereas the [row] signifies the row (which aligns with the r-axis in cube coordinates).
 *
 * The [EvenRCoordinates] can be particularly advantageous when working with graphical representations or user interface elements, where Cartesian coordinates (column and row) would be more straightforward and convenient.
 */
data class EvenRCoordinates(override val col: Int, override val row: Int) : OffsetCoordinates<EvenRCoordinates>,
    FromHexCoordinates<EvenRCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): EvenRCoordinates = toEvenRCoordinates()

    operator fun get(int: Int): EvenRCoordinates {
        val (qDiff, rDiff) = directionDiffs[row and 1][int]
        return EvenRCoordinates(col + qDiff, row + rDiff)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col - (row + (row and 1)) / 2
        val r = row
        return HexCoordinates.cached(q, r)
    }

    companion object {
        private val directionDiffs: Array<Array<Pair<Int, Int>>> = arrayOf(
            arrayOf(Pair(1, 0), Pair(1, -1), Pair(0, -1), Pair(-1, 0), Pair(0, 1), Pair(1, 1)),
            arrayOf(Pair(1, 0), Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, 1)),
        )
    }
}


/**
 * Data class representing odd-Q coordinates in a flat-topped hexagonal grid layout.
 * The Odd-Q coordinate system employs a 2D grid representation (column and row), which differentiates it from the cube coordinate system utilized in the [HexCoordinates] class:
 * - The [HexCoordinates] class represents hexagonal grid coordinates with three axes [q, r, s], forming a cube structure.
 * - The [OddQCoordinates] class, alternatively, interprets the hexagonal grid using a vertical column system in line with odd-Q. The column ('q') holds its origin at the top-left.
 * - In contrast to even-Q layout, in this layout, it is the odd-numbered columns that are slightly to the left of the even-numbered columns when moving upwards in the positive direction.
 *
 * The [col] field stands for the column (corresponding to the q-axis in cube coordinates), while the [row] represents the row.
 *
 * The [OddQCoordinates] class can be beneficial when working with UI elements or graphical representations where Cartesian coordinates are more practical.
 */
data class OddQCoordinates(override val col: Int, override val row: Int) : OffsetCoordinates<OddQCoordinates>,
    FromHexCoordinates<OddQCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): OddQCoordinates = toOddQCoordinates()

    operator fun get(int: Int): OddQCoordinates {
        val (qDiff, rDiff) = directionDiffs[col and 1][int]
        return OddQCoordinates(col + qDiff, row + rDiff)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col
        val r = row - (col - (col and 1)) / 2
        return HexCoordinates.cached(q, r)
    }

    companion object {
        private val directionDiffs: Array<Array<Pair<Int, Int>>> = arrayOf(
            arrayOf(Pair(1, 0), Pair(1, -1), Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(0, 1)),
            arrayOf(Pair(1, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, 1)),
        )
    }
}

/**
 * Data class representing odd-R coordinates in a pointy-topped hexagonal grid layout.
 * The Odd-R coordinate system uses a 2D grid representation (column and row) differing from the cube coordinate system utilized in the [HexCoordinates] class:
 * - The [HexCoordinates] class maps hexagonal grid coordinates using three axes [q, r, s], forming a cube structure.
 * - Conversely, the [OddRCoordinates] class interprets the hexagonal grid using a horizontal row system consistent with odd-R. Here, 'R' aligns with the row, with the origin placed at the top-left.
 * - In this layout, odd rows are offset by half a column value to the left. As one moves vertically in the positive direction, each odd row appears shifted left to the midway of the adjacent columns.
 *
 * The [col] attribute denotes the column, whereas the [row] signifies the row (which aligns with the r-axis in cube coordinates).
 *
 * The [OddRCoordinates] can be particularly advantageous when working with graphical representations or user interface elements, where Cartesian coordinates (column and row) would be more straightforward and convenient.
 */
data class OddRCoordinates(override val col: Int, override val row: Int) : OffsetCoordinates<OddRCoordinates>,
    FromHexCoordinates<OddRCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): OddRCoordinates = toOddRCoordinates()

    operator fun get(int: Int): OddRCoordinates {
        val (qDiff, rDiff) = directionDiffs[row and 1][int]
        return OddRCoordinates(col + qDiff, row + rDiff)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col - (row - (row and 1)) / 2
        val r = row
        return HexCoordinates.cached(q, r)
    }

    companion object {
        private val directionDiffs: Array<Array<Pair<Int, Int>>> = arrayOf(
            arrayOf(Pair(1, 0), Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, 1)),
            arrayOf(Pair(1, 0), Pair(1, -1), Pair(0, -1), Pair(-1, 0), Pair(0, 1), Pair(1, 1))
        )
    }
}


fun HexCoordinates.toEvenQCoordinates(): EvenQCoordinates {
    val col = q
    val row = r + (q + (q and 1)) / 2
    return EvenQCoordinates(col, row)
}

fun HexCoordinates.toEvenRCoordinates(): EvenRCoordinates {
    val col = q + (r + (r and 1)) / 2
    val row = r
    return EvenRCoordinates(col, row)
}

fun HexCoordinates.toOddQCoordinates(): OddQCoordinates {
    val col = q
    val row = r + (q - (q and 1)) / 2
    return OddQCoordinates(col, row)
}

fun HexCoordinates.toOddRCoordinates(): OddRCoordinates {
    val col = q + (r - (r and 1)) / 2
    val row = r
    return OddRCoordinates(col, row)
}
