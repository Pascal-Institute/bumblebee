package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.FileType
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Operator.Companion.invert
import bumblebee.core.Packet
import bumblebee.util.StringObject.BIT_COUNT
import bumblebee.util.StringObject.HEIGHT
import bumblebee.util.StringObject.SIZE
import bumblebee.util.StringObject.START_OFFSET
import bumblebee.util.StringObject.WIDTH
import komat.space.Mat
import java.nio.ByteBuffer

class BMP(private var byteArray: ByteArray) : ImgPix() {

    private var fileHeader = Packet()
    private var infoHeader = Packet()

    init {
        extract()
    }

    override fun extract() {
        metaData.fileType = FileType.BMP

        fileHeader["signature"] = byteArray.cut(0, 2)
        fileHeader["filterSize"] = byteArray.cut(2, 6).invert()
        fileHeader["reversed1"] = byteArray.cut(6, 8).invert()
        fileHeader["reversed2"] = byteArray.cut(8, 10).invert()
        fileHeader[START_OFFSET] = byteArray.cut(10, 14).invert()

        infoHeader[SIZE] = byteArray.cut(14, 18).invert()
        infoHeader[WIDTH] = byteArray.cut(18, 22).invert()
        infoHeader[HEIGHT] = byteArray.cut(22, 26).invert()
        infoHeader["planes"] = byteArray.cut(26, 28).invert()
        infoHeader[BIT_COUNT] = byteArray.cut(28, 32).invert()
        infoHeader["compression"] = byteArray.cut(32, 36).invert()
        infoHeader["imageSize"] = byteArray.cut(36, 40).invert()
        infoHeader["xPixelsPerM"] = byteArray.cut(40, 42).invert()
        infoHeader["yPixelsPerM"] = byteArray.cut(42, 46).invert()
        infoHeader["colorsUsed"] = byteArray.cut(46, 50).invert()
        infoHeader["colorsImportant"] = byteArray.cut(50, 54).invert()

        setMetaData(infoHeader)

        mat = Mat(width, height * bytesPerPixel, ByteArray(width * height * bytesPerPixel))

        val startIdx = fileHeader[START_OFFSET].byteToInt()
        val endIdx = startIdx + mat.elements.size

        byteArray.cut(startIdx, endIdx).forEachIndexed { index, byte ->
            mat[bytesPerPixel * width * (height - (index / (width * bytesPerPixel)) - 1) + ((index % (width * bytesPerPixel))/bytesPerPixel + 1) * bytesPerPixel - index % bytesPerPixel - 1] = byte
        }
    }

    override fun setMetaData(packet : Packet) {
        metaData.width = packet[WIDTH].byteToInt()
        metaData.height = packet[HEIGHT].byteToInt()
        metaData.colorType = when(packet[BIT_COUNT].byteToInt()) {
            32-> ColorType.TRUE_COLOR_ALPHA
            24-> ColorType.TRUE_COLOR
            16 -> ColorType.GRAY_SCALE_ALPHA
            8 -> ColorType.GRAY_SCALE
            else -> ColorType.GRAY_SCALE
        }
    }
}