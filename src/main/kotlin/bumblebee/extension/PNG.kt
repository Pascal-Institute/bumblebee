package bumblebee.extension

import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.hexToInt
import bumblebee.core.ImgPix
import bumblebee.type.ChunkType
import bumblebee.type.ColorType
import bumblebee.type.FilterType
import bumblebee.type.ImgFileType
import bumblebee.util.Converter
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

        chunkArray.forEach{ it ->
            when(byteToHex(it.type)){
                byteToHex(ChunkType.IHDR.byte) -> {
                    metaData.width = it.getWidth(it.data.sliceArray(0..3))
                    metaData.height = it.getHeight(it.data.sliceArray(4..7))
                    bitDepth = it.getBitDepth(it.data[8])
                    metaData.colorType = ColorType.fromInt(it.getColorType(it.data[9]))
                    bytesPerPixel = metaData.colorType.colorSpace * (bitDepth / OCTA)
                }

                byteToHex(ChunkType.IDAT.byte) -> {
                    outputStream.write(it.data)
                }
            }
        }

        val decompressedByteArray = decompress(outputStream.toByteArray())
        offFilter(decompressedByteArray)
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

        pixelBufferArray = ByteBuffer.allocate(metaData.width * metaData.height * bytesPerPixel)

        for(col : Int in 0 until metaData.height ){

            var filterType: FilterType = try{
                FilterType.fromInt(
                    hexToInt(
                        byteToHex(
                            decompressedByteBuffer.get(((metaData.width * bytesPerPixel) + 1) * col)
                        )
                    )
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
                FilterType.SUB -> sub( byteArray, fromReal)
                FilterType.UP -> up( byteArray, fromReal)
                FilterType.AVERAGE -> average(byteArray, fromReal)
                FilterType.PAETH -> paeth(byteArray, fromReal)
            }
        }
    }

    private fun none(byteArray : ByteArray, from: Int) {
        pixelBufferArray.put(byteArray)
    }

    private fun sub(byteArray: ByteArray, from: Int) {
        var idx = from
        var count = 0
        byteArray.forEach {
            if(count < bytesPerPixel){
                pixelBufferArray.put(it)
            }else{
                pixelBufferArray.put((pixelBufferArray.get(idx-bytesPerPixel) + it).toByte())
            }
            idx++
            count++
        }
    }

    private fun up(byteArray: ByteArray, from: Int) {

        var idx = from
        byteArray.forEachIndexed { index, it ->
            pixelBufferArray.put((pixelBufferArray.get(idx - (metaData.width * bytesPerPixel)) + it).toByte())
            idx++
        }
    }

    private fun average(byteArray : ByteArray, from: Int) {

        var idx = from
        var count = 0

        byteArray.forEach{
            if(count < bytesPerPixel){
                val b = pixelBufferArray.get(idx - (metaData.width * bytesPerPixel)).toUByte().toInt()
                val c = floor( b  * 0.5 ).toInt()
                pixelBufferArray.put((c + it).toByte())

            }else{

                val a = pixelBufferArray.get(idx - bytesPerPixel).toUByte().toInt()
                val b = pixelBufferArray.get(idx - (metaData.width * bytesPerPixel)).toUByte().toInt()
                val c = floor((a  + b) * 0.5 ).toInt()
                pixelBufferArray.put((c + it).toByte())
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
                val b = pixelBufferArray.get(idx - (metaData.width * bytesPerPixel)).toUByte().toInt()
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

                pixelBufferArray.put((pR + it).toByte())

            }else{
                val a = pixelBufferArray.get(idx - bytesPerPixel).toUByte().toInt()
                val b = pixelBufferArray.get(idx - (metaData.width * bytesPerPixel)).toUByte().toInt()
                val c = pixelBufferArray.get(idx - (metaData.width * bytesPerPixel) - bytesPerPixel).toUByte().toInt()

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

                pixelBufferArray.put((pR + it).toByte())
            }
            idx++
            count++
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
            return hexToInt(byteToHex(byteArray))
        }

        fun getHeight(byteArray: ByteArray): Int {
            return hexToInt(byteToHex(byteArray))
        }

        fun getLength(): Int {
            var string = byteToHex(length)
            return hexToInt(string)
        }

        fun getColorType(byte: Byte): Int {
            return hexToInt(byteToHex(byte))
        }

        fun getBitDepth(byte: Byte): Int {
            return hexToInt(byteToHex(byte))
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