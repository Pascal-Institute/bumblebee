import bumblebee.Converter.Companion.convertHexToRGB
import bumblebee.ImgPix

fun main(){
    val imgPix = ImgPix("C:\\Users\\user\\Desktop\\lenna.png")
    println(imgPix.width)
    println(imgPix.height)
    println(imgPix.colorType)
    println(imgPix.get(0, 0))
    println(imgPix.get(256, 256))
    println(convertHexToRGB(imgPix.get(0, 0)))
}