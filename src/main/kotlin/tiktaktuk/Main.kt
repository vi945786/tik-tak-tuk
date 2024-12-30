package tiktaktuk

import tiktaktuk.GameNode.Companion.node
import tiktaktuk.GameNode.Companion.nodes
import tiktaktuk.ai.Ai
import tiktaktuk.ai.ColoredNode
import tiktaktuk.game.Board
import tiktaktuk.game.Color
import tiktaktuk.game.Color.*
import tiktaktuk.game.Moves
import tiktaktuk.game.Moves.BL
import tiktaktuk.game.Moves.ML
import java.util.*

fun generateGraph() {
    for (i in generateValidBoardIds()) {
        val b = Board.deserialize(i)
        val bNode = b.node()

        if(b.win != EMPTY) {
            continue
        }

        for(move in Moves.entries) {
            val board = b.move(move)
            if(board != null) {
                bNode.addChild(board.node(), move)
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

fun isPerfect(color: Color): Map<GameNode, Boolean> {

    infix fun GameNode.isCycle(child: GameNode): Boolean {
        return child.children.any { it.node == this }
    }

    val isPerfect: MutableMap<GameNode, Boolean> = mutableMapOf()

    var finished = -1

    do {
        var lastFinished = finished
        finished = 0

        for(node in nodes.values) {
            if(isPerfect[node] != null) {
                finished++
                continue
            }

            if(node.board.win != EMPTY) {
                isPerfect[node] = node.board.win == color
            } else if(node.board.turn == color) {
                if(node.children.any { isPerfect[it.node] == true || node isCycle it.node }) {
                    isPerfect[node] = true
                } else if(node.children.all { isPerfect[it.node] == false || node isCycle it.node }) {
                    isPerfect[node] = false
                }
            } else {
                if(node.children.any { isPerfect[it.node] == false || node isCycle it.node }) {
                    isPerfect[node] = false
                } else if(node.children.all { isPerfect[it.node] == true || node isCycle it.node }) {
                    isPerfect[node] = true
                }
            }
        }

        if(finished == lastFinished) {
            var nullNodes = nodes.values.filter { isPerfect[it] == null }

            for(i in 0 ..< nullNodes.size) {
                val node = nullNodes[i]

                if(node.children.filter { isPerfect[it.node] == null }.size != 1) continue

                isPerfect[node] = isPerfect[node.children.first { isPerfect[it.node] != null }.node]!!
            }
        }
    } while(finished != nodes.size)

    return isPerfect
}

const val errorRedColor = "\u001b[91m" //for errors
const val redColor = "\u001b[31m"      //for red player
const val greenColor = "\u001b[32m"    //for ties
const val yellowColor = "\u001b[33m"   //for yellow player
const val blueColor = "\u001b[34m"     //for board
const val purpleColor = "\u001B[0;35m" //for moves
const val cyanColor = "\u001b[96m"     //for ai messages
const val grayColor = "\u001b[90m"     //for board shifts
const val reset = "\u001b[0m"

fun main(args: Array<String>) {
//    try {

//        UI.before(args)

        generateGraph()
//        var isPerfect = isPerfect(RED)


        Ai.train()


////    listOf(
////        Thread { testAi(ColoredNode(Color.YELLOW)) },
////        Thread { testAi(ColoredNode(Color.RED)) }
////    ).let {
////        it.forEach { it.start() }
////        it.forEach { it.join() }
////    }

//        UI.start(true)

//    } catch (_: RuntimeException) { }
}


