package bumblebee.core

import bumblebee.color.Color
import bumblebee.color.GRAY
import bumblebee.color.RGB
import bumblebee.color.RGBA
import bumblebee.util.Converter.Companion.toHex
import komat.space.Vect

class ImgInspector {
    companion object{
        fun getColorAt(imgPix: ImgPix, row : Int, col: Int) : Color {
            return when(imgPix.bytesPerPixel){

                1-> GRAY((imgPix.mat[imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte).toUByte().toInt())

                3-> RGB(
                    (imgPix.mat[0 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte).toUByte().toInt(),
                    (imgPix.mat[1 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte).toUByte().toInt(),
                    (imgPix.mat[2 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte).toUByte().toInt())

                //GBAR to RGBA
                4-> RGBA(
                    (imgPix.mat[3 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte).toUByte().toInt(),
                    (imgPix.mat[0 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte).toUByte().toInt(),
                    (imgPix.mat[1 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte).toUByte().toInt(),
                    (imgPix.mat[2 + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte).toUByte().toInt()
                )

                else->{
                    GRAY(0)
                }
            }
        }

        fun getHexStringAt(imgPix: ImgPix, row : Int, col : Int) : String{
            val vect = ByteArray(imgPix.bytesPerPixel)
            for (i : Int in 0 until imgPix.bytesPerPixel){
                vect[i] = imgPix.mat[i + imgPix.bytesPerPixel * col + (imgPix.width * imgPix.bytesPerPixel) * row] as Byte
            }
            return vect.toHex()
        }

    }
}