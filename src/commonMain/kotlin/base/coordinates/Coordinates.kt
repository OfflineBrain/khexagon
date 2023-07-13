package base.coordinates

import base.coordinates.offset.*
import base.coordinates.doubled.*


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
    fun hexRound(): HexCoordinates = base.math.hexRound(q, r)
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
