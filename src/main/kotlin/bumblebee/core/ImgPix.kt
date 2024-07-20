 package bumblebee.core

import bumblebee.FileManager
import bumblebee.color.Color
import bumblebee.type.*
import bumblebee.util.Histogram
import komat.Element
import komat.space.Cube
import komat.space.Mat
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants


 open class ImgPix() : Cloneable {

    val metaData = MetaData(FileType.PIX, 0, 0, ColorType.GRAY_SCALE)
    private var isManipulated = false

    //property getter
    val fileType : FileType
        get() = metaData.fileType
    val width : Int
        get() = metaData.width
    val height : Int
        get() = metaData.height
    val colorType : ColorType
        get() = metaData.colorType
    val bytesPerPixel : Int
        get() = metaData.colorType.bytesPerPixel

     //depth : row
     //row : column
     //column : color size
    var cube = Cube(0,0,0)

    constructor(width: Int, height: Int, colorType: ColorType) : this() {
        metaData.width = width
        metaData.height = height
        metaData.colorType = colorType
        cube = Cube(width, height, colorType.bytesPerPixel)
     }

    constructor(filePath : String) : this() {
        val imgPix  = FileManager.read(filePath)
        metaData.width = imgPix.width
        metaData.height = imgPix.height
        metaData.colorType = imgPix.colorType
        cube = imgPix.cube
    }

     public override fun clone(): ImgPix {
        return super.clone() as ImgPix
    }

    fun show(){
        val buffer = DataBufferByte(cube.elements.map { it.toByte() }.toByteArray(), cube.elements.size)

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
     open fun setMetaData(packet: Packet){}

     //ImgHandler
     fun getColorAt(row : Int, col: Int) : Color {
         return ImgInspector.getColorAt(this, row, col)
     }

     fun getHexStringAt(row : Int, col : Int) : String{
         return ImgInspector.getHexStringAt(this, row, col)
     }

     fun histogram() : Histogram {
         return Histogram(this)
     }

     fun set(row : Int, col : Int, color : Color) : ImgPix {
         return ImgProcessor.set(this, row, col, color)
     }

     fun invert() : ImgPix {
         isManipulated = true
         return ImgProcessor.invert(this)
     }

     fun flip(orientation: OrientationType) : ImgPix {
         isManipulated = true
         return ImgProcessor.flip(this, orientation)
     }

     fun rotate(degree : Int) : ImgPix {
         isManipulated = true
         return ImgProcessor.rotate(this, degree)
     }

     fun threshold(thresholdType: ThresholdType): ImgPix {
         isManipulated = true
         return ImgProcessor.threshold(this, thresholdType)
     }

     fun threshold(level : Int) : ImgPix {
         isManipulated = true
         return ImgProcessor.threshold(this, level)
     }

     fun getChannel(channelIndex : Int) : ImgPix{
         val copy = this.clone()
         return ImgProcessor.getChannel(copy, channelIndex)
     }

     fun toGrayScale() : ImgPix {
         isManipulated = true
         return ImgProcessor.toGrayScale(this)
     }

     fun resize(width: Int, height: Int) : ImgPix {
         isManipulated = true
         return ImgProcessor.resize(this, width, height)
     }

     fun crop(row : Int, col : Int, width : Int, height : Int) : ImgPix {
         isManipulated = true
         return ImgProcessor.crop(this, row, col, width, height)
     }

     fun pad(padType: PadType, padSize : Int) : ImgPix{
         isManipulated = true
         return ImgProcessor.pad(this, padType, padSize)
     }

     fun filter(filterType: FilterType, filterSize : Int) : ImgPix{
         isManipulated = true
         return ImgProcessor.filter(this, filterType, filterSize)
     }

     fun filter(filterType: FilterType, filterSize : Int, stdev : Double) : ImgPix{
         isManipulated = true
         return ImgProcessor.filter(this, filterType, filterSize, stdev)
     }


 }