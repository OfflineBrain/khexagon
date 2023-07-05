package base

interface Coordinates<T> where T : Coordinates<T> {
    object Flat {
        const val RightBottom = 0
        const val RightTop = 1
        const val Top = 2
        const val LeftTop = 3
        const val LeftBottom = 4
        const val Bottom = 5
    }

    object Pointy {
        const val Right = 0
        const val TopRight = 1
        const val TopLeft = 2
        const val Left = 3
        const val BottomLeft = 4
        const val BottomRight = 5
    }

    val hex: HexCoordinates

    fun HexCoordinates.into(): T
}

typealias AxialCoordinates = HexCoordinates
typealias CubeCoordinates = HexCoordinates

data class HexCoordinates(val q: Int, val r: Int, val s: Int = -q - r) : Coordinates<HexCoordinates> {
    override val hex: HexCoordinates
        get() = this

    override fun HexCoordinates.into(): HexCoordinates = this

    init {
        require(q + r + s == 0) { "q + r + s must be 0" }
    }

    val neighbors by lazy { directions.map { this + it } }

    /**
     * Returns the neighbor in the given direction.
     * @see Coordinates.Flat directions
     * @see Coordinates.Pointy directions
     *
     * @param int the direction
     * @return the neighbor in the given direction
     *
     * @throws IndexOutOfBoundsException if the direction is not in the range [0, 5]
     */
    operator fun get(int: Int) = directions[int] + this

    operator fun minus(other: HexCoordinates): HexCoordinates {
        return HexCoordinates(q - other.q, r - other.r)
    }

    operator fun plus(other: HexCoordinates): HexCoordinates {
        return HexCoordinates(q + other.q, r + other.r)
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

    companion object {
        val directions = listOf(
            HexCoordinates(1, 0),
            HexCoordinates(1, -1),
            HexCoordinates(0, -1),
            HexCoordinates(-1, 0),
            HexCoordinates(-1, 1),
            HexCoordinates(0, 1),
        )

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

data class EvenQCoordinates(val col: Int, val row: Int) : Coordinates<EvenQCoordinates> {
    override val hex: HexCoordinates by lazy { toHexCoordinates() }

    override fun HexCoordinates.into(): EvenQCoordinates = toEvenQCoordinates()

    operator fun get(int: Int): EvenQCoordinates {
        val (qDiff, rDiff) = directionDiffs[col and 1][int]
        return EvenQCoordinates(col + qDiff, row + rDiff)
    }

    fun toHexCoordinates(): HexCoordinates {
        val q = col
        val r = row - (col + (col and 1)) / 2
        return HexCoordinates(q, r)
    }

    companion object {
        private val directionDiffs: Array<Array<Pair<Int, Int>>> = arrayOf(
            arrayOf(Pair(1, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, 1)),
            arrayOf(Pair(1, 0), Pair(1, -1), Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(0, 1)),
        )
    }
}

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
        return HexCoordinates(q, r)
    }

    companion object {
        private val directionDiffs: Array<Array<Pair<Int, Int>>> = arrayOf(
            arrayOf(Pair(1, 0), Pair(1, -1), Pair(0, -1), Pair(-1, 0), Pair(0, 1), Pair(1, 1)),
            arrayOf(Pair(1, 0), Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, 1)),
        )
    }
}

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
        return HexCoordinates(q, r)
    }

    companion object {
        private val directionDiffs: Array<Array<Pair<Int, Int>>> = arrayOf(
            arrayOf(Pair(1, 0), Pair(1, -1), Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(0, 1)),
            arrayOf(Pair(1, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, 1)),
        )
    }
}

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
        return HexCoordinates(q, r)
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
        return HexCoordinates(q, r)
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
        return HexCoordinates(q, r)
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