import bumblebee.FileManager

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.pix")
    imgPix.show()
    println(imgPix.width)
    println(imgPix.height)
}