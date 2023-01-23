import bumblebee.FileManager

fun main(){
    val imgPix = FileManager.read("src/main/resources/f14.tif")
    imgPix.show()
}
