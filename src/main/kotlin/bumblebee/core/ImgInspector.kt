package bumblebee.core

import bumblebee.color.Color
import bumblebee.color.GRAY
import bumblebee.color.RGB
import bumblebee.color.RGBA
import bumblebee.util.Converter.Companion.toHex

class ImgInspector {
    companion object{
        fun getColorAt(imgPix: ImgPix, row : Int, col: Int) : Color {
            return when(imgPix.bytesPerPixel){

                1-> GRAY(imgPix.pixelByteBuffer.get(imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row).toUByte().toInt())

                3-> RGB(
                    imgPix.pixelByteBuffer.get(0 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row).toUByte().toInt(),
                    imgPix.pixelByteBuffer.get(1 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row).toUByte().toInt(),
                    imgPix.pixelByteBuffer.get(2 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row).toUByte().toInt())

                //GBAR to RGBA
                4-> RGBA(
                    imgPix.pixelByteBuffer.get(3 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row).toUByte().toInt(),
                    imgPix.pixelByteBuffer.get(0 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row).toUByte().toInt(),
                    imgPix.pixelByteBuffer.get(1 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row).toUByte().toInt(),
                    imgPix.pixelByteBuffer.get(2 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row).toUByte().toInt()
                )

                else->{
                    GRAY(0)
                }
            }
        }

        fun getHexStringAt(imgPix: ImgPix, row : Int, col : Int) : String{
            val byteArray = ByteArray(imgPix.bytesPerPixel)
            for (i : Int in 0 until imgPix.bytesPerPixel){
                byteArray[i] = imgPix.pixelByteBuffer.get(i + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row)
            }
            return byteArray.toHex()
        }

    }
}