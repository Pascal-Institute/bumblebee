import bumblebee.Converter.Companion.convertHexToRGB
import bumblebee.FileManager
import bumblebee.PNG
import bumblebee.type.ImgFileType

fun main(){
    val imgPng = FileManager.read("src/main/resources/lenna.png")
    FileManager.write("src/main/resources/lenna", imgPng, ImgFileType.PIX)
    val imgPix = FileManager.read("src/main/resources/lenna.pix")
    imgPng.show()
    imgPix.show()
}