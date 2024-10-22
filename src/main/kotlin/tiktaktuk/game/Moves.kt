package tiktaktuk.game

enum class Moves {
    L,
    M,
    R,

    TR,
    MR,
    BR,

    TL,
    ML,
    BL;

    fun typeOfPlace() = when(this) {
        L, M, R -> true
        TR, MR, BR, TL, ML, BL -> false
    }

    fun getColumn() = when(this) {
        L -> 0
        M -> 1
        R -> 2
        else -> throw IllegalStateException("this function is only available for placing moves")
    }

    fun getRow() = when(this) {
        TR, TL -> 0
        MR, ML -> 1
        BR, BL -> 2
        else -> throw IllegalStateException("this function is only available for shifting moves")
    }

    fun getDirection() = when(this) {
        TR, MR, BR -> 1
        TL, ML, BL -> -1
        else -> throw IllegalStateException("this function is only available for shifting moves")
    }

    fun getDirectionRangeFunction(): Int.(Int) -> (IntProgression) = when(this) {
        TR, MR, BR -> { end: Int -> end downTo this }
        TL, ML, BL -> Int::rangeTo
        else -> throw IllegalStateException("this function is only available for shifting moves")
    }
}