package bumblebee.type

enum class ImgFileType(val byte : ByteArray) {
    PNG(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)),
    JPEG(byteArrayOf(255.toByte(), 216.toByte()))

}