import korlibs.datastructure.*

enum class ChessPieceKind(val key: Char, val index: Int) {
    PAWN('P', 0),
    BISHOP('B', 1),
    KNIGHT('K', 2),
    ROOK('R', 3),
    QUEEN('Q', 4),
    KING('N', 5)
}

enum class ChessPlayer {
    BLACK, WHITE
}

sealed interface ChessCell {
    val key: Char

    object Empty : ChessCell {
        override val key: Char get() = '.'
        override fun toString(): String = "$key"
    }
    data class Piece(
        val kind: ChessPieceKind,
        val player: ChessPlayer
    ) : ChessCell {
        override val key: Char get() = when (player) {
            ChessPlayer.BLACK -> kind.key.uppercaseChar()
            ChessPlayer.WHITE -> kind.key.lowercaseChar()
        }
        override fun toString(): String = "$key"
    }
}

class ChessBoard(val array: Array2<ChessCell> = Array2<ChessCell>(8, 8) { ChessCell.Empty }) {
    companion object {
        fun createDefault(): ChessBoard {
            val board = ChessBoard()
            for (player in listOf(ChessPlayer.BLACK, ChessPlayer.WHITE)) {
                fun getY(y: Int): Int = when (player) {
                    ChessPlayer.BLACK -> y
                    ChessPlayer.WHITE -> 7 - y
                }
                val pawn = ChessCell.Piece(ChessPieceKind.PAWN, player)
                val rook = ChessCell.Piece(ChessPieceKind.ROOK, player)
                val knight = ChessCell.Piece(ChessPieceKind.KNIGHT, player)
                val bishop = ChessCell.Piece(ChessPieceKind.BISHOP, player)
                val queen = ChessCell.Piece(ChessPieceKind.QUEEN, player)
                val king = ChessCell.Piece(ChessPieceKind.KING, player)
                for (n in 0 until 8) board[n, getY(1)] = pawn
                board[0, getY(0)] = rook
                board[1, getY(0)] = knight
                board[2, getY(0)] = bishop
                board[3, getY(0)] = queen
                board[4, getY(0)] = king
                board[5, getY(0)] = bishop
                board[6, getY(0)] = knight
                board[7, getY(0)] = rook
            }
            return board
        }
    }

    operator fun get(x: Int, y: Int): ChessCell = array[x, y]
    operator fun set(x: Int, y: Int, value: ChessCell) { array[x, y] = value }

    override fun toString(): String {
        return (0 until array.height).joinToString("\n") { y ->
            (0 until array.width).joinToString("") { x ->
                array[x, y].toString()
            }
        }
    }
}

