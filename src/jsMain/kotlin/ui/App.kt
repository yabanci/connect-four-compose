package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import game.GameConfig
import game.GameLogic
import game.GameState
import game.Storage
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun App() {
    var state by remember {
        mutableStateOf(Storage.load() ?: GameLogic.newGame(GameConfig()))
    }

    // SideEffect runs after a successful frame — good enough for a localStorage write,
    // and avoids pulling kotlinx-coroutines in just to satisfy LaunchedEffect's receiver.
    SideEffect { Storage.save(state) }

    Div(attrs = { classes("app") }) {
        H1 { Text("Connect Four") }

        SettingsPanel(
            config = state.config,
            onConfigChange = { newConfig -> state = GameLogic.newGame(newConfig) },
        )

        GameStatus(state)

        GameBoard(
            state = state,
            onColumnClick = { col ->
                GameLogic.dropPiece(state, col)?.let { state = it }
            },
        )

        Div(attrs = { classes("actions") }) {
            Button(attrs = {
                classes("primary-btn")
                onClick { state = GameLogic.newGame(state.config) }
            }) { Text("New Game") }
        }
    }
}

internal fun AttrsScope<HTMLDivElement>.classes(vararg names: String) {
    classes(names.toList())
}
