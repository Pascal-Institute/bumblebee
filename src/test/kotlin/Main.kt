import bumblebee.FileManager
import bumblebee.core.ImgPix
import bumblebee.core.ImgProcess
import bumblebee.type.FilterType

fun main(){
    val imgPix = ImgPix("src/main/resources/lenna.png").filter(FilterType.AVERAGE, 15)
    imgPix.show()
}

