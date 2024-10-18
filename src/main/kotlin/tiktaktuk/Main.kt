package tiktaktuk

import tiktaktuk.game.Board
import tiktaktuk.game.Color
import tiktaktuk.game.Color.Companion.BOTH
import tiktaktuk.game.Color.Companion.EMPTY
import tiktaktuk.game.Color.Companion.RED
import tiktaktuk.game.Color.Companion.YELLOW
import tiktaktuk.game.Moves
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.system.exitProcess

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

const val errorRedColor = "\u001b[91m" //for errors
const val redColor = "\u001b[31m"      //for red player
const val greenColor = "\u001b[32m"    //for ties
const val yellowColor = "\u001b[33m"   //for yellow player
const val blueColor = "\u001b[34m"     //for board
const val cyanColor = "\u001b[96m"     //for ai messages
const val grayColor = "\u001b[90m"     //for board shifts
const val reset = "\u001b[0m"

fun main() {
    println(cyanColor + "Preparing AI: this might take a few seconds" + reset)

    generateGraph()

    System.console().let { console ->
        var board = Board()
        board.print()

        while (board.win == EMPTY) {
            try {
                board = board.move(Moves.valueOf(console.readLine().uppercase())) ?: throw IllegalStateException()
                board = GameNode.of(board).children.maxBy { it.node.redWinOdds }.let { println(cyanColor + "the AI played: " + it.move.name + reset) ; it.node.board }

                board.print()
            } catch (e: java.lang.IllegalArgumentException) {
                 println(errorRedColor + "move doesn't exist" + reset)
            } catch (e: java.lang.NullPointerException) {
                exitProcess(0)
            } catch (e: java.lang.IllegalStateException) {
                println(errorRedColor + "invalid move" + reset)
                continue
            } catch (e: java.util.NoSuchElementException) {
                board.print()
                break
            }
        }

        when(board.win) {
            YELLOW -> println(yellowColor + "you won" + reset)
            RED -> println(redColor + "the AI beat you" + reset)
            BOTH -> println(greenColor + "it's a tie" + reset)
        }
    }
}

fun Board.print() {
    val sb = StringBuilder()

    for (row in 0..<3) {
        val cells = board[row]
        for(cell in cells) {
            sb.append("$blueColor|$reset").append(cell.char())
        }
        sb.append("$blueColor|$reset")

        when (shifts[row]) {
            -1 -> sb.insert(0, "$grayColor|█|█$reset")
            0 -> sb.insert(0, "$grayColor  |█$reset").append("$grayColor█|$reset")
            1 -> sb.insert(0, "    ").append("$grayColor█|█|$reset")
        }

        println(sb.toString())
        sb.clear()
    }
}

fun Color.char() = when(this) {
    Color.YELLOW -> "${yellowColor}o$reset"
    Color.RED -> "${redColor}o$reset"
    Color.EMPTY -> "o"
    else -> error("invalid color")
}
