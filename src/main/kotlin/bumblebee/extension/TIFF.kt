package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ImgFileType
import bumblebee.util.Converter
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.hexToInt
import bumblebee.util.Converter.Companion.invert
import java.nio.ByteBuffer

//TIFF Revision 6.0 / Author : Aldus Corporation
class TIFF(private var byteArray: ByteArray) : ImgPix() {

    private var ifh = IFH()
    private var ifdArray = ArrayList<IFD>()
    private var compressionType = CompressionType.NONE
    companion object{

        private fun endianArray(imgFileType: ImgFileType, byteArray: ByteArray) : ByteArray{
            return if(imgFileType.signature.contentEquals(ImgFileType.TIFF_LITTLE.signature)){
                invert(byteArray)
            }else{
                byteArray
            }
        }

        private fun endianArray(imgFileType: ImgFileType, byteArray: ByteArray, dataType: DataType, dataCount : Int) : ByteArray{

            if(dataCount > 1){
               return endianArray(imgFileType, byteArray)
            }

           return when(dataType){
                DataType.SHORT -> {
                    endianArray(imgFileType, byteArray.sliceArray(0 until 2)).plus(byteArray.sliceArray(2 until byteArray.size))
                }
                else->{
                    endianArray(imgFileType, byteArray)
                }
            }
        }

        private fun endianArray(imgFileType : ImgFileType ,byteArray: ByteArray, startIdx : Int, endIdx : Int) : ByteArray{
            return if(imgFileType.signature.contentEquals(ImgFileType.TIFF_LITTLE.signature)){
                invert(byteArray.sliceArray(startIdx until endIdx))
            }else{
                byteArray.sliceArray(startIdx until endIdx)
            }
        }

    }

    init {
        imgFileType = if (byteArray.sliceArray(0 until 2).contentEquals(ImgFileType.TIFF_LITTLE.signature)){
            ImgFileType.TIFF_LITTLE
        }else{
            ImgFileType.TIFF_BIG
        }
        extract()
    }

    override fun extract() {
        ifh.extract(imgFileType, ifdArray, byteArray)
        ifdArray.forEach {
            it.tagArray.forEach {tag->
                when(tag.tagId){
                    TagType.IMAGE_WIDTH -> metaData.width = byteToInt(tag.dataOffset.sliceArray(0 until 2))
                    TagType.IMAGE_LENGTH -> metaData.height = byteToInt(tag.dataOffset.sliceArray(0 until 2))
                    TagType.SAMPLES_PER_PIXEL -> bytesPerPixel = byteToInt(tag.dataOffset.sliceArray(0 until 2))
                    TagType.COMPRESSION -> compressionType = CompressionType.fromInt(byteToInt(tag.dataOffset.sliceArray(0 until 2)))
                    else -> {}
                }
            }

            it.tagArray.forEach {tag->
                if(tag.tagId == TagType.STRIP_OFFSETS) {
                        extractRasterImage(tag)
                }
            }
        }
    }

    private fun extractRasterImage(tag: Tag) {

        val stripCount = tag.dataCount
        val firstStripOffset = byteToInt(tag.dataOffset)
        val lastStripOffset = firstStripOffset + (4 * stripCount)
        this.pixelBufferArray = ByteBuffer.allocate(width * height * bytesPerPixel)
        val startIdx = byteToInt(endianArray(imgFileType, byteArray.sliceArray(firstStripOffset until firstStripOffset + 4)))
        //width * height / stripCount is needed because of last stripOffset is only single.
        val endIdx = byteToInt(endianArray(imgFileType, byteArray.sliceArray(lastStripOffset - 4 until lastStripOffset))) + (width * (height / stripCount))

        when(compressionType){
            CompressionType.LZW->{

            }
            else->{
                pixelBufferArray.put(byteArray.sliceArray(startIdx until endIdx))
            }
        }
    }

    //Image File Header
    private class IFH {
        lateinit var byteOrder : ByteArray
        lateinit var fortyTwo : ByteArray
        lateinit var ifdOffset : ByteArray

        fun extract(imgFileType: ImgFileType, ifdArray: ArrayList<IFD>, byteArray: ByteArray){
            byteOrder = byteArray.sliceArray(0 until 2)
            fortyTwo = byteArray.sliceArray(2 until 4)
            ifdOffset = byteArray.sliceArray(4 until 8)

            val startIdx = if(imgFileType.signature.contentEquals(ImgFileType.TIFF_LITTLE.signature)){
                hexToInt(byteToHex(invert(ifdOffset)))
            }else{
                hexToInt(byteToHex(ifdOffset))
            }

            do{
                val ifd = IFD(imgFileType, byteArray.sliceArray(startIdx until byteArray.size))
                ifdArray.add(ifd)

            }while(!ifd.nextIFDOffset.contentEquals(byteArrayOf(0, 0, 0, 0)))
        }
    }

