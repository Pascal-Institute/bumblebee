import bumblebee.FileManager
import bumblebee.core.ImgPix
import bumblebee.type.ThresholdType
import bumblebee.util.ByteViewer

fun main(){
//    var imgPix = ImgPix("src/main/resources/balloons.jpg")
    ByteViewer(FileManager.readBytes("src/main/resources/balloons.jpg"))
//    imgPix.show()

}