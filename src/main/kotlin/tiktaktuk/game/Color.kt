package tiktaktuk.game

enum class Color(val i: Int) {

    EMPTY(0),
    YELLOW(1),
    RED(2),
    BOTH(3);

    companion object {
        fun of(i: Int) = when (i) {
            0 -> EMPTY
            1 -> YELLOW
            2 -> RED
            3 -> BOTH
            else -> error { "legal values are 0, 1, 2 and 3" }
        }
    }

    fun opposite() = when(i) {
        YELLOW.i -> RED
        RED.i -> YELLOW
        else -> error {}
    }
}