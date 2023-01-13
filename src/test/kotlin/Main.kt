import bumblebee.FileManager
import bumblebee.application.ByteViewer

fun main(){
//    ByteViewer(FileManager.readBytes("src/main/resources/venus2.tif"))
    val imgPix = FileManager.read("src/main/resources/venus2.tif")
}