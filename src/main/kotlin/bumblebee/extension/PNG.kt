package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import bumblebee.util.Converter
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.toHex
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32
import java.util.zip.Checksum
import java.util.zip.Inflater
import kotlin.math.abs
import kotlin.math.floor

//PNG Version 1.2 / Author : G. Randers-Pehrson, et. al.
class PNG(private var byteArray: ByteArray) : ImgPix() {
    private val chunkArray = ArrayList<Chunk>()
    init {
        imgFileType = ImgFileType.PNG
        extract()
    }

    override fun extract() {

        val size = byteArray.size
        var idx = 8

        while (idx < size){

            val chunk = Chunk()

            //length 4 byte
            chunk.length = byteArray.sliceArray(idx until idx + 4)
            idx += 4

            //type 4 byte
            chunk.type = byteArray.sliceArray(idx until idx + 4)
            idx += 4

            val length = chunk.getLength()
            chunk.initData(length)

            try{
                chunk.data = byteArray.sliceArray(idx until idx + length)
            }catch (e : Exception){
                System.err.println("ERROR : Extract failed")
            }

            idx += length

            //crc 4 byte
            chunk.crc = byteArray.sliceArray(idx until idx + 4)
            idx += 4

            chunkArray.add(chunk)
        }

        val outputStream = ByteArrayOutputStream()
        var byteArray = ByteArray(0)

        chunkArray.forEach{
            when(it.type.toHex()){
                ChunkType.IHDR.byte.toHex() -> {
                    metaData.width = it.getWidth(it.data.sliceArray(0 until 4))
                    metaData.height = it.getHeight(it.data.sliceArray(4 until 8))
                    bitDepth = it.getBitDepth(it.data[8])
                    metaData.colorType = ColorType.fromInt(it.getColorType(it.data[9]))
                    bytesPerPixel = metaData.colorType.colorSpace * (bitDepth / OCTA)
                }

                ChunkType.IDAT.byte.toHex() -> {
                    if(byteArray.isNotEmpty()){
                        byteArray += it.data
                    }else{
                        byteArray = it.data
                    }
                }

                ChunkType.PLTE.byte.toHex()->{
                }
            }
        }

        outputStream.write(byteArray)
        val decompressedByteBuffer = decompress(outputStream.toByteArray())
        offFilter(decompressedByteBuffer)
    }

    private fun decompress(byteArray: ByteArray) : ByteBuffer{
        val decompresser = Inflater()
        decompresser.setInput(byteArray, 0, byteArray.size)
        val decompressedByteArray = ByteArray(metaData.height * (1 + metaData.width * bytesPerPixel))
        decompresser.inflate(decompressedByteArray)
        decompresser.end()
        return ByteBuffer.wrap(decompressedByteArray)
    }
    private fun offFilter(decompressedByteBuffer: ByteBuffer) {

        pixelByteBuffer = ByteBuffer.allocate(metaData.width * metaData.height * bytesPerPixel)

        for(col : Int in 0 until metaData.height ){

            var filterType: FilterType = try{
                FilterType.fromInt(
                    decompressedByteBuffer.get(((metaData.width * bytesPerPixel) + 1) * col).byteToInt()
                )
            }catch (e : Exception){
                FilterType.NONE
            }

            val from = ((metaData.width * bytesPerPixel) + 1) * col + 1
            val fromReal = (metaData.width * bytesPerPixel) * col
            val size = metaData.width * bytesPerPixel
            val byteArray = ByteArray(size)

            for(i : Int in 0 until size){
                byteArray[i] =  decompressedByteBuffer.get(from+i)
            }


            when(filterType){
                FilterType.NONE -> none(byteArray, fromReal)
                FilterType.SUB -> sub(byteArray, fromReal)
                FilterType.UP -> up(byteArray, fromReal)
                FilterType.AVERAGE -> average(byteArray, fromReal)
                FilterType.PAETH -> paeth(byteArray, fromReal)
            }
        }
    }

    private fun none(byteArray : ByteArray, from: Int) {
        pixelByteBuffer.put(byteArray)
    }

    private fun sub(byteArray: ByteArray, from: Int) {
        var idx = from
        var count = 0
        byteArray.forEach {
            if(count < bytesPerPixel){
                pixelByteBuffer.put(it)
            }else{
                pixelByteBuffer.put((pixelByteBuffer.get(idx-bytesPerPixel) + it).toByte())
            }
            idx++
            count++
        }
    }

