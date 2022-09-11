package bumblebee

import java.nio.ByteBuffer
import kotlin.experimental.inv

class ImgProcess {
    companion object{
        fun crop(imgPix : ImgPix, row : Int, col : Int, width : Int, height : Int) : ImgPix{
            imgPix.manipulatedIntance = true
            imgPix.width = width
            imgPix.height = height

            var bytesPerPixel = imgPix.bytesPerPixel
            var pixelBufferArray = ByteBuffer.allocate(imgPix.width * imgPix.height * imgPix.bytesPerPixel)

            var startIdx = row * (width * bytesPerPixel) + col * bytesPerPixel

            for(i : Int in 0 until height){
                for(j : Int in 0 until width){
                    for(k : Int in 0 until bytesPerPixel){
                        pixelBufferArray.put(imgPix.pixelBufferArray.get(startIdx  + j * bytesPerPixel + k + (i * bytesPerPixel * imgPix.width)))
                    }
                }
            }

            imgPix.pixelBufferArray = pixelBufferArray

            return imgPix
        }

        fun invert(imgPix: ImgPix): ImgPix {

            for(i : Int in 0 until imgPix.width * imgPix.height * imgPix.bytesPerPixel){
                imgPix.pixelBufferArray.put(i, imgPix.pixelBufferArray[i].inv())
               }
            return imgPix
        }
    }
}