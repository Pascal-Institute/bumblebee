package bumblebee

import bumblebee.type.ColorType

data class RGBA(val r : Int, val g : Int, val b : Int, val a : Int) : Color {
    override val colorType = ColorType.TRUE_COLOR_ALPHA
    override var colorArray = arrayOf(r, g, b)
}