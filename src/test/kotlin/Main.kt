import bumblebee.ImgPix
import javax.swing.JFrame

fun main(){

    val imgPix = ImgPix("src/main/resources/lenna.png")
    println(imgPix.width)
    println(imgPix.height)
    println(imgPix.colorType)
    imgPix.show()

}