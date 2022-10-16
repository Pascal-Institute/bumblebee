import bumblebee.FileManager
import bumblebee.type.ThresholdType

fun main(){
    val imgPix = FileManager.read("src/main/resources/blackbuck.bmp")
    imgPix.threshold(ThresholdType.OTSU)
    imgPix.show()
}