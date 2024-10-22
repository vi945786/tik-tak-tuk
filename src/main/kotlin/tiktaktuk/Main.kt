package tiktaktuk

import tiktaktuk.GameNode.Companion.node
import tiktaktuk.ai.Ai
import tiktaktuk.ai.ColoredNode
import tiktaktuk.game.Board
import tiktaktuk.game.Color
import tiktaktuk.game.Moves
import java.io.File
import java.net.URISyntaxException
import kotlin.system.exitProcess

fun generateGraph() {
    for (i in generateValidBoardIds()) {
        val b = Board.deserialize(i)
        val bNode = b.node()

        if(b.win != Color.EMPTY) {
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

fun testAi(coloredNode: ColoredNode) {
    val nodes = mutableListOf(Board.node())
    val visitedNodes = mutableSetOf<GameNode>()

    var w = 0
    var l = 0
    var t = 0
    while (nodes.isNotEmpty()) {
        val currentNode = nodes.removeLast()

        if(currentNode.board.win != Color.EMPTY) {
            when (currentNode.board.win) {
                coloredNode.winningColor -> w++
                coloredNode.winningColor.opposite() -> l++
                Color.BOTH -> t++
                else -> error { "${currentNode.boardId}: ${currentNode.board.win}" }
            }
            continue
        }

        when (currentNode.board.turn) {
            coloredNode.winningColor -> Ai.move(currentNode).node.let { visitedNodes.add(it) ; nodes.add(it) }
            else -> currentNode.children.map { it.node }.filter { it !in visitedNodes }.let { visitedNodes.addAll(it) ; nodes.addAll(it) }
        }
    }

    val sb = StringBuilder()

    sb.appendLine(coloredNode.winningColor)
    sb.appendLine("w: $w")
    sb.appendLine("l: $l")
    sb.appendLine("t: $t")

    println(sb.toString())
}

const val errorRedColor = "\u001b[91m" //for errors
const val redColor = "\u001b[31m"      //for red player
const val greenColor = "\u001b[32m"    //for ties
const val yellowColor = "\u001b[33m"   //for yellow player
const val blueColor = "\u001b[34m"     //for board
const val purpleColor = "\u001B[0;35m" // for moves
const val cyanColor = "\u001b[96m"     //for ai messages
const val grayColor = "\u001b[90m"     //for board shifts
const val reset = "\u001b[0m"

fun getCurrentJarLocation(): String? {
    return try {
        val jarPath = object {}.javaClass.protectionDomain.codeSource.location.toURI().path
        File(jarPath).absolutePath
    } catch (e: URISyntaxException) {
        e.printStackTrace()
        null
    }
}

fun main(args: Array<String>) {

    UI.before(args)

    generateGraph()
    Ai.train()

//    listOf(
//        Thread { testAi(ColoredNode(Color.YELLOW)) },
//        Thread { testAi(ColoredNode(Color.RED)) }
//    ).let {
//        it.forEach { it.start() }
//        it.forEach { it.join() }
//    }

    UI.start()
}


