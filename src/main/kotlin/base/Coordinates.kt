package base


/**
 * The [Coordinates] interface is a generic contract for types which extend or implement
 * [Coordinates] themselves. It provides constant directions for two hexagonal coordinate systems,
 * Flat-top and Pointy-top.
 *
 * The [Flat] object has six directions, starting from 'right-bottom' and moving counter-clockwise.
 * The [Pointy] object also has six directions, starting from 'right' and moving counter-clockwise.
 *
 * @property hex Expects a property of [HexCoordinates] type, which should be provided by instances
 * of this interface.
 *
 * @param T This represents the type of the Coordinates, which is a subtype of [Coordinates] itself.
 */
interface Coordinates<T> where T : Coordinates<T> {

    /**
     * Enumeration of direction constants for Flat-top hexagonal layout.
     * Directions start from 'RightBottom' and move counter-clockwise.
     */
    object Flat {
        const val RightBottom = 0
        const val RightTop = 1
        const val Top = 2
        const val LeftTop = 3
        const val LeftBottom = 4
        const val Bottom = 5
    }

    /**
     * Enumeration of direction constants for Pointy-top hexagonal layout.
     * Directions start from 'Right' and move counter-clockwise.
     */
    object Pointy {
        const val Right = 0
        const val TopRight = 1
        const val TopLeft = 2
        const val Left = 3
        const val BottomLeft = 4
        const val BottomRight = 5
    }

    val hex: HexCoordinates

    /**
     * Transforms the given [this] from [HexCoordinates] into the corresponding subtype of [Coordinates].
     *
     * @return The transformed [HexCoordinates] as a subtype of [Coordinates].
     */
    fun HexCoordinates.into(): T
}

data class FractionalHexCoordinates(val q: Float, val r: Float, val s: Float) {
    fun hexRound(): HexCoordinates = hexRound(q, r)
}

typealias AxialCoordinates = HexCoordinates
typealias CubeCoordinates = HexCoordinates

/**
 *  A data class representing a point in a hexagonal grid using cube coordinates (q, r, s).
 * The [s] coordinate is calculated based on [q] and [r] as `-q - r`.
 */
class HexCoordinates private constructor(val q: Int, val r: Int) : Coordinates<HexCoordinates> {
    val s: Int
        get() = -q - r

    override val hex: HexCoordinates
        get() = this

    override fun HexCoordinates.into(): HexCoordinates = this

    init {
        require(q + r + s == 0) { "q + r + s must be 0" }
    }

    val neighbors by lazy { directions.map { this + it } }

    /**
     * Retrieves the neighboring HexCoordinates in the specified direction.
     *
     * @param direction An integer representing the direction. Should be in the range [0, 5].
     * For the mapping of integer values to directions, refer to
     * [Coordinates.Flat] and [Coordinates.Pointy].
     *
     * @return The neighboring [HexCoordinates] in the specified direction.
     *
     * @throws IndexOutOfBoundsException if the provided direction integer is not in the range [0, 5].
     */
    operator fun get(direction: Int) = directions[direction] + this

    operator fun minus(other: HexCoordinates): HexCoordinates {
        return from(q - other.q, r - other.r)
    }

    operator fun plus(other: HexCoordinates): HexCoordinates {
        return from(q + other.q, r + other.r)
    }

    fun toEvenQCoordinates(): EvenQCoordinates {
        val col = q
        val row = r + (q + (q and 1)) / 2
        return EvenQCoordinates(col, row)
    }

    fun toEvenRCoordinates(): EvenRCoordinates {
        val col = q + (r + (r and 1)) / 2
        val row = r
        return EvenRCoordinates(col, row)
    }

    fun toOddQCoordinates(): OddQCoordinates {
        val col = q
        val row = r + (q - (q and 1)) / 2
        return OddQCoordinates(col, row)
    }

    fun toOddRCoordinates(): OddRCoordinates {
        val col = q + (r - (r and 1)) / 2
        val row = r
        return OddRCoordinates(col, row)
    }

    fun toDoubleHeightCoordinates(): DoubleHeightCoordinates {
        val col = q
        val row = (r * 2) + q
        return DoubleHeightCoordinates(col, row)
    }

