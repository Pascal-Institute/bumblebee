import bumblebee.Converter.Companion.convertHexToRGB
import bumblebee.FileManager
import bumblebee.PNG

fun main(){

    val imgPix = FileManager.read("src/main/resources/lenna.png")
    imgPix.show()
    FileManager.write("d", imgPix)
//  for( i : Int in 256 until 256 + 64){
//      for( j : Int in 256 until 256 + 64){
//          imgPix.set(i, j , RGB(0, 0, 0))
//      }
//  }
//  var new = imgPix.crop(256,256,256,256)
//  new.show()

}