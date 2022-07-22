package bumblebee


class Chunk {
    var length : ByteArray = ByteArray(4)
    var type : ByteArray = ByteArray(4)
    lateinit var data : ByteArray
    var crc : ByteArray = ByteArray(4)

    fun initData(size : Int) {
        data = ByteArray(size)
    }
    private fun convertHexToInt(hexString :String) : Int{
        var coef = 1
        var num = 0

        hexString.reversed().forEach {

            var hex = when(it){
                '0' -> 0
                '1' -> 1
                '2' -> 2
                '3' -> 3
                '4' -> 4
                '5' -> 5
                '6' -> 6
                '7' -> 7
                '8' -> 8
                '9' -> 9
                'A' -> 10
                'B' -> 11
                'C' -> 12
                'D' -> 13
                'E' -> 14
                'F' -> 15
                else -> 0
            }
            num += hex * coef
            coef *= 16
        }
        return num
    }

    fun convertByteToHex(byteArray : ByteArray) : String{
        var string = ""

        byteArray.forEach {

            var first =  it.toUByte().toInt() / 16
            var second = it.toUByte().toInt() % 16

            string += String.format("%01X", first)
            string += String.format("%01X", second)
        }

        return string
    }

    fun getWidth(byteArray: ByteArray) : Int{
        return convertHexToInt(convertByteToHex(byteArray))
    }

    fun getHeight(byteArray: ByteArray) : Int{
        return convertHexToInt(convertByteToHex(byteArray))
    }

    fun getLength() : Int{
        var string = convertByteToHex(length)
        return convertHexToInt(string)
    }
}