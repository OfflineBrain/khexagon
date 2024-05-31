# KHexagon Library

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.offlinebrain/khexagon?color=%2300aa00)](https://central.sonatype.com/artifact/io.github.offlinebrain/khexagon)


## Overview

KHexagon is a Kotlin library designed to assist with the mathematical manipulation and geometric visualization of
hexagonal grids. Whether you're developing a board game or a hexagonal tile-based simulation, KHexagon provides a
foundational toolset to streamline the grid calculation and representation processes.

The library offers comprehensive functionality for operations in a hexagonal grid context including, but not limited to:

- Generating lines and shapes like circles or rings on a hexagonal grid.
- Calculating distances between points.
- Providing support for different types of hexagonal layouts (like pointy-top, flat-top, offset, and doubled grids).


### Add dependency to your project


#### Gradle
```kotlin
dependencies {
    implementation("io.github.offlinebrain:khexagon:$khexagonVersion")
}
```
#### Maven
```xml
<dependency>
    <groupId>io.github.offlinebrain</groupId>
    <artifactId>khexagon</artifactId>
    <version>${khexagon.version}</version>
</dependency>
```

