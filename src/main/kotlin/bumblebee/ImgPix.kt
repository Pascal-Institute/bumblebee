package bumblebee

import bumblebee.Converter.Companion.convertByteToHex
import bumblebee.Converter.Companion.convertHexToInt
import java.awt.Graphics
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.File
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

    private lateinit var pixelByteArray: ByteArray

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
            while(count < 4){
                chunk.length[count] = byteArray[idx]
                count++
                idx++
            }

            //type 4 byte
            count = 0
            while(count < 4){
                chunk.type[count] = byteArray[idx]
                count++
                idx++
            }

            count = 0
            val length = chunk.getLength()
            chunk.initData(length)
            while (count < length){
                try{
                    chunk.data[count] = byteArray[idx]
                    count++
                    idx++
                }catch (e : Exception){
                    error("extract failed")
                }

            }

            //crc 4byte
            count = 0
            while (count < 4){

                chunk.crc[count] = byteArray[idx]
                count++
                idx++
            }
            chunkArray.add(chunk)
        }

        chunkArray.forEach{ it ->
            when(convertByteToHex(it.type)){
                "49484452" -> {
                    width = it.getWidth(it.data.sliceArray(0..3))
                    height = it.getHeight(it.data.sliceArray(4..7))

                    bitDepth = it.getBitDepth(it.data[8])
                    colorType = ColorType.fromInt(it.getColorType(it.data[9]))
                }

                "49444154"->{


                    val decompressedByteArray = decompress(it)
                    offFilter(decompressedByteArray)
                }
            }
        }

    }

    fun show(){
        val buffer = DataBufferByte(pixelByteArray, pixelByteArray.size)
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
        frame.setSize(width*2,  height*2 + 32)


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

    private fun decompress(chunk: Chunk) : ByteArray{
        val decompresser = Inflater()
        decompresser.setInput(chunk.data, 0, chunk.data.size)
        val decompressedByteArray = ByteArray(height * (1 + width * 3))
        decompresser.inflate(decompressedByteArray)
        decompresser.end()
        return decompressedByteArray
    }
    private fun offFilter(decompressedByteArray: ByteArray) {

        pixelByteArray = ByteArray(width * height * 3)

        for(col : Int in 0 until height ){
            //extract filter
            var filterType: FilterType
            filterType = try{
                FilterType.fromInt( convertHexToInt(convertByteToHex(
                    decompressedByteArray[((width * 3) + 1 ) * col]
                )))
            }catch (e : Exception){
                FilterType.NONE
            }

            val from = ((width * 3) + 1) * col + 1
            val to = from + (width * 3)
            val fromReal = (width * 3) * col

            when(filterType){
                FilterType.NONE -> fun0(decompressedByteArray.copyOfRange(from, to), fromReal)
                FilterType.SUB -> fun1( decompressedByteArray.copyOfRange(from, to), fromReal)
                FilterType.UP -> fun2( decompressedByteArray.copyOfRange(from,to), fromReal)
                FilterType.AVERAGE -> fun3( decompressedByteArray.copyOfRange(from,to), fromReal)
                FilterType.PAETH -> fun4( decompressedByteArray.copyOfRange(from,to), fromReal)
            }
        }
    }

    private fun fun0(byteArray : ByteArray, from: Int) {
        val idx = from
        byteArray.forEachIndexed { index, it ->
            pixelByteArray[idx] = it
        }
    }

    private fun fun1(byteArray: ByteArray, from: Int) {

        var idx = from
        var count = 0
        byteArray.forEachIndexed { index, it ->
            if(count < 3){
                pixelByteArray[idx] = it
            }else{
                pixelByteArray[idx] = (pixelByteArray[idx-3] + it).toByte()
            }
            idx++
            count++

        }
    }

    private fun fun2(byteArray: ByteArray, from: Int) {

        var idx = from
        byteArray.forEachIndexed { index, it ->
                pixelByteArray[idx] = (pixelByteArray[idx - (width * 3)]  + it).toByte()
                idx++
        }
    }

    private fun fun3(byteArray : ByteArray, from: Int) {

        var idx = from
        var count = 0
        byteArray.forEachIndexed { index, it ->
            if(count < 3){

                val b = convertHexToInt(convertByteToHex(pixelByteArray[idx - (width * 3)]))
                val c = floor(b  * 0.5 ).toInt()
                pixelByteArray[idx] = (c + it).toByte()
            }else{
                val a = convertHexToInt(convertByteToHex(pixelByteArray[idx - 3]))
                val b = convertHexToInt(convertByteToHex(pixelByteArray[idx - (width * 3)]))
                val c = floor((a  + b) * 0.5 ).toInt()
                pixelByteArray[idx] = (c + it).toByte()
            }

            idx++
            count++
        }
    }

    private fun fun4(byteArray: ByteArray, from: Int) {

        var idx = from
        var count = 0
        byteArray.forEachIndexed { index, it ->

            if(count < 3){

                val a = 0
                val b = convertHexToInt(convertByteToHex(pixelByteArray[idx - (width * 3)]))
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

                pixelByteArray[idx] = (pR + it).toByte()

            }else{
                val a = convertHexToInt(convertByteToHex(pixelByteArray[idx - 3]))
                val b = convertHexToInt(convertByteToHex(pixelByteArray[idx - (width * 3)]))
                val c = convertHexToInt(convertByteToHex(pixelByteArray[idx - (width * 3) - 3]))

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

                pixelByteArray[idx] = (pR + it).toByte()
            }
            idx++
            count++
        }
    }
}