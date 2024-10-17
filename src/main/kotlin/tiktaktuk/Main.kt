package tiktaktuk

import tiktaktuk.game.Board
import tiktaktuk.game.Color.Companion.EMPTY
import tiktaktuk.game.Moves
import java.util.concurrent.TimeUnit

fun generateGraph() {
    for (i in generateValidBoardIds()) {
        val b = Board.deserialize(i)
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

    GameNode.finalize()
}

fun generateValidBoardIds(): MutableSet<Int> {
    val ids = mutableSetOf<Int>()
    val possibleColumns = arrayOf("000", "001", "002", "011", "012", "021", "022", "111", "112", "121", "122", "211", "212", "221", "222") //all the valid columns in a trinary string

    for (i in 0..26) {
        val s = i.toString(radix = 3).padStart(3, '0')
        for (c1 in possibleColumns) {
            for (c2 in possibleColumns) {
                for (c3 in possibleColumns) {

                    val board = "$c1$c2$c3$s".toInt(3) shl 1
                    for (t in 0..1) {
                        ids.add(board or t)
                    }

                }
            }
        }
    }

    return ids
}

fun main() {
    generateGraph()
}