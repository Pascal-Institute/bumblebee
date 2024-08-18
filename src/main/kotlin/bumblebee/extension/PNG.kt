package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.FileType
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.cut
import bumblebee.core.Packet
import bumblebee.util.StringObject.CRC
import bumblebee.util.StringObject.DATA
import bumblebee.util.StringObject.SIZE
import bumblebee.util.StringObject.TYPE
import delta.ZLib
import komat.Element
import komat.space.Cube
import kotlin.math.abs
import kotlin.math.floor

//PNG Version 1.2 / Author : G. Randers-Pehrson, et. al.
class PNG(private var byteArray: ByteArray) : ImgPix() {
    private val chunkArray = ArrayList<Packet>()
    private var bitDepth = 0
    init {
        extract()
    }

    override fun extract() {
        metaData.fileType = FileType.PNG

        val totalSize = byteArray.size
        var idx = 8

        while (idx < totalSize){

            val chunk = Packet()

            //length 4 byte
            chunk[SIZE] = byteArray.cut(idx , idx + 4)
            idx += 4

            //type 4 byte
            chunk[TYPE] = byteArray.cut(idx, idx + 4)
            idx += 4

            val size = chunk[SIZE].byteToInt()
            try{
                chunk[DATA] = byteArray.cut(idx, idx + size)
            }catch (e : Exception){
                System.err.println("ERROR : Extract failed")
            }

            idx += size

            //crc 4 byte
            chunk[CRC] = byteArray.cut(idx, idx + 4)
            idx += 4

            chunkArray.add(chunk)
        }

        var byteArray = ByteArray(0)

        chunkArray.forEach{
            when(ChunkType.fromByteArray(it[TYPE])){
                ChunkType.IHDR -> {
                    setMetaData(it)
                    bitDepth = it[DATA][8].byteToInt()
                }

                ChunkType.ICCP -> {

                }

                ChunkType.IDAT -> {
                    if(byteArray.isNotEmpty()){
                        byteArray += it[DATA]
                    }else{
                        byteArray = it[DATA]
                    }
                }

                ChunkType.PLTE->{
                }

                else->{}
            }
        }

        val decompressedByteArray = ZLib.decode(byteArray, height * (1 + width * bytesPerPixel))
        offFilter(decompressedByteArray)
    }

    override fun setMetaData(packet: Packet) {
        metaData.width = packet[DATA].cut(0, 4).byteToInt()
        metaData.height = packet[DATA].cut(4, 8).byteToInt()
        metaData.colorType = ColorType.fromInt(packet[DATA][9].byteToInt())
    }

    private fun offFilter(decompressedByteArray: ByteArray) {

        cube = Cube(width, height, bytesPerPixel, Element(0.toByte()))

        for(col : Int in 0 until height ){

            val filterType: FilterType = try{
                FilterType.fromInt(
                    decompressedByteArray[((width * bytesPerPixel) + 1) * col].byteToInt()
                )
            }catch (e : Exception){
                FilterType.NONE
            }

            val from = (width * bytesPerPixel + 1) * col + 1
            val fromReal = width * bytesPerPixel * col
            val size = width * bytesPerPixel
            val byteArray = ByteArray(size)

            for(i : Int in 0 until size){
                byteArray[i] = decompressedByteArray[from+i]
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
        var idx = from
        byteArray.forEach {
            cube[idx] = it
            idx++
        }
    }

    private fun sub(byteArray: ByteArray, from: Int) {
        var idx = from
        var count = 0
        byteArray.forEach {
            if(count < bytesPerPixel){
                cube[idx] = it
            }else{
                cube[idx] = (cube[idx-bytesPerPixel].toByte() + it).toByte()
            }
            idx++
            count++
        }
    }

    private fun up(byteArray: ByteArray, from: Int) {
        var idx = from
        byteArray.forEach{
            cube[idx] = (cube[idx - width * bytesPerPixel].toByte() + it).toByte()
            idx++
        }
    }

    private fun average(byteArray : ByteArray, from: Int) {

        var idx = from
        var count = 0

        byteArray.forEach{
            if(count < bytesPerPixel){
                val b = (cube[idx - width * bytesPerPixel].toByte() ).toUByte().toInt()
                val c = floor( b  * 0.5 ).toInt()
                cube[idx] = (c + it).toByte()

            }else{

                val a = (cube[idx - bytesPerPixel].toByte()).toUByte().toInt()
                val b = (cube[idx - width * bytesPerPixel].toByte()).toUByte().toInt()
                val c = floor((a  + b) * 0.5 ).toInt()
                cube[idx] = (c + it).toByte()
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
                val b = (cube[idx - width * bytesPerPixel].toByte()).toUByte().toInt()
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

                cube[idx] = (pR + it).toByte()

            }else{
                val a = (cube[idx - bytesPerPixel].toByte()).toUByte().toInt()
                val b = (cube[idx - width * bytesPerPixel].toByte()).toUByte().toInt()
                val c = (cube[idx - width * bytesPerPixel - bytesPerPixel].toByte()).toUByte().toInt()

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

                cube[idx] = ((pR + it).toByte())
            }
            idx++
            count++
        }
    }

    private enum class ChunkType(val byte : ByteArray) {
        IHDR(byteArrayOf(73, 72, 68, 82)),
        ICCP(byteArrayOf(105, 67, 67, 80)),
        IDAT(byteArrayOf(73, 68, 65, 84)),
        IEND(byteArrayOf(73, 69, 78, 68)),
        PHYS(byteArrayOf(112, 72, 89, 115)),
        PLTE(byteArrayOf(80, 76, 84, 69)),
        SRGB(byteArrayOf(115, 82, 71, 66)),
        GAMA(byteArrayOf(103, 65, 77, 65));
        companion object {
            fun fromByteArray(byteArray: ByteArray) = entries.first { it.byte.contentEquals(byteArray) }
        }
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
}