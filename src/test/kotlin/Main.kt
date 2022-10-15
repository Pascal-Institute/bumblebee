import bumblebee.FileManager

fun main(){
    val imgPix = FileManager.read("src/main/resources/blackbuck.bmp")
    imgPix.threshold(50)
    imgPix.show()
}