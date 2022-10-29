package bumblebee.extension

import bumblebee.core.ImgPix

class JPG(private var byteArray: ByteArray) : ImgPix(){

    init {
        extract()
    }

    override fun extract() {
        println("JPG")
    }
}