package bumblebee.core

import bumblebee.color.Color
import bumblebee.type.ColorType
import bumblebee.type.OrientationType
import bumblebee.type.PadType
import bumblebee.type.ThresholdType
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.colorToByte
import bumblebee.util.Converter.Companion.hexToInt
import bumblebee.util.Histogram
import java.nio.ByteBuffer
import kotlin.experimental.inv

class ImgProcess {
    companion object{
        fun set(imgPix : ImgPix, row : Int, col : Int, color : Color) : ImgPix {
            if (imgPix.metaData.colorType != color.colorType){
                System.err.println("ERROR : ColorType does not match")
            }else{
                val byteArray : ByteArray = colorToByte(color)
                for (i : Int in 0 until imgPix.bytesPerPixel){
                    imgPix.pixelBufferArray.put(i + imgPix.bytesPerPixel * col + (imgPix.metaData.width * imgPix.bytesPerPixel) * row, byteArray[i])
                }
            }
            return imgPix
        }

        fun crop(imgPix : ImgPix, row : Int, col : Int, width : Int, height : Int) : ImgPix {

            val bytesPerPixel = imgPix.bytesPerPixel
            val pixelBufferArray = ByteBuffer.allocate(width * height * bytesPerPixel)
            val startIdx = row * (imgPix.metaData.width * bytesPerPixel) + col * bytesPerPixel

            for(i : Int in 0 until height){
                for(j : Int in 0 until width){
                    for(k : Int in 0 until bytesPerPixel){
                        pixelBufferArray.put(imgPix.pixelBufferArray.get(startIdx  + j * bytesPerPixel + k + (i * bytesPerPixel * imgPix.metaData.width)))
                    }
                }
            }

            imgPix.metaData.width = width
            imgPix.metaData.height = height
            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun invert(imgPix: ImgPix) : ImgPix {

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel

            for(i : Int in 0 until width * height * bytesPerPixel){
                imgPix.pixelBufferArray.put(i, imgPix.pixelBufferArray[i].inv())
               }
            return imgPix
        }

        fun flip(imgPix : ImgPix, orientation: OrientationType) : ImgPix{

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel

            val pixelBufferArray = ByteBuffer.allocate(width * height * bytesPerPixel)

            when(orientation){
                OrientationType.HORIZONTAL -> {
                    for(i : Int in 0 until height){
                        for(j : Int in 0 until width){
                            for(k : Int in 0 until bytesPerPixel){
                                pixelBufferArray.put(imgPix.pixelBufferArray.get(((i + 1) * bytesPerPixel * width - 1) - (j * bytesPerPixel + (bytesPerPixel - 1) - k)))
                            }
                        }
                    }
                }

                OrientationType.VERTICAL ->{
                    for(i : Int in 0 until height){
                        for(j : Int in 0 until width){
                            for(k : Int in 0 until bytesPerPixel){
                                pixelBufferArray.put(imgPix.pixelBufferArray.get(width * (height - (i + 1)) * bytesPerPixel + j * bytesPerPixel + k))
                            }
                        }
                    }
                }
            }

            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun toGrayScale(imgPix : ImgPix) : ImgPix{
            val oldBytesPerPixel = imgPix.bytesPerPixel

            val width = imgPix.width
            val height = imgPix.height

            imgPix.bytesPerPixel = 1
            imgPix.metaData.colorType = ColorType.GRAY_SCALE

            val pixelBufferArray = ByteBuffer.allocate(width * height * imgPix.bytesPerPixel)

            for(i : Int in 0 until height){
                for(j : Int in 0 until width){
                    for(k : Int in 0 until oldBytesPerPixel){
                        pixelBufferArray.put(i * width + j, imgPix.pixelBufferArray.get((i * oldBytesPerPixel* width) + (j * oldBytesPerPixel) + k))
                    }
                }
            }

            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun threshold(imgPix: ImgPix, level : Int) : ImgPix{

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel

            if(imgPix.metaData.colorType != ColorType.GRAY_SCALE){
               toGrayScale(imgPix)
            }

            for(i : Int in 0 until width * height * bytesPerPixel){
                val byte = if (hexToInt(byteToHex(imgPix.pixelBufferArray.get(i))) > level) (255).toByte() else (0).toByte()
                imgPix.pixelBufferArray.put(i, byte)
            }

            return imgPix
        }

        fun threshold(imgPix : ImgPix, thresholdType : ThresholdType) : ImgPix{
            if(imgPix.metaData.colorType != ColorType.GRAY_SCALE){
                toGrayScale(imgPix)
            }

            when(thresholdType){
                ThresholdType.OTSU -> {
                    return threshold(imgPix, Histogram(imgPix).getOtsuLevel())
                }
                else -> {}
            }

            return imgPix
        }

        fun  pad(imgPix: ImgPix, padType: PadType, padPixelSize: Int) : ImgPix{

            val width = padPixelSize + imgPix.width + padPixelSize
            val height = padPixelSize + imgPix.height + padPixelSize
            val bytesPerPixel = imgPix.bytesPerPixel

            val pixelBufferArray = ByteBuffer.allocate(width * height * bytesPerPixel)

            when(padType){
                PadType.ZERO -> {
                }

                PadType.AVERAGE -> {
                    var averagePixel = Histogram(imgPix).getAverage(imgPix.metaData.colorType)

                    for(i : Int in 0 until height){
                        for(j : Int in 0 until width){
                            for(k : Int in 0 until bytesPerPixel){
                                pixelBufferArray.put(averagePixel[k])
                            }
                        }
                    }
                }
            }


            imgPix.metaData.width = width
            imgPix.metaData.height = height

            for(i : Int in padPixelSize * bytesPerPixel until height - padPixelSize * bytesPerPixel){
                for(j : Int in padPixelSize * bytesPerPixel until width - padPixelSize * bytesPerPixel){
                    for(k : Int in 0 until bytesPerPixel){
                        pixelBufferArray.put(j * bytesPerPixel + k + i * bytesPerPixel * width,
                            imgPix.pixelBufferArray.get((j-padPixelSize) * bytesPerPixel + k + (i-padPixelSize) * bytesPerPixel * (width - 2 * padPixelSize)))
                    }
                }
            }

            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }
    }
}