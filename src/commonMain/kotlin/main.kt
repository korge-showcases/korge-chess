import korlibs.datastructure.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.korge.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.korge.view.filter.*
import korlibs.math.geom.*
import korlibs.math.geom.slice.*
import korlibs.memory.*

suspend fun main() = Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
	val sceneContainer = sceneContainer()

	sceneContainer.changeTo({ MyScene() })
}

class MyScene : PixelatedScene(128 * 8, 128 * 8, sceneSmoothing = true) {
    @KeepOnReload
    var board: ChessBoard = ChessBoard.createDefault()

	override suspend fun SContainer.sceneMain() {
        var currentPlayer = ChessPlayer.WHITE
        val boardContainer = container {  }
        val placeholderContainer = container {  }
        val piecesContainer = container {  }
        val text = text("position", textSize = 64f, color = Colors.RED)
            .filters(DropshadowFilter(dropX = 1f, dropY = 1f, blurRadius = 1f))
        val images = KR.gfx.chessShadow.read()
        val blacks = images.run { listOf(b_pawn, b_bishop, b_knight, b_rook, b_queen, b_king) }
        val whites = images.run { listOf(w_pawn, w_bishop, w_knight, w_rook, w_queen, w_king) }
        val bgs = images.run { listOf(square_brown_dark, square_brown_light) }

        //board = board
        //    .withCell(PointInt(2, 3), ChessCell.Piece(ChessPieceKind.PAWN, ChessPlayer.BLACK))
        //    .withCell(PointInt(6, 4), ChessCell.Piece(ChessPieceKind.ROOK, ChessPlayer.WHITE))
        //    .withCell(PointInt(3, 4), ChessCell.Piece(ChessPieceKind.QUEEN, ChessPlayer.WHITE))
        //    .withCell(PointInt(2, 4), ChessCell.Piece(ChessPieceKind.KING, ChessPlayer.WHITE))

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                boardContainer.image(bgs[(x + y).isEven.toInt()]).xy(128 * x, 128 * y).apply {
                }
            }
        }

        fun Container.representBoard() {
            fun ChessCell.getImage(): BmpSlice {
                return when (this) {
                    is ChessCell.Empty -> Bitmaps.transparent
                    is ChessCell.Piece -> {
                        val array = when (this.player) {
                            ChessPlayer.BLACK -> blacks
                            ChessPlayer.WHITE -> whites
                        }
                        array[this.kind.index]
                    }
                }
            }

            board.each { piecePos, v ->
                val image = image(v.getImage()).centered.xy(piecePos.toFloat() * Size(128, 128) + Point(64, 64))
                    .scale(0.9)
                    .alpha(if (v.player == currentPlayer) 1f else .5f)
                if (v.player == currentPlayer) {
                    image.mouse {
                        image.draggable {
                            text.text = "PIECE: $v, piecePos=$piecePos"
                            val newPos = (it.viewNextXY / Size(128, 128)).toInt()
                            val validMovements = try {
                                board.availableMovements(piecePos)// + piecePos
                            } catch (e: Throwable) {
                                text.text = "ERROR: ${e.message}"
                                emptyList()
                            }
                            val isValidMovement = newPos in validMovements

                            if (it.start) {
                                image.zIndex = 1000f
                                //text.text = "$x,$y : $movements"
                                placeholderContainer.removeChildren()
                                for (movement in validMovements) {
                                    val color = if (!board[movement].isEmpty) Colors.DARKRED else Colors.GREENYELLOW
                                    placeholderContainer.solidRect(128, 128, color.withAd(0.25))
                                        .xy(movement.toFloat() * Size(128, 128))
                                }
                            }
                            if (it.end) {
                                placeholderContainer.removeChildren()
                                if (isValidMovement) {
                                    board = board.withMovement(piecePos, newPos)
                                    currentPlayer = currentPlayer.opposite
                                    text.text = "${board.isCheckMate()}"
                                }
                                //text.text = "${isValidMovement}"
                                piecesContainer.removeChildren()
                                piecesContainer.representBoard()
                            }
                        }
                    }
                }
            }
        }
        //for (n in 0 until 6) {
        //    image(whites[n]).centered.xy(n * 128 + 64, 128 + 64).scale(0.9)
        //    //image(whites[n]).xy(n * 128, 128)
        //}

        piecesContainer.representBoard()

	}
}
