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

            mutableListOf(
                Thread { calculateOdds(YellowNode()) },
                Thread { calculateOdds(RedNode()) }
            ).let {
                it.forEach { it.start() }
                it.forEach { it.join() }
            }
        }

        interface ColoredNode {
            var GameNode.winOdds: Double

            fun isWinningColor(color: Color): Boolean
        }

        class YellowNode : ColoredNode {
            override var GameNode.winOdds: Double
                set(d) = run { this.yellowWinOdds = d }
                get() = this.yellowWinOdds

            override fun isWinningColor(color: Color) = color == Color.YELLOW
        }

        class RedNode : ColoredNode {
            override var GameNode.winOdds: Double
                set(d) = run { this.redWinOdds = d }
                get() = this.redWinOdds

            override fun isWinningColor(color: Color) = color == Color.RED
        }

        //TODO find values for easy, medium, hard, and impossible mode
        private const val DROP_OFF_VALUE = 0.95
        private const val WIN_VALUE = 10.0
        private const val THRESHOLD = 0.005

        private const val LOSE_WEIGHT_MULTIPLIER = 2.0

        private const val BEST_MOVE_WEIGHT = 0.4
        private const val AVERAGE_MOVE_WEIGHT = 0.6

        private fun calculateOdds(coloredNode: ColoredNode) {
            var currentTotalDifference: Double

            do {
                currentTotalDifference = 0.0

                for(node in table.values) {
                    currentTotalDifference += updateNodeOdds(node, coloredNode)
                }

            } while (currentTotalDifference > THRESHOLD)
        }

        private fun updateNodeOdds(node: GameNode, coloredNode: ColoredNode) = with(coloredNode) {
            val old = node.winOdds
            node.winOdds = when(node.board.win) {
                Color.YELLOW, Color.RED -> if(isWinningColor(node.board.win)) WIN_VALUE else -WIN_VALUE * LOSE_WEIGHT_MULTIPLIER
                Color.BOTH -> 0.0
                else -> {
                    val bestMoveOdds = node.children.maxOf { it.node.winOdds * DROP_OFF_VALUE }
                    val avgMoveOdds = node.children.map { it.node.winOdds * DROP_OFF_VALUE }.average()

                    (BEST_MOVE_WEIGHT * bestMoveOdds) + (AVERAGE_MOVE_WEIGHT * avgMoveOdds)
                }
            }

            return@with abs(old - node.winOdds)
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
