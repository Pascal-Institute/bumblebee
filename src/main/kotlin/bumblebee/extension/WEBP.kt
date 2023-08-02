package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.core.Packet
import bumblebee.type.FileType
import bumblebee.util.Converter.Companion.cut
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

    }
}
