package tiktaktuk.ai

import tiktaktuk.GameEdge
import tiktaktuk.GameNode
import tiktaktuk.GameNode.Companion.nodes
import tiktaktuk.game.Color
import tiktaktuk.game.Color.YELLOW
import tiktaktuk.generateGraph
import tiktaktuk.testAi
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object Ai {
    private val yellow: ColoredNode = ColoredNode(Color.YELLOW)
    private val red: ColoredNode = ColoredNode(Color.RED)

    private var isTraining = Training.Before
    enum class Training { Before, During, After }
    fun train(): Ai {
        if(isTraining != Training.Before) return this
            isTraining = Training.During
        listOf(
            Thread { calculateOdds(yellow) },
            Thread { calculateOdds(red) }
        ).let {
            it.forEach { it.start() }
            it.forEach { it.join() }
        }
        isTraining = Training.After

        return this
    }

    @JvmStatic
    fun main(args: Array<String>) {
        var t0 = System.nanoTime()

        generateGraph()
        Ai.train()
        testAi(YELLOW)
        testAi(Color.RED)

        print("${TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0)} milliseconds")
    }

    fun move(node: GameNode): GameEdge {
        return move(node, node.board.turn)
    }

    fun move(node: GameNode, color: Color): GameEdge {
        if (isTraining != Training.After) error { "move is not available before training" }

        return when (color) {
            Color.YELLOW -> with(yellow) { node.bestMove() }
            Color.RED -> with(red) { node.bestMove() }
            else -> error {}
        }
    }

    private fun calculateOdds(coloredNode: ColoredNode) {
        var currentTotalDifference: Double

        var i = 0
        do {
            currentTotalDifference = 0.0

            for (node in nodes.values) currentTotalDifference += updateNodeOdds(node, coloredNode)

        } while (currentTotalDifference > coloredNode.THRESHOLD)
    }

    private fun updateNodeOdds(node: GameNode, coloredNode: ColoredNode) = with(coloredNode) {
        val old = node.winOdds
        node.winOdds = when (node.board.win) {
            Color.YELLOW, Color.RED -> if (isMyColor(node.board.win)) WIN_VALUE else LOSE_VALUE
            Color.BOTH -> TIE_VALUE
            else -> run {
                if(isTerminalByValue(node.children.first().node) && node.children.all { it.node.winOdds == node.children.first().node.winOdds }) return@run node.children.first().node.winOdds
                if(isMyColor(node.board.turn) && isTerminalByWin(node.children.maxBy { dropOff(it.node) }.node)) return@run node.children.maxOf { dropOff(it.node) }
                if(!isMyColor(node.board.turn) && isTerminalByWin(node.children.minBy { dropOff(it.node) }.node)) return@run node.children.minOf { dropOff(it.node) }

                val bestMoveOdds = node.children.maxOf { dropOff(it.node) }
                val avgMoveOdds = node.children.map { dropOff(it.node) }.average()
                val worstMoveOdds = node.children.minOf { dropOff(it.node) }

                return@run when(isMyColor(node.board.turn)) {
                    true -> ((1-WORST_MOVE_WEIGHT) * avgMoveOdds) + (WORST_MOVE_WEIGHT * worstMoveOdds)
                    false -> ((1-BEST_MOVE_WEIGHT) * avgMoveOdds) + (BEST_MOVE_WEIGHT * bestMoveOdds)
                }
            }
        }


        return@with abs(old - node.winOdds)
    }

    private fun isTerminalByWin(node: GameNode) = node.board.turn != Color.EMPTY
    private fun ColoredNode.isTerminalByValue(node: GameNode) = node.winOdds in listOf(WIN_VALUE, LOSE_VALUE, TIE_VALUE)

    private fun ColoredNode.dropOff(node: GameNode) = when(isTerminalByWin(node)) {
        false -> node.winOdds * DROP_OFF_VALUE
        true -> node.winOdds * TERMINAL_DROP_OFF_VALUE
    }
}