package bumblebee.extension

import bumblebee.core.ImgHeader
import bumblebee.core.ImgPix
import bumblebee.util.Converter
import bumblebee.util.Converter.Companion.byteArrOf
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Converter.Companion.toHex
import bumblebee.util.StringObj
import bumblebee.util.StringObj.COEFFICIENT
import bumblebee.util.StringObj.COMPONENT
import bumblebee.util.StringObj.DATA
import bumblebee.util.StringObj.ENDIAN
import bumblebee.util.StringObj.HEIGHT
import bumblebee.util.StringObj.IDENTIFIER
import bumblebee.util.StringObj.NAME
import bumblebee.util.StringObj.PRECISION
import bumblebee.util.StringObj.SIZE
import bumblebee.util.StringObj.TEXT
import bumblebee.util.StringObj.TRANSITION_METHOD
import bumblebee.util.StringObj.UNITS
import bumblebee.util.StringObj.VERSION
import bumblebee.util.StringObj.WIDTH
import bumblebee.util.StringObj.X_DENSITY
import bumblebee.util.StringObj.X_THUMBNAIL
import bumblebee.util.StringObj.Y_DENSITY
import bumblebee.util.StringObj.Y_THUMBNAIL

class JPG(private var byteArray: ByteArray) : ImgPix(){
    private val segmentArray = ArrayList<ImgHeader>()

    var startIndex = 0
    lateinit var soi : ByteArray
    private lateinit var app0 : APP0
    private lateinit var app1 : APP1

    init {
        extract()
    }

    override fun extract() {

        val totalSize = byteArray.size
        var idx = 2
        var segmentDetector = byteArrayOf()

        while (idx < totalSize){
            segmentDetector = byteArray.cut(idx, idx + 2)

            val segment = ImgHeader()

            when(MarkerType.fromByteArray(segmentDetector)){

                MarkerType.APP0->{
                    segment[NAME] = MarkerType.APP0.byteArray
                    segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                    segment[IDENTIFIER] = byteArray.cut(idx + 4, idx + 9)
                    segment[VERSION] = byteArray.cut(idx + 9, idx + 11)
                    segment[UNITS] = byteArray.cut(idx + 11, idx + 12)
                    segment[X_DENSITY] = byteArray.cut(idx + 12, idx + 14)
                    segment[Y_DENSITY] = byteArray.cut(idx + 14, idx + 16)
                    segment[X_THUMBNAIL] = byteArray.cut(idx + 16, idx + 17)
                    segment[Y_THUMBNAIL] = byteArray.cut(idx + 17, idx + 18)
                    val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                    if(segment[SIZE].byteToInt() - 16 > 0){
                        segment[DATA] = byteArray.cut(idx + 18 , idx + endIdx)
                    }
                    idx += endIdx
                }

                MarkerType.APP1->{
                    segment[NAME] = MarkerType.APP1.byteArray
                    segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                    segment[TEXT] = byteArray.cut(idx + 4, idx + 10)
                    segment[ENDIAN] = byteArray.cut(idx + 10, idx + 12)
                    val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                    segment[DATA] = byteArray.cut(idx + 12, idx + endIdx)
                    idx += endIdx
                }

                MarkerType.APP2->{
                    segment[NAME] = MarkerType.APP1.byteArray
                    segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                    segment[TEXT] = byteArray.cut(idx + 4, idx + 10)
                    val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                    segment[DATA] = byteArray.cut(idx + 10, idx + endIdx)
                    idx += endIdx
                }

                MarkerType.APP12->{
                    segment[NAME] = MarkerType.APP12.byteArray
                    segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                    segment[TEXT] = byteArray.cut(idx + 4, idx + 9)
                    segment[IDENTIFIER] = byteArray.cut(idx + 9, idx + 10)
                    val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                    segment[DATA] = byteArray.cut(idx + 10, idx + endIdx)
                    idx += endIdx
                }

                MarkerType.APP13->{
                    segment[NAME] = MarkerType.APP13.byteArray
                    segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                    segment[IDENTIFIER] = byteArray.cut(idx + 4, idx + 9)
                    val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                    segment[DATA] = byteArray.cut(idx + 9, idx + endIdx)
                    idx += endIdx
                }

                MarkerType.APP14->{
                    segment[NAME] = MarkerType.APP14.byteArray
                    segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                    segment[TEXT] = byteArray.cut(idx + 4, idx + 9)
                    segment[TRANSITION_METHOD] = byteArray.cut(idx + 9, idx + 10)
                    segment[COEFFICIENT] = byteArray.cut(idx + 12, idx + 14)
                    segment["pointBlack"] = byteArray.cut(idx + 14, idx + 15)
                    segment["pointWhite"] = byteArray.cut(idx + 15, idx + 16)
                    segment["specialMethod"] = byteArray.cut(idx + 16, idx + 17)
                    val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                    idx+= endIdx
                }

                MarkerType.DQT -> {
                    segment[NAME] = MarkerType.DQT.byteArray
                    segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                    val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                    segment[DATA] = byteArray.cut(idx + 4, idx + endIdx)
                    idx+= endIdx
                }

                MarkerType.SOF -> {
                    segment[NAME] = MarkerType.SOF.byteArray
                    segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                    segment[PRECISION] = byteArray.cut(idx + 4, idx + 5)
                    segment[HEIGHT] = byteArray.cut(idx + 5, idx + 7)
                    segment[WIDTH] = byteArray.cut(idx + 7, idx + 9)
                    val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                    segment[COMPONENT] = byteArray.cut(idx + 9, idx + endIdx)
                    idx+= endIdx
                }

                MarkerType.SOS -> {
                    println("bumblebee")
                }

                else -> {
                    idx++
                }

            }
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



    private enum class MarkerType(val byteArray : ByteArray) {
        SOF(byteArrOf("FF", "C0")),
        EOI(byteArrOf("FF", "D9")),
        SOS(byteArrOf("FF","DA")),
        DQT(byteArrOf("FF", "DB")),
        APP0(byteArrOf("FF", "E0")),
        APP1(byteArrOf("FF", "E1")),
        APP2(byteArrOf("FF", "E2")),
        APP3(byteArrOf("FF", "E3")),
        APP4(byteArrOf("FF", "E4")),
        APP5(byteArrOf("FF", "E5")),
        APP6(byteArrOf("FF", "E6")),
        APP7(byteArrOf("FF", "E7")),
        APP8(byteArrOf("FF", "E8")),
        APP9(byteArrOf("FF", "E9")),
        APP10(byteArrOf("FF", "EA")),
        APP11(byteArrOf("FF", "EB")),
        APP12(byteArrOf("FF", "EC")),
        APP13(byteArrOf("FF", "ED")),
        APP14(byteArrOf("FF", "EE")),
        APP15(byteArrOf("FF", "EF"));
        companion object {
            fun fromByteArray(byteArray: ByteArray) = MarkerType.values().first { it.byteArray.contentEquals(byteArray) }
        }
    }

}