    fun toDoubleWidthCoordinates(): DoubleWidthCoordinates {
        val col = (q * 2) + r
        val row = r
        return DoubleWidthCoordinates(col, row)
    }

    override fun toString(): String {
        return "[q=$q, r=$r, s=$s]"
    }

    override fun hashCode(): Int {
        var result = q
        result = 31 * result + r
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HexCoordinates) return false

        if (q != other.q) return false
        if (r != other.r) return false

        return true
    }

    operator fun component1(): Int = q
    operator fun component2(): Int = r
    operator fun component3(): Int = s

    companion object {
        private val cache = mutableMapOf<Pair<Int, Int>, HexCoordinates>()

        /**
         * Retrieves or creates a HexCoordinates object.
         *
         * @param q First hexagonal coordinate component.
         * @param r Second hexagonal coordinate component.
         * @return Cached or new [HexCoordinates] instance.
         */
        fun from(q: Int, r: Int): HexCoordinates {
            return cache.getOrPut(q to r) { HexCoordinates(q, r) }
        }

        /**
         * Alias for [from].
         */
        operator fun invoke(q: Int, r: Int): HexCoordinates = from(q, r)

        /**
         * List of [HexCoordinates] representing the six neighbor directions.
         * These directions are represented as offsets from a central HexCoordinate.
         */
        val directions = listOf(
            HexCoordinates(1, 0),
            HexCoordinates(1, -1),
            HexCoordinates(0, -1),
            HexCoordinates(-1, 0),
            HexCoordinates(-1, 1),
            HexCoordinates(0, 1),
        )

        /**
         * List of [HexCoordinates] representing the six diagonal directions.
         * These directions are represented as offsets from a central HexCoordinate.
         */
        val diagonals = listOf(
            HexCoordinates(2, -1),
            HexCoordinates(1, -2),
            HexCoordinates(-1, -1),
            HexCoordinates(-2, 1),
            HexCoordinates(-1, 2),
            HexCoordinates(1, 1),
        )
    }
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
data class EvenQCoordinates(val col: Int, val row: Int) : Coordinates<EvenQCoordinates> {
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
        return HexCoordinates.from(q, r)
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
data class EvenRCoordinates(val col: Int, val row: Int) : Coordinates<EvenRCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): EvenRCoordinates = toEvenRCoordinates()

    operator fun get(int: Int): EvenRCoordinates {
        val (qDiff, rDiff) = directionDiffs[row and 1][int]
        return EvenRCoordinates(col + qDiff, row + rDiff)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col - (row + (row and 1)) / 2
        val r = row
        return HexCoordinates.from(q, r)
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
data class OddQCoordinates(val col: Int, val row: Int) : Coordinates<OddQCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): OddQCoordinates = toOddQCoordinates()

    operator fun get(int: Int): OddQCoordinates {
        val (qDiff, rDiff) = directionDiffs[col and 1][int]
        return OddQCoordinates(col + qDiff, row + rDiff)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col
        val r = row - (col - (col and 1)) / 2
        return HexCoordinates.from(q, r)
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
data class OddRCoordinates(val col: Int, val row: Int) : Coordinates<OddRCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): OddRCoordinates = toOddRCoordinates()

    operator fun get(int: Int): OddRCoordinates {
        val (qDiff, rDiff) = directionDiffs[row and 1][int]
        return OddRCoordinates(col + qDiff, row + rDiff)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col - (row - (row and 1)) / 2
        val r = row
        return HexCoordinates.from(q, r)
    }

    companion object {
        private val directionDiffs: Array<Array<Pair<Int, Int>>> = arrayOf(
            arrayOf(Pair(1, 0), Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, 1)),
            arrayOf(Pair(1, 0), Pair(1, -1), Pair(0, -1), Pair(-1, 0), Pair(0, 1), Pair(1, 1))
        )
    }
}

data class DoubleHeightCoordinates(val col: Int, val row: Int) : Coordinates<DoubleHeightCoordinates> {
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

data class DoubleWidthCoordinates(val col: Int, val row: Int) : Coordinates<DoubleWidthCoordinates> {
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