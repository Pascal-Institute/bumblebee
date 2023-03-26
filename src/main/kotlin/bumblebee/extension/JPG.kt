package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Converter.Companion.toHex

class JPG(private var byteArray: ByteArray) : ImgPix(){
    var startIndex = 0
    lateinit var soi : ByteArray
    private lateinit var app0 : APP0
    private lateinit var app1 : APP1

    init {
        extract()
    }

    override fun extract() {

        byteArray.forEachIndexed { idx, it ->
            if(it.toHex() + byteArray.get(idx + 1).toHex() == "FFE0"){
                println()
            }
            print(it.toHex())
        }

        soi = byteArray.cut(startIndex, 2)
        startIndex += 2
        app0 = APP0(byteArray.cut(startIndex, byteArray.size))
        startIndex += app0.endIndex
        app1 = APP1(byteArray.cut(startIndex, byteArray.size))
        startIndex += app1.endIndex
        var bytes = byteArray.cut(startIndex, byteArray.size)
    }

    private class APP0(byteArray: ByteArray) {

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
            app0Marker = byteArray.cut(0, 2)
            length = byteArray.cut(2, 4)
            identifier = byteArray.cut(4, 9)
            version = byteArray.cut(9, 11)
            densityUnits = byteArray.cut(11, 12)
            xDensity = byteArray.cut(12, 14)
            yDensity = byteArray.cut(14, 16)
            xThumbnail = byteArray.cut(16, 17)
            yThumbnail = byteArray.cut(17, 18)

            n = (length.byteToInt() - 16)/3
            endIndex = 18 + 3 * n
            thumbnail = byteArray.cut(18, endIndex)
        }
    }

    private class APP1(byteArray: ByteArray) {

        var endIndex = 0
        private var n = 0
        private var app1Marker : ByteArray
        private var length : ByteArray
        init {
            app1Marker = byteArray.cut(0, 2)
            length = byteArray.cut(2, 4)
            endIndex = 2 + length.byteToInt()
        }
    }

}