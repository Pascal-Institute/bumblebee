import bumblebee.Converter.Companion.convertHexToRGB
import bumblebee.ImgPix
import bumblebee.RGB
import bumblebee.RGBA

fun main(){
    val imgPix = ImgPix("src/main/resources/lenna.png")

    println(imgPix.width)
    println(imgPix.height)
    println(imgPix.colorType)
    println(imgPix.get(0, 0))
    println(imgPix.get(512-128, 128))
    println(convertHexToRGB(imgPix.get(0, 0)))

//    for( i : Int in 256 until 256 + 64){
//        for( j : Int in 256 until 256 + 64){
//            imgPix.set(i, j , RGB(0, 0, 0))
//        }
//    }

    var new = imgPix.crop(256,256,256,256)
    new.show()
    imgPix.show()
}