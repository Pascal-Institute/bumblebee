 package bumblebee.core

import bumblebee.FileManager
import bumblebee.color.Color
import bumblebee.type.*
import bumblebee.util.Converter.Companion.toHex
import bumblebee.util.Histogram
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.nio.ByteBuffer
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants


 open class ImgPix() : Cloneable {

    val metaData = MetaData(0, 0, ColorType.GRAY_SCALE)
    protected val OCTA = 8
    var bytesPerPixel = 0
    var bitDepth = 0
    val width : Int
        get() = metaData.width
    val height : Int
        get() = metaData.height
    val colorType : ColorType
        get() = metaData.colorType

    private var manipulatedInstance = false
    var pixelByteBuffer: ByteBuffer = ByteBuffer.allocate(0)
    var imgFileType : ImgFileType = ImgFileType.PIX

    constructor(width: Int, height: Int, colorType: ColorType) : this() {
        metaData.width = width
        metaData.height = height
        metaData.colorType = colorType
        this.pixelByteBuffer = ByteBuffer.allocate(width * height * colorType.colorSpace)
    }

    constructor(filePath : String) : this() {
       var imgPix  = FileManager.read(filePath)
        metaData.width = imgPix.width
        metaData.height = imgPix.height
        metaData.colorType = imgPix.colorType
        bytesPerPixel = imgPix.bytesPerPixel
        bitDepth = imgPix.bitDepth
        this.pixelByteBuffer = imgPix.pixelByteBuffer

    }

     public override fun clone(): ImgPix {
        return super.clone() as ImgPix
    }

    fun get(row : Int, col : Int) : String{
        val byteArray = ByteArray((colorType.colorSpace * (bitDepth/OCTA)))
        for (i : Int in 0 until bytesPerPixel){
            byteArray[i] = pixelByteBuffer.get(i + bytesPerPixel * col + (width * bytesPerPixel) * row)
        }
        return byteArray.toHex()
    }

    fun get() : ByteArray {
        return pixelByteBuffer.array()
    }

    fun show(){
        val pixelByteBufferArray = pixelByteBuffer.array()
        val buffer = DataBufferByte(pixelByteBufferArray, pixelByteBufferArray.size)

        val bufferedImage : BufferedImage
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

            ColorType.INDEXED_COLOR->{
                bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED)
                bufferedImage.data = Raster.createInterleavedRaster(buffer, width, height, width * 4, 4, intArrayOf(0,1,2,3), null)
            }

            else -> {
                bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                bufferedImage.data = Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, intArrayOf(0,1,2), null)
            }
        }

        val frame = JFrame()
        val img: Image = frame.toolkit.getImage("src/main/resources/bumblebee_icon.png")
        frame.iconImage = img
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.title = "image"
        frame.isResizable = false
        frame.isVisible = true
        frame.isResizable = true

        val pane: JPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                g.drawImage(bufferedImage, 0, 0, null)
                g.dispose()
            }
        }

        pane.preferredSize = Dimension(width, height)
        frame.add(pane)
        frame.pack()
    }
     open fun extract(){}
     open fun setMetaData(imgHeader: ImgHeader){}

     fun set(row : Int, col : Int, color : Color) : ImgPix {
         return ImgProcess.set(this, row, col, color)
     }
     fun invert() : ImgPix {
         manipulatedInstance = true
         return ImgProcess.invert(this)
     }
     fun flip(orientation: OrientationType) : ImgPix {
         manipulatedInstance = true
         return ImgProcess.flip(this, orientation)
     }

     fun crop(row : Int, col : Int, width : Int, height : Int) : ImgPix {
         manipulatedInstance = true
         return ImgProcess.crop(this, row, col, width, height)
     }

     fun toGrayScale() : ImgPix {
         manipulatedInstance = true
         return ImgProcess.toGrayScale(this)
     }

     fun threshold(thresholdType: ThresholdType): ImgPix {
         manipulatedInstance = true
         return ImgProcess.threshold(this, thresholdType)
     }

     fun threshold(level : Int) : ImgPix {
         manipulatedInstance = true
         return ImgProcess.threshold(this, level)
     }

     fun pad(padType: PadType, padSize : Int) : ImgPix{
         manipulatedInstance = true
         return ImgProcess.pad(this, padType, padSize)
     }

     fun filter(filterType: FilterType, filterSize : Int) : ImgPix{
         manipulatedInstance = true
         return ImgProcess.filter(this, filterType, filterSize)
     }

     fun histogram() : Histogram{
        return Histogram(this)
     }

 }