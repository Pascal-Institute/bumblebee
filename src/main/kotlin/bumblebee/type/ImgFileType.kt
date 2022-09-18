package bumblebee.type

enum class ImgFileType(val signature : ByteArray) {
    PNG(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)),
    JPEG(byteArrayOf(255.toByte(), 216.toByte())),
    PIX(byteArrayOf(80, 73, 88)),
    TIFF_LITTLE(byteArrayOf(73, 73)),
    TIFF_BIG(byteArrayOf(77, 77))
}