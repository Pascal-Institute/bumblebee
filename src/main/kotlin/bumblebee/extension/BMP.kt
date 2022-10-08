package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.hexToInt

class BMP(private var byteArray: ByteArray) : ImgPix() {

    private var header = Header()
    private var infoHeader = InfoHeader()

    init {

        extract()

        println(byteToHex(header.signature))
        println(byteToHex(header.fileSize))
        println(byteToHex(header.reversed))
        println(byteToHex(header.dataOffset))

        println((byteToHex(infoHeader.imageSize)))
        println(byteToHex(infoHeader.bitCount))
        println(byteToHex(infoHeader.width))
        println(byteToHex(infoHeader.height))
        println(hexToInt(byteToHex(infoHeader.width)))
        println(hexToInt(byteToHex(infoHeader.height)))


    }

    private class Header{
        lateinit var signature : ByteArray
        lateinit var fileSize : ByteArray
        lateinit var reversed : ByteArray
        lateinit var dataOffset : ByteArray

        fun extract(byteArray: ByteArray){
            signature = byteArray.sliceArray(0 until 2)
            fileSize = byteArray.sliceArray(2 until 6)
            reversed = byteArray.sliceArray(6 until 10)
            dataOffset = byteArray.sliceArray(10 until 14)
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
            size = byteArray.sliceArray(0 until 4)
            width = byteArray.sliceArray(4 until 8)
            height = byteArray.sliceArray(8 until 12)
            planes = byteArray.sliceArray(12 until 14)
            bitCount = byteArray.sliceArray(14 until 16)
            compression = byteArray.sliceArray(16 until 20)
            imageSize = byteArray.sliceArray(20 until 24)
            xPixelsPerM = byteArray.sliceArray(24 until 28)
            yPixelsPerM = byteArray.sliceArray(28 until 32)
            colorsUsed = byteArray.sliceArray(32 until 36)
            colorsImportant = byteArray.sliceArray(36 until 40)
        }
    }

    override fun extract() {
        header.extract(byteArray.sliceArray(0 until 14))
        infoHeader.extract(byteArray.sliceArray(14 until 54))

    }
}