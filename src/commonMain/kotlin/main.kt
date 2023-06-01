import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.text.*
import korlibs.korge.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.korge.view.filter.*
import korlibs.korge.view.property.*
import korlibs.math.geom.*
import korlibs.memory.*

suspend fun main() = Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
	val sceneContainer = sceneContainer()

	//sceneContainer.changeTo({ MainMenuScene() })
    sceneContainer.changeTo({ GameScene() })
}

class MainMenuScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        uiVerticalStack(adjustSize = false) {
            uiButton("START GAME!").onClick {
                sceneContainer.changeTo({ GameScene() })
            }
            uiButton("EXIT GAME!").onClick {
                gameWindow.close()
            }
        }.xy(256, 256)
    }
}

class GameScene : PixelatedScene(128 * 8, 128 * 8, sceneSmoothing = true) {
    @KeepOnReload
    var board: ChessBoard = ChessBoard.createDefault()
        set(value) {
            field = value
            redrawBoard()
        }

    var currentPlayer: ChessPlayer? = ChessPlayer.WHITE

    fun setScenario(currentPlayer: ChessPlayer?, board: ChessBoard) {
        this.currentPlayer = currentPlayer
        this.board = board
    }

    @ViewProperty
    fun setDefaultBoard() {
        setScenario(
            ChessPlayer.WHITE,
            ChessBoard.createDefault()
        )
    }

    @ViewProperty
    fun setAlmostFinishBoard() {
        setScenario(
            ChessPlayer.WHITE,
            ChessBoard.createFromString(
                """
                ........
                ........
                ....N...
                ...q.q..
                ........
                ........
                ........
                ........
            """.trimIndent()
        ))
    }

    lateinit var boardContainer: Container
    lateinit var placeholderContainer: Container
    lateinit var piecesContainer: Container
    lateinit var overlayContainer: Container
    lateinit var text: Text
    lateinit var blacks: List<BmpSlice>
    lateinit var whites: List<BmpSlice>
    lateinit var fullBoard: View

    override suspend fun SContainer.sceneMain() {
        fullBoard = solidRect(width, height, Colors.TRANSPARENT)
        boardContainer = container {  }
        placeholderContainer = container {  }
        piecesContainer = container {  }
        overlayContainer = container {  }
        text = text("position", textSize = 64f, color = Colors.RED)
            .filters(DropshadowFilter(dropX = 1f, dropY = 1f, blurRadius = 1f))
        val images = KR.gfx.chessShadow.read()
        blacks = images.run { listOf(b_pawn, b_bishop, b_knight, b_rook, b_queen, b_king) }
        whites = images.run { listOf(w_pawn, w_bishop, w_knight, w_rook, w_queen, w_king) }
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

        //for (n in 0 until 6) {
        //    image(whites[n]).centered.xy(n * 128 + 64, 128 + 64).scale(0.9)
        //    //image(whites[n]).xy(n * 128, 128)
        //}

        redrawBoard()

        uiHorizontalStack {
            uiButton("QUIT GAME").onClick {
                sceneContainer.changeTo({ MainMenuScene() })
            }
            //uiButton("UNDO").onClick {}
        }

        //setAlmostFinishBoard()
	}

    fun redrawBoard() {
        overlayContainer.removeChildren()
        placeholderContainer.removeChildren()
        piecesContainer.removeChildren()

        val wonPlayer = board.isCheckMate()
        if (wonPlayer != null) {
            overlayContainer.solidRect(Size(sceneWidth, sceneHeight), Colors.TRANSPARENT).onClick {
                text.text = "CLICKED!"
                //setDefaultBoard()
                sceneContainer.changeTo({ MainMenuScene() })
            }
            overlayContainer.text(
                "Player ${wonPlayer}\nWON",
                alignment = TextAlignment.MIDDLE_CENTER,
                color = Colors.YELLOW,
                textSize = 128f
            ).centerOn(this.sceneContainer)
                .filters(DropshadowFilter())
            currentPlayer = null
        }

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
            val image = piecesContainer.image(v.getImage()).centered.xy(piecePos.toFloat() * Size(128, 128) + Point(64, 64))
                .scale(0.9)
                .alpha(
                    when (currentPlayer) {
                        null -> 1f
                        v.player -> 1f
                        else -> .5f
                    }
                )
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
                            if (isValidMovement) {
                                setScenario(
                                    currentPlayer?.opposite,
                                    board.withMovement(piecePos, newPos)
                                )
                            } else {
                                redrawBoard()
                            }
                            //text.text = "${isValidMovement}"
                        }
                    }
                }
            }
        }
    }
}
