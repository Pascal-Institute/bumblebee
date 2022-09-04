package bumblebee

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
        width = Converter.convertHexToInt(Converter.convertByteToHex(byteArray.copyOfRange(3, 7)))
        height = Converter.convertHexToInt(Converter.convertByteToHex(byteArray.copyOfRange(7, 11)))
        pixelBufferArray = ByteBuffer.allocate(width * height * 3)
        pixelBufferArray.put(byteArray.copyOfRange(11 , 11 + width * height * 3 ))
    }
}