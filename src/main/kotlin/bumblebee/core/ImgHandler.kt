package bumblebee.core

import bumblebee.color.Color
import bumblebee.type.FilterType
import bumblebee.type.OrientationType
import bumblebee.type.PadType
import bumblebee.type.ThresholdType
import bumblebee.util.Histogram

open class ImgHandler(){
    lateinit var imgPix : ImgPix
    private var isManipulated = false

    fun getColorAt(row : Int, col: Int) : Color{
        return ImgInspector.getColorAt(imgPix, row, col)
    }

    fun getHexStringAt(row : Int, col : Int) : String{
        return ImgInspector.getHexStringAt(imgPix, row, col)
    }

    fun histogram() : Histogram {
        return Histogram(imgPix)
    }

    fun set(row : Int, col : Int, color : Color) : ImgPix {
        return ImgProcessor.set(imgPix, row, col, color)
    }

    fun invert() : ImgPix {
        isManipulated = true
        return ImgProcessor.invert(imgPix)
    }

    fun flip(orientation: OrientationType) : ImgPix {
        isManipulated = true
        return ImgProcessor.flip(imgPix, orientation)
    }

    fun rotate(degree : Int) : ImgPix {
        isManipulated = true
        return ImgProcessor.rotate(imgPix, degree)
    }

    fun threshold(thresholdType: ThresholdType): ImgPix {
        isManipulated = true
        return ImgProcessor.threshold(imgPix, thresholdType)
    }

    fun threshold(level : Int) : ImgPix {
        isManipulated = true
        return ImgProcessor.threshold(imgPix, level)
    }

    fun getChannel(channelIndex : Int) : ImgPix{
        val copy = imgPix.clone()
        return ImgProcessor.getChannel(copy, channelIndex)
    }

    fun toGrayScale() : ImgPix {
        isManipulated = true
        return ImgProcessor.toGrayScale(imgPix)
    }

    fun resize(width: Int, height: Int) : ImgPix {
        isManipulated = true
        return ImgProcessor.resize(imgPix, width, height)
    }

    fun crop(row : Int, col : Int, width : Int, height : Int) : ImgPix {
        isManipulated = true
        return ImgProcessor.crop(imgPix, row, col, width, height)
    }

    fun pad(padType: PadType, padSize : Int) : ImgPix{
        isManipulated = true
        return ImgProcessor.pad(imgPix, padType, padSize)
    }

    fun filter(filterType: FilterType, filterSize : Int) : ImgPix{
        isManipulated = true
        return ImgProcessor.filter(imgPix, filterType, filterSize)
    }

    fun filter(filterType: FilterType, filterSize : Int, stdev : Double) : ImgPix{
        isManipulated = true
        return ImgProcessor.filter(imgPix, filterType, filterSize, stdev)
    }
}