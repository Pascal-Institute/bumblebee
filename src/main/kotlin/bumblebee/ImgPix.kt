 package bumblebee

import bumblebee.Converter.Companion.byteToHex
import bumblebee.Converter.Companion.colorToByte
import bumblebee.color.Color
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import java.awt.Graphics
import java.awt.image.*
import java.nio.ByteBuffer
import javax.swing.*

open class ImgPix() : ImgExtractor, Cloneable {

    //PNG
    protected val OCTA = 8
    var bytesPerPixel = 0
    var bitDepth = 0

    var manipulatedIntance = false
    var width = 0
    var height = 0

     lateinit var pixelBufferArray: ByteBuffer
     lateinit var colorType : ColorType
     var imgFileType : ImgFileType = ImgFileType.PIX

    constructor(width: Int, height: Int, colorType: ColorType) : this() {
        this.width = width
        this.height = height
        this.colorType = colorType
        this.pixelBufferArray = ByteBuffer.allocate(width * height * colorType.colorSpace)
    }

    public override fun clone(): ImgPix {
        return super.clone() as ImgPix
    }
    fun set(row : Int, col : Int, color : Color) {
        if (colorType != color.colorType){
            System.err.println("ERROR : ColorType does not match")
            return
        }else{
            val byteArray : ByteArray = colorToByte(color)
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

        return byteToHex(byteArray)
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

     override fun extract() {
         TODO("Not yet implemented")
     }

     fun invert() {
         ImgProcess.invert(this)
     }
     fun crop(row : Int, col : Int, width : Int, height : Int) {
         ImgProcess.crop(this, row, col, width, height)
     }

 }