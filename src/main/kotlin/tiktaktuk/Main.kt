package tiktaktuk

import tiktaktuk.game.Board
import tiktaktuk.game.Color.Companion.EMPTY
import tiktaktuk.game.Moves
import kotlin.math.pow

fun isNotFloating(b: Board): Boolean {
    for (column in 0..<3) {
        for (row in 0..<2) {
            if (b.board[row][column] != EMPTY && b.board[row +1][column] == EMPTY) {
                return false
            }
        }
    }
    return true
}

fun generateGraph() {
    for (i in 0..(3.0.pow(12.0).toInt() -1).shl(1) +1) {
        val b = Board.deserialize(i)
        if(!isNotFloating(b)) continue
        val bNode = GameNode.of(b)

        if(b.win != EMPTY) {
            continue
        }

        for(move in Moves.entries) {
            val board = b.move(move)
            if(board != null) {
                bNode.addChild(GameNode.of(board), move)
            }
        }
    }
}

fun main() {
    generateGraph()
}
