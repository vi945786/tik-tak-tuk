import tiktaktuk.game.Board
import tiktaktuk.game.Color.Companion.EMPTY
import tiktaktuk.game.Color.Companion.RED
import tiktaktuk.game.Color.Companion.YELLOW
import com.google.common.collect.ImmutableList
import tiktaktuk.game.Moves.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BoardLogicTest {

    @Test
    fun moves() {
        val board = Board().move(LC)!!.move(MC)!!.move(RC)!!.move(LC)!!.move(MC)!!.move(RC)!!.move(LC)
        assertEquals(actual = board!!.board[2][0], expected = YELLOW)
        assertEquals(actual = board.board[2][1], expected = RED)
        assertEquals(actual = board.board[2][2], expected = YELLOW)
        assertEquals(actual = board.board[1][0], expected = RED)
        assertEquals(actual = board.board[1][1], expected = YELLOW)
        assertEquals(actual = board.board[1][2], expected = RED)
        assertEquals(actual = board.board[0][0], expected = YELLOW)
        assertEquals(actual = board.board[0][1], expected = EMPTY)
        assertEquals(actual = board.board[0][2], expected = EMPTY)
        assertEquals(actual = board.shifts[2], expected = 0)
        assertEquals(actual = board.shifts[1], expected = 0)
        assertEquals(actual = board.shifts[0], expected = 0)
        assertEquals(actual = board.win, expected = YELLOW)
    }

    @Test
    fun shiftNoFall() {
        val board = Board().move(LC)!!.move(MC)!!.move(RC)!!.move(LC)!!.move(MC)!!.move(RC)!!.move(MC)!!.move(RC)!!.move(TL)
        assertEquals(actual = board!!.board[2][0], expected = YELLOW)
        assertEquals(actual = board.board[2][1], expected = RED)
        assertEquals(actual = board.board[2][2], expected = YELLOW)
        assertEquals(actual = board.board[1][0], expected = RED)
        assertEquals(actual = board.board[1][1], expected = YELLOW)
        assertEquals(actual = board.board[1][2], expected = RED)
        assertEquals(actual = board.board[0][0], expected = YELLOW)
        assertEquals(actual = board.board[0][1], expected = RED)
        assertEquals(actual = board.board[0][2], expected = EMPTY)
        assertEquals(actual = board.shifts[2], expected = 0)
        assertEquals(actual = board.shifts[1], expected = 0)
        assertEquals(actual = board.shifts[0], expected = -1)
        assertEquals(actual = board.win, expected = YELLOW)
    }

    @Test
    fun shiftFall3rdRow() {
        val board = Board().move(LC)!!.move(MC)!!.move(RC)!!.move(MC)!!.move(LC)!!.move(LC)!!.move(MC)!!.move(TR)
        assertEquals(actual = board!!.board[2][0], expected = YELLOW)
        assertEquals(actual = board.board[2][1], expected = RED)
        assertEquals(actual = board.board[2][2], expected = YELLOW)
        assertEquals(actual = board.board[1][0], expected = YELLOW)
        assertEquals(actual = board.board[1][1], expected = RED)
        assertEquals(actual = board.board[1][2], expected = YELLOW)
        assertEquals(actual = board.board[0][0], expected = EMPTY)
        assertEquals(actual = board.board[0][1], expected = RED)
        assertEquals(actual = board.board[0][2], expected = EMPTY)
        assertEquals(actual = board.shifts[2], expected = 0)
        assertEquals(actual = board.shifts[1], expected = 0)
        assertEquals(actual = board.shifts[0], expected = 1)
        assertEquals(actual = board.win, expected = RED)
    }

    @Test
    fun shiftFall1stRow() {
        val board = Board(
            ImmutableList.of(
                ImmutableList.of(RED, YELLOW, EMPTY),
                ImmutableList.of(RED, RED, EMPTY),
                ImmutableList.of(YELLOW, RED, EMPTY)
            ),
            ImmutableList.of(1, 1, 1),
            YELLOW
        ).move(BL)

        assertEquals(actual = board!!.board[2][0], expected = RED)
        assertEquals(actual = board.board[2][1], expected = RED)
        assertEquals(actual = board.board[2][2], expected = EMPTY)
        assertEquals(actual = board.board[1][0], expected = RED)
        assertEquals(actual = board.board[1][1], expected = YELLOW)
        assertEquals(actual = board.board[1][2], expected = EMPTY)
        assertEquals(actual = board.board[0][0], expected = RED)
        assertEquals(actual = board.board[0][1], expected = EMPTY)
        assertEquals(actual = board.board[0][2], expected = EMPTY)
        assertEquals(actual = board.shifts[2], expected = 0)
        assertEquals(actual = board.shifts[1], expected = 1)
        assertEquals(actual = board.shifts[0], expected = 1)
        assertEquals(actual = board.win, expected = RED)
        assertEquals(actual = board.turn, expected = RED)
    }

    @Test
    fun winTest() {
        var board = Board(
            ImmutableList.of(
                ImmutableList.of(RED, EMPTY, EMPTY),
                ImmutableList.of(RED, EMPTY, EMPTY),
                ImmutableList.of(RED, EMPTY, EMPTY)
            )
        )

        assertEquals(RED, board.win)

        board = Board(
            ImmutableList.of(
                ImmutableList.of(YELLOW, YELLOW, YELLOW),
                ImmutableList.of(EMPTY, EMPTY, EMPTY),
                ImmutableList.of(EMPTY, EMPTY, EMPTY)
            )
        )

        assertEquals(YELLOW, board.win)

        board = Board(
            ImmutableList.of(
                ImmutableList.of(YELLOW, EMPTY, EMPTY),
                ImmutableList.of(EMPTY, YELLOW, EMPTY),
                ImmutableList.of(EMPTY, EMPTY, YELLOW)
            )
        )

        assertEquals(YELLOW, board.win)
    }
}