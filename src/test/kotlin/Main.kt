import bumblebee.FileManager
import bumblebee.util.ByteViewer

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.png")
    ByteViewer(FileManager.readBytes("src/main/resources/lenna.png"))
}