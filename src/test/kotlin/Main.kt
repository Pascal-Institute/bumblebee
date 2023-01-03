import bumblebee.FileManager
import bumblebee.application.ByteViewer

fun main(){
    ByteViewer(FileManager.readBytes("src/main/resources/balloons.jpg"))
    FileManager.read("src/main/resources/balloons.jpg")
}