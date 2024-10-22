package tiktaktuk

import com.google.common.collect.ImmutableSet
import tiktaktuk.game.Board
import tiktaktuk.game.Color
import tiktaktuk.game.Moves
import kotlin.math.abs

private fun ImmutableSet<GameEdge>.mutable() = this.toMutableSet()
private fun MutableSet<GameEdge>.immutable() = ImmutableSet.copyOf(this)
private fun MutableSet<GameEdge>.addValue(move: Moves, node: GameNode): MutableSet<GameEdge> {
    this.add(GameEdge(node, move))
    return this
}

private val table = HashMap<Int, GameNode>()

fun getTable() = HashMap(table)

data class GameEdge(val node: GameNode, val move: Moves)

class GameNode private constructor(val board: Board) {
    val boardId = board.serialize()

    var children: ImmutableSet<GameEdge> = ImmutableSet.of()
        private set

    var parents: ImmutableSet<GameEdge> = ImmutableSet.of()
        private set

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

        fun Board.Companion.node() = of(Board())
        fun Board.node() = of(this)

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

            stack.add(Board.node())

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
