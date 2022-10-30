package bumblebee.extension

import bumblebee.core.ImgPix
import org.intellij.lang.annotations.Identifier

class JPG(private var byteArray: ByteArray) : ImgPix(){

    init {
        extract()
    }

    override fun extract() {
        println("JPG")
    }
}