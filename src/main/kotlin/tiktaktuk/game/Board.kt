package tiktaktuk.game

import tiktaktuk.game.Color.EMPTY
import tiktaktuk.game.Color.YELLOW
import com.google.common.collect.ImmutableList
import tiktaktuk.game.Color.BOTH
import tiktaktuk.game.Color.RED

private fun ImmutableList<Int>.mutable() = this.toTypedArray()
private fun Array<Int>.immutable() = ImmutableList.copyOf(this)
private fun Array<Int>.setValue(index: Int, value: Int): Array<Int> {
    this[index] = value
    return this
}

private fun ImmutableList<ImmutableList<Color>>.mutable() = map { it.toTypedArray() }.toTypedArray()
private fun Array<Array<Color>>.immutable() = ImmutableList.copyOf(map { ImmutableList.copyOf(it) })
private fun Array<Array<Color>>.setValue(row: Int, column: Int, color: Color): Array<Array<Color>> {
    this[row][column] = color
    return this
}

private val defaultBoard = ImmutableList.of(ImmutableList.of(EMPTY, EMPTY, EMPTY), ImmutableList.of(EMPTY, EMPTY, EMPTY), ImmutableList.of(EMPTY, EMPTY, EMPTY))
private val defaultShifts: ImmutableList<Int> = ImmutableList.of(0, 0, 0)
private val defaultColor = YELLOW

data class Board(val board: ImmutableList<ImmutableList<Color>> = defaultBoard, val shifts: ImmutableList<Int> = defaultShifts, val turn: Color = defaultColor) {

    val win: Color

    init {
        require(board.size == 3) { "board size must be 3" }
        for (i in 0..<3) require(board[i].size == 3) { "board length must be 3" }
        require(shifts.size == 3) { "shifts size must be 3" }
        for (i in 0..<3) require(shifts[i] in -1..1) { "invalid shift positions" }

        win = checkWin()
    }

    private fun checkWin(): Color {
        val wins = mutableListOf<Color>()

        if(board[0][0] != EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) wins.add(board[0][0])
        if(board[2][0] != EMPTY && board[2][0] == board[1][1] && board[1][1] == board[0][2]) wins.add(board[2][0])

        for(i in 0..<3) {
           if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) wins.add(board[i][0])
           if (board[0][i] != EMPTY && board[0][i] == board[1][i] && board[1][i] == board[2][i]) wins.add(board[0][i])
       }

        var yellow = false
        var red = false

        for (win in wins) {
            if(win == YELLOW) yellow = true
            else if(win == RED) red = true
        }

        if(yellow && red) return BOTH
        if(yellow) return YELLOW
        if(red) return RED
        return EMPTY
    }

    fun move(moves: Moves): Board? {
        if(win != EMPTY) return null

        if(moves.typeOfPlace()) return place(moves)
        return shift(moves)
    }

    private fun place(moves: Moves): Board? {
        val column = moves.getColumn()
        var row = 2

        while(board[row][column] != EMPTY) {
            row--
            if(-1 == row) return null
        }

        return Board(board.mutable().setValue(row, column, turn).immutable(), shifts, turn.opposite())
    }

    private fun shift(moves: Moves): Board? {
        val row = moves.getRow()
        val direction = moves.getDirection()

        if(shifts[row] * direction == 1) return null

        val newBoard = board.mutable()

        val boardRange = 0.(moves.getDirectionRangeFunction())(2)
        for (i in boardRange) {
            val newI = i + direction
            if(newI !in boardRange) continue

            newBoard[row][newI] = board[row][i]
            newBoard[row][i] = EMPTY
        }

        return Board(gravity(newBoard), shifts.mutable().setValue(row, shifts[row] + direction).immutable(), turn.opposite())
    }

    private fun gravity(board: Array<Array<Color>>): ImmutableList<ImmutableList<Color>> {
        for (column in 0..<3) {
            for (row in 2 downTo 0) {
                var times = 0
                while (board[row][column] == EMPTY && times < 2) {
                    for (i in row downTo 1) {
                        board[i][column] = board[i - 1][column]
                    }
                    board[0][column] = EMPTY
                    times++
                }
            }
        }

        return board.immutable()
    }

    fun serialize(): Int {
        val sb = StringBuilder()

        sb.append(board[0][0].i)
        sb.append(board[1][0].i)
        sb.append(board[2][0].i)

        sb.append(board[0][1].i)
        sb.append(board[1][1].i)
        sb.append(board[2][1].i)

        sb.append(board[0][2].i)
        sb.append(board[1][2].i)
        sb.append(board[2][2].i)

        sb.append(shifts[0] +1)
        sb.append(shifts[1] +1)
        sb.append(shifts[2] +1)

        return sb.toString().toInt(3) shl 1 or (turn.i - 1)
    }

    companion object {
        fun deserialize(i: Int): Board {

            val turn = Color.of((i and 1) + 1)

            val boardAndShifts = (i shr 1).toString(3).padStart(12, '0')
            if(boardAndShifts.length >= 13) error {"invalid board id"}

            val nums = arrayListOf<Int>()

            for(c in boardAndShifts) {
                nums.add(c.digitToInt())
            }

            val board = arrayOf(
                arrayOf(Color.of(nums[0]), Color.of(nums[3]), Color.of(nums[6])),
                arrayOf(Color.of(nums[1]), Color.of(nums[4]), Color.of(nums[7])),
                arrayOf(Color.of(nums[2]), Color.of(nums[5]), Color.of(nums[8]))
            )

            var shifts = arrayOf(nums[9] -1, nums[10] -1, nums[11] -1)

            return Board(board.immutable(), shifts.immutable(), turn)
        }
    }
}