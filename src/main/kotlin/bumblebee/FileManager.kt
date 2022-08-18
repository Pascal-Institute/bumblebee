package bumblebee

import bumblebee.Converter.Companion.convertByteToHex
import bumblebee.Converter.Companion.convertIntToByteArray
import bumblebee.Converter.Companion.convertLongToByteArray
import bumblebee.type.ChunkType
import bumblebee.type.ImgFileType
import java.io.File
import javax.imageio.ImageIO

class FileManager {

    companion object {
        fun read(filePath : String) : ImgPix{
            val byteArray = File(filePath).readBytes()
            var fileSignature = byteArray.sliceArray(0 until 8)

            return when (convertByteToHex(fileSignature)){
                convertByteToHex(ImgFileType.PNG.byte) -> PNG(byteArray)

                else ->{
                    fileSignature = fileSignature.sliceArray(0 until 2)
                    return when(convertByteToHex(fileSignature)){
                        convertByteToHex(ImgFileType.PNG.byte) -> PNG(byteArray)
                        else -> PNG(byteArray)
                    }
                }
            }
        }

        fun write(filePath: String, imgPix : ImgPix){
            when(imgPix.imgFileType){
                ImgFileType.PNG -> {
                    val fileSignature = ImgFileType.PNG.byte

                    val idhr = Chunk()
                    idhr.length = convertIntToByteArray(13,4)
                    idhr.initData(13)

                    idhr.type = ChunkType.IHDR.byte

                    var widthArray = convertIntToByteArray(imgPix.width, 4);
                    var heightArray = convertIntToByteArray(imgPix.height, 4);
                    var bitDepthArray = convertIntToByteArray(imgPix.bitDepth, 1);
                    var colorTypeArray = convertIntToByteArray(imgPix.colorType.num, 1);
                    var compressionMethodTypeArray = convertIntToByteArray(0, 1);
                    var filterMethodTypeArray = convertIntToByteArray(0, 1);
                    var interlaceMethodType = convertIntToByteArray(0, 1);

                    idhr.data = widthArray +
                                heightArray +
                                bitDepthArray +
                                colorTypeArray +
                                compressionMethodTypeArray +
                                filterMethodTypeArray +
                                interlaceMethodType
                    idhr.crc = idhr.getCRC()

                    //IDAT
                    val idat = Chunk()
                    idat.length = convertIntToByteArray(imgPix.width * imgPix.height, 4);
                    idat.initData(imgPix.bytesPerPixel * (imgPix.width + 1) * imgPix.height)
                    idat.generateData(imgPix)
                    idat.type = ChunkType.IDAT.byte
                    idat.crc = idat.getCRC()

                    //IEND
                    val iend = Chunk()
                    iend.length = byteArrayOf(0, 0, 0, 4)
                    iend.initData(0)
                    iend.type = ChunkType.IEND.byte
                    iend.crc = iend.getCRC()
                }
                else ->{}
            }
        }

//        fun write(filePath: String, imgPix: ImgPix){
//            return kernel(filePath, imgPix)
//        }


    }

}