package bumblebee.type

enum class FilterType(val num : Int) {

    NONE(0),
    SUB(1),
    UP(2),
    AVERAGE(3),
    PAETH(4);

    companion object {
        fun fromInt(num: Int) = FilterType.values().first { it.num == num }
    }
}