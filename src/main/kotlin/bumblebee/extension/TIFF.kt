package bumblebee.extension

import bumblebee.core.Packet
import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.FileType
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.util.Converter.Companion.hexToInt
import bumblebee.util.Converter.Companion.intToByteArray
import bumblebee.util.Operator.Companion.invert
import bumblebee.util.Converter.Companion.toHex
import bumblebee.util.StringObject.BYTE_ORDER
import bumblebee.util.StringObject.DATA
import bumblebee.util.StringObject.DATA_COUNT
import bumblebee.util.StringObject.DATA_OFFSET
import bumblebee.util.StringObject.DATA_TYPE
import bumblebee.util.StringObject.FORTY_TWO
import bumblebee.util.StringObject.IFD_OFFSET
import bumblebee.util.StringObject.TAG_ID
import delta.Cipher
import java.nio.ByteBuffer

//TIFF Revision 6.0 / Author : Aldus Corporation
class TIFF(private var byteArray: ByteArray) : ImgPix() {
    private var ifh = IFH()
    private var ifdArray = ArrayList<IFD>()
    private var compressionType = CompressionType.NONE
    private var rowsPerStrip = 0
    private var stripByteCounts = 0
    companion object {
        private var isLittle = false
        fun ByteArray.toEndian() : ByteArray {
            return if(isLittle){
                this.invert()
            }else{
                this
            }
        }

        fun ByteArray.toEndian(dataType: DataType) : ByteArray {
            return this.cut(0, dataType.byteSize).toEndian()
        }
    }
    init {
        metaData.fileType = if (byteArray.cut(0, 2).contentEquals(FileType.TIFF_LITTLE.signature)){
            isLittle = true
            FileType.TIFF_LITTLE
        }else{
            isLittle = false
            FileType.TIFF_BIG
        }
        extract()
    }

    override fun extract() {

        ifh.extract(fileType, ifdArray, byteArray)

        //don't need to make endianArray from here
        ifdArray.forEach {
            it.tagArray.forEach {tag->
                when(TagType.fromByteArray(tag[TAG_ID])){
                    TagType.IMAGE_WIDTH -> metaData.width = tag[DATA].byteToInt()
                    TagType.IMAGE_LENGTH -> metaData.height = tag[DATA].byteToInt()
                    TagType.SAMPLES_PER_PIXEL -> {
                        if(bytesPerPixel == 3){
                            metaData.colorType = ColorType.TRUE_COLOR
                        }else{
                            metaData.colorType = ColorType.GRAY_SCALE
                        }
                    }
                    TagType.COLOR_MAP -> {
                        metaData.colorType = ColorType.TRUE_COLOR
                    }
                    TagType.COMPRESSION -> {
                        compressionType = CompressionType.fromInt(tag[DATA].byteToInt())
                    }
                    TagType.ROWS_PER_STRIP -> {
                        rowsPerStrip = tag[DATA].byteToInt()
                    }
                    TagType.STRIP_BYTE_COUNTS -> {
                        stripByteCounts = tag[DATA].byteToInt()
                    }
                    TagType.BITS_PER_SAMPLE -> {}
                    else -> {}
                }
            }

            it.tagArray.forEach {tag->
                if(TagType.fromByteArray(tag[TAG_ID]) == TagType.STRIP_OFFSETS) {
                        extractRasterImage(tag)
                }else{
                    println(TagType.fromByteArray(tag[TAG_ID]))
                }
            }
        }
    }

    private fun extractRasterImage(tag: Tag) {

        val stripCount = tag[DATA_COUNT].byteToInt()
        val firstStripOffset = tag[DATA].byteToInt()
        val lastStripOffset = firstStripOffset + (4 * stripCount)
        var startIdx = byteArray.cut(firstStripOffset, firstStripOffset + 4).toEndian().byteToInt()
        val endIdx = byteArray.cut(lastStripOffset - 4, lastStripOffset).toEndian().byteToInt() + byteArray.cut(stripByteCounts + (4 * stripCount) - 4, stripByteCounts + 4 * stripCount).toEndian().byteToInt()

        this.pixelByteBuffer = ByteBuffer.allocate(width * height * bytesPerPixel)
        when(compressionType){
            CompressionType.LZW -> {

                for(i : Int in 0 until stripCount){

                    var counts =  byteArray.cut(stripByteCounts + (4 * i), stripByteCounts + (4 * i) + 4).toEndian().byteToInt()
                    //Do LZW
                    pixelByteBuffer.put(lzwDecode(byteArray.cut(startIdx, startIdx + counts)))

                    startIdx += counts
                }
            }

            CompressionType.PACKBITS -> {

                for(i : Int in 0 until stripCount){
                    var counts = byteArray.cut(stripByteCounts + (4 * i), stripByteCounts + (4 * i) + 4).toEndian().byteToInt()
                    var result = Cipher.decodePackBits(byteArray.cut(startIdx, startIdx + counts))
                    pixelByteBuffer.put(result)
                    println(result.size)
                    startIdx += counts
                }

            }

            else->{
                pixelByteBuffer.put(byteArray.cut(startIdx, endIdx))
            }
        }
    }

    private fun lzwDecode(encodedData: ByteArray): ByteArray {
        return encodedData
    }


//Image File Header
private class IFH  : Packet(){
    fun extract(fileType: FileType, ifdArray: ArrayList<IFD>, byteArray: ByteArray){
        this[BYTE_ORDER] = byteArray.cut(0, 2)
        this[FORTY_TWO] = byteArray.cut(2, 4)
        this[IFD_OFFSET] = byteArray.cut(4, 8)

        val startIdx = if(fileType.signature.contentEquals(FileType.TIFF_LITTLE.signature)){
            this[IFD_OFFSET].invert().byteToInt()
        }else{
            this[IFD_OFFSET].byteToInt()
        }

        do{
            val ifd = IFD(byteArray.cut(startIdx, byteArray.size))
            ifdArray.add(ifd)
        }while(!ifd.nextIFDOffset.contentEquals(byteArrayOf(0, 0, 0, 0)))
    }
}

//Image File Directory
private class IFD(byteArray: ByteArray){
    var numOfTags : ByteArray = byteArray.cut(0, 2).toEndian()
    var tagArray = ArrayList<Tag>() //12 Byte * numOfTags
    var nextIFDOffset : ByteArray //4 Byte

    init {
        val value = numOfTags.toHex().hexToInt()
        for(i : Int in 0 until  value){
            var tag = Tag(byteArray.cut(2 + i*12, 2 + (i+1) * 12))
            tagArray.add(tag)
        }
        nextIFDOffset = byteArray.cut(2 + 12 * value, 2 + 12 * value + 4).toEndian()
    }
}

private class Tag(byteArray: ByteArray) : Packet() {
    init {
        this[TAG_ID] = byteArray.cut(0, 2).toEndian()
        this[DATA_TYPE] = byteArray.cut(2, 4).toEndian()
        this[DATA_COUNT] =  byteArray.cut(4, 8).toEndian()
        this[DATA_OFFSET] = byteArray.cut(8, 12).toEndian()
        this[DATA] = byteArray.cut(8, 12).toEndian(DataType.fromByteArray(this[DATA_TYPE]))
    }
}
private enum class TagType (val byteArray : ByteArray) {

