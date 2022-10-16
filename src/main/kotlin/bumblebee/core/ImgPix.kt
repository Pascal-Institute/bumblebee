 package bumblebee.core

import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.colorToByte
import bumblebee.ImgExtractor
import bumblebee.color.Color
import bumblebee.type.ColorType
import bumblebee.type.ImgFileType
import bumblebee.type.Orientation
import bumblebee.util.Histogram
import java.awt.Dimension
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
    val width : Int
        get() = metaData.width
     val height : Int
        get() = metaData.height

     var manipulatedInstance = false

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

        val bufferedImage : BufferedImage
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
        frame.title = "image"
        frame.isResizable = false
        frame.isVisible = true
        frame.setLocation(0,0)

        val pane: JPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                g.drawImage(bufferedImage, 0, 0, null)
                g.dispose()
            }
        }

        pane.preferredSize = Dimension(metaData.width, metaData.height)
        frame.add(pane)
        frame.pack()
    }

     override fun extract() {
         TODO("Not yet implemented")
     }

     fun invert() : ImgPix {
         return ImgProcess.invert(this)
     }
     fun flip(orientation: Orientation) : ImgPix {
         return ImgProcess.flip(this, orientation)
     }

     fun crop(row : Int, col : Int, width : Int, height : Int) : ImgPix {
         return ImgProcess.crop(this, row, col, width, height)
     }

     fun toGrayScale() : ImgPix {
         return ImgProcess.toGrayScale(this)
     }

     fun threshold(level : Int) : ImgPix {
         return ImgProcess.threshold(this, level)
     }

     fun histogram() : Histogram{
        return Histogram(this)
     }
 }