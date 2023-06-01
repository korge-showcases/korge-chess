import korlibs.datastructure.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.math.geom.slice.*
import korlibs.memory.*

suspend fun main() = Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
	val sceneContainer = sceneContainer()

	sceneContainer.changeTo({ MyScene() })
}

class MyScene : PixelatedScene(128 * 8, 128 * 8, sceneSmoothing = true) {
	override suspend fun SContainer.sceneMain() {
        val images = KR.gfx.chessShadow.read()
        val blacks = images.run { listOf(b_pawn, b_bishop, b_knight, b_rook, b_queen, b_king) }
        val whites = images.run { listOf(w_pawn, w_bishop, w_knight, w_rook, w_queen, w_king) }
        val bgs = images.run { listOf(square_brown_dark, square_brown_light) }
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                image(bgs[(x + y).isEven.toInt()]).xy(128 * x, 128 * y)
            }
        }

        val board = ChessBoard.createDefault()

        representBoard(board, blacks, whites)
        //for (n in 0 until 6) {
        //    image(whites[n]).centered.xy(n * 128 + 64, 128 + 64).scale(0.9)
        //    //image(whites[n]).xy(n * 128, 128)
        //}
	}

    private fun Container.representBoard(
        board: ChessBoard = ChessBoard(),
        blacks: List<RectSlice<out Bitmap>>,
        whites: List<RectSlice<out Bitmap>>
    ) {
        fun ChessCell.getImage(): BmpSlice {
            return when (this) {
                ChessCell.Empty -> Bitmaps.transparent
                is ChessCell.Piece -> {
                    val array = when (this.player) {
                        ChessPlayer.BLACK -> blacks
                        ChessPlayer.WHITE -> whites
                    }
                    array[this.kind.index]
                }
            }
        }

        board.array.each { x, y, v ->
            image(v.getImage()).centered.xy(x * 128 + 64, y * 128 + 64).scale(0.9)
        }
    }
}
