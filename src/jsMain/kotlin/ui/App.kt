package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import game.GameConfig
import game.GameLogic
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun App() {
    var state by remember { mutableStateOf(GameLogic.newGame(GameConfig())) }

    Div(attrs = { classes("app") }) {
        H1 { Text("Connect Four") }
        GameBoard(state) { col -> GameLogic.dropPiece(state, col)?.let { state = it } }
    }
}

internal fun AttrsScope<HTMLDivElement>.classes(vararg names: String) {
    classes(names.toList())
}
