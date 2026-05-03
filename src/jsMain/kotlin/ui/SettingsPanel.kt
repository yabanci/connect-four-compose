package ui

import androidx.compose.runtime.Composable
import game.GameConfig
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

@Composable
fun SettingsPanel(config: GameConfig, onConfigChange: (GameConfig) -> Unit) {
    Div(attrs = { classes("settings") }) {
        SettingItem(
            label = "Rows",
            value = config.rows,
            range = GameConfig.MIN_SIZE..GameConfig.MAX_SIZE,
            onChange = { onConfigChange(safeUpdate(config.copy(rows = it))) },
        )
        SettingItem(
            label = "Cols",
            value = config.cols,
            range = GameConfig.MIN_SIZE..GameConfig.MAX_SIZE,
            onChange = { onConfigChange(safeUpdate(config.copy(cols = it))) },
        )
        SettingItem(
            label = "Connect",
            value = config.winLength,
            range = GameConfig.MIN_WIN..GameConfig.MAX_WIN,
            onChange = { onConfigChange(safeUpdate(config.copy(winLength = it))) },
        )
    }
}

@Composable
private fun SettingItem(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    Div(attrs = { classes("setting") }) {
        Label { Text(label) }
        Input(type = InputType.Number) {
            value(value.toString())
            min(range.first.toString())
            max(range.last.toString())
            onInput { e ->
                val parsed = e.value?.toInt() ?: return@onInput
                if (parsed in range) onChange(parsed)
            }
        }
    }
}

// If the user shrinks the board below the current winLength, coerce winLength down so
// GameConfig's init { } doesn't throw (the game would also be unwinnable).
private fun safeUpdate(c: GameConfig): GameConfig {
    val win = c.winLength.coerceIn(GameConfig.MIN_WIN, maxOf(c.rows, c.cols))
    return c.copy(winLength = win)
}