    //Image File Directory
    private class IFD(imgFileType: ImgFileType, byteArray: ByteArray){
        var numOfTags : ByteArray = endianArray(imgFileType, byteArray, 0, 2)
        var tagArray = ArrayList<Tag>() //12 Byte * numOfTags
        var nextIFDOffset : ByteArray //4 Byte

        init {
            val value = hexToInt(byteToHex(numOfTags))
            for(i : Int in 0 until  value){
                var tag = Tag(imgFileType, byteArray.sliceArray(2 + i*12 until 2 + (i+1) * 12))
                tagArray.add(tag)
            }
            nextIFDOffset = endianArray(imgFileType, byteArray, 2 + 12 * value, 2 + 12 * value + 4)
        }
    }

    private class Tag(imgFileType: ImgFileType, byteArray: ByteArray) {

        var tagId : TagType = TagType.fromByteArray(endianArray(imgFileType, byteArray, 0, 2))
        var dataType : DataType = DataType.fromByteArray(endianArray(imgFileType,byteArray.sliceArray(2 until 4))) //2 Byte
        var dataCount : Int = byteToInt(endianArray(imgFileType,byteArray.sliceArray(4 until 8))) //4 Byte
        var dataOffset : ByteArray = endianArray(imgFileType,byteArray.sliceArray(8 until 12), dataType, dataCount) // 4Byte
    }

    private class LZW{
        companion object{
           fun decode(byteArray: ByteArray){

           }
        }
    }

    private enum class TagType (val byteArray : ByteArray) {

