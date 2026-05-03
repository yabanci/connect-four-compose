import game.Cell
import game.CellPos
import game.GameConfig
import game.GameLogic
import game.GameState
import game.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameLogicTest {

    private val standard = GameConfig(rows = 6, cols = 7, winLength = 4)

    @Test
    fun newGameIsEmptyAndRedToMove() {
        val s = GameLogic.newGame(standard)
        assertEquals(Player.RED, s.currentPlayer)
        assertEquals(0, s.moveCount)
        assertNull(s.winner)
        assertTrue(s.board.all { row -> row.all { it == Cell.EMPTY } })
    }

    @Test
    fun pieceLandsAtBottom() {
        val s = GameLogic.dropPiece(GameLogic.newGame(standard), col = 0)
        assertNotNull(s)
        assertEquals(Cell.RED, s.board[5][0])
        assertEquals(Player.YELLOW, s.currentPlayer)
        assertEquals(1, s.moveCount)
    }

    @Test
    fun piecesStackUpward() {
        val s = drops(standard, 2, 2, 2)
        assertEquals(Cell.RED, s.board[5][2])
        assertEquals(Cell.YELLOW, s.board[4][2])
        assertEquals(Cell.RED, s.board[3][2])
    }

    @Test
    fun fullColumnRejectsDrop() {
        val s = drops(standard, 0, 0, 0, 0, 0, 0)
        assertNull(GameLogic.dropPiece(s, 0))
    }

    @Test
    fun outOfRangeColumnRejectsDrop() {
        val s = GameLogic.newGame(standard)
        assertNull(GameLogic.dropPiece(s, -1))
        assertNull(GameLogic.dropPiece(s, standard.cols))
    }

    // Win-direction tests: build a board one move from a win and let dropPiece complete it.
    // This exercises placement + detection together without scripting alternating turns.

    @Test
    fun horizontalWin() {
        val pre = preBuilt(standard, Player.RED,
            CellPos(5, 0) to Cell.RED,
            CellPos(5, 1) to Cell.RED,
            CellPos(5, 2) to Cell.RED,
        )
        val after = GameLogic.dropPiece(pre, col = 3)!!
        assertEquals(Player.RED, after.winner)
        assertEquals(4, after.winningCells.size)
        assertTrue(after.winningCells.all { it.row == 5 })
    }

    @Test
    fun verticalWin() {
        val pre = preBuilt(standard, Player.YELLOW,
            CellPos(5, 2) to Cell.YELLOW,
            CellPos(4, 2) to Cell.YELLOW,
            CellPos(3, 2) to Cell.YELLOW,
        )
        val after = GameLogic.dropPiece(pre, col = 2)!!
        assertEquals(Player.YELLOW, after.winner)
        assertTrue(after.winningCells.all { it.col == 2 })
    }

    @Test
    fun diagonalWinAscending() {
        // RED at (5,0) (4,1) (3,2) (2,3). Columns under the diagonal need fillers
        // so the final piece actually lands at (2,3).
        val pre = preBuilt(standard, Player.RED,
            CellPos(5, 0) to Cell.RED,
            CellPos(5, 1) to Cell.YELLOW, CellPos(4, 1) to Cell.RED,
            CellPos(5, 2) to Cell.YELLOW, CellPos(4, 2) to Cell.YELLOW, CellPos(3, 2) to Cell.RED,
            CellPos(5, 3) to Cell.YELLOW, CellPos(4, 3) to Cell.YELLOW, CellPos(3, 3) to Cell.YELLOW,
        )
        val after = GameLogic.dropPiece(pre, col = 3)!!
        assertEquals(Player.RED, after.winner)
        assertEquals(4, after.winningCells.size)
        assertTrue(CellPos(2, 3) in after.winningCells)
    }

    @Test
    fun diagonalWinDescending() {
        // YELLOW at (2,0) (3,1) (4,2) (5,3). Final piece drops into col 0 to land at (2,0).
        val pre = preBuilt(standard, Player.YELLOW,
            CellPos(5, 0) to Cell.RED, CellPos(4, 0) to Cell.RED, CellPos(3, 0) to Cell.RED,
            CellPos(5, 1) to Cell.RED, CellPos(4, 1) to Cell.RED, CellPos(3, 1) to Cell.YELLOW,
            CellPos(5, 2) to Cell.RED, CellPos(4, 2) to Cell.YELLOW,
            CellPos(5, 3) to Cell.YELLOW,
        )
        val after = GameLogic.dropPiece(pre, col = 0)!!
        assertEquals(Player.YELLOW, after.winner)
        assertEquals(4, after.winningCells.size)
        assertTrue(CellPos(2, 0) in after.winningCells)
        assertTrue(CellPos(5, 3) in after.winningCells)
    }

    @Test
    fun winLengthFiveIgnoresFourInARow() {
        val cfg = GameConfig(rows = 6, cols = 7, winLength = 5)
        val pre = preBuilt(cfg, Player.RED,
            CellPos(5, 0) to Cell.RED,
            CellPos(5, 1) to Cell.RED,
            CellPos(5, 2) to Cell.RED,
        )
        val after = GameLogic.dropPiece(pre, col = 3)!!
        assertNull(after.winner)
    }

    @Test
    fun postWinDropsAreRejected() {
        val pre = preBuilt(standard, Player.RED,
            CellPos(5, 0) to Cell.RED,
            CellPos(5, 1) to Cell.RED,
            CellPos(5, 2) to Cell.RED,
        )
        val won = GameLogic.dropPiece(pre, col = 3)!!
        assertEquals(Player.RED, won.winner)
        assertNull(GameLogic.dropPiece(won, col = 5))
    }

    // Config validation

    @Test
    fun configRejectsWinLengthLargerThanBoard() {
        assertFailsWith<IllegalArgumentException> { GameConfig(rows = 5, cols = 5, winLength = 6) }
        assertFailsWith<IllegalArgumentException> { GameConfig(rows = 4, cols = 4, winLength = 5) }
    }

    @Test
    fun configRejectsOutOfRangeDimensions() {
        assertFailsWith<IllegalArgumentException> { GameConfig(rows = 3, cols = 7, winLength = 4) }
        assertFailsWith<IllegalArgumentException> { GameConfig(rows = 6, cols = 16, winLength = 4) }
        assertFailsWith<IllegalArgumentException> { GameConfig(rows = 6, cols = 7, winLength = 3) }
        assertFailsWith<IllegalArgumentException> { GameConfig(rows = 6, cols = 7, winLength = 11) }
    }

    @Test
    fun configAcceptsBoundaryValues() {
        // No throw — winLength equals the larger board dimension is legal.
        GameConfig(rows = 4, cols = 4, winLength = 4)
        GameConfig(rows = 15, cols = 15, winLength = 10)
    }

    // Boundary wins

    @Test
    fun winOnSmallestBoard() {
        val cfg = GameConfig(rows = 4, cols = 4, winLength = 4)
        val pre = preBuilt(cfg, Player.RED,
            CellPos(3, 0) to Cell.RED,
            CellPos(3, 1) to Cell.RED,
            CellPos(3, 2) to Cell.RED,
        )
        val after = GameLogic.dropPiece(pre, col = 3)!!
        assertEquals(Player.RED, after.winner)
    }

    @Test
    fun verticalWinTouchingTopRow() {
        // Stack reaches all the way to row 0 — make sure the upward walk doesn't read past the
        // edge of the board. Yellows fill the bottom so the dropped piece lands at row 0.
        val pre = preBuilt(standard, Player.RED,
            CellPos(5, 6) to Cell.YELLOW,
            CellPos(4, 6) to Cell.YELLOW,
            CellPos(3, 6) to Cell.RED,
            CellPos(2, 6) to Cell.RED,
            CellPos(1, 6) to Cell.RED,
        )
        val after = GameLogic.dropPiece(pre, col = 6)!!
        assertEquals(Player.RED, after.winner)
        assertTrue(CellPos(0, 6) in after.winningCells)
    }

    @Test
    fun fullBoardWithoutWinIsDraw() {
        // 4×5, winLength 5: vertical (4 < 5) and diagonal (max diag = 4) wins are impossible
        // by geometry. The row pattern below alternates RYRYR / YRYRY so no horizontal stretch
        // of 5 ever forms either. Final drop in col 4 fills the last cell.
        val cfg = GameConfig(rows = 4, cols = 5, winLength = 5)
        val board = listOf(
            listOf(Cell.RED, Cell.YELLOW, Cell.RED, Cell.YELLOW, Cell.RED),
            listOf(Cell.YELLOW, Cell.RED, Cell.YELLOW, Cell.RED, Cell.YELLOW),
            listOf(Cell.RED, Cell.YELLOW, Cell.RED, Cell.YELLOW, Cell.RED),
            listOf(Cell.YELLOW, Cell.RED, Cell.YELLOW, Cell.RED, Cell.EMPTY),
        )
        val pre = GameState(cfg, board, Player.YELLOW, moveCount = 19)
        val after = GameLogic.dropPiece(pre, col = 4)!!
        assertNull(after.winner)
        assertTrue(after.isDraw)
        assertTrue(after.isFinished)
    }

    private fun drops(config: GameConfig, vararg cols: Int): GameState =
        cols.fold(GameLogic.newGame(config)) { acc, col -> GameLogic.dropPiece(acc, col)!! }

    // Builds a state with [pieces] pre-placed. Skips the alternating-turn invariant on purpose
    // so individual win-directions can be set up cheaply.
    private fun preBuilt(
        config: GameConfig,
        currentPlayer: Player,
        vararg pieces: Pair<CellPos, Cell>,
    ): GameState {
        val board = MutableList(config.rows) { MutableList(config.cols) { Cell.EMPTY } }
        for ((pos, cell) in pieces) board[pos.row][pos.col] = cell
        return GameState(
            config = config,
            board = board.map { it.toList() },
            currentPlayer = currentPlayer,
            moveCount = pieces.size,
        )
    }
}
