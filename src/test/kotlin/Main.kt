import bumblebee.FileManager

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.png")
    imgPix.toGrayScale()
//    imgPix.flip()
    imgPix.show()
    println(imgPix.width)
    println(imgPix.height)
//    val imgPix = FileManager.read("src/main/resources/tiff_sample.tiff")
}