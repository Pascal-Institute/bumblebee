package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Converter.Companion.invert
import bumblebee.util.ImgHeader
import bumblebee.util.StringObj.BIT_COUNT
import bumblebee.util.StringObj.HEIGHT
import bumblebee.util.StringObj.SIZE
import bumblebee.util.StringObj.WIDTH
import java.nio.ByteBuffer

class BMP(private var byteArray: ByteArray) : ImgPix() {

    private var fileHeader = ImgHeader()
    private var infoHeader = ImgHeader()

    init {
        imgFileType = ImgFileType.BMP
        extract()
    }

    override fun extract() {

        fileHeader["signature"] = byteArray.cut(0, 2)
        fileHeader["filterSize"] = byteArray.cut(2, 6).invert()
        fileHeader["reversed1"] = byteArray.cut(6, 8).invert()
        fileHeader["reversed2"] = byteArray.cut(8, 10).invert()
        fileHeader["dataOffset"] = byteArray.cut(10, 14).invert()

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

        setMetaData()

        bytesPerPixel = colorType.colorSpace
        pixelByteBuffer = ByteBuffer.allocate(width * height * bytesPerPixel)

        byteArray.cut(54, byteArray.size).forEachIndexed { index, byte ->
            pixelByteBuffer.put( bytesPerPixel * width * (height - (index / (width * bytesPerPixel)) - 1) + ((index % (width * bytesPerPixel))/bytesPerPixel + 1) * bytesPerPixel - index % bytesPerPixel - 1 , byte)
        }
    }

    override fun setMetaData() {
        metaData.width = infoHeader[WIDTH]!!.byteToInt()
        metaData.height = infoHeader[HEIGHT]!!.byteToInt()
        metaData.colorType = if (infoHeader["bitCount"]!!.byteToInt() == 24) {
            ColorType.TRUE_COLOR
        } else {
            ColorType.GRAY_SCALE
        }
    }
}