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
                    imgPix.pixelByteBuffer.put(i + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row, byteArray[i])
                }
            }
            return imgPix
        }

        fun crop(imgPix : ImgPix, row : Int, col : Int, width : Int, height : Int) : ImgPix {

            val bytesPerPixel = imgPix.bytesPerPixel
            val pixelByteBuffer = ByteBuffer.allocate(width * height * bytesPerPixel)
            val startIdx = row * (imgPix.width * bytesPerPixel) + col * bytesPerPixel

            for(i : Int in 0 until height){
                for(j : Int in 0 until width){
                    for(k : Int in 0 until bytesPerPixel){
                        pixelByteBuffer.put(imgPix.pixelByteBuffer.get(startIdx  + j * bytesPerPixel + k + (i * bytesPerPixel * imgPix.width)))
                    }
                }
            }

            imgPix.metaData.width = width
            imgPix.metaData.height = height
            imgPix.pixelByteBuffer = pixelByteBuffer

            return imgPix
        }

        fun invert(imgPix: ImgPix) : ImgPix {

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel

            for(i : Int in 0 until width * height * bytesPerPixel){
                imgPix.pixelByteBuffer.put(i, imgPix.pixelByteBuffer[i].inv())
               }
            return imgPix
        }

        fun flip(imgPix : ImgPix, orientation: OrientationType) : ImgPix{

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel

            val pixelByteBuffer = ByteBuffer.allocate(width * height * bytesPerPixel)

            when(orientation){
                OrientationType.HORIZONTAL -> {
                    for(i : Int in 0 until height){
                        for(j : Int in 0 until width){
                            for(k : Int in 0 until bytesPerPixel){
                                pixelByteBuffer.put(imgPix.pixelByteBuffer.get(((i + 1) * bytesPerPixel * width - 1) - (j * bytesPerPixel + (bytesPerPixel - 1) - k)))
                            }
                        }
                    }
                }

                OrientationType.VERTICAL ->{
                    for(i : Int in 0 until height){
                        for(j : Int in 0 until width){
                            for(k : Int in 0 until bytesPerPixel){
                                pixelByteBuffer.put(imgPix.pixelByteBuffer.get(width * (height - (i + 1)) * bytesPerPixel + j * bytesPerPixel + k))
                            }
                        }
                    }
                }
            }

            imgPix.pixelByteBuffer = pixelByteBuffer

            return imgPix
        }

        fun toGrayScale(imgPix : ImgPix) : ImgPix{
            val oldBytesPerPixel = imgPix.bytesPerPixel

            val width = imgPix.width
            val height = imgPix.height

            imgPix.bytesPerPixel = 1
            imgPix.metaData.colorType = ColorType.GRAY_SCALE

            val pixelByteBuffer = ByteBuffer.allocate(width * height * imgPix.bytesPerPixel)

            for(i : Int in 0 until height){
                for(j : Int in 0 until width){
                    var integer = 0
                    for(k : Int in 0 until oldBytesPerPixel){
                         integer += imgPix.pixelByteBuffer.get((i * oldBytesPerPixel* width) + (j * oldBytesPerPixel) + k).toUByte().toInt()
                    }
                    pixelByteBuffer.put(i * width + j, (integer / oldBytesPerPixel).toByte())
                }
            }

            imgPix.pixelByteBuffer = pixelByteBuffer

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
                val byte = if (imgPix.pixelByteBuffer.get(i).byteToInt() > level) (255).toByte() else (0).toByte()
                imgPix.pixelByteBuffer.put(i, byte)
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

            val pixelByteBuffer = ByteBuffer.allocate(width * height * bytesPerPixel)

            when(padType){
                PadType.ZERO -> {
                    for(i : Int in 0 until height){
                        for(j : Int in 0 until width){
                            for(k : Int in 0 until bytesPerPixel){
                                pixelByteBuffer.put(0)
                            }
                        }
                    }
                }

                PadType.AVERAGE -> {
                    var averagePixel = Histogram(imgPix).getAverage(imgPix.colorType)

                    for(i : Int in 0 until height){
                        for(j : Int in 0 until width){
                            for(k : Int in 0 until bytesPerPixel){
                                pixelByteBuffer.put(averagePixel[k])
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
                        pixelByteBuffer.put(j * bytesPerPixel + k + i * bytesPerPixel * width,
                            imgPix.pixelByteBuffer.get((j-padSize) * bytesPerPixel + k + (i-padSize) * bytesPerPixel * (width - 2 * padSize)))
                    }
                }
            }

            imgPix.pixelByteBuffer = pixelByteBuffer

            return imgPix
        }

        /**
         *
         * This function process image filtering : Average, Median
         *
         * @param[imgPix] The ImgPix
         * @param[filterType] The FilterType
         * @param[filterSize] The filterSize it will be odd number
         * @return[imgPix]
         */
        fun filter(imgPix: ImgPix, filterType : FilterType, filterSize : Int) : ImgPix{

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel
            val pixelByteBuffer = ByteBuffer.allocate(width * height * bytesPerPixel)

            when(filterType){
                FilterType.AVERAGE->{

                    val windowSize = filterSize * filterSize
                    val halfFilterSize = filterSize / 2

                    val tempImgPix = imgPix.pad(PadType.AVERAGE, halfFilterSize)
                    val padImgPixWidth = tempImgPix.width
                    val temppixelByteBuffer = tempImgPix.pixelByteBuffer

                    for(i : Int in halfFilterSize until height + halfFilterSize){
                        for(j : Int in halfFilterSize until width + halfFilterSize){
                            for(k : Int in 0 until bytesPerPixel){
                                var intValue = 0
                                for(l : Int in 0 until windowSize){
                                       intValue += temppixelByteBuffer.get(((i - halfFilterSize + (l % filterSize)) * bytesPerPixel * padImgPixWidth) + ((j - halfFilterSize + (l / filterSize)) * bytesPerPixel) + k).toUByte().toInt()
                                }
                                pixelByteBuffer.put((intValue/windowSize).toByte())
                            }
                        }
                    }
                }

                FilterType.MEDIAN->{

                    val windowSize = filterSize * filterSize
                    val middleSize = windowSize/2
                    val halfFilterSize = filterSize/2

                    val tempImgPix = imgPix.pad(PadType.AVERAGE, halfFilterSize)
                    val padImgPixWidth = tempImgPix.width
                    val temppixelByteBuffer = tempImgPix.pixelByteBuffer

                    for(i : Int in halfFilterSize until height + halfFilterSize){
                        for(j : Int in halfFilterSize until width + halfFilterSize){
                            repeat(bytesPerPixel){
                                k->
                                var uByteArray = UByteArray(windowSize)
                                repeat(windowSize){
                                    l-> uByteArray[l] = (temppixelByteBuffer.get(((i - halfFilterSize + (l % filterSize)) * bytesPerPixel * padImgPixWidth) + ((j - halfFilterSize + (l / filterSize)) * bytesPerPixel) + k)).toUByte()
                                }
                                uByteArray.sort()
                                pixelByteBuffer.put((uByteArray[middleSize]).toByte())
                            }
                        }
                    }

                }

                FilterType.SHARPEN -> {
                    val padSize = 1
                    val windowSize = 9
                    val filterSize = 3
                    val strength = 9
                    val exceptStrength = -1
                    val tempImgPix = imgPix.pad(PadType.AVERAGE, padSize)
                    val padImgPixWidth = tempImgPix.width
                    val temppixelByteBuffer = tempImgPix.pixelByteBuffer

                    for(i : Int in padSize until height + padSize){
                        for(j : Int in padSize until width + padSize){
                            for(k : Int in 0 until bytesPerPixel){
                                var intValue = 0
                                for(l : Int in 0 until windowSize){
                                    var temp = (temppixelByteBuffer.get(((i - padSize + (l % filterSize)) * bytesPerPixel * padImgPixWidth) + ((j - padSize + (l / filterSize)) * bytesPerPixel) + k).toInt())
                                    intValue += if(l == 4){
                                        strength * temp
                                    }else{
                                        exceptStrength * temp
                                    }
                                }
                                pixelByteBuffer.put(intValue.toByte())
                            }
                        }
                    }

                }

                else->{}
            }

            imgPix.metaData.width = width
            imgPix.metaData.height = height
            imgPix.pixelByteBuffer = pixelByteBuffer

            return imgPix
        }
    }
}