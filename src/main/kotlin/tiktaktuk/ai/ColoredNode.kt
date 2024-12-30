package tiktaktuk.ai

import tiktaktuk.GameEdge
import tiktaktuk.GameNode
import tiktaktuk.game.Color

class ColoredNode(
    val winningColor: Color

    ,val THRESHOLD: Double = 0.0
    ,val DROP_OFF_VALUE: Double = 0.5
    ,val TERMINAL_DROP_OFF_VALUE: Double = 0.5

    ,val WIN_VALUE: Double = 10.0
    ,val LOSE_VALUE: Double = -10.0
    ,val TIE_VALUE: Double = -0.5

    ,val BEST_MOVE_WEIGHT: Double = 0.3
    ,val WORST_MOVE_WEIGHT: Double = 0.3
) {
    private val graphWinOdds: HashMap<GameNode, Double> = hashMapOf()
    var GameNode.winOdds: Double
        get() = graphWinOdds[this] ?: 0.0
        set(value) = run { graphWinOdds[this] = value }

    fun GameNode.bestMove(): GameEdge = this.children.maxBy { it.node.winOdds }

    fun isMyColor(color: Color): Boolean = color == winningColor

    init {
        val validRange = 0.0..1.0
        if(DROP_OFF_VALUE !in validRange) error { "illegal value for DROP_OFF_VALUE" }
        if(TERMINAL_DROP_OFF_VALUE !in validRange) error { "illegal value for TERMINAL_DROP_OFF_VALUE" }

        if(BEST_MOVE_WEIGHT !in validRange) error { "illegal value for BEST_MOVE_WEIGHT" }
        if(WORST_MOVE_WEIGHT !in validRange) error { "illegal value for WORST_MOVE_WEIGHT" }
    }
}
