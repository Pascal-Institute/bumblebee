package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.invert
import java.nio.ByteBuffer

class BMP(private var byteArray: ByteArray) : ImgPix() {

    private var header = Header()
    private var infoHeader = InfoHeader()

    init {
        imgFileType = ImgFileType.BMP
        extract()
    }

    private class Header{
        lateinit var signature : ByteArray
        lateinit var fileSize : ByteArray
        lateinit var reversed1 : ByteArray
        lateinit var reversed2 : ByteArray
        lateinit var dataOffset : ByteArray

        fun extract(byteArray: ByteArray){
            signature = byteArray.sliceArray(0 until 2)
            fileSize = byteArray.sliceArray(2 until 6).invert()
            reversed1 = byteArray.sliceArray(6 until 8).invert()
            reversed2 = byteArray.sliceArray(8 until 10).invert()
            dataOffset = byteArray.sliceArray(10 until 14).invert()
        }
    }

    private class InfoHeader{
        lateinit var size : ByteArray
        lateinit var width : ByteArray
        lateinit var height : ByteArray
        lateinit var planes : ByteArray
        lateinit var bitCount : ByteArray
        lateinit var compression : ByteArray
        lateinit var imageSize : ByteArray
        lateinit var xPixelsPerM : ByteArray
        lateinit var yPixelsPerM : ByteArray
        lateinit var colorsUsed : ByteArray
        lateinit var colorsImportant : ByteArray

        fun extract(byteArray: ByteArray) {
            size = byteArray.sliceArray(0 until 4).invert()
            width = byteArray.sliceArray(4 until 8).invert()
            height = byteArray.sliceArray(8 until 12).invert()
            planes = byteArray.sliceArray(12 until 14).invert()
            bitCount = byteArray.sliceArray(14 until 16).invert()
            compression = byteArray.sliceArray(16 until 20).invert()
            imageSize = byteArray.sliceArray(20 until 24).invert()
            xPixelsPerM = byteArray.sliceArray(24 until 28).invert()
            yPixelsPerM = byteArray.sliceArray(28 until 32).invert()
            colorsUsed = byteArray.sliceArray(32 until 36).invert()
            colorsImportant = byteArray.sliceArray(36 until 40).invert()
        }
    }

    override fun extract() {
        header.extract(byteArray.sliceArray(0 until 14))
        infoHeader.extract(byteArray.sliceArray(14 until 54))
        metaData.width = infoHeader.width.byteToInt()
        metaData.height = infoHeader.height.byteToInt()
        metaData.colorType = if (infoHeader.bitCount.byteToInt() == 24) {
            ColorType.TRUE_COLOR
        } else {
            ColorType.GRAY_SCALE
        }
        bytesPerPixel = colorType.colorSpace
        pixelBufferArray = ByteBuffer.allocate(width * height * bytesPerPixel)

        byteArray.sliceArray(54 until byteArray.size).forEachIndexed { index, byte ->
            pixelBufferArray.put( bytesPerPixel * width * (height - (index / (width * bytesPerPixel)) - 1) + ((index % (width * bytesPerPixel))/bytesPerPixel + 1) * bytesPerPixel - index % bytesPerPixel - 1 , byte)
        }


    }
}