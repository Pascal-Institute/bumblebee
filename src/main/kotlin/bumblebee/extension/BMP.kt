package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.hexToInt
import bumblebee.util.Converter.Companion.invert

class BMP(private var byteArray: ByteArray) : ImgPix() {

    private var header = Header()
    private var infoHeader = InfoHeader()

    init {
        extract()
        println(byteToHex(infoHeader.width))
        println(byteToHex(infoHeader.height))

    }

    private class Header{
        lateinit var signature : ByteArray
        lateinit var fileSize : ByteArray
        lateinit var reversed1 : ByteArray
        lateinit var reversed2 : ByteArray
        lateinit var dataOffset : ByteArray

        fun extract(byteArray: ByteArray){
            signature = byteArray.sliceArray(0 until 2)
            fileSize = invert(byteArray.sliceArray(2 until 6))
            reversed1 = invert(byteArray.sliceArray(6 until 8))
            reversed2 = invert(byteArray.sliceArray(8 until 10))
            dataOffset = invert(byteArray.sliceArray(10 until 14))
        }
    }
//
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
            size = invert(byteArray.sliceArray(0 until 4))
            width = invert(byteArray.sliceArray(4 until 8))
            height = invert(byteArray.sliceArray(8 until 12))
            planes = invert(byteArray.sliceArray(12 until 14))
            bitCount = invert(byteArray.sliceArray(14 until 16))
            compression = invert(byteArray.sliceArray(16 until 20))
            imageSize = invert(byteArray.sliceArray(20 until 24))
            xPixelsPerM = invert(byteArray.sliceArray(24 until 28))
            yPixelsPerM = invert(byteArray.sliceArray(28 until 32))
            colorsUsed = invert(byteArray.sliceArray(32 until 36))
            colorsImportant = invert(byteArray.sliceArray(36 until 40))
        }
    }

    override fun extract() {
        header.extract(byteArray.sliceArray(0 until 14))
        infoHeader.extract(byteArray.sliceArray(14 until 54))

    }
}