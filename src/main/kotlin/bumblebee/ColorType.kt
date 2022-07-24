package bumblebee

enum class ColorType(val num : Int) {
    GRAY_SCALE(0),
    TRUE_COLOR(2),
    INDEXED_COLOR(3),
    GRAY_SCALE_ALPHA(4),
    TRUE_COLOR_ALPHA(6);

    companion object {
        fun fromInt(num: Int) = ColorType.values().first { it.num == num }
    }
}