import bumblebee.FileManager
import bumblebee.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType

fun main(){
    val imgPix = ImgPix(128, 128, ColorType.TRUE_COLOR)

    val imgPix1 = FileManager.read("src/main/resources/lenna.png")
    imgPix1.crop(0,0,512,128)
    imgPix1.invert()
//    FileManager.write("src/main/resources/lenna", imgPix1, ImgFileType.PIX)
//    val imgPix2 = FileManager.read("src/main/resources/lenna.pix")
    imgPix1.show()
//    imgPix2.show()
}