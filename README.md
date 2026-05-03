# Connect Four — Compose HTML

Browser-based [Connect Four](https://en.wikipedia.org/wiki/Connect_Four) built with Compose HTML (Compose Multiplatform's web target). Configurable board, two-player local play, falling-piece animation, save/restore via `localStorage`, unit-tested game logic.

## Run it

```bash
./gradlew jsBrowserDevelopmentRun        # http://localhost:8080
./gradlew jsBrowserDistribution          # production bundle in build/dist/js/productionExecutable/
./gradlew jsTest                         # 12 unit tests on the game logic
```

Java 21+ is the only thing you need installed — the Gradle wrapper pulls Kotlin, Compose, Node, webpack, and Karma.

For tests, Karma drives a headless Chrome. On macOS, if Karma can't find Chrome on `PATH`:

```bash
CHROME_BIN="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" ./gradlew jsTest
```

## What's in here

```
src/jsMain/
├── kotlin/
│   ├── Main.kt                  renderComposable("root") { App() }
│   ├── game/                    pure logic, no UI imports
│   │   ├── Cell.kt              Cell, Player, CellPos
│   │   ├── GameState.kt         GameConfig + GameState (data classes, @Serializable)
│   │   ├── GameLogic.kt         newGame, dropPiece, win detection
│   │   └── Storage.kt           JSON ↔ localStorage
│   └── ui/                      composables
│       ├── App.kt               root, owns the GameState
│       ├── SettingsPanel.kt     rows / cols / win-length inputs
│       ├── GameStatus.kt        whose turn / winner / draw banner
│       └── GameBoard.kt         grid + columns + cells + pieces
└── resources/
    └── index.html               DOM root + embedded CSS

src/jsTest/kotlin/
└── GameLogicTest.kt             17 tests
```

The `game` package has zero Compose imports, which is what makes the unit tests testable in isolation.

## Configuration

| | range | default |
|---|---|---|
| rows | 4..15 | 6 |
| cols | 4..15 | 7 |
| connect (winLength) | 4..10 | 4 |

`winLength` is auto-clamped to `max(rows, cols)` when the user resizes the board down — otherwise `GameConfig`'s `init { }` block would throw, and the game would be unwinnable anyway.

## Notes on the design

**Win detection.** After a piece lands at `(row, col)`, only four lines through that cell can have changed: horizontal, vertical, and the two diagonals. `findWinningCells` walks each direction outward from the placed piece, collecting same-coloured cells, and returns the run if it reaches `winLength`. `O(winLength)` per move, no full-board scan.

**State.** A single `GameState` lives in `App` via `mutableStateOf`. `dropPiece` is a pure function returning a new state; the UI just re-renders. `SideEffect { Storage.save(state) }` writes to `localStorage` after each successful frame — `LaunchedEffect` would have worked too but pulls in `kotlinx-coroutines` for nothing.

**Falling animation.** CSS-only: `@keyframes drop` translates `-700% → 0%` with a small overshoot for bounce. Each piece div is wrapped in `key(row to col to cell)` so Compose creates a fresh DOM node when a cell goes from empty to filled, which makes the browser fire the animation exactly once.

**Persistence.** `kotlinx.serialization` → JSON → `localStorage`. Errors (private mode, quota exceeded) are logged and swallowed — the game stays playable.

**CSS lives in `index.html`.** Avoids configuring a webpack CSS loader. If styles ever became dynamic I'd move them to Compose's `Style` DSL.

## Bonus features

All three from the task description are in:

- Falling animation (`@keyframes drop` in `index.html`)
- Save/restore across refresh (`Storage` + `SideEffect`)
- Unit tests (`GameLogicTest`, 17 cases covering all four win directions, draw, full column, post-win lock, longer win lengths, config validation, and edge-of-board wins)
