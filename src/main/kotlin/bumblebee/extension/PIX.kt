package bumblebee.extension

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.type.FileType
import bumblebee.util.Converter.Companion.byteToInt
import komat.space.Cube
import komat.Element

class PIX(private var byteArray : ByteArray) : ImgPix() {

    /*
    * Signature 3 Byte
    * Width     4 Byte
    * Height    4 Byte
    * ColorType 1 Byte
    *
    * DATA      [Width * Height * ColorType] Byte
    *
    * */

    init {
        extract()
    }

    override fun extract() {
        metaData.fileType = FileType.PIX

        metaData.width = byteArray.copyOfRange(3, 7).byteToInt()
        metaData.height = byteArray.copyOfRange(7, 11).byteToInt()
        metaData.colorType = ColorType.fromInt(byteArray[11].byteToInt())
        cube = Cube(metaData.width, metaData.height, metaData.colorType.bytesPerPixel, Element(0.toByte()))
        cube = Cube(metaData.width, metaData.height, metaData.colorType.bytesPerPixel, Element(0.toByte()))
        cube.elements = byteArray.copyOfRange(12 , 12 + metaData.width * metaData.height * metaData.colorType.bytesPerPixel).map { Element(it) }.toTypedArray()
    }
}