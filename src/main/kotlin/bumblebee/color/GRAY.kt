package bumblebee.color

import bumblebee.type.ColorType

class GRAY(var g : Int) : Color {
    override val colorType  = ColorType.GRAY_SCALE
    override var colorArray = arrayOf(g)
}