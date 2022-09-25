package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import bumblebee.type.TagType
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.hexToInt
import bumblebee.util.Converter.Companion.invert
import java.nio.ByteBuffer

//TIFF Revision 6.0 / Author : Aldus Corporation
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
        ifh.extract(imgFileType, ifdArray, byteArray)
    }

    //Image File Header
    private class IFH {
        lateinit var byteOrder : ByteArray
        lateinit var fortyTwo : ByteArray
        lateinit var firstIFDOffset : ByteArray

        fun extract(imgFileType: ImgFileType, ifdArray: ArrayList<IFD>, byteArray: ByteArray){
            byteOrder = byteArray.sliceArray(0 until 2)
            fortyTwo = byteArray.sliceArray(2 until 4)
            firstIFDOffset = byteArray.sliceArray(4 until 8)

            val startIdx = if(imgFileType.signature.contentEquals(ImgFileType.TIFF_LITTLE.signature)){
                hexToInt(byteToHex(invert(firstIFDOffset)))
            }else{
                hexToInt(byteToHex(firstIFDOffset))
            }

            println(startIdx - 8)
            ifdArray.add(IFD(imgFileType, byteArray.sliceArray(startIdx until byteArray.size)))
        }
    }

    //Image File Directory
    private class IFD(imgFileType: ImgFileType, byteArray: ByteArray){
        var numOfTags : ByteArray = byteArray.sliceArray(0 until 2) //2 Byte
        var tagArray = ArrayList<Tag>() //12 Byte * numOfTags
        var nextIFDOffset : ByteArray //4 Byte

        init {
            var value = hexToInt(byteToHex(invert(numOfTags)))
            for(i : Int in 0 until  value){
                tagArray.add(Tag(imgFileType, byteArray.sliceArray(2 + i*12 until 2 + (i+1) * 12)))
            }
            nextIFDOffset = byteArray.sliceArray(2 + 12 * value until 2 + 12 * value + 4)
        }
    }

    private class Tag(imgFileType: ImgFileType, byteArray: ByteArray) {

        var tagId : TagType = TagType.fromByteArray(
            if(imgFileType.signature.contentEquals(ImgFileType.TIFF_LITTLE.signature)){
                invert(byteArray.sliceArray(0 until 2))
            }else{
                byteArray.sliceArray(0 until 2)
            }
        )

        //data type
        /*
        * 1  = BYTE
        * 2  = ASCII
        * 3  = SHORT
        * 4  = LONG
        * 5  = RATIONAL
        * 6  = SBYTE
        * 7  = UNDEFINED
        * 8  = SSHORT
        * 9  = SLONG
        * 10 = SRATIONAL
        * 11 = FLOAD
        * 12 = DOUBLE
        * */

        var dataType : ByteArray = byteArray.sliceArray(2 until 4) //2 Byte
        var dataCount : ByteArray = byteArray.sliceArray(4 until 8) //4 Byte
        var dataOffset : ByteArray = byteArray.sliceArray(8 until 12) // 4Byte

        init {
            println(tagId.name)
            println(byteToHex(invert(dataType)))
            println(byteToHex(invert(dataCount)))
            println(byteToHex(invert(dataOffset)))
        }

    }

}