    private fun up(byteArray: ByteArray, from: Int) {
        var idx = from
        byteArray.forEach{
            pixelByteBuffer.put((pixelByteBuffer.get(idx - (metaData.width * bytesPerPixel)) + it).toByte())
            idx++
        }
    }

    private fun average(byteArray : ByteArray, from: Int) {

        var idx = from
        var count = 0

        byteArray.forEach{
            if(count < bytesPerPixel){
                val b = pixelByteBuffer.get(idx - (metaData.width * bytesPerPixel)).toUByte().toInt()
                val c = floor( b  * 0.5 ).toInt()
                pixelByteBuffer.put((c + it).toByte())

            }else{

                val a = pixelByteBuffer.get(idx - bytesPerPixel).toUByte().toInt()
                val b = pixelByteBuffer.get(idx - (metaData.width * bytesPerPixel)).toUByte().toInt()
                val c = floor((a  + b) * 0.5 ).toInt()
                pixelByteBuffer.put((c + it).toByte())
            }

            idx++
            count++
        }
    }

    private fun paeth(byteArray: ByteArray, from: Int) {

        var idx = from
        var count = 0
        byteArray.forEach {
            if(count < bytesPerPixel){

                val a = 0
                val b = pixelByteBuffer.get(idx - (metaData.width * bytesPerPixel)).toUByte().toInt()
                val c = 0

                val byteP = (a + b - c)

                val pA = abs(byteP - a.toByte())
                val pB = abs(byteP - b)
                val pC = abs(byteP - a.toByte())

                val pR = if(pA <= pB && pA <= pC){
                    a.toByte()
                }else if(pB <= pC){
                    b.toByte()
                }else{
                    c.toByte()
                }

                pixelByteBuffer.put((pR + it).toByte())

            }else{
                val a = pixelByteBuffer.get(idx - bytesPerPixel).toUByte().toInt()
                val b = pixelByteBuffer.get(idx - (metaData.width * bytesPerPixel)).toUByte().toInt()
                val c = pixelByteBuffer.get(idx - (metaData.width * bytesPerPixel) - bytesPerPixel).toUByte().toInt()

                val byteP = (a + b - c)

                val pA = abs(byteP - a)
                val pB = abs(byteP - b)
                val pC = abs(byteP - c)

                val pR = if(pA <= pB && pA <= pC){
                    a
                }else if(pB <= pC){
                    b
                }else{
                    c
                }

                pixelByteBuffer.put((pR + it).toByte())
            }
            idx++
            count++
        }
    }

    private enum class ChunkType(val byte : ByteArray) {
        IHDR(byteArrayOf(73, 72, 68, 82)),
        IDAT(byteArrayOf(73, 68, 65, 84)),
        IEND(byteArrayOf(73, 69, 78, 68)),
        PLTE(byteArrayOf(80, 76, 84, 69)),
        SRGB(byteArrayOf(115, 82, 71, 66)),
        GAMA(byteArrayOf(103, 65, 77, 65)),
    }

    private enum class FilterType(val num : Int) {

        NONE(0),
        SUB(1),
        UP(2),
        AVERAGE(3),
        PAETH(4);

        companion object {
            fun fromInt(num: Int) = FilterType.values().first { it.num == num }
        }
    }

    private class Chunk {
        var length: ByteArray = ByteArray(4)
        var type: ByteArray = ByteArray(4)
        lateinit var data: ByteArray
        var crc: ByteArray = ByteArray(4)

        fun initData(size: Int) {
            data = ByteArray(size)
        }

        fun getWidth(byteArray: ByteArray): Int {
            return byteArray.byteToInt()
        }

        fun getHeight(byteArray: ByteArray): Int {
            return byteArray.byteToInt()
        }

        fun getLength(): Int {
            return length.byteToInt()
        }

        fun getColorType(byte: Byte): Int {
            return byte.byteToInt()
        }

        fun getBitDepth(byte: Byte): Int {
            return byte.byteToInt()
        }

        fun getCRC(): ByteArray {
            val checksum: Checksum = CRC32()
            var source = type + data
            checksum.update(source, 0, source.size)
            return Converter.longToByteArray(checksum.value, 4)
        }

        fun generateData(imgPix: ImgPix) {
            var byteArray = imgPix.get()
            data = ByteArray(imgPix.metaData.height * (imgPix.metaData.width * imgPix.bytesPerPixel + 1))
            var count = 0
            data.forEachIndexed { index, byte ->
                if(index % (imgPix.metaData.width * imgPix.bytesPerPixel + 1) == 0){
                    data[index] = 0
                }else{
                    data[index] = byteArray[count]
                    count++
                }

            }
        }
    }
}