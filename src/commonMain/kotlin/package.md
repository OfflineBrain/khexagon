# Module khexagon

This module provides a set of classes and functions for working with hexagonal grids. It is based on
the [Red Blob Games](https://www.redblobgames.com/grids/hexagons/) hexagonal grid guide.

# Package io.github.offlinebrain.khexagon.coordinates

Provides the classes and interfaces for representing different coordinates in hexagonal
grid layouts. The key components of this package are:

- **Coordinates<T>**: This interface sets a general contract for types that implement or extend **Coordinates**. It
  provides constant directions for two specific coordinate layouts: Flat-top and Pointy-top hexagonal grids.

- **FractionalHexCoordinates**: This class is used to represent the fractional parts of a hexagonal coordinate.

- **HexCoordinates**: This class represents a point in a hexagonal grid using a cubical coordinate system (q, r, s).

- **Offset** and **Doubled** Coordinate Classes: This package includes a variety of classes derived from the
  [Coordinates] interface. These classes are used to represent
  different types of hexagonal layouts, like offset (even or odd) or doubled (width or height) coordinates.

# Package io.github.offlinebrain.khexagon.coordinates.offset

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

# Package io.github.offlinebrain.khexagon.coordinates.doubled

This package provides classes to represent doubled coordinates in a hexagonal grid layout. The classes included in this
package are:

- **DoubleWidthCoordinates**: Represents a pointy top hex layout, where the column step size is doubled, increasing the
  column by 2 for each hex.

- **DoubleHeightCoordinates**: Represents a flat top hex layout, where the row step size is doubled, increasing the row
  by 2 for each hex.

Both classes enforce the constraint `(col + row) % 2 == 0`.

# Package io.github.offlinebrain.khexagon.math

The package contains utility functions and classes that aid in performing various mathematical operations
specific to the program's focus on coordinates and shapes in a hexagonal grid:

- **Bresenham's Line Algorithm Functions**: These functions, named `bresenhamsLine`, implement the Bresenham's line
  generation algorithm. They can produce a symmetric line of coordinates between two points on a hexagonal grid. It's
  slightly modified to be symmetric, so that the line is the same when drawn from either point.

- **Line Drawing Functions**: The package includes functions like `T.lineTo` that use the Bresenham's line algorithm to
  draw lines between two given coordinates on a hexagonal grid.

- **Distance Calculation Functions**: Functions such as `T.distanceTo` and the standalone `distance` function are used
  to calculate the distance between two given coordinates on a hexagonal grid.

- **Hexagonal Shape Functions**: The package also contains functions like `T.circle` and `T.ring` to generate a circular
  or ring-like arrangement of coordinates around a given point on a hexagonal grid.

Please refer to individual function documentation for more detailed explanations and examples of usage.

# Package io.github.offlinebrain.khexagon.algorithm

- **Path Finding and Node Representation**: The package provides efficient methods for graph traversal and path finding
  using the A* search algorithm. This is implemented via aStar() function and AccessibilityTrie class. It also provides
  extensible interfaces such as PathTile for flexible representation of graph nodes.

- **Visibility Computations**: It includes the SymmetricPreComputedVisionTries class, which is used for pre-calculating
  and handling operations related to line-of-sight (LoS) and field-of-view (FoV) on symmetric grids. It is based
  on [denismr implementation](https://github.com/denismr/SymmetricPCVT).