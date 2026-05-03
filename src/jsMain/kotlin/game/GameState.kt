package game

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val rows: Int = 6,
    val cols: Int = 7,
    val winLength: Int = 4,
) {
    init {
        require(rows in MIN_SIZE..MAX_SIZE) { "rows out of range: $rows" }
        require(cols in MIN_SIZE..MAX_SIZE) { "cols out of range: $cols" }
        require(winLength in MIN_WIN..MAX_WIN) { "winLength out of range: $winLength" }
        require(winLength <= maxOf(rows, cols)) { "winLength $winLength can't fit in ${rows}x$cols" }
    }

    companion object {
        const val MIN_SIZE = 4
        const val MAX_SIZE = 15
        const val MIN_WIN = 4
        const val MAX_WIN = 10
    }
}

@Serializable
data class GameState(
    val config: GameConfig,
    val board: List<List<Cell>>,
    val currentPlayer: Player,
    val winner: Player? = null,
    val winningCells: List<CellPos> = emptyList(),
    val isDraw: Boolean = false,
    val moveCount: Int = 0,
) {
    val isFinished: Boolean get() = winner != null || isDraw
}
