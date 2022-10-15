import bumblebee.FileManager
import bumblebee.util.ByteViewer

fun main(){
    val imgPix = FileManager.read("src/main/resources/blackbuck.bmp")
    imgPix.crop(255,255,256,256)
    imgPix.show()
}