package bumblebee

import bumblebee.Converter.Companion.convertByteToHex
import bumblebee.Converter.Companion.convertIntToByteArray
import bumblebee.type.ChunkType
import bumblebee.type.ImgFileType
import java.io.File

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
                    val chunkArray = ArrayList<Chunk>()
                    val fileSignature = ImgFileType.PNG.byte

                    //IDHR
                    //Width 4 byte
                    //Height 4 byte
                    //Bit depth 1 byte
                    //Color Type 1 byte
                    //Compression method 1 byte
                    //Filter method 1 byte

                    val idhr = Chunk()
                    idhr.length = byteArrayOf(0, 0, 0, 13)
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
                    idat.generateData(imgPix.get())
                    idat.type = ChunkType.IDAT.byte
                    idat.crc = idat.getCRC()

                    //IEND
                    val iend = Chunk()
                    iend.length = byteArrayOf(0, 0, 0, 4)
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