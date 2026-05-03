package ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun App() {
    Div(attrs = { classes("app") }) {
        H1 { Text("Connect Four") }
    }
}

internal fun AttrsScope<HTMLDivElement>.classes(vararg names: String) {
    classes(names.toList())
}
