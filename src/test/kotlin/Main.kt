import bumblebee.FileManager
import bumblebee.util.ByteViewer

fun main(){
    var imgPix = FileManager.read("src/main/resources/balloons.jpg")
    ByteViewer(FileManager.readBytes("src/main/resources/balloons.jpg"))
}