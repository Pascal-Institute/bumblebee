package bumblebee

enum class ColorType(val num: Int, val colorSpace: Int) {
    GRAY_SCALE(0, 2),
    TRUE_COLOR(2, 3),
    INDEXED_COLOR(3, 1),
    GRAY_SCALE_ALPHA(4, 2),
    TRUE_COLOR_ALPHA(6, 3);

    companion object {
        fun fromInt(num: Int) = ColorType.values().first { it.num == num }
    }
}