package tiktaktuk

import tiktaktuk.GameNode.Companion.node
import tiktaktuk.ai.Ai
import tiktaktuk.game.Board
import tiktaktuk.game.Color
import tiktaktuk.game.Moves
import kotlin.system.exitProcess

object UI {

    private val difficulty: Boolean by lazy {
        difficulty()
    }

    fun before(args: Array<String>) {
        if ("new-console" in args || System.console() == null && System.`in` == null) {

            val os = System.getProperty("os.name").lowercase()
            val location = getCurrentJarLocation()

            try {
                if (os.contains("win")) {
                    val command = arrayOf("cmd.exe", "/c", "start", "java", "-jar", location)
                    ProcessBuilder(*command).start()
                } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
                    val command = arrayOf("xterm", "-e", "java", "-jar", location)
                    ProcessBuilder(*command).start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            exitProcess(-1)
        }

        println("${cyanColor}select AI difficulty: ${purpleColor}hard${reset}${cyanColor} or ${purpleColor}harder${reset}" + reset)

        difficulty

        println(cyanColor + "Preparing AI: this might take a few seconds" + reset)
    }

    fun start() {

        System.console().let {
            try {

                var board = Board()
                board.print()

                val aiColor = if (difficulty) Color.YELLOW else Color.RED
                while (board.win == Color.EMPTY) {
                    board = if (difficulty) aiMove(board) else playerMove(board)
                    board.print()
                    if (board.win != Color.EMPTY) break
                    board = if (!difficulty) aiMove(board) else playerMove(board)
                    board.print()
                }

                when (board.win) {
                    aiColor.opposite() -> println(aiColor.opposite().color() + "you won" + reset)
                    aiColor -> println(aiColor.color() + "the AI beat you" + reset)
                    Color.BOTH -> println(greenColor + "it's a tie" + reset)
                    else -> error {}
                }

                Thread.sleep(2000)

            } catch (e: java.lang.NullPointerException) {
                exitProcess(0)
            }
        }
    }

    private fun difficulty(): Boolean {
        while (true) {
            try {
                val s = readln().lowercase()
                return when (s) {
                    "hard" -> false
                    "harder" -> true
                    else -> error {}
                }
            } catch (e: java.lang.IllegalStateException) {
                println(errorRedColor + "invalid difficulty" + reset)
            }
        }
    }

    private fun playerMove(board: Board): Board {
        while (true) {
            try {
                return board.move(Moves.valueOf(readln().uppercase())) ?: throw IllegalStateException()
            } catch (e: java.lang.IllegalArgumentException) {
                println(errorRedColor + "move doesn't exist" + reset)
            } catch (e: java.lang.NullPointerException) {
                exitProcess(0)
            } catch (e: java.lang.IllegalStateException) {
                println(errorRedColor + "invalid move" + reset)
            }
        }
    }

    private fun aiMove(board: Board): Board {
        val move = Ai.move(board.node())
        println(cyanColor + "the AI played: " + purpleColor + move.move.name + reset)
        return move.node.board
    }

    private fun Board.print() {
        val sb = StringBuilder()

        println("${purpleColor}┌────── l m r ──────┐${reset}")
        for (row in 0..<3) {
            val cells = board[row]
            for (cell in cells) {
                sb.append("$blueColor|$reset").append(cell.color() + "o" + reset)
            }
            sb.append("$blueColor|$reset")

            val rowLetter = if (row == 0) "t" else if (row == 1) "m" else "b"
            when (shifts[row]) {
                -1 -> sb.insert(0, "$grayColor|█|█$reset").append("     ")
                0 -> sb.insert(0, "$grayColor  |█$reset").append("$grayColor█|$reset   ")
                1 -> sb.insert(0, "    ").append("$grayColor█|█|$reset ")
            }
            sb.insert(0, "$purpleColor${rowLetter}l$reset ")
            sb.append("$purpleColor${rowLetter}r$reset")

            println(sb.toString())
            sb.clear()
        }
        println("${purpleColor}└───────────────────┘${reset}")
    }

    private fun Color.color() = when (this) {
        Color.YELLOW -> yellowColor
        Color.RED -> redColor
        Color.EMPTY -> reset
        else -> error("invalid color")
    }
}