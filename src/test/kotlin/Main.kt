import bumblebee.FileManager
import bumblebee.core.ImgPix
import bumblebee.type.ThresholdType
import bumblebee.util.ByteViewer

fun main(){

    var lenna = FileManager.read("C:\\Users\\volta\\IdeaProjects\\bumblebee\\src\\main\\resources\\lenna.png")
    lenna.show()

//    ByteViewer(FileManager.readBytes("src/main/resources/balloons.jpg"))
}