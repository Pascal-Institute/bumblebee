import bumblebee.FileManager

fun main(){
    val imgPix = FileManager.read("src/main/resources/lenna.png")

    val byteViewer = ByteViewer(imgPix.get())

}