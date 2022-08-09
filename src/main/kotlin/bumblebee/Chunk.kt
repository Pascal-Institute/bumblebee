package bumblebee

import bumblebee.Converter.Companion.convertByteToHex
import bumblebee.Converter.Companion.convertHexToInt
import bumblebee.Converter.Companion.convertLongToByteArray
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
        return convertHexToInt(convertByteToHex(byteArray))
    }

    fun getHeight(byteArray: ByteArray): Int {
        return convertHexToInt(convertByteToHex(byteArray))
    }

    fun getLength(): Int {
        var string = convertByteToHex(length)
        return convertHexToInt(string)
    }

    fun getColorType(byte: Byte): Int {
        return convertHexToInt(convertByteToHex(byte))
    }

    fun getBitDepth(byte: Byte): Int {
        return convertHexToInt(convertByteToHex(byte))
    }

    fun getCRC(): ByteArray {
        val checksum: Checksum = CRC32()
        var source = type + data
        checksum.update(source, 0, source.size)
        return convertLongToByteArray(checksum.value, 4)
    }

    fun generateData(byteArray: ByteArray) : ByteArray{
        return byteArray
    }
}