    NEW_SUBFILE_TYPE(intToByteArray(254, 2)),
    SUBFILE_TYPE(intToByteArray(255, 2)),
    IMAGE_WIDTH(intToByteArray(256, 2)),
    IMAGE_LENGTH(intToByteArray(257, 2)),
    BITS_PER_SAMPLE(intToByteArray(258, 2)),
    COMPRESSION(intToByteArray(259, 2)),
    PHOTOMETRIC_INTERPRETATION(intToByteArray(262, 2)),
    THRESHOLDING(intToByteArray(263, 2)),
    CELL_WIDTH(intToByteArray(264, 2)),
    CELL_LENGTH(intToByteArray(265, 2)),
    FILL_ORDER(intToByteArray(266, 2)),
    DOCUMENT_NAME(intToByteArray(269, 2)),
    IMAGE_DESCRIPTION(intToByteArray(270, 2)),
    MAKE(intToByteArray(271, 2)),
    MODEL(intToByteArray(272, 2)),
    STRIP_OFFSETS(intToByteArray(273, 2)),
    ORIENTATION(intToByteArray(274, 2)),
    SAMPLES_PER_PIXEL(intToByteArray(277, 2)),
    ROWS_PER_STRIP(intToByteArray(278, 2)),
    STRIP_BYTE_COUNTS(intToByteArray(279, 2)),
    MIN_SAMPLE_VALUE(intToByteArray(280, 2)),
    MAX_SAMPLE_VALUE(intToByteArray(281, 2)),
    X_RESOLUTION(intToByteArray(282, 2)),
    Y_RESOLUTION(intToByteArray(283, 2)),
    PLANAR_CONFIGURATION(intToByteArray(284, 2)),
    PAGE_NAME(intToByteArray(285, 2)),
    X_POSITION(intToByteArray(286, 2)),
    Y_POSITION(intToByteArray(287, 2)),
    FREE_OFFSETS(intToByteArray(288, 2)),
    FREE_BYTE_COUNTS(intToByteArray(289, 2)),
    GRAY_RESPONSE_UNIT(intToByteArray(290, 2)),
    GRAY_RESPONSE_CURVE(intToByteArray(291, 2)),
    T4_OPTIONS(intToByteArray(292, 2)),
    T6_OPTIONS(intToByteArray(293, 2)),
    RESOLUTION_UNIT(intToByteArray(296, 2)),
    PAGE_NUMBER(intToByteArray(297, 2)),
    TRANSFER_FUNCTION(intToByteArray(301, 2)),
    SOFTWARE(intToByteArray(305, 2)),
    DATE_TIME(intToByteArray(306, 2)),
    ARTIST(intToByteArray(315, 2)),
    HOST_COMPUTER(intToByteArray(316, 2)),
    PREDICTOR(intToByteArray(317, 2)),
    WHITE_POINT(intToByteArray(318, 2)),
    PRIMARY_CHROMATICITIES(intToByteArray(319, 2)),
    COLOR_MAP(intToByteArray(320, 2)),
    HALF_ONE_HINTS(intToByteArray(321, 2)),
    TILE_WIDTH(intToByteArray(322, 2)),
    TILE_LENGTH(intToByteArray(323, 2)),
    TILE_OFFSETS(intToByteArray(324, 2)),
    TILE_BYTE_COUNTS(intToByteArray(325, 2)),
    INK_SET(intToByteArray(332, 2)),
    INK_NAMES(intToByteArray(333, 2)),
    NUMBER_OF_INKS(intToByteArray(334, 2)),
    DOT_RANGE(intToByteArray(336, 2)),
    TARGET_PRINTER(intToByteArray(337, 2)),
    EXTRA_SAMPLES(intToByteArray(338, 2)),
    SAMPLE_FORMAT(intToByteArray(339, 2)),
    S_MIN_SAMPLE_VALUE(intToByteArray(340, 2)),
    S_MAX_SAMPLE_VALUE(intToByteArray(341, 2)),
    TRANSFER_RANGE(intToByteArray(342, 2)),
    JPEG_PROC(intToByteArray(512, 2)),
    JPEG_INTERCHANGE_FORMAT(intToByteArray(513, 2)),
    JPEG_INTERCHANGE_FORMAT_LENGTH(intToByteArray(514, 2)),
    JPEG_RESTART_INTERVAL(intToByteArray(515, 2)),
    JPEG_LOSSLESS_PREDICTORS(intToByteArray(517, 2)),
    JPEG_POINT_TRANSFORMS(intToByteArray(518, 2)),
    JPEG_Q_TABLES(intToByteArray(519, 2)),
    JPEG_DC_TABLES(intToByteArray(520, 2)),
    JPEG_AC_TABLES(intToByteArray(521, 2)),
    Y_CB_CR_COEFFICIENTS(intToByteArray(529, 2)),
    Y_CB_CR_SUB_SAMPLING(intToByteArray(530, 2)),
    REFERENCE_BLACK_WHITE(intToByteArray(532, 2));
    companion object {
        fun fromByteArray(byteArray : ByteArray) = TagType.values().first { it.byteArray.contentEquals(byteArray) }
    }
}

enum class DataType (val byteArray: ByteArray, val byteSize : Int){
    BYTE(byteArrayOf(0,1), 1),
    ASCII(byteArrayOf(0,2), 1),
    SHORT(byteArrayOf(0,3), 2),
    LONG(byteArrayOf(0,4), 4),
    RATIONAL(byteArrayOf(0,5), 4),
    SBYTE(byteArrayOf(0,6), 1),
    UNDEFINED(byteArrayOf(0,7), 1),
    SSHORT(byteArrayOf(0,8), 2),
    SLONG(byteArrayOf(0,9), 4),
    SRATIONAL(byteArrayOf(0,10), 4),
    FLOAD(byteArrayOf(0,11), 4),
    DOUBLE(byteArrayOf(0,12), 4);
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