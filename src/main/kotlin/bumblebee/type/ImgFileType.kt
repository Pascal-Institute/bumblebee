package bumblebee.type

enum class ImgFileType(val signature : ByteArray) {
    BMP(byteArrayOf(66,77)),
    JPEG(byteArrayOf(255.toByte(), 216.toByte())),
    PNG(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)),
    PIX(byteArrayOf(80, 73, 88)),
    TIFF_LITTLE(byteArrayOf(73, 73)),
    TIFF_BIG(byteArrayOf(77, 77))
}