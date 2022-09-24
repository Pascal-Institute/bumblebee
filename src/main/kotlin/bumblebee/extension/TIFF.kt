package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.hexToInt
import bumblebee.util.Converter.Companion.invert
import java.nio.ByteBuffer

class TIFF(private var byteArray: ByteArray) : ImgPix() {

    private var ifh = IFH()
    private var ifdArray = ArrayList<IFD>()
    init {
        imgFileType = if (byteArray.sliceArray(0 until 2).contentEquals(ImgFileType.TIFF_LITTLE.signature)){
            ImgFileType.TIFF_LITTLE
        }else{
            ImgFileType.TIFF_BIG
        }

        metaData.colorType = ColorType.TRUE_COLOR
        this.pixelBufferArray = ByteBuffer.allocate(0)

        extract()
    }

    override fun extract() {
        ifh.extract(ifdArray, byteArray)
    }

    //Image File Header
    private class IFH{
        lateinit var byteOrder : ByteArray
        lateinit var fortyTwo : ByteArray
        lateinit var firstIFDOffset : ByteArray

        fun extract(ifdArray: ArrayList<IFD>, byteArray: ByteArray){
            byteOrder = byteArray.sliceArray(0 until 2)
            fortyTwo = byteArray.sliceArray(2 until 4)
            firstIFDOffset = byteArray.sliceArray(4 until 8)

            ifdArray.add(IFD())
            ifdArray.get(0).extract(byteArray.sliceArray(
                hexToInt(byteToHex(invert(firstIFDOffset)))
                        until
                    byteArray.size))
        }
    }

    //Image File Directory
    class IFD{
        lateinit var numOfTags : ByteArray
        var tagArray = ArrayList<Tag>()
        lateinit var nextIFDOffset : ByteArray

        lateinit var imageData : ByteArray
        fun extract(byteArray: ByteArray) {

            numOfTags = byteArray.sliceArray(0 until 2)

            for(i : Int in 0 until  hexToInt(byteToHex(invert(numOfTags)))){
                tagArray.add(Tag(byteArray.sliceArray(i*12 until (i+1) * 12)))
            }
            nextIFDOffset = byteArray.sliceArray(0 until 2)
            imageData = ByteArray(0)
        }
    }

    class Tag(private var byteArray: ByteArray) {

        lateinit var tagId : ByteArray //2 Byte
        lateinit var dataType : ByteArray //2 Byte
        lateinit var dataCount : ByteArray //4 Byte
        lateinit var dataOffset : ByteArray // 4Byte

        init {
            extract()
        }

        fun extract(){
            tagId = byteArray.sliceArray(0 until 2)
            dataType = byteArray.sliceArray(2 until 4)
            dataCount = byteArray.sliceArray(4 until 8)
            dataOffset = byteArray.sliceArray(8 until 12)
        }

    }

}