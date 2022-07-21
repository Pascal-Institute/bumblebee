package bumblebee


class Chunk {
    var length : ByteArray = ByteArray(4)
    var type : ByteArray = ByteArray(4)
    lateinit var data : ByteArray
    var crc : ByteArray = ByteArray(4)

    init {

    }

    fun getLength(){

        var string = ""

        length.forEach {

            var first =  it.toUByte().toInt() / 16
            var second = it.toUByte().toInt() % 16

            string += String.format("%02X", first)
            string += String.format("%02X", second)
        }

        println(string)
    }
}