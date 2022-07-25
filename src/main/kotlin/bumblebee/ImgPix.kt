package bumblebee

import bumblebee.Converter.Companion.convertByteToHex
import bumblebee.Converter.Companion.convertHexToInt
import java.awt.Graphics
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.Inflater
import javax.swing.*
import kotlin.math.abs
import kotlin.math.floor


class ImgPix(filePath : String) {

    private val file = File(filePath)
    private val byteArray = file.readBytes()
    private val chunkArray = ArrayList<Chunk>()

    var width = 0
    var height = 0
    var bitDepth = 0

    private lateinit var pixelBufferArray: ByteBuffer
    private var bytesPerPixel : Int = 0

    lateinit var colorType : ColorType

    init {
        extractImageInfo(byteArray)
    }
    private fun extractImageInfo(byteArray: ByteArray){

        val size = byteArray.size
        var idx = 8

        while (idx < size){

            val chunk = Chunk()

            //length 4 byte
            var count  = 0
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
                error("Extract failed")
            }

            idx += length

            chunk.crc = byteArray.sliceArray(idx until idx + 4)
            idx += 4

            chunkArray.add(chunk)
        }
        val outputStream = ByteArrayOutputStream()

        chunkArray.forEach{ it ->
            when(convertByteToHex(it.type)){
                convertByteToHex(ChunkType.IHDR.byte) -> {
                   width = it.getWidth(it.data.sliceArray(0..3))
                   height = it.getHeight(it.data.sliceArray(4..7))
                   bitDepth = it.getBitDepth(it.data[8])
                   colorType = ColorType.fromInt(it.getColorType(it.data[9]))

                   bytesPerPixel = colorType.colorSpace * (bitDepth / 8)
            }

                convertByteToHex(ChunkType.IDAT.byte)-> {

                    outputStream.write(it.data)

                }
            }
        }

        val decompressedByteArray = decompress(outputStream.toByteArray())
        offFilter(decompressedByteArray)

    }

    fun show(){
        val buffer = DataBufferByte(pixelBufferArray.array(), pixelBufferArray.array().size)
        val cm: ColorModel = ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            intArrayOf(8, 8, 8),
            false,
            false,
            Transparency.OPAQUE,
            DataBuffer.TYPE_BYTE
        )

        val bufferedImage = BufferedImage(
            cm,
            Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, intArrayOf(0, 1, 2), null),
            false,
            null
        )

        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isResizable = false
        frame.setLocationRelativeTo(null)
        frame.setSize((width * 1.1).toInt(),  (height * 1.1).toInt())


        val pane: JPanel = object : JPanel() {

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                g.drawImage(bufferedImage, 0, 0, null)
            }
        }

        val box = Box(BoxLayout.X_AXIS)

        box.add(Box.createVerticalGlue())
        box.add(pane)
        box.add(Box.createVerticalGlue())
        frame.add(box)

        frame.isVisible = true
    }

    private fun decompress(byteArray: ByteArray) : ByteBuffer{
        val decompresser = Inflater()
        decompresser.setInput(byteArray, 0, byteArray.size)
        val decompressedByteArray = ByteArray(height * (1 + width * bytesPerPixel))
        decompresser.inflate(decompressedByteArray)
        decompresser.end()
        return ByteBuffer.wrap(decompressedByteArray)
    }
    private fun offFilter(decompressedByteBuffer: ByteBuffer) {

        pixelBufferArray = ByteBuffer.allocate(width * height * bytesPerPixel)

        for(col : Int in 0 until height ){

            var filterType: FilterType = try{
                FilterType.fromInt( convertHexToInt(convertByteToHex(
                    decompressedByteBuffer.get(((width * bytesPerPixel) + 1 ) * col)
                )))
            }catch (e : Exception){
                FilterType.NONE
            }

            val from = ((width * bytesPerPixel) + 1) * col + 1
            val fromReal = (width * bytesPerPixel) * col
            val byteArray = ByteArray(width * bytesPerPixel)

            decompressedByteBuffer.get(from, byteArray, 0 ,width * bytesPerPixel)

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
            pixelBufferArray.put((pixelBufferArray.get(idx - (width * bytesPerPixel)) + it).toByte())
            idx++
        }
    }

    private fun average(byteArray : ByteArray, from: Int) {

        var idx = from
        var count = 0

        byteArray.forEach{
            if(count < bytesPerPixel){
                val b = pixelBufferArray.get(idx - (width * bytesPerPixel)).toUByte().toInt()
                val c = floor( b  * 0.5 ).toInt()
                pixelBufferArray.put((c + it).toByte())

            }else{

                val a = pixelBufferArray.get(idx - bytesPerPixel).toUByte().toInt()
                val b = pixelBufferArray.get(idx - (width * bytesPerPixel)).toUByte().toInt()
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
                val b = pixelBufferArray.get(idx - (width * bytesPerPixel)).toUByte().toInt()
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
                val b = pixelBufferArray.get(idx - (width * bytesPerPixel)).toUByte().toInt()
                val c = pixelBufferArray.get(idx - (width * bytesPerPixel) - bytesPerPixel).toUByte().toInt()

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
}