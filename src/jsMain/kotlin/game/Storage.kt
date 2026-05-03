package game

import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// localStorage can throw in private/incognito modes or when the quota is full.
// Persistence is best-effort — the game stays playable either way.
object Storage {
    private const val KEY = "connect-four-state-v1"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun save(state: GameState) {
        runCatching { localStorage.setItem(KEY, json.encodeToString(state)) }
            .onFailure { console.warn("connect-four: save failed:", it.message) }
    }

    fun load(): GameState? = runCatching {
        localStorage.getItem(KEY)?.let { json.decodeFromString<GameState>(it) }
    }.onFailure { console.warn("connect-four: load failed, starting fresh:", it.message) }
        .getOrNull()
}
