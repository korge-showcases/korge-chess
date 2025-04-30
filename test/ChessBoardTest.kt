import korlibs.math.geom.*
import kotlin.test.*

class ChessBoardTest {
    @Test
    fun testEmptyBoard() {
        val board = ChessBoard.createEmpty()
        assertEquals(
            """
                ........
                ........
                ........
                ........
                ........
                ........
                ........
                ........
            """.trimIndent(),
            board.toString()
        )
    }

    @Test
    fun testSimpleBoard() {
        val board = ChessBoard.createEmpty()
            .withCell(PointInt(0, 0), ChessCell.Piece(ChessPieceKind.BISHOP, ChessPlayer.BLACK))
            .withCell(PointInt(1, 0), ChessCell.Piece(ChessPieceKind.KNIGHT, ChessPlayer.WHITE))
            .withCell(PointInt(7, 6), ChessCell.Piece(ChessPieceKind.KING, ChessPlayer.BLACK))
        assertEquals(
            """
                Bk......
                ........
                ........
                ........
                ........
                ........
                .......N
                ........
            """.trimIndent(),
            board.toString()
        )
    }

    @Test
    fun testDefaultBoard() {
        assertEquals(
            """
                RKBQNBKR
                PPPPPPPP
                ........
                ........
                ........
                ........
                pppppppp
                rkbqnbkr
            """.trimIndent(),
            ChessBoard.createDefault().toString()
        )
    }

    @Test
    fun testKnightMovements() {
        val board = ChessBoard.createDefault()

        assertEquals(
            setOf(PointInt(0, 2), PointInt(2, 2)),
            board.availableMovements(PointInt(1, 0))
        )
    }

    @Test
    fun testMovement() {
        assertEquals(
            """
                RKBQNBKR
                P.PPPPPP
                .P......
                ........
                ........
                ........
                pppppppp
                rkbqnbkr
            """.trimIndent(),
            ChessBoard.createDefault().withMovement(PointInt(1, 1), PointInt(1, 2)).toString()
        )
    }

    @Test
    fun testFromString() {
        val default = ChessBoard.createDefault()
        assertEquals(
            ChessBoard.createFromString(default.toString()).toString(),
            default.toString()
        )
    }
}
