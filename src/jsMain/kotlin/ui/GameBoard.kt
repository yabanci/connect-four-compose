package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import game.Cell
import game.GameState
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.dom.Div

@Composable
fun GameBoard(state: GameState, onColumnClick: (Int) -> Unit) {
    val cfg = state.config
    Div(attrs = {
        classes("board")
        style { gridTemplateColumns("repeat(${cfg.cols}, 1fr)") }
    }) {
        for (col in 0 until cfg.cols) {
            ColumnView(state, col, onClick = { onColumnClick(col) })
        }
    }
}

@Composable
private fun ColumnView(state: GameState, col: Int, onClick: () -> Unit) {
    Div(attrs = {
        classes("column")
        if (state.isFinished) classes("disabled") else onClick { onClick() }
    }) {
        for (row in 0 until state.config.rows) {
            CellView(state, row, col)
        }
    }
}

@Composable
private fun CellView(state: GameState, row: Int, col: Int) {
    val cell = state.board[row][col]
    val isWinning = state.winningCells.any { it.row == row && it.col == col }
    Div(attrs = { classes("cell") }) {
        if (cell != Cell.EMPTY) {
            // Stable key per cell+colour so Compose doesn't reuse a DOM node between an empty
            // and a filled state — that would skip the CSS drop animation on placement.
            key(row to col to cell) {
                Div(attrs = {
                    classes("piece", cell.name.lowercase())
                    if (isWinning) classes("winning")
                })
            }
        }
    }
}
