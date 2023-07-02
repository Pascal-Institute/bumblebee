package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Operator.Companion.invert
import bumblebee.core.ImgHeader
import bumblebee.util.StringObj.BIT_COUNT
import bumblebee.util.StringObj.HEIGHT
import bumblebee.util.StringObj.SIZE
import bumblebee.util.StringObj.START_OFFSET
import bumblebee.util.StringObj.TYPE
import bumblebee.util.StringObj.WIDTH
import java.nio.ByteBuffer

class ICO(private var byteArray: ByteArray) : ImgPix() {

    private var header = ImgHeader()
    private var imageDir = ImgHeader()

    init {
        extract()
    }

    override fun extract() {
        //6 bytes.
        header["reversed"] = byteArray.cut(0, 2)
        header[TYPE] = byteArray.cut(2, 4)
        header["count"] = byteArray.cut(4, 6)

        //16 bytes.
        imageDir[WIDTH] = byteArray.cut(6, 7).invert()
        imageDir[HEIGHT] = byteArray.cut(7, 8).invert()
        imageDir["numberOfColors"] = byteArray.cut(8, 9).invert()
        imageDir["reversed"] = byteArray.cut(9, 10).invert()
        imageDir["planes"] = byteArray.cut(10, 12).invert()
        imageDir[BIT_COUNT] = byteArray.cut(12, 14).invert()
        imageDir[SIZE] = byteArray.cut(14, 18).invert()
        imageDir[START_OFFSET] = byteArray.cut(18, 22).invert()

        setMetaData(imageDir)

        bytesPerPixel = colorType.colorSpace
        pixelByteBuffer = ByteBuffer.allocate(width * height * bytesPerPixel)

        val startIdx = imageDir[START_OFFSET].byteToInt()
        val endIdx = startIdx + pixelByteBuffer.capacity()

        //BGR, ABGR
        byteArray.cut(startIdx, endIdx).forEachIndexed { index, byte ->
            pixelByteBuffer.put( bytesPerPixel * width * (height - (index / (width * bytesPerPixel)) - 1) + ((index % (width * bytesPerPixel))/bytesPerPixel + 1) * bytesPerPixel - index % bytesPerPixel - 1 , byte)
        }

        //RGBA to GBAR
        if(bytesPerPixel == 4){
            val copyPixelByteArray = pixelByteBuffer.array().clone()
            for(i : Int in copyPixelByteArray.indices step 4){
                pixelByteBuffer.put(i, copyPixelByteArray[i + 1])
                pixelByteBuffer.put(i + 1, copyPixelByteArray[i + 2])
                pixelByteBuffer.put(i + 2, copyPixelByteArray[i + 3])
                pixelByteBuffer.put(i + 3, copyPixelByteArray[i])
            }
        }
    }

    override fun setMetaData(header: ImgHeader) {
        metaData.width = header[WIDTH].byteToInt()
        metaData.height = header[HEIGHT].byteToInt()
        metaData.colorType = when(header[BIT_COUNT].byteToInt()) {
            32-> ColorType.TRUE_COLOR_ALPHA
            24-> ColorType.TRUE_COLOR
            16 -> ColorType.GRAY_SCALE_ALPHA
            8 -> ColorType.GRAY_SCALE
            else -> ColorType.GRAY_SCALE
        }
    }
}