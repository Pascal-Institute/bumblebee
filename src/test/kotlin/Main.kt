import bumblebee.FileManager
import bumblebee.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.png")
    imgPix.invert()
    imgPix.show()
}