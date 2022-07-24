package bumblebee

class Converter {
    companion object {

        fun convertHexToInt(hexString :String) : Int{
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

        fun convertByteToHex(byte : Byte) : String{

            var string = ""

            var first =  byte.toUByte().toInt() / 16
            var second = byte.toUByte().toInt() % 16

            string += String.format("%01X", first)
            string += String.format("%01X", second)

            return string
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
    }


}