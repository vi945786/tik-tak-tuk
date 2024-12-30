package tiktaktuk

import tiktaktuk.GameNode.Companion.node
import tiktaktuk.ai.Ai
import tiktaktuk.game.Board
import tiktaktuk.game.Color
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale
import kotlin.collections.HashMap


private val memo: HashMap<GameNode, HashMap<Color, Triple<BigInteger, BigInteger, BigInteger>>> = HashMap()
data class Parameters(val node: GameNode, val remainingChildren: MutableList<GameNode>, val w: BigInteger = BigInteger.valueOf(0), val l: BigInteger = BigInteger.valueOf(0), val t: BigInteger = BigInteger.valueOf(0))

fun testAi(color: Color) {
    System.gc()
    val isPerfect = isPerfect(color.opposite())

    val stack = mutableListOf<Parameters>()
    stack.add(Parameters(Board.node(),
        if(color == Color.YELLOW) {
            mutableListOf(Ai.move(Board.node()).node)
        } else {
            Board.node().children.map { it.node }.toMutableList()
        }
    ))

    var w = BigInteger.valueOf(0)
    var l = BigInteger.valueOf(0)
    var t = BigInteger.valueOf(0)

    while (stack.isNotEmpty()) {
        val params = stack.last()
        val remainingChildren = params.remainingChildren

        if(remainingChildren.isEmpty()) {
            stack.removeLast().let {
                memo.put(it.node, HashMap<Color, Triple<BigInteger, BigInteger, BigInteger>>().let { map -> map.put(color, Triple(w - it.w, l - it.l, t - it.t)) ; map })
            }
            continue
        }

        val node = params.remainingChildren.removeLast()
        if(stack.any { it.node == node }) continue
        if(memo.contains(node)) {
            val colorValues = memo[node]!!
            if (colorValues.contains(color)) {
                val values = colorValues[color]!!

                w += values.first
                l += values.second
                t += values.third

                continue
            }
        }

        if (node.board.turn == color.opposite() && isPerfect[node]!! && !stack.filter { it.node.board.turn == color.opposite() }.all { isPerfect[it.node]!! }) {
            l++
            continue
        }

        if (node.board.win != Color.EMPTY) {
            when (node.board.win) {
                color -> w++
                color.opposite() -> if (stack.any { it.node.board.turn == color.opposite() && !isPerfect[it.node]!! }) l++
                Color.BOTH -> t++
                else -> throw IllegalStateException()
            }
            continue
        }

        when (node.board.turn) {
            color -> {
                stack.add(Parameters(node, mutableListOf(Ai.move(node).node), w, l, t))
            }
            else -> {
                stack.add(Parameters(node,
                    if (node.children.isEmpty()) mutableListOf()
                    else node.children.map { it.node }.toMutableList()
                ))
            }
        }
    }

    val sb = StringBuilder()

    sb.appendLine(color)

    sb.appendLine("w: ${DecimalFormat.getNumberInstance(Locale.ENGLISH).format(w.toBigDecimal().divide((w + l + t).toBigDecimal(), RoundingMode.HALF_UP).times(BigDecimal.valueOf(100)))}%")
    sb.appendLine("c: ${DecimalFormat.getNumberInstance(Locale.ENGLISH).format(w + l + t)}")

    println(sb.toString())
}
