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
private fun MutableSet<GameEdge>.removeValue(move: Moves, node: GameNode): MutableSet<GameEdge> {
    this.removeAll { it.node == node && it.move == move }
    return this
}

private val nodes = HashMap<Int, GameNode>()

data class GameEdge(val node: GameNode, val move: Moves)

class GameNode private constructor(val board: Board) {
    val boardId = board.serialize()

    var children: ImmutableSet<GameEdge> = ImmutableSet.of()
        private set

    var parents: ImmutableSet<GameEdge> = ImmutableSet.of()
        private set

    fun move(move: Moves): GameNode? {
        if(!isFinished) error("graph has not yet finished building")

        return children.firstOrNull { it.move == move }?.node
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

    private fun removeChild(node: GameNode, move: Moves) {
        if(isFinished) error("graph has already finished building")
        children = children.mutable().removeValue(move, node).immutable()
        node.parents = node.parents.mutable().removeValue(move, this).immutable()
    }

    private fun removeParent(node: GameNode, move: Moves) {
        if(isFinished) error("graph has already finished building")
        parents = parents.mutable().removeValue(move, node).immutable()
        node.children = node.children.mutable().removeValue(move, this).immutable()
    }

    companion object {
        private var isFinished = false

        val nodes
            get() = tiktaktuk.nodes

        fun Board.Companion.node() = of(Board())
        fun Board.node() = of(this)

        fun of(board: Board): GameNode {
            val id = board.serialize()
            if(nodes.contains(id)) return nodes[id]!!

            GameNode(board).let {
                nodes[id] = it
                return it
            }
        }

        fun of(id: Int): GameNode {
            val board = Board.deserialize(id)
            if(nodes.contains(id)) return nodes[id]!!

            GameNode(board).let {
                nodes[id] = it
                return it
            }
        }

        fun finalize() {
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

            nodes.toMutableMap().forEach {
                if (it.value !in visitedNodes) {
                    nodes.remove(it.key)
                    it.value.children.forEach { child -> child.node.removeParent(it.value, child.move) }
                    it.value.parents.forEach { parent -> parent.node.removeChild(it.value, parent.move) }
                }
            }

            isFinished = true
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
