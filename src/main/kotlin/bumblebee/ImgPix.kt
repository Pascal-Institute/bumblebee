 package bumblebee

import bumblebee.Converter.Companion.convertByteToHex
import bumblebee.Converter.Companion.convertColorToByte
import bumblebee.Converter.Companion.convertHexToInt
import bumblebee.color.Color
import bumblebee.type.ChunkType
import bumblebee.type.ColorType
import bumblebee.type.FilterType
import bumblebee.type.ImgFileType
import java.awt.Graphics
import java.awt.image.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.Inflater
import javax.swing.*
import kotlin.math.abs
import kotlin.math.floor

open class ImgPix : Cloneable {

    //PNG
    protected val OCTA = 8
    var bytesPerPixel = 0
    var bitDepth = 0

    var manipulatedIntance = false
    var width = 0
    var height = 0

    protected lateinit var pixelBufferArray: ByteBuffer
    lateinit var colorType : ColorType
    lateinit var imgFileType: ImgFileType

    public override fun clone(): ImgPix {
        return super.clone() as ImgPix
    }

    fun crop(row : Int, col : Int, width : Int, height : Int) : ImgPix {

        var imgPix = this.clone()

        imgPix.manipulatedIntance = true
        imgPix.width = width
        imgPix.height = height

        var bytesPerPixel = imgPix.bytesPerPixel
        var pixelBufferArray = ByteBuffer.allocate(imgPix.width * imgPix.height * imgPix.bytesPerPixel)

        var startIdx = row * (width * bytesPerPixel) + col * bytesPerPixel

        for(i : Int in 0 until height){
            for(j : Int in 0 until width){
                for(k : Int in 0 until bytesPerPixel){
                    pixelBufferArray.put(this.pixelBufferArray.get(startIdx  + j * bytesPerPixel + k + (i * bytesPerPixel * this.width)))
                }
            }
        }

        imgPix.pixelBufferArray = pixelBufferArray

        return imgPix
    }

    fun set(row : Int, col : Int, color : Color) {
        if (colorType != color.colorType){
            System.err.println("ERROR : ColorType does not match")
            return
        }else{
            val byteArray : ByteArray = convertColorToByte(color)
            for (i : Int in 0 until bytesPerPixel){
                pixelBufferArray.put(i + bytesPerPixel * col + (width * bytesPerPixel) * row, byteArray[i])
            }
        }
    }

    fun get(row : Int, col : Int) : String{
        val byteArray = ByteArray((colorType.colorSpace * (bitDepth/OCTA)))
        for (i : Int in 0 until bytesPerPixel){
            byteArray[i] = pixelBufferArray.get(i + bytesPerPixel * col + (width * bytesPerPixel) * row)
        }

        return convertByteToHex(byteArray)
    }

    fun get() : ByteArray {
        return pixelBufferArray.array()
    }

    fun show(){
        val buffer = DataBufferByte(pixelBufferArray.array(), pixelBufferArray.array().size)

        var bufferedImage : BufferedImage
        when(colorType){
            ColorType.GRAY_SCALE ->{
                bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
                bufferedImage.data = Raster.createInterleavedRaster(buffer, width, height, width * 1, 1, intArrayOf(0), null)
            }

            ColorType.TRUE_COLOR ->{
               bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
               bufferedImage.data = Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, intArrayOf(0,1,2), null)
            }

            ColorType.TRUE_COLOR_ALPHA->{
                bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                bufferedImage.data = Raster.createInterleavedRaster(buffer, width, height, width * 4, 4, intArrayOf(0,1,2,3), null)
            }

            else -> {
                bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                bufferedImage.data = Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, intArrayOf(0,1,2), null)
            }
        }

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

}