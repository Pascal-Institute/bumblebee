package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.core.Packet
import bumblebee.type.FileType

class WEBP(private var byteArray: ByteArray) : ImgPix() {
    private val riffHeader = ArrayList<Packet>()
    init {
        metaData.fileType = FileType.WEBP
        extract()
    }
}