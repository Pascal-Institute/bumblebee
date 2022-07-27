package bumblebee

import bumblebee.type.ColorType

data class RGB(var r : Int, var g : Int, var b : Int) : Color {
    override val colorType  = ColorType.TRUE_COLOR
    override var colorArray = arrayOf(r, g, b)
}