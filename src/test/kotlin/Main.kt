import bumblebee.FileManager
import bumblebee.util.ByteViewer

fun main(){
    val imgPix = FileManager.read("src/main/resources/blackbuck.bmp")
    imgPix.show()
}