        NEW_SUBFILE_TYPE(Converter.intToByteArray(254, 2)),
        SUBFILE_TYPE(Converter.intToByteArray(255, 2)),
        IMAGE_WIDTH(Converter.intToByteArray(256, 2)),
        IMAGE_LENGTH(Converter.intToByteArray(257, 2)),
        BITS_PER_SAMPLE(Converter.intToByteArray(258, 2)),
        COMPRESSION(Converter.intToByteArray(259, 2)),
        PHOTOMETRIC_INTERPRETATION(Converter.intToByteArray(262, 2)),
        THRESHOLDING(Converter.intToByteArray(263, 2)),
        CELL_WIDTH(Converter.intToByteArray(264, 2)),
        CELL_LENGTH(Converter.intToByteArray(265, 2)),
        FILL_ORDER(Converter.intToByteArray(266, 2)),
        DOCUMENT_NAME(Converter.intToByteArray(269, 2)),
        IMAGE_DESCRIPTION(Converter.intToByteArray(270, 2)),
        MAKE(Converter.intToByteArray(271, 2)),
        MODEL(Converter.intToByteArray(272, 2)),
        STRIP_OFFSETS(Converter.intToByteArray(273, 2)),
        ORIENTATION(Converter.intToByteArray(274, 2)),
        SAMPLES_PER_PIXEL(Converter.intToByteArray(277, 2)),
        ROWS_PER_STRIP(Converter.intToByteArray(278, 2)),
        STRIP_BYTE_COUNTS(Converter.intToByteArray(279, 2)),
        MIN_SAMPLE_VALUE(Converter.intToByteArray(280, 2)),
        MAX_SAMPLE_VALUE(Converter.intToByteArray(281, 2)),
        X_RESOLUTION(Converter.intToByteArray(282, 2)),
        Y_RESOLUTION(Converter.intToByteArray(283, 2)),
        PLANAR_CONFIGURATION(Converter.intToByteArray(284, 2)),
        PAGE_NAME(Converter.intToByteArray(285, 2)),
        X_POSITION(Converter.intToByteArray(286, 2)),
        Y_POSITION(Converter.intToByteArray(287, 2)),
        FREE_OFFSETS(Converter.intToByteArray(288, 2)),
        FREE_BYTE_COUNTS(Converter.intToByteArray(289, 2)),
        GRAY_RESPONSE_UNIT(Converter.intToByteArray(290, 2)),
        GRAY_RESPONSE_CURVE(Converter.intToByteArray(291, 2)),
        T4_OPTIONS(Converter.intToByteArray(292, 2)),
        T6_OPTIONS(Converter.intToByteArray(293, 2)),
        RESOLUTION_UNIT(Converter.intToByteArray(296, 2)),
        PAGE_NUMBER(Converter.intToByteArray(297, 2)),
        TRANSFER_FUNCTION(Converter.intToByteArray(301, 2)),
        SOFTWARE(Converter.intToByteArray(305, 2)),
        DATE_TIME(Converter.intToByteArray(306, 2)),
        ARTIST(Converter.intToByteArray(315, 2)),
        HOST_COMPUTER(Converter.intToByteArray(316, 2)),
        PREDICTOR(Converter.intToByteArray(317, 2)),
        WHITE_POINT(Converter.intToByteArray(318, 2)),
        PRIMARY_CHROMATICITIES(Converter.intToByteArray(319, 2)),
        COLOR_MAP(Converter.intToByteArray(320, 2)),
        HALF_ONE_HINTS(Converter.intToByteArray(321, 2)),
        TILE_WIDTH(Converter.intToByteArray(322, 2)),
        TILE_LENGTH(Converter.intToByteArray(323, 2)),
        TILE_OFFSETS(Converter.intToByteArray(324, 2)),
        TILE_BYTE_COUNTS(Converter.intToByteArray(325, 2)),
        INK_SET(Converter.intToByteArray(332, 2)),
        INK_NAMES(Converter.intToByteArray(333, 2)),
        NUMBER_OF_INKS(Converter.intToByteArray(334, 2)),
        DOT_RANGE(Converter.intToByteArray(336, 2)),
        TARGET_PRINTER(Converter.intToByteArray(337, 2)),
        EXTRA_SAMPLES(Converter.intToByteArray(338, 2)),
        SAMPLE_FORMAT(Converter.intToByteArray(339, 2)),
        S_MIN_SAMPLE_VALUE(Converter.intToByteArray(340, 2)),
        S_MAX_SAMPLE_VALUE(Converter.intToByteArray(341, 2)),
        TRANSFER_RANGE(Converter.intToByteArray(342, 2)),
        JPEG_PROC(Converter.intToByteArray(512, 2)),
        JPEG_INTERCHANGE_FORMAT(Converter.intToByteArray(513, 2)),
        JPEG_INTERCHANGE_FORMAT_LENGTH(Converter.intToByteArray(514, 2)),
        JPEG_RESTART_INTERVAL(Converter.intToByteArray(515, 2)),
        JPEG_LOSSLESS_PREDICTORS(Converter.intToByteArray(517, 2)),
        JPEG_POINT_TRANSFORMS(Converter.intToByteArray(518, 2)),
        JPEG_Q_TABLES(Converter.intToByteArray(519, 2)),
        JPEG_DC_TABLES(Converter.intToByteArray(520, 2)),
        JPEG_AC_TABLES(Converter.intToByteArray(521, 2)),
        Y_CB_CR_COEFFICIENTS(Converter.intToByteArray(529, 2)),
        Y_CB_CR_SUB_SAMPLING(Converter.intToByteArray(530, 2)),
        REFERENCE_BLACK_WHITE(Converter.intToByteArray(532, 2));
        companion object {
            fun fromByteArray(byteArray : ByteArray) = TagType.values().first { it.byteArray.contentEquals(byteArray) }
        }
    }

    private enum class DataType (val byteArray: ByteArray){

        BYTE(byteArrayOf(0,1)),
        ASCII(byteArrayOf(0,2)),
        SHORT(byteArrayOf(0,3)),
        LONG(byteArrayOf(0,4)),
        RATIONAL(byteArrayOf(0,5)),
        SBYTE(byteArrayOf(0,6)),
        UNDEFINED(byteArrayOf(0,7)),
        SSHORT(byteArrayOf(0,8)),
        SLONG(byteArrayOf(0,9)),
        SRATIONAL(byteArrayOf(0,10)),
        FLOAD(byteArrayOf(0,11)),
        DOUBLE(byteArrayOf(0,12));
        companion object {
            fun fromByteArray(byteArray : ByteArray) = DataType.values().first { it.byteArray.contentEquals(byteArray) }
        }
    }

    private enum class CompressionType (val integer : Int){
        NONE(1),
        CCITTRLE(2),
        CCITTFAX3(3),
        CCITTFAX4(4),
        LZW(5),
        OJPEG(6),
        JPEG(7),
        NEXT(32766),
        CCITTRLEW(32771),
        PACKBITS(32773),
        THUNDERSCAN(32809),
        IT8CTPAD(32895),
        IT8LW(32896),
        IT8MP(32897),
        IT8BL(32898),
        PIXARFILM(32908),
        PIXARLOG(32909),
        DEFLATE(32946),
        ADOBE_DEFLATE(8),
        DCS(32947),
        JBIG(34661),
        SGILOG(34676),
        SGILOG24(34677),
        JP2000(34712);

        companion object {
            fun fromInt(integer : Int) = CompressionType.values().first { it.integer == integer }
        }
    }
}