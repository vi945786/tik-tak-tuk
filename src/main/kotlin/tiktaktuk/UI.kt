package tiktaktuk

import tiktaktuk.GameNode.Companion.node
import tiktaktuk.ai.Ai
import tiktaktuk.game.Board
import tiktaktuk.game.Color
import tiktaktuk.game.Moves
import java.io.File
import java.net.URISyntaxException
import kotlin.system.exitProcess

object UI {

    private const val ERROR_RED_COLOR = "\u001b[91m" //for errors
    private const val RED_COLOR = "\u001b[31m"      //for red player
    private const val GREEN_COLOR = "\u001b[32m"    //for ties
    private const val YELLOW_COLOR = "\u001b[33m"   //for yellow player
    private const val BLUE_COLOR = "\u001b[34m"     //for board
    private const val PURPLE_COLOR = "\u001B[0;35m" //for moves
    private const val CYAN_COLOR = "\u001b[96m"     //for ai messages
    private const val GARY_COLOR = "\u001b[90m"     //for board shifts
    private const val RESET_COLOR = "\u001b[0m"

    private val difficulty: Boolean by lazy {
        difficulty()
    }

    private fun getCurrentJarLocation(): String? {
        return try {
            val jarPath = object {}.javaClass.protectionDomain.codeSource.location.toURI().path
            File(jarPath).absolutePath
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            null
        }
    }

    fun before(args: Array<String>) {
        if ("new-console" in args || "no-console" !in args && System.console() == null) {

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

        println("${CYAN_COLOR}select AI difficulty: ${PURPLE_COLOR}hard${RESET_COLOR}${CYAN_COLOR} or ${PURPLE_COLOR}harder${RESET_COLOR}")

        difficulty

        println("${CYAN_COLOR}Preparing AI: this might take a few seconds${RESET_COLOR}")
    }

    fun start(debugging: Boolean = false) {

        System.console().let {
            try {
                val aiColor = if (difficulty) Color.YELLOW else Color.RED

                while (true) {

                    var board = Board()
                    board.print(debugging)

                    while (board.win == Color.EMPTY) {
                        board = if (difficulty) aiMove(board) else playerMove(board)
                        board.print(debugging)
                        if (board.win != Color.EMPTY) break
                        board = if (!difficulty) aiMove(board) else playerMove(board)
                        board.print(debugging)
                    }

                    when (board.win) {
                        aiColor.opposite() -> println("${aiColor.opposite().color()}you won${RESET_COLOR}")
                        aiColor -> println("${aiColor.color()}the AI beat you${RESET_COLOR}")
                        Color.BOTH -> println("${GREEN_COLOR}it's a tie${RESET_COLOR}")
                        else -> error {}
                    }
                }

            } catch (_: java.lang.NullPointerException) {
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
            } catch (_: java.lang.IllegalStateException) {
                println("${ERROR_RED_COLOR}invalid difficulty${RESET_COLOR}")
            }
        }
    }

    private fun playerMove(board: Board): Board {
        while (true) {
            try {
                return board.move(Moves.valueOf(readln().uppercase())) ?: throw IllegalStateException()
            } catch (_: java.lang.IllegalArgumentException) {
                println("${ERROR_RED_COLOR}move doesn't exist${RESET_COLOR}")
            } catch (_: java.lang.NullPointerException) {
                exitProcess(0)
            } catch (_: java.lang.IllegalStateException) {
                println("${ERROR_RED_COLOR}invalid move${RESET_COLOR}")
            }
        }
    }

    private fun aiMove(board: Board): Board {
        val move = Ai.move(board.node())
        println(CYAN_COLOR + "the AI played: ${PURPLE_COLOR}${move.move.name}${RESET_COLOR}")
        return move.node.board
    }

    private fun Board.print(debugging: Boolean) {
        val sb = StringBuilder()

        if(debugging) println(this.serialize())
        println("${PURPLE_COLOR}┌────── l m r ──────┐${RESET_COLOR}")
        for (row in 0..<3) {
            val cells = board[row]
            for (cell in cells) {
                sb.append("$BLUE_COLOR|$RESET_COLOR").append("${cell.color()}o${RESET_COLOR}")
            }
            sb.append("${BLUE_COLOR}|${RESET_COLOR}")

            val rowLetter = if (row == 0) "t" else if (row == 1) "m" else "b"
            when (shifts[row]) {
                -1 -> sb.insert(0, "${GARY_COLOR}|█|█${RESET_COLOR}").append("     ")
                0 -> sb.insert(0, "$GARY_COLOR  |█$RESET_COLOR").append("${GARY_COLOR}█|${RESET_COLOR}   ")
                1 -> sb.insert(0, "    ").append("${GARY_COLOR}█|█|${RESET_COLOR} ")
            }
            sb.insert(0, "${PURPLE_COLOR}${rowLetter}l${RESET_COLOR} ")
            sb.append("${PURPLE_COLOR}${rowLetter}r${RESET_COLOR}")

            println(sb.toString())
            sb.clear()
        }
        println("${PURPLE_COLOR}└───────────────────┘${RESET_COLOR}")
    }

    private fun Color.color() = when (this) {
        Color.YELLOW -> YELLOW_COLOR
        Color.RED -> RED_COLOR
        Color.EMPTY -> RESET_COLOR
        else -> error("invalid color")
    }
}