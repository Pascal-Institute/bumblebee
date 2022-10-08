package bumblebee

import bumblebee.util.Converter.Companion.intToByteArray
import bumblebee.core.ImgPix
import bumblebee.extension.BMP
import bumblebee.extension.PIX
import bumblebee.extension.PNG
import bumblebee.extension.TIFF
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
                    }else{
                        fileSignature = fileSignature.sliceArray(0 until 2)

                        if(fileSignature.contentEquals(ImgFileType.TIFF_BIG.signature) || fileSignature.contentEquals(ImgFileType.TIFF_LITTLE.signature)){
                            return TIFF(byteArray)
                        }else if(fileSignature.contentEquals(ImgFileType.BMP.signature)){
                            return BMP(byteArray)
                        }
                    }

                }

            return PIX(byteArray)
        }

        fun write(filePath: String, imgPix : ImgPix, imgFileType : ImgFileType){
           try{
               when(imgFileType){
                    ImgFileType.PIX ->{
                        var byteArray = ImgFileType.PIX.signature +
                                        intToByteArray(imgPix.metaData.width, 4) +
                                        intToByteArray(imgPix.metaData.height, 4) +
                                        intToByteArray(imgPix.metaData.colorType.num, 1) +
                                        imgPix.get()
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