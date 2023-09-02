package bumblebee.util

import bumblebee.util.Converter.Companion.cut

class Cipher {
    companion object{
        fun decodePackBits(byteArray: ByteArray) : ByteArray{
            var decryption = byteArrayOf()

            var i = 0
            while(i < byteArray.size){
                when (val integer = byteArray[i].toInt()) {
                    -128 -> {
                        i = byteArray.size
                    }
                    in 0 until 128 -> {
                        decryption += byteArray.cut(i+ 1, i + 1 + (integer + 1))
                        i += integer + 2
                    }
                    in -127 until 0 -> {
                        for(j : Int in 0 until -integer + 1){
                            decryption += byteArray[i + 1]
                        }
                        i += 2
                    }
                }
            }
            return decryption
        }
    }
}