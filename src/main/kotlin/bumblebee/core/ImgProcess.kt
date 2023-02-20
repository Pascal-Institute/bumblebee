package bumblebee.core

import bumblebee.color.Color
import bumblebee.type.*
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.colorToByte
import bumblebee.util.Histogram
import java.nio.ByteBuffer
import kotlin.experimental.inv

class ImgProcess {
    companion object{
        fun set(imgPix : ImgPix, row : Int, col : Int, color : Color) : ImgPix {
            if (imgPix.colorType != color.colorType){
                System.err.println("ERROR : ColorType does not match")
            }else{
                val byteArray : ByteArray = colorToByte(color)
                for (i : Int in 0 until imgPix.bytesPerPixel){
                    imgPix.pixelBufferArray.put(i + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row, byteArray[i])
                }
            }
            return imgPix
        }

        fun crop(imgPix : ImgPix, row : Int, col : Int, width : Int, height : Int) : ImgPix {

            val bytesPerPixel = imgPix.bytesPerPixel
            val pixelBufferArray = ByteBuffer.allocate(width * height * bytesPerPixel)
            val startIdx = row * (imgPix.width * bytesPerPixel) + col * bytesPerPixel

            for(i : Int in 0 until height){
                for(j : Int in 0 until width){
                    for(k : Int in 0 until bytesPerPixel){
                        pixelBufferArray.put(imgPix.pixelBufferArray.get(startIdx  + j * bytesPerPixel + k + (i * bytesPerPixel * imgPix.width)))
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
                    var integer = 0
                    for(k : Int in 0 until oldBytesPerPixel){
                         integer += imgPix.pixelBufferArray.get((i * oldBytesPerPixel* width) + (j * oldBytesPerPixel) + k).toUByte().toInt()
                    }
                    pixelBufferArray.put(i * width + j, (integer / oldBytesPerPixel).toByte())
                }
            }

            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun threshold(imgPix: ImgPix, level : Int) : ImgPix{

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel

            if(imgPix.colorType != ColorType.GRAY_SCALE){
               toGrayScale(imgPix)
            }

            for(i : Int in 0 until width * height * bytesPerPixel){
                val byte = if (byteToInt(imgPix.pixelBufferArray.get(i)) > level) (255).toByte() else (0).toByte()
                imgPix.pixelBufferArray.put(i, byte)
            }

            return imgPix
        }

        fun threshold(imgPix : ImgPix, thresholdType : ThresholdType) : ImgPix{
            if(imgPix.colorType != ColorType.GRAY_SCALE){
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

        fun  pad(imgPix: ImgPix, padType: PadType, padSize: Int) : ImgPix{

            val width = padSize + imgPix.width + padSize
            val height = padSize + imgPix.height + padSize
            val bytesPerPixel = imgPix.bytesPerPixel

            val pixelBufferArray = ByteBuffer.allocate(width * height * bytesPerPixel)

            when(padType){
                PadType.ZERO -> {
                    for(i : Int in 0 until height){
                        for(j : Int in 0 until width){
                            for(k : Int in 0 until bytesPerPixel){
                                pixelBufferArray.put(0)
                            }
                        }
                    }
                }

                PadType.AVERAGE -> {
                    var averagePixel = Histogram(imgPix).getAverage(imgPix.colorType)

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

            for(i : Int in padSize until height - padSize){
                for(j : Int in padSize  until width - padSize){
                    for(k : Int in 0 until bytesPerPixel){
                        pixelBufferArray.put(j * bytesPerPixel + k + i * bytesPerPixel * width,
                            imgPix.pixelBufferArray.get((j-padSize) * bytesPerPixel + k + (i-padSize) * bytesPerPixel * (width - 2 * padSize)))
                    }
                }
            }

            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun filter(imgPix: ImgPix, filterType : FilterType, filterSize : Int) : ImgPix{

            when(filterType){
                FilterType.AVERAGE->{

                    val width = imgPix.width
                    val height = imgPix.height
                    val tempImgPix = imgPix.pad(PadType.AVERAGE,filterSize/2)
                    println(tempImgPix.width)
                    val tempPixelBufferArray = tempImgPix.pixelBufferArray
                    val pixelBufferArray = ByteBuffer.allocate(width * height * imgPix.bytesPerPixel)

                    for(i : Int in filterSize/2 until height + filterSize/2){
                        for(j : Int in filterSize/2 until width + filterSize/2){
                            for(k : Int in 0 until imgPix.bytesPerPixel){
                                var intValue = 0
                                for(l : Int in 0 until filterSize){
                                    for(m : Int in 0 until filterSize){
                                       intValue += tempPixelBufferArray.get(((i - filterSize/2 + m) * imgPix.bytesPerPixel * tempImgPix.width) + ((j - filterSize/2 + l) * imgPix.bytesPerPixel) + k).toUByte().toInt()
                                    }
                                }
                                pixelBufferArray.put((intValue/(filterSize*filterSize)).toByte())
                            }
                        }
                    }

                    imgPix.metaData.width = width
                    imgPix.metaData.height = height
                    imgPix.pixelBufferArray = pixelBufferArray
                }

                FilterType.MEDIAN->{

                    val width = imgPix.width
                    val height = imgPix.height
                    val tempImgPix = imgPix.pad(PadType.AVERAGE,filterSize/2)
                    val tempPixelBufferArray = tempImgPix.pixelBufferArray
                    val pixelBufferArray = ByteBuffer.allocate(width * height * imgPix.bytesPerPixel)

                    for(i : Int in filterSize/2 until height + filterSize/2){
                        for(j : Int in filterSize/2 until width + filterSize/2){
                            for(k : Int in 0 until imgPix.bytesPerPixel){
                                var list = mutableListOf<UByte>()
                                for(l : Int in 0 until filterSize){
                                    for(m : Int in 0 until filterSize){
                                        list.add(tempPixelBufferArray.get(((i - filterSize/2 + l) * imgPix.bytesPerPixel * tempImgPix.width) + ((j - filterSize/2 + m) * imgPix.bytesPerPixel) + k).toUByte())
                                    }
                                }
                                list.sort()
                                pixelBufferArray.put((list[(filterSize * filterSize)/2]).toByte())
                            }
                        }
                    }

                    imgPix.metaData.width = width
                    imgPix.metaData.height = height
                    imgPix.pixelBufferArray = pixelBufferArray
                }
            }

            return imgPix
        }
    }
}