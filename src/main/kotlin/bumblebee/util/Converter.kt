package bumblebee.util

import bumblebee.color.*
class Converter {
    companion object {
        fun longToByteArray(long : Long, byteSize : Int) : ByteArray {
            var byteArray = ByteArray(byteSize)
            var num = long
            var coef = 256

            for(i : Int in 0 until byteSize){
                byteArray[byteSize - (i+1)] = (num % coef).toByte()
                num /= coef
            }

            return byteArray
        }
        fun intToByteArray(int : Int, byteSize : Int) : ByteArray {
            var byteArray = ByteArray(byteSize)
            var num = int
            var coef = 256

            for(i : Int in 0 until byteSize){
                byteArray[byteSize - (i+1)] = (num % coef).toByte()
                num /= coef
            }

            return byteArray
        }

        fun hexToInt(hexString :String) : Int{
            var coef = 1
            var num = 0

            hexString.reversed().forEach {

                val hex = when(it){
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

        fun byteToHex(byte : Byte) : String{

            var string = ""
            val first =  byte.toUByte().toInt() / 16
            val second = byte.toUByte().toInt() % 16

            string += String.format("%01X", first)
            string += String.format("%01X", second)

            return string
        }
        fun byteToHex(byteArray : ByteArray) : String{
            var string = ""

            byteArray.forEach {

                val first =  it.toUByte().toInt() / 16
                val second = it.toUByte().toInt() % 16

                string += String.format("%01X", first)
                string += String.format("%01X", second)
            }

            return string
        }

        fun invert(byteArray : ByteArray) : ByteArray {
            return byteArray.reversedArray()
        }
        fun hexToRGB(hex : String) : RGB {
            var r = hexToInt(hex.slice(0 until 2))
            var g = hexToInt(hex.slice(2 until 4))
            var b = hexToInt(hex.slice(4 until 6))

            return RGB(r, g, b)
        }

        fun hexToRGBA(hex : String) : RGBA {
            var r = hexToInt(hex.slice(0 until 2))
            var g = hexToInt(hex.slice(2 until 4))
            var b = hexToInt(hex.slice(4 until 6))
            var a = hexToInt(hex.slice(6 until 8))

            return RGBA(r, g, b, a)
        }

        fun colorToByte(color : Color) : ByteArray {
            var byteArray = ByteArray(color.colorArray.size)

            color.colorArray.forEachIndexed{ index , i ->
                byteArray[index] = i.toByte()
            }

            return byteArray
        }
    }


}