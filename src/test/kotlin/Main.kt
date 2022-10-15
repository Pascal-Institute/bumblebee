import bumblebee.FileManager

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.png")
    imgPix.toGrayScale()
    imgPix.show()
}