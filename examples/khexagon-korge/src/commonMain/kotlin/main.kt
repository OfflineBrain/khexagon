import io.github.offlinebrain.khexagon.coordinates.*
import io.github.offlinebrain.khexagon.math.*
import io.github.offlinebrain.khexagon.math.Orientation.Companion.Flat
import korlibs.datastructure.iterators.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import io.github.offlinebrain.khexagon.math.Point as HexPoint

suspend fun main() = Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo { MyScene() }
}

class MyScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val coordinates = arrayListOf(
            HexCoordinates(1, 1),
            HexCoordinates(2, 1),
            HexCoordinates(2, 2),
            HexCoordinates(1, 2),
            HexCoordinates(0, 2),
            HexCoordinates(0, 1),
            HexCoordinates(4, 0),
        )
        val layout = Layout(Flat, HexPoint(30.0f, 30.0f), HexPoint(30.0f, 10.0f))
        val polygonCorners =
            coordinates.map { hx -> polygonCorners(layout, hx).parallelMap { Point(it.x, it.y) } }


        val bitmap = Bitmap32(256, 256).context2d {
            fill(Colors.BLACK) {
                rect(0, 0, 256, 256)
            }
            polygonCorners.forEach {
                polygon(it)
            }

            stroke(Colors.WHITE)
        }

        val image = image(bitmap) {
            position(150, 150)
        }

        addUpdater {
            if (views.input.mouseButtons != 0) {
                val mouse = views.input.mousePos - image.globalBounds.position
                val hex = pixelToHex(layout, HexPoint(mouse.x.toFloat(), mouse.y.toFloat()))
                bitmap.context2d {
                    fill(Colors.BLACK) {
                        rect(0, 0, 256, 256)
                    }
                    polygonCorners.forEach {
                        polygon(it)
                    }
                    stroke(Colors.WHITE)
                    fill(Colors.RED) {
                        val corners = polygonCorners(layout, hex.hexRound())
                        polygon(corners.parallelMap { Point(it.x, it.y) })
                    }
                }
            }
        }
    }
}
