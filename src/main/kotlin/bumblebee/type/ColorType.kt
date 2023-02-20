package bumblebee.type

enum class ColorType(val num: Int, val colorSpace: Int) {
    GRAY_SCALE(0, 1),
    TRUE_COLOR(2, 3),
    INDEXED_COLOR(3, 4),
    GRAY_SCALE_ALPHA(4, 2),
    TRUE_COLOR_ALPHA(6, 4);

    companion object {
        fun fromInt(num: Int) = ColorType.values().first { it.num == num }
    }
}