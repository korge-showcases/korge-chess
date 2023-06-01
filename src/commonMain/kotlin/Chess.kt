import korlibs.datastructure.*
import korlibs.io.lang.*
import korlibs.math.geom.*
import kotlin.math.*

enum class ChessPieceKind(val key: Char, val index: Int) {
    PAWN('P', 0),
    BISHOP('B', 1),
    KNIGHT('K', 2),
    ROOK('R', 3),
    QUEEN('Q', 4),
    KING('N', 5);
    companion object {
        val VALUES = values()
        val UPPER_KEYS = values().associateBy { it.key.uppercaseChar() }
        val LOWER_KEYS = values().associateBy { it.key.lowercaseChar() }
    }
}

enum class ChessPlayer {
    BLACK, WHITE;

    val opposite: ChessPlayer get() = if (isBlack) WHITE else BLACK
    val isBlack get() = this == BLACK
    val isWhite get() = this == WHITE
    val sign: Int get() = if (isBlack) +1 else -1
}

sealed interface ChessCell {
    val value: Int
    val key: Char
    val isEmpty: Boolean get() = this == EMPTY
    val kind: ChessPieceKind? get() = null
    val player: ChessPlayer? get() = null

    companion object {
        val EMPTY = Empty()

        fun fromValue(value: Int): ChessCell {
            if (value == 0) return EMPTY
            val kindIndex = value.absoluteValue - 1
            val kind = ChessPieceKind.VALUES[kindIndex]
            val player = if (value > 0) ChessPlayer.BLACK else ChessPlayer.WHITE
            return Piece(kind, player)
        }
    }

    class Empty : ChessCell {
        override val value: Int = 0
        override val key: Char get() = '.'
        override fun toString(): String = "$key"
    }
    data class Piece(
        override val kind: ChessPieceKind,
        override val player: ChessPlayer
    ) : ChessCell {
        override val value: Int = (kind.index + 1) * (player.sign)
        override val key: Char get() = when (player) {
            ChessPlayer.BLACK -> kind.key.uppercaseChar()
            ChessPlayer.WHITE -> kind.key.lowercaseChar()
        }
        override fun toString(): String = "$key"
    }
}

inline class ChessBoard(
    @PublishedApi internal val array: IntArray2
) {
    val width: Int get() = array.width
    val height: Int get() = array.height
    operator fun get(x: Int, y: Int): ChessCell = ChessCell.fromValue(array[x, y])
    operator fun get(p: PointInt): ChessCell = this[p.x, p.y]
    //operator fun set(p: PointInt, value: ChessCell) { this[p.x, p.y] = value }

    companion object {
        fun createFromString(str: String): ChessBoard {
            return ChessBoard(IntArray2(str, '.') { char, x, y ->
                val upperKind = ChessPieceKind.UPPER_KEYS[char]
                val lowerKind = ChessPieceKind.LOWER_KEYS[char]
                when {
                    upperKind != null -> ChessCell.Piece(upperKind, ChessPlayer.BLACK)
                    lowerKind != null -> ChessCell.Piece(lowerKind, ChessPlayer.WHITE)
                    else -> ChessCell.EMPTY
                }.value
            })
        }
        fun createEmpty(): ChessBoard = ChessBoard(IntArray2(8, 8) { 0 })
        fun createDefault(): ChessBoard {
            val array = createEmpty().array.clone()
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
                for (n in 0 until 8) array[n, getY(1)] = pawn.value
                array[0, getY(0)] = rook.value
                array[1, getY(0)] = knight.value
                array[2, getY(0)] = bishop.value
                array[3, getY(0)] = queen.value
                array[4, getY(0)] = king.value
                array[5, getY(0)] = bishop.value
                array[6, getY(0)] = knight.value
                array[7, getY(0)] = rook.value
            }
            return ChessBoard(array)
        }
    }

    fun withCell(pos: PointInt, cell: ChessCell): ChessBoard {
        return ChessBoard(array.clone().also { it[pos] = cell.value })
    }

    fun withMovement(oldPos: PointInt, newPos: PointInt): ChessBoard {
        val newArray = array.clone()
        val cell = newArray[oldPos]
        newArray[oldPos] = ChessCell.EMPTY.value
        newArray[newPos] = cell
        return ChessBoard(newArray)
    }


    override fun toString(): String {
        return (0 until this.height).joinToString("\n") { y ->
            (0 until this.width).joinToString("") { x ->
                this[x, y].toString()
            }
        }
    }

    fun inBoard(pos: PointInt): Boolean =
        pos.x in 0..7
            && pos.y in 0..7

    fun validMovementPosition(oldPos: PointInt, newPos: PointInt): Boolean =
        inBoard(newPos)
            && this[newPos].player != this[oldPos].player

    fun availableMovements(oldPos: PointInt, acceptCheck: Boolean = false): Set<PointInt> {
        val piece = this[oldPos]
        val player = piece.player
        return when (piece) {
            is ChessCell.Empty -> return emptySet()
            is ChessCell.Piece -> {
                when (piece.kind) {
                    ChessPieceKind.PAWN -> {
                        buildList {
                            val dir = when (piece.player) {
                                ChessPlayer.BLACK -> PointInt(0, +1)
                                ChessPlayer.WHITE -> PointInt(0, -1)
                            }
                            val isInitialPosition = when (piece.player) {
                                ChessPlayer.BLACK -> oldPos.y == 1
                                ChessPlayer.WHITE -> oldPos.y == 6
                            }
                            fun addIfEmpty(pos: PointInt) {
                                if (inBoard(pos) && this@ChessBoard[pos].isEmpty) {
                                    add(pos)
                                }
                            }
                            fun addHasPlayer(pos: PointInt, player: ChessPlayer?) {
                                if (inBoard(pos) && this@ChessBoard[pos].player == player) {
                                    add(pos)
                                }
                            }
                            addIfEmpty(oldPos + dir)
                            if (isInitialPosition) {
                                addIfEmpty(oldPos + (dir.toFloat() * 2).toInt())
                            }
                            addHasPlayer(oldPos + dir + PointInt(-1, 0), player?.opposite)
                            addHasPlayer(oldPos + dir + PointInt(+1, 0), player?.opposite)
                        }
                    }
                    ChessPieceKind.KNIGHT -> {
                        listOf(
                            oldPos - PointInt(-1, -2),
                            oldPos - PointInt(+1, -2),
                            oldPos - PointInt(-1, +2),
                            oldPos - PointInt(+1, +2),
                            oldPos - PointInt(-2, -1),
                            oldPos - PointInt(+2, -1),
                            oldPos - PointInt(-2, +1),
                            oldPos - PointInt(+2, +1),
                        )
                    }
                    ChessPieceKind.BISHOP, ChessPieceKind.ROOK, ChessPieceKind.QUEEN, ChessPieceKind.KING -> {
                        buildList {
                            val bishopDirs = listOf(PointInt(-1, -1), PointInt(+1, -1), PointInt(-1, +1), PointInt(+1, +1))
                            val rookDirs = listOf(PointInt(-1, 0), PointInt(0, -1), PointInt(+1, 0), PointInt(0, +1))
                            val extent = if (piece.kind == ChessPieceKind.KING) 1 else 7
                            val dirs = when (piece.kind) {
                                ChessPieceKind.BISHOP -> bishopDirs
                                ChessPieceKind.ROOK -> rookDirs
                                ChessPieceKind.KING, ChessPieceKind.QUEEN -> bishopDirs + rookDirs
                                else -> unreachable
                            }
                            for (dir in dirs) {
                                for (n in 1 .. extent) {
                                    val newPos = oldPos + (dir.toFloat() * n.toFloat()).toInt()
                                    if (!validMovementPosition(oldPos, newPos)) break
                                    add(newPos)
                                    if (this@ChessBoard[newPos] != ChessCell.EMPTY) break
                                }
                            }
                        }
                    }
                }
            }
        }
            .filter { validMovementPosition(oldPos, it) }
            .filter { if (acceptCheck) true else !withMovement(oldPos, it).isCheck(player) && this[it].kind != ChessPieceKind.KING }
            .toSet()

    }

    fun isCheckMate(player: ChessPlayer?): Boolean {
        if (player == null) return false
        each { pos, v ->
            if (v.player == player) {
                if (availableMovements(pos, acceptCheck = false).isNotEmpty()) {
                    return false
                }
            }
        }
        return true
    }

    fun isCheck(player: ChessPlayer?): Boolean {
        if (player == null) return false
        each { pos, v ->
            if (v.player != player) {
                for (newPos in availableMovements(pos, acceptCheck = true)) {
                    val newCell = this[newPos]
                    if (newCell.kind == ChessPieceKind.KING && newCell.player == player) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun isCheck(): ChessPlayer? {
        for (player in ChessPlayer.values()) if (isCheck(player)) return player
        return null
    }

    fun isCheckMate(): ChessPlayer? {
        for (player in ChessPlayer.values()) if (isCheckMate(player)) return player
        return null
    }

    inline fun each(callback: (pos: PointInt, v: ChessCell) -> Unit) {

        this.array.each { x, y, v -> callback(PointInt(x, y), ChessCell.fromValue(v)) }
    }
}
