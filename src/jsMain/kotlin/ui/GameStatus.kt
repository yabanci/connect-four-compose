package ui

import androidx.compose.runtime.Composable
import game.GameState
import game.Player
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun GameStatus(state: GameState) {
    when {
        state.winner != null -> Div(attrs = { classes("status", "won") }) {
            ColorDot(state.winner!!)
            Text(" ${state.winner!!.name} wins!")
        }
        state.isDraw -> Div(attrs = { classes("status", "draw") }) {
            Text("Draw!")
        }
        else -> Div(attrs = { classes("status") }) {
            ColorDot(state.currentPlayer)
            Text(" ${state.currentPlayer.name}'s turn")
        }
    }
}

@Composable
private fun ColorDot(player: Player) {
    Span(attrs = { classes("dot", player.name.lowercase()) })
}
