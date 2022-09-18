package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import java.nio.ByteBuffer

class TIFF(byteArray: ByteArray) : ImgPix() {

    //Image File Header
    //Image File Directory

    init {
        imgFileType = if (byteArray.sliceArray(0 until 2).contentEquals(ImgFileType.TIFF_LITTLE.signature)){
            ImgFileType.TIFF_LITTLE
        }else{
            ImgFileType.TIFF_BIG
        }

        metaData.colorType = ColorType.TRUE_COLOR
        this.pixelBufferArray = ByteBuffer.allocate(0)
        byteArray.forEach {
            println(it)
        }
        extract()
    }

    override fun extract() {

    }

    private class IFH(){
        lateinit var byteOrder : ByteArray
        lateinit var fortyTwo : ByteArray
        lateinit var firstIFDOffset : ByteArray
    }

}