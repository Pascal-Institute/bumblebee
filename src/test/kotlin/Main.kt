import bumblebee.FileManager
import bumblebee.core.ImgPix
import bumblebee.type.PadType
import bumblebee.type.ThresholdType
import bumblebee.util.ByteViewer
import bumblebee.util.Numeric

fun main(){
    var imgPix = FileManager.read("src/main/resources/blackbuck.bmp")
    imgPix.threshold(ThresholdType.OTSU)
    imgPix.show()

//    var imgPix = FileManager.read("src/main/resources/balloons.jpg")

//    var answer = Numeric.softMax(doubleArrayOf(1.0, 2.0, 3.0))
//
//    ByteViewer(FileManager.readBytes("src/main/resources/balloons.jpg"))
}