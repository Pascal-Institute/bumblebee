package bumblebee.extension

import bumblebee.core.Packet
import bumblebee.core.ImgPix
import bumblebee.type.FileType
import bumblebee.util.Converter.Companion.byteArrOf
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Operator.Companion.contains
import bumblebee.util.StringObject.AC_INDEX
import bumblebee.util.StringObject.COEFFICIENT
import bumblebee.util.StringObject.COMPONENT
import bumblebee.util.StringObject.DATA
import bumblebee.util.StringObject.DC_INDEX
import bumblebee.util.StringObject.ENDIAN
import bumblebee.util.StringObject.HEIGHT
import bumblebee.util.StringObject.HUFFMAN_TABLE
import bumblebee.util.StringObject.IDENTIFIER
import bumblebee.util.StringObject.NAME
import bumblebee.util.StringObject.NUM_OF_COMPONENT
import bumblebee.util.StringObject.ORDER
import bumblebee.util.StringObject.PRECISION
import bumblebee.util.StringObject.RESTART_INTERVAL
import bumblebee.util.StringObject.SIZE
import bumblebee.util.StringObject.TEXT
import bumblebee.util.StringObject.TRANSITION_METHOD
import bumblebee.util.StringObject.UNITS
import bumblebee.util.StringObject.VERSION
import bumblebee.util.StringObject.WIDTH
import bumblebee.util.StringObject.X_DENSITY
import bumblebee.util.StringObject.X_THUMBNAIL
import bumblebee.util.StringObject.Y_DENSITY
import bumblebee.util.StringObject.Y_THUMBNAIL
import java.lang.Math.*

class JPG(private var byteArray: ByteArray) : ImgPix(){
    private val segmentArray = mutableListOf<Packet>()
    private var encodedByteArray = byteArrayOf()
    init {
        extract()
    }
    override fun extract() {
        metaData.fileType = FileType.JPG

        val totalSize = byteArray.size
        var idx = 2
        var segmentDetector = byteArrayOf()

        while (idx < totalSize){
            segmentDetector = byteArray.cut(idx, idx + 2)

            val segment = Packet()

            if(MarkerType.contains(segmentDetector)){
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

                    MarkerType.DHT -> {
                        segment[NAME] = MarkerType.DHT.byteArray
                        segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                        val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                        segment[HUFFMAN_TABLE] = byteArray.cut(idx + 4, idx + endIdx)
                        idx+= endIdx
                    }

                    MarkerType.DQT -> {
                        segment[NAME] = MarkerType.DQT.byteArray
                        segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                        val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                        segment[DATA] = byteArray.cut(idx + 4, idx + endIdx)
                        idx+= endIdx
                    }

                    MarkerType.DRI -> {
                        segment[NAME] = MarkerType.DRI.byteArray
                        segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                        val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                        segment[RESTART_INTERVAL] = byteArray.cut(idx + 4, idx + endIdx)
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
                        segment[NAME] = MarkerType.SOS.byteArray
                        segment[SIZE] = byteArray.cut(idx + 2, idx + 4)
                        segment[NUM_OF_COMPONENT] = byteArray.cut(idx + 4, idx + 5)

                        val strings = arrayListOf<String>()
                        strings.add(ORDER)
                        strings.add(DC_INDEX)
                        strings.add(AC_INDEX)

                        var index = idx + 5

                        for(i : Int in 0 until segment[NUM_OF_COMPONENT].byteToInt()){
                            segment[strings[i]] = byteArray.cut(index, index + 2)
                            index += 2
                        }

                        val endIdx = segment[NAME].size + segment[SIZE].byteToInt()
                        idx+= endIdx
                    }

                    else->{}
                }
            }else{
                encodedByteArray = byteArray.cut(idx , byteArray.size)

                if(encodedByteArray.contains(MarkerType.EOI.byteArray)){
                    segment[NAME] = MarkerType.EOI.byteArray
                    encodedByteArray.cut(0, encodedByteArray.size - 2)
                }
                idx = byteArray.size
            }

            segmentArray.add(segment)
        }
    }

    private enum class MarkerType(val byteArray : ByteArray) {
        SOF(byteArrOf("FF", "C0")),
        DHT(byteArrOf("FF", "C4")),
        EOI(byteArrOf("FF", "D9")),
        SOS(byteArrOf("FF","DA")),
        DQT(byteArrOf("FF", "DB")),
        DRI(byteArrOf("FF","DD")),
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
            fun contains(byteArray : ByteArray): Boolean {
                var isContained = false
                MarkerType.values().forEach {
                     if(it.byteArray.contentEquals(byteArray)){
                         isContained = true
                     }
                }
                return isContained
            }
        }
    }

}