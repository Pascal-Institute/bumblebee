package bumblebee.core

import java.nio.ByteBuffer
import kotlin.experimental.inv

class ImgProcess {
    companion object{
        fun crop(imgPix : ImgPix, row : Int, col : Int, width : Int, height : Int) : ImgPix {
            imgPix.manipulatedInstance = true
            imgPix.metaData.width = width
            imgPix.metaData.height = height

            var bytesPerPixel = imgPix.bytesPerPixel
            var pixelBufferArray = ByteBuffer.allocate(imgPix.metaData.width * imgPix.metaData.height * imgPix.bytesPerPixel)

            var startIdx = row * (width * bytesPerPixel) + col * bytesPerPixel

            for(i : Int in 0 until height){
                for(j : Int in 0 until width){
                    for(k : Int in 0 until bytesPerPixel){
                        pixelBufferArray.put(imgPix.pixelBufferArray.get(startIdx  + j * bytesPerPixel + k + (i * bytesPerPixel * imgPix.metaData.width)))
                    }
                }
            }

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

        fun flip(imgPix : ImgPix) : ImgPix{
            imgPix.manipulatedInstance = true
            var pixelBufferArray = ByteBuffer.allocate(imgPix.metaData.width * imgPix.metaData.height * imgPix.bytesPerPixel)
            for(i : Int in 0 until imgPix.metaData.height){
                for(j : Int in 0 until imgPix.metaData.width){
                    for(k : Int in 0 until imgPix.bytesPerPixel){
                        pixelBufferArray.put(imgPix.pixelBufferArray.get(((i + 1) * imgPix.bytesPerPixel * imgPix.metaData.width - 1) - (j * imgPix.bytesPerPixel + (imgPix.bytesPerPixel - 1) - k)))
                    }
                }
            }
            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun toGrayScale(imgPix : ImgPix) : ImgPix{
            imgPix.manipulatedInstance = true
            var pixelBufferArray = ByteBuffer.allocate(imgPix.metaData.width * imgPix.metaData.height * imgPix.bytesPerPixel)
            for(i : Int in 0 until imgPix.metaData.height){
                for(j : Int in 0 until imgPix.metaData.width){
                    for(k : Int in 0 until imgPix.bytesPerPixel){
                        pixelBufferArray.put(imgPix.pixelBufferArray.get(((i + 1) * imgPix.bytesPerPixel * imgPix.metaData.width - 1) - (j * imgPix.bytesPerPixel + k) - ((imgPix.bytesPerPixel - 1) - k)))
                    }
                }
            }
            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }


    }
}