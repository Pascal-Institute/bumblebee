package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import java.nio.ByteBuffer

class TIFF(byteArray: ByteArray) : ImgPix() {

    //Image File Header (IFH)
    lateinit var identifier : ByteArray
    lateinit var version : ByteArray
    lateinit var ifdOffset : ByteArray

    //Image File Directory (IFD)9

    init {
        imgFileType = ImgFileType.TIFF
        metaData.colorType = ColorType.TRUE_COLOR
        this.pixelBufferArray = ByteBuffer.allocate(0)
        println(byteArray)
        extract()
    }

    override fun extract() {

    }
}