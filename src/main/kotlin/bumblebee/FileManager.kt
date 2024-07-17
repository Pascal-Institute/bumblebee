package bumblebee

import bumblebee.util.Converter.Companion.intToByteArray
import bumblebee.core.ImgPix
import bumblebee.extension.*
import bumblebee.type.FileType
import java.io.File

class FileManager {

    companion object {
        fun read(filePath : String) : ImgPix {
            val byteArray = File(filePath).readBytes()
            var fileSignature = byteArray.sliceArray(0 until 8)
            if(fileSignature.contentEquals(FileType.PNG.signature)){ return PNG(byteArray) }

            fileSignature = byteArray.sliceArray(0 until 4)
            if(fileSignature.contentEquals(FileType.ICO_ICON.signature) || fileSignature.contentEquals(FileType.ICO_CURSOR.signature)){return ICO(byteArray)}
            if(fileSignature.contentEquals(FileType.WEBP.signature)){return WEBP(byteArray)}

            fileSignature = fileSignature.sliceArray(0 until 3)
            if(fileSignature.contentEquals(FileType.PIX.signature)){ return PIX(byteArray) }

            fileSignature = fileSignature.sliceArray(0 until 2)
            if(fileSignature.contentEquals(FileType.TIFF_BIG.signature) || fileSignature.contentEquals(FileType.TIFF_LITTLE.signature)){ return TIFF(byteArray) }
            if(fileSignature.contentEquals(FileType.BMP.signature)){ return BMP(byteArray) }
            if(fileSignature.contentEquals(FileType.JPG.signature)){ return JPG(byteArray) }

            return PIX(byteArray)
        }

        fun write(filePath: String, imgPix : ImgPix, fileType : FileType){
           try{
               when(fileType){
                    FileType.PIX ->{
                        var byteArray = FileType.PIX.signature +
                                        intToByteArray(imgPix.width, 4) +
                                        intToByteArray(imgPix.height, 4) +
                                        intToByteArray(imgPix.colorType.num, 1) +
                                        imgPix.mat.elements.map { it.toByte() }.toByteArray()
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