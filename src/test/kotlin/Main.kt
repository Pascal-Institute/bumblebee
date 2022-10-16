import bumblebee.FileManager
import bumblebee.type.Orientation
import bumblebee.util.ByteViewer

fun main(){
    val imgPix = FileManager.read("src/main/resources/blackbuck.bmp")
    val hist = imgPix.toGrayScale().histogram()
    hist.channelG.forEach{
        println(it)
    }
    imgPix.show()
}