package bumblebee.mode

import bumblebee.Converter.Companion.byteToHex
import bumblebee.Converter.Companion.hexToInt
import bumblebee.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import java.nio.ByteBuffer

class PIX(private var byteArray : ByteArray) : ImgPix() {

    init {
        imgFileType = ImgFileType.PIX
        extract()
    }

    override fun extract() {
        colorType = ColorType.TRUE_COLOR
        metaData.width = hexToInt(byteToHex(byteArray.copyOfRange(3, 7)))
        metaData.height = hexToInt(byteToHex(byteArray.copyOfRange(7, 11)))
        pixelBufferArray = ByteBuffer.allocate(metaData.width * metaData.height * 3)
        pixelBufferArray.put(byteArray.copyOfRange(11 , 11 + metaData.width * metaData.height * 3 ))
    }
}