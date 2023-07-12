# Package base

- **OddRCoordinates**: A representation of odd-R coordinates in a pointy-topped hexagonal grid layout. In this layout,
  odd rows are offset by half a column value to the left. The OddRCoordinates class is advantageous when working with
  graphical representations or user interface elements, where Cartesian coordinates (column and row) would be more
  straightforward and convenient.


- **EvenRCoordinates**: A representation of even-R coordinates in a pointy-topped hexagonal grid layout. In this layout,
  even rows are offset by half a column value. The EvenRCoordinates class is also advantageous when working with
  graphical representations or user interface elements.


- **OddQCoordinates**: A representation of odd-Q coordinates in a flat-topped hexagonal grid layout. The OddQCoordinates
  class interprets the hexagonal grid using a vertical column system in line with odd-Q. In comparison to even-Q layout,
  in this layout, it is the odd-numbered columns that are slightly to the left of the even-numbered columns when moving
  upwards in the positive direction.


- **EvenQCoordinates**: A representation of even-Q coordinates in a flat-topped hexagonal grid layout. This layout
  causes every other column to be offset. Consequently, when progressing vertically in the positive direction, the
  even-numbered columns are slightly to the left of odd-numbered columns.

All these coordinate systems use a 2D grid representation (column and row) differing from the cube coordinate system
utilized in the HexCoordinates class, which maps hexagonal grid coordinates using three axes [q, r, s], forming a cube
structure.