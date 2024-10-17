package tiktaktuk

import com.google.common.collect.ImmutableSet
import com.google.common.primitives.Floats.max
import tiktaktuk.game.Board
import tiktaktuk.game.Color
import tiktaktuk.game.Moves
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.min

private fun ImmutableSet<GameEdge>.mutable() = this.toMutableSet()
private fun MutableSet<GameEdge>.immutable() = ImmutableSet.copyOf(this)
private fun MutableSet<GameEdge>.addValue(move: Moves, node: GameNode): MutableSet<GameEdge> {
    this.add(GameEdge(node, move))
    return this
}

private val table = HashMap<Int, GameNode>()

data class GameEdge(val node: GameNode, val move: Moves)

class GameNode private constructor(val board: Board) {
    val boardId = board.serialize()

    var children: ImmutableSet<GameEdge> = ImmutableSet.of()
        private set

    var parents: ImmutableSet<GameEdge> = ImmutableSet.of()
        private set

    var yellowWinOdds = 0.0
        private set
        get() {
            if(!isFinished) error("winOdds can only be read after the graph has finished building")
            return field
        }

    var redWinOdds = 0.0
        private set
        get() {
            if(!isFinished) error("winOdds can only be read after the graph has finished building")
            return field
        }


    fun addChild(node: GameNode, move: Moves) {
        if(isFinished) error("graph has already finished building")
        children = children.mutable().addValue(move, node).immutable()
        node.parents = node.parents.mutable().addValue(move, this).immutable()
    }

    fun addParent(node: GameNode, move: Moves) {
        if(isFinished) error("graph has already finished building")
        parents = parents.mutable().addValue(move, node).immutable()
        node.children = node.children.mutable().addValue(move, this).immutable()
    }

    companion object {
        private var isFinished = false

        fun of(board: Board): GameNode {
            val id = board.serialize()
            if(table.contains(id)) return table[id]!!

            GameNode(board).let {
                table[id] = it
                return it
            }
        }

        fun finalize() {
            isFinished = true

            val visitedNodes = mutableSetOf<GameNode>()
            val stack = mutableListOf<GameNode>()

            stack.add(GameNode.of(Board()))

            while (stack.isNotEmpty()) {
                val currentNode = stack.removeAt(stack.size - 1)

                if (currentNode in visitedNodes) continue
                visitedNodes.add(currentNode)

                for (child in currentNode.children) {
                    if (child.node in visitedNodes) continue
                    stack.add(child.node)
                }
            }

            val keysToRemove = mutableSetOf<Int>()

            table.entries.forEach {
                if (it.value !in visitedNodes) {
                    keysToRemove.add(it.key)
                }
            }

            keysToRemove.forEach {
                table.remove(it)
            }

            val tY = Thread {calculateOdds({ this.yellowWinOdds }, { this.yellowWinOdds = it }, { color, d -> if(color == Color.RED) -d else d })}.let { it.start() ; it }
            val tR = Thread {calculateOdds({ this.redWinOdds }, { this.redWinOdds = it }, { color, d -> if(color == Color.YELLOW) -d else d })}.let { it.start() ; it }

            tY.join()
            tR.join()
        }

        private const val DROP_OFF_VALUE = 1
        private const val TIE_VALUE = -7.5
        private const val WIN_VALUE = 10.0
        private const val THRESHOLD = 0.0

        private fun calculateOdds(getWinOdds: GameNode.() -> Double, setWinOdds: GameNode.(Double) -> Unit, flipLoss: (Color, Double) -> Double) {
            var lastTotalDifference = 0.0
            var currentTotalDifference = 0.0

            do {
                lastTotalDifference = currentTotalDifference
                currentTotalDifference = 0.0

                for(node in table.values) {
                    currentTotalDifference += updateNodeOdds(node, getWinOdds, setWinOdds, flipLoss)
                }

            } while (abs(lastTotalDifference - currentTotalDifference) > THRESHOLD)
        }

        private fun updateNodeOdds(node: GameNode, getWinOdds: GameNode.() -> Double, setWinOdds: GameNode.(Double) -> Unit, flipLoss: (Color, Double) -> Double): Double {
            val oldOdds = node.getWinOdds()
            node.setWinOdds(when(node.board.win) {
                Color.YELLOW -> flipLoss(Color.YELLOW, WIN_VALUE)
                Color.RED -> flipLoss(Color.RED, WIN_VALUE)
                Color.BOTH -> TIE_VALUE
                else -> node.children.map { it.node.getWinOdds() * DROP_OFF_VALUE }.average()
            })

            return abs(oldOdds - node.getWinOdds())
        }
    }

    override fun hashCode(): Int {
        return boardId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameNode

        return board == other.board
    }
}
