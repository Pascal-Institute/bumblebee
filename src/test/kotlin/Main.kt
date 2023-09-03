import bumblebee.color.RGB
import bumblebee.util.Converter.Companion.toRGB
import bumblebee.util.Converter.Companion.toYCBCR

fun main(){

    val rgb = RGB(10,20,30)
    val converted = rgb.toYCBCR()
    val final = converted.toRGB()
}