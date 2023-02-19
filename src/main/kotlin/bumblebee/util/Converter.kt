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

        fun intToHex(int : Int) : String{

            if(int == 0){
                return "0"
            }

            var hexString = ""
            var num = int
            while (num > 0){
                val hex = when(num % 16){
                    0 -> "0"
                    1 -> "1"
                    2 -> "2"
                    3 -> "3"
                    4 -> "4"
                    5 -> "5"
                    6 -> "6"
                    7 -> "7"
                    8 -> "8"
                    9 -> "9"
                    10 -> "A"
                    11 -> "B"
                    12 -> "C"
                    13 -> "D"
                    14 -> "E"
                    15 -> "F"
                    else -> "0"
                }
                hexString += hex
                num /= 16
            }
            return hexString.reversed()
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
            return String.format("%02X", byte.toUByte().toInt())
        }
        fun byteToHex(byteArray : ByteArray) : String{
            var string = ""

            byteArray.forEach {
                string += String.format("%02X", it.toUByte().toInt())
            }

            return string
        }

        fun byteToInt(byte: Byte) : Int{
            return hexToInt(byteToHex(byte))
        }

        fun byteToInt(byteArray: ByteArray) : Int{
            return hexToInt(byteToHex(byteArray))
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