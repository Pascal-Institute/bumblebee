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

    var metaData = MetaData(0, 0)

    //PNG
    protected val OCTA = 8
    var bytesPerPixel = 0
    var bitDepth = 0

    var manipulatedIntance = false

    lateinit var pixelBufferArray: ByteBuffer
    var imgFileType : ImgFileType = ImgFileType.PIX

    constructor(width: Int, height: Int, colorType: ColorType) : this() {
        metaData.width = width
        metaData.height = height
        metaData.colorType = colorType
        this.pixelBufferArray = ByteBuffer.allocate(width * height * colorType.colorSpace)
    }

    public override fun clone(): ImgPix {
        return super.clone() as ImgPix
    }
    fun set(row : Int, col : Int, color : Color) {
        if (metaData.colorType != color.colorType){
            System.err.println("ERROR : ColorType does not match")
            return
        }else{
            val byteArray : ByteArray = colorToByte(color)
            for (i : Int in 0 until bytesPerPixel){
                pixelBufferArray.put(i + bytesPerPixel * col + (metaData.width * bytesPerPixel) * row, byteArray[i])
            }
        }
    }

    fun get(row : Int, col : Int) : String{
        val byteArray = ByteArray((metaData.colorType.colorSpace * (bitDepth/OCTA)))
        for (i : Int in 0 until bytesPerPixel){
            byteArray[i] = pixelBufferArray.get(i + bytesPerPixel * col + (metaData.width * bytesPerPixel) * row)
        }
        return byteToHex(byteArray)
    }

    fun get() : ByteArray {
        return pixelBufferArray.array()
    }

    fun show(){
        val buffer = DataBufferByte(pixelBufferArray.array(), pixelBufferArray.array().size)

        var bufferedImage : BufferedImage
        when(metaData.colorType){
            ColorType.GRAY_SCALE ->{
                bufferedImage = BufferedImage(metaData.width, metaData.height, BufferedImage.TYPE_BYTE_GRAY)
                bufferedImage.data = Raster.createInterleavedRaster(buffer, metaData.width, metaData.height, metaData.width * 1, 1, intArrayOf(0), null)
            }

            ColorType.TRUE_COLOR ->{
               bufferedImage = BufferedImage(metaData.width, metaData.height, BufferedImage.TYPE_INT_RGB)
               bufferedImage.data = Raster.createInterleavedRaster(buffer, metaData.width, metaData.height, metaData.width * 3, 3, intArrayOf(0,1,2), null)
            }

            ColorType.TRUE_COLOR_ALPHA->{
                bufferedImage = BufferedImage(metaData.width, metaData.height, BufferedImage.TYPE_INT_ARGB)
                bufferedImage.data = Raster.createInterleavedRaster(buffer, metaData.width, metaData.height, metaData.width * 4, 4, intArrayOf(0,1,2,3), null)
            }

            else -> {
                bufferedImage = BufferedImage(metaData.width, metaData.height, BufferedImage.TYPE_INT_RGB)
                bufferedImage.data = Raster.createInterleavedRaster(buffer, metaData.width, metaData.height, metaData.width * 3, 3, intArrayOf(0,1,2), null)
            }
        }

        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isResizable = false
        frame.setLocationRelativeTo(null)
        frame.setSize((metaData.width * 1.1).toInt(),  (metaData.height * 1.1).toInt())

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