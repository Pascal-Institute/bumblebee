import bumblebee.core.ImgPix
import bumblebee.type.FilterType
import bumblebee.type.OrientationType
import bumblebee.type.PadType

fun main(){
    //val imgPix = ImgPix("src/main/resources/bumblebee_icon.png")
    //imgPix.resize(128, 128)
    //imgPix.toGrayScale()


    //imgPix.getChannel(0).show()

    /*imgPix.invert()
    imgPix.flip(OrientationType.HORIZONTAL)
    imgPix.flip(OrientationType.VERTICAL)
    imgPix.toGrayScale()
    imgPix.threshold(120)
    imgPix.pad(PadType.ZERO, 3)
    imgPix.pad(PadType.AVERAGE, 5)
    imgPix.filter(FilterType.GAUSSIAN, 11, 3.0)*/
    //imgPix.show()

    val lenna = ImgPix("src/main/resources/lenna.png")
    lenna.crop(256,256,256,256)
    lenna.flip(OrientationType.HORIZONTAL)
    //lenna.pad(PadType.AVERAGE, 10)
    //lenna.filter(FilterType.GAUSSIAN, 9, 2.0)
    lenna.show()
}