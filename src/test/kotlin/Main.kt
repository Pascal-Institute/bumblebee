import bumblebee.FileManager
import bumblebee.application.ByteViewer

fun main(){
//    ByteViewer(FileManager.readBytes("src/main/resources/tiff_sample.tiff"))
    val imgPix = FileManager.read("src/main/resources/f14.tif")
    imgPix.show()
}
