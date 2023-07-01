package bumblebee.core

class ImgKernel(vararg element : ByteArray) {
    val width = element.size
    val height = element[0].size
}