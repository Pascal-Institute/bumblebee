package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.util.Converter.Companion.cut
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
        imageDir[WIDTH] = byteArray.cut(6, 7)
        imageDir[HEIGHT] = byteArray.cut(7, 8)
        imageDir["reversed"] = byteArray.cut(8, 9)
        imageDir["planes"] = byteArray.cut(9, 11)
        imageDir[BIT_COUNT] = byteArray.cut(11, 13)
        imageDir["size"] = byteArray.cut(13, 17)
        imageDir["offset"] = byteArray.cut(17, 21)

        println()

    }
}