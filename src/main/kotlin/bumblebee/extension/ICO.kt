package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Operator.Companion.invert
import bumblebee.core.Packet
import bumblebee.type.FileType
import bumblebee.util.StringObject.BIT_COUNT
import bumblebee.util.StringObject.COUNT
import bumblebee.util.StringObject.HEIGHT
import bumblebee.util.StringObject.NUM_OF_COLORS
import bumblebee.util.StringObject.PLANES
import bumblebee.util.StringObject.REVERSED
import bumblebee.util.StringObject.SIZE
import bumblebee.util.StringObject.START_OFFSET
import bumblebee.util.StringObject.TYPE
import bumblebee.util.StringObject.WIDTH
import java.nio.ByteBuffer

class ICO(private var byteArray: ByteArray) : ImgPix() {

    private var header = Packet()
    private var imageDir = Packet()

    init {
        extract()
    }

    override fun extract() {
        metaData.fileType = FileType.ICO_ICON

        //6 bytes.
        header[REVERSED] = byteArray.cut(0, 2)
        header[TYPE] = byteArray.cut(2, 4)
        header[COUNT] = byteArray.cut(4, 6)

        //16 bytes.
        imageDir[WIDTH] = byteArray.cut(6, 7).invert()
        imageDir[HEIGHT] = byteArray.cut(7, 8).invert()
        imageDir[NUM_OF_COLORS] = byteArray.cut(8, 9).invert()
        imageDir[REVERSED] = byteArray.cut(9, 10).invert()
        imageDir[PLANES] = byteArray.cut(10, 12).invert()
        imageDir[BIT_COUNT] = byteArray.cut(12, 14).invert()
        imageDir[SIZE] = byteArray.cut(14, 18).invert()
        imageDir[START_OFFSET] = byteArray.cut(18, 22).invert()

        setMetaData(imageDir)

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

    override fun setMetaData(header: Packet) {
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