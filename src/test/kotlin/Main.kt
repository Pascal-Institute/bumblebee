import bumblebee.FileManager

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.pix")
    imgPix.show()
}