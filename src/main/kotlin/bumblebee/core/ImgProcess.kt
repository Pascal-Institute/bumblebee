package bumblebee.core

import bumblebee.type.ColorType
import bumblebee.type.Orientation
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.hexToInt
import java.nio.ByteBuffer
import kotlin.experimental.inv

class ImgProcess {
    companion object{
        fun crop(imgPix : ImgPix, row : Int, col : Int, width : Int, height : Int) : ImgPix {
            imgPix.manipulatedInstance = true


            val bytesPerPixel = imgPix.bytesPerPixel
            val pixelBufferArray = ByteBuffer.allocate(width * height * imgPix.bytesPerPixel)

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
            imgPix.manipulatedInstance = true
            for(i : Int in 0 until imgPix.metaData.width * imgPix.metaData.height * imgPix.bytesPerPixel){
                imgPix.pixelBufferArray.put(i, imgPix.pixelBufferArray[i].inv())
               }
            return imgPix
        }

        fun flip(imgPix : ImgPix, orientation: Orientation) : ImgPix{
            imgPix.manipulatedInstance = true
            val pixelBufferArray = ByteBuffer.allocate(imgPix.metaData.width * imgPix.metaData.height * imgPix.bytesPerPixel)

            when(orientation){
                Orientation.HORIZONTAL -> {
                    for(i : Int in 0 until imgPix.metaData.height){
                        for(j : Int in 0 until imgPix.metaData.width){
                            for(k : Int in 0 until imgPix.bytesPerPixel){
                                pixelBufferArray.put(imgPix.pixelBufferArray.get(((i + 1) * imgPix.bytesPerPixel * imgPix.metaData.width - 1) - (j * imgPix.bytesPerPixel + (imgPix.bytesPerPixel - 1) - k)))
                            }
                        }
                    }
                }

                Orientation.VERTICAL ->{
                    for(i : Int in 0 until imgPix.metaData.height){
                        for(j : Int in 0 until imgPix.metaData.width){
                            for(k : Int in 0 until imgPix.bytesPerPixel){
                                pixelBufferArray.put(imgPix.pixelBufferArray.get(imgPix.metaData.width * (imgPix.metaData.height - (i + 1)) * imgPix.bytesPerPixel + j * imgPix.bytesPerPixel + k))
                            }
                        }
                    }
                }
            }

            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun toGrayScale(imgPix : ImgPix) : ImgPix{
            imgPix.manipulatedInstance = true

            val oldBytesPerPixel = imgPix.bytesPerPixel

            imgPix.bytesPerPixel = 1
            imgPix.metaData.colorType = ColorType.GRAY_SCALE

            val pixelBufferArray = ByteBuffer.allocate(imgPix.metaData.width * imgPix.metaData.height * imgPix.bytesPerPixel)

            for(i : Int in 0 until imgPix.metaData.height){
                for(j : Int in 0 until imgPix.metaData.width){
                    for(k : Int in 0 until oldBytesPerPixel){
                        pixelBufferArray.put(i * imgPix.metaData.width + j, imgPix.pixelBufferArray.get((i * oldBytesPerPixel* imgPix.metaData.width) + (j * oldBytesPerPixel) + k))
                    }
                }
            }

            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun threshold(imgPix: ImgPix, level : Int) : ImgPix{
            if(imgPix.metaData.colorType != ColorType.GRAY_SCALE){
               toGrayScale(imgPix)
            }

            for(i : Int in 0 until imgPix.metaData.width * imgPix.metaData.height * imgPix.bytesPerPixel){
                val byte = if (hexToInt(byteToHex(imgPix.pixelBufferArray.get(i))) > level) (255).toByte() else (0).toByte()
                imgPix.pixelBufferArray.put(i, byte)
            }

            return imgPix
        }

    }
}