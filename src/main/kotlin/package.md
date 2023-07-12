# Package base.coordinates

Provides the classes and interfaces for representing different coordinates in hexagonal
grid layouts. The key components of this package are:

- **Coordinates<T>**: This interface sets a general contract for types that implement or extend **Coordinates**. It
  provides constant directions for two specific coordinate layouts: Flat-top and Pointy-top hexagonal grids.

- **FractionalHexCoordinates**: This class is used to represent the fractional parts of a hexagonal coordinate.

- **HexCoordinates**: This class represents a point in a hexagonal grid using a cubical coordinate system (q, r, s).

- **Offset** and **Doubled** Coordinate Classes: This package includes a variety of classes derived from the
  [Coordinates] interface. These classes are used to represent
  different types of hexagonal layouts, like offset (even or odd) or doubled (width or height) coordinates.

# Package base.coordinates.offset

- **Offset Coordinates**: These are representations beneficial for graphical or UI elements in hexagonal grid layouts.
  Based on the type of offset (Odd/Even R/Q), the rows or columns are offset accordingly. The type of offset coordinate
  system (OddR, EvenR, OddQ, EvenQ) is defined by the parities of the rows(columns) and the layout (pointy-topped or
  flat-topped).

- **OddR and EvenR**: In these pointy-topped layouts, rows are offset. Depending on whether the row is odd or even, the
  offset direction varies between left and right respectively.

- **OddQ and EvenQ**: In these flat-topped layouts, columns are offset. Here, either the odd or even columns are
  slightly to the left when moving upwards in the positive direction, depending on whether it's an OddQ or EvenQ layout
  respectively.

All these coordinate systems use a 2D grid representation (column and row) differing from the cube coordinate system
utilized in the [HexCoordinates] class, which maps hexagonal grid coordinates using
three axes [q, r, s], forming a cube
structure.

# Package base.coordinates.doubled

This package provides classes to represent doubled coordinates in a hexagonal grid layout. The classes included in this
package are:

- **DoubleWidthCoordinates**: Represents a pointy top hex layout, where the column step size is doubled, increasing the
  column by 2 for each hex.

- **DoubleHeightCoordinates**: Represents a flat top hex layout, where the row step size is doubled, increasing the row
  by 2 for each hex.

Both classes enforce the constraint `(col + row) % 2 == 0`.

# Package base.math