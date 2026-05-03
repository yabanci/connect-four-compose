package game

object GameLogic {

    fun newGame(config: GameConfig): GameState = GameState(
        config = config,
        board = List(config.rows) { List(config.cols) { Cell.EMPTY } },
        currentPlayer = Player.RED,
    )

    /** Drops a piece into [col] for the current player, or returns null if the move is illegal. */
    fun dropPiece(state: GameState, col: Int): GameState? {
        if (state.isFinished) return null
        if (col !in 0 until state.config.cols) return null

        val landingRow = (state.config.rows - 1 downTo 0)
            .firstOrNull { state.board[it][col] == Cell.EMPTY }
            ?: return null

        val newBoard = state.board.mapIndexed { r, row ->
            if (r == landingRow) row.toMutableList().also { it[col] = state.currentPlayer.cell }
            else row
        }
        val winning = findWinningCells(newBoard, landingRow, col, state.config.winLength)
        val moves = state.moveCount + 1
        val full = moves == state.config.rows * state.config.cols
        val won = winning.isNotEmpty()

        return state.copy(
            board = newBoard,
            currentPlayer = if (won) state.currentPlayer else state.currentPlayer.next(),
            winner = if (won) state.currentPlayer else null,
            winningCells = winning,
            isDraw = !won && full,
            moveCount = moves,
        )
    }

    // Only the four lines through (row, col) can have changed. For each direction, walk both
    // ways from the placed piece collecting same-colour cells, and return the line if it's long
    // enough. O(winLength) per move.
    private fun findWinningCells(
        board: List<List<Cell>>,
        row: Int,
        col: Int,
        winLength: Int,
    ): List<CellPos> {
        val cell = board[row][col]
        if (cell == Cell.EMPTY) return emptyList()
        val rows = board.size
        val cols = board[0].size

        for ((dr, dc) in DIRECTIONS) {
            val line = mutableListOf(CellPos(row, col))
            var r = row + dr; var c = col + dc
            while (r in 0 until rows && c in 0 until cols && board[r][c] == cell) {
                line += CellPos(r, c); r += dr; c += dc
            }
            r = row - dr; c = col - dc
            while (r in 0 until rows && c in 0 until cols && board[r][c] == cell) {
                line.add(0, CellPos(r, c)); r -= dr; c -= dc
            }
            if (line.size >= winLength) return line
        }
        return emptyList()
    }

    private val DIRECTIONS = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
}
