package tiktaktuk

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import tiktaktuk.game.Board
import tiktaktuk.game.Moves

private fun ImmutableSet<GameEdge>.mutable() = this.toMutableSet()
private fun MutableSet<GameEdge>.immutable() = ImmutableSet.copyOf(this)
private fun MutableSet<GameEdge>.addValue(move: Moves, node: GameNode): MutableSet<GameEdge> {
    this.add(GameEdge(node, move))
    return this
}

private val table = HashMap<Int, GameNode>()

data class GameEdge(val node: GameNode, val move: Moves)

class GameNode private constructor(val board: Board) {
    private var children: ImmutableSet<GameEdge> = ImmutableSet.of()
    private var parents: ImmutableSet<GameEdge> = ImmutableSet.of()

    fun addChild(node: GameNode, move: Moves) {
        children = children.mutable().addValue(move, node).immutable()
        node.parents = node.parents.mutable().addValue(move, this).immutable()
    }

    fun addParent(node: GameNode, move: Moves) {
        parents = parents.mutable().addValue(move, node).immutable()
        node.children = node.children.mutable().addValue(move, this).immutable()
    }

    companion object {
        fun of(board: Board): GameNode {
            val id = board.serialize()
            if(table.contains(id)) return table[id]!!

            GameNode(board).let {
                table[id] = it
                return it
            }
        }
    }

    override fun hashCode(): Int {
        return board.serialize()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameNode

        return board == other.board
    }
}
