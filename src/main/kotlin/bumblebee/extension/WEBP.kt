package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.core.Packet
import bumblebee.type.FileType
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Converter.Companion.toASCII
import bumblebee.util.Converter.Companion.toHex
import bumblebee.util.StringObject.FORMAT
import bumblebee.util.StringObject.NAME
import bumblebee.util.StringObject.SIZE

class WEBP(private var byteArray: ByteArray) : ImgPix() {
    private val riffHeader = Packet()

    init {
        extract()
    }

    override fun extract() {
        metaData.fileType = FileType.WEBP
        riffHeader[NAME] = byteArray.cut(0, 4)
        riffHeader[SIZE] = byteArray.cut(4, 8)
        riffHeader[FORMAT] = byteArray.cut(8, 12)
        riffHeader["ss"] = byteArray.cut(12, 16)
        riffHeader["sss"] = byteArray.cut(16, 20)
        riffHeader["ssss"] = byteArray.cut(20, 21)
        println(riffHeader["ss"].toASCII())
        println(riffHeader["ssss"].byteToInt())

        println(riffHeader[NAME].toASCII())
        println(riffHeader[NAME].toHex())
        println(riffHeader[FORMAT].toASCII())

    }
}
