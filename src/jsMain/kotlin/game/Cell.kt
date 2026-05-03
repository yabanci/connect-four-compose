package game

import kotlinx.serialization.Serializable

@Serializable
enum class Cell { EMPTY, RED, YELLOW }

@Serializable
enum class Player {
    RED, YELLOW;

    val cell: Cell get() = if (this == RED) Cell.RED else Cell.YELLOW
    fun next(): Player = if (this == RED) YELLOW else RED
}

@Serializable
data class CellPos(val row: Int, val col: Int)
