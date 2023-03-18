package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Converter.Companion.invert
import bumblebee.util.ImgHeader
import bumblebee.util.StringObj.BIT_COUNT
import bumblebee.util.StringObj.HEIGHT
import bumblebee.util.StringObj.WIDTH

class ICO(private var byteArray: ByteArray) : ImgPix() {

    private var header = ImgHeader()
    private var imageDir = ImgHeader()

    init {
        extract()
    }

    override fun extract() {
        //6 bytes.
        header["reversed"] = byteArray.cut(0, 2)
        header["type"] = byteArray.cut(2, 4)
        header["count"] = byteArray.cut(4, 6)

        //16 bytes.
        imageDir[WIDTH] = byteArray.cut(6, 7).invert()
        imageDir[HEIGHT] = byteArray.cut(7, 8).invert()
        imageDir["numberOfColors"] = byteArray.cut(8, 9).invert()
        imageDir["reversed"] = byteArray.cut(9, 10).invert()
        imageDir["planes"] = byteArray.cut(10, 12).invert()
        imageDir[BIT_COUNT] = byteArray.cut(12, 14).invert()
        imageDir["size"] = byteArray.cut(14, 18).invert()
        imageDir["offset"] = byteArray.cut(18, 22).invert()

        println()

    }
}