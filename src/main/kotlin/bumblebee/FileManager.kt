package bumblebee

import bumblebee.Converter.Companion
import bumblebee.Converter.Companion.intToByteArray
import bumblebee.mode.PIX
import bumblebee.mode.PNG
import bumblebee.type.ImgFileType
import java.io.File

class FileManager {

    companion object {
        fun read(filePath : String) : ImgPix {
            val byteArray = File(filePath).readBytes()
            var fileSignature = byteArray.sliceArray(0 until 8)

                if(fileSignature.contentEquals(ImgFileType.PNG.signature)){
                    return PNG(byteArray)
                }else{
                    fileSignature = fileSignature.sliceArray(0 until 3)
                    if(fileSignature.contentEquals(ImgFileType.PIX.signature )){
                        return PIX(byteArray)
                    }
                }
            return PIX(byteArray)
        }

        fun write(filePath: String, imgPix : ImgPix, imgFileType : ImgFileType){
           try{
               when(imgFileType){
                    ImgFileType.PIX ->{
                        var byteArray = ImgFileType.PIX.signature + intToByteArray(imgPix.width, 4) + intToByteArray(imgPix.height, 4) + imgPix.get()
                        File("$filePath.pix").writeBytes(byteArray)
                    }
                   else -> {}
               }
           }catch (e : Exception){
               e.printStackTrace()
           }
        }
    }

}