package bumblebee

import bumblebee.Converter.Companion.byteToHex
import bumblebee.Converter.Companion.hexToInt
import bumblebee.Converter.Companion.longToByteArray
import java.util.zip.CRC32
import java.util.zip.Checksum

class Chunk {
    var length: ByteArray = ByteArray(4)
    var type: ByteArray = ByteArray(4)
    lateinit var data: ByteArray
    var crc: ByteArray = ByteArray(4)

    fun initData(size: Int) {
        data = ByteArray(size)
    }

    fun getWidth(byteArray: ByteArray): Int {
        return hexToInt(byteToHex(byteArray))
    }

    fun getHeight(byteArray: ByteArray): Int {
        return hexToInt(byteToHex(byteArray))
    }

    fun getLength(): Int {
        var string = byteToHex(length)
        return hexToInt(string)
    }

    fun getColorType(byte: Byte): Int {
        return hexToInt(byteToHex(byte))
    }

    fun getBitDepth(byte: Byte): Int {
        return hexToInt(byteToHex(byte))
    }

    fun getCRC(): ByteArray {
        val checksum: Checksum = CRC32()
        var source = type + data
        checksum.update(source, 0, source.size)
        return longToByteArray(checksum.value, 4)
    }

    fun generateData(imgPix: ImgPix) {
        var byteArray = imgPix.get()
        data = ByteArray(imgPix.metaData.height * (imgPix.metaData.width * imgPix.bytesPerPixel + 1))
        var count = 0
        data.forEachIndexed { index, byte ->
            if(index % (imgPix.metaData.width * imgPix.bytesPerPixel + 1) == 0){
                data[index] = 0
            }else{
                data[index] = byteArray[count]
                count++
            }

        }
    }
}

