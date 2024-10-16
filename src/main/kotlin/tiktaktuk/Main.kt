package tiktaktuk

import tiktaktuk.game.Board
import tiktaktuk.game.Color.Companion.EMPTY
import tiktaktuk.game.Moves
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
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
    for (i in 0..1062881) { //(3.0.pow(12.0).toInt() - 1).shl(1) + 1 (look at Board.serialize)
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

fun main() { //TODO optimize (Board.move)
    val t1 = System.nanoTime()
    generateGraph()
    val t2 = System.nanoTime()
    println("graph: ${TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS)}")
    var allNodes = findAllChildNodes()
    println("clean up: ${TimeUnit.MILLISECONDS.convert(System.nanoTime() - t2, TimeUnit.NANOSECONDS)}")
}

fun findAllChildNodes(node: GameNode = GameNode.of(Board())): MutableSet<GameNode> {
    val visitedNodes = mutableSetOf<GameNode>()
    val stack = mutableListOf<GameNode>()

    stack.add(node)

    while (stack.isNotEmpty()) {
        val currentNode = stack.removeAt(stack.size - 1)

        if (currentNode in visitedNodes) continue
        visitedNodes.add(currentNode)

        for (child in currentNode.children) {
            if (child.node in visitedNodes) continue
            stack.add(child.node)
        }

    }

    return visitedNodes
}