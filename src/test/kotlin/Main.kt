import bumblebee.FileManager
import bumblebee.util.ByteViewer

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.png")
    val byteViewer = ByteViewer(FileManager.readBytes("src/main/resources/blackbuck.bmp"))
}