package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.util.Converter.Companion.byteToInt

class JPG(private var byteArray: ByteArray) : ImgPix(){
    var startIndex = 0
    lateinit var soi : ByteArray
    private lateinit var app0 : APP0
    private lateinit var app1 : APP1

    init {
        extract()
    }

    override fun extract() {
        soi = byteArray.sliceArray(startIndex until 2)
        startIndex += 2
        app0 = APP0(byteArray.sliceArray(startIndex until byteArray.size))
        startIndex += app0.endIndex
        app1 = APP1(byteArray.sliceArray(startIndex until byteArray.size))
        startIndex += app1.endIndex
        var bytes = byteArray.sliceArray(startIndex until byteArray.size)
        println(app0.size())
        println(app1.size())
    }

    private class APP0(byteArray: ByteArray) : Segment(byteArray) {

        var endIndex = 0
        private var n = 0
        private var app0Marker : ByteArray
        private var length : ByteArray
        private var identifier : ByteArray
        private var version : ByteArray
        private var densityUnits : ByteArray
        private var xDensity : ByteArray
        private var yDensity : ByteArray
        private var xThumbnail : ByteArray
        private var yThumbnail : ByteArray
        private var thumbnail : ByteArray

        init {
            app0Marker = byteArray.sliceArray(0 until 2)
            length = byteArray.sliceArray(2 until 4)
            identifier = byteArray.sliceArray(4 until 9)
            version = byteArray.sliceArray(9 until 11)
            densityUnits = byteArray.sliceArray(11 until 12)
            xDensity = byteArray.sliceArray(12 until 14)
            yDensity = byteArray.sliceArray(14 until 16)
            xThumbnail = byteArray.sliceArray(16 until 17)
            yThumbnail = byteArray.sliceArray(17 until 18)

            n = (byteToInt(length) - 16)/3
            endIndex = 18 + 3 * n
            thumbnail = byteArray.sliceArray(18 until endIndex)
        }
    }

    private class APP1(byteArray: ByteArray) : Segment(byteArray) {

        var endIndex = 0
        private var n = 0
        private var app1Marker : ByteArray
        private var length : ByteArray
        init {
            app1Marker = byteArray.sliceArray(0 until 2)
            length = byteArray.sliceArray(2 until 4)
            endIndex = 2 + byteToInt(length)
        }
    }

    open class Segment(var byteArray: ByteArray) {
        fun size() : Int{
            return byteArray.size
        }
    }
}