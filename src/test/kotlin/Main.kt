import bumblebee.FileManager
import bumblebee.util.ByteViewer

fun main(){
    val byteArray = FileManager.readBytes("src/main/resources/lenna.png")
    val byteViewer = ByteViewer(byteArray)
}