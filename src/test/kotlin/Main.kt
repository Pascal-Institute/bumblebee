import bumblebee.FileManager
import bumblebee.type.ThresholdType

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.png")
    imgPix.threshold(ThresholdType.OTSU)
    imgPix.show()
}