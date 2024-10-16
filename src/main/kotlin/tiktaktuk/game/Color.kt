package tiktaktuk.game

@JvmInline
value class Color private constructor(val i: Byte) {

    companion object {
        val EMPTY = Color(0)
        val YELLOW = Color(1)
        val RED = Color(2)
        val BOTH = Color(3)

        fun of(i: Int) = when(i) {
            0 -> EMPTY
            1 -> YELLOW
            2 -> RED
            3 -> BOTH
            else -> error {"legal values are 0, 1, 2 and 3"}
        }
    }

    fun opposite() = when(i) {
        YELLOW.i -> RED
        RED.i -> YELLOW
        else -> error {}
    }
}