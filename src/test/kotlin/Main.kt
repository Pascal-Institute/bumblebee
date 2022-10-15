import bumblebee.FileManager
import bumblebee.type.Orientation

fun main(){
    val imgPix = FileManager.read("src/main/resources/blackbuck.bmp")
    imgPix.flip(Orientation.VERTICAL)
    imgPix.show()
}