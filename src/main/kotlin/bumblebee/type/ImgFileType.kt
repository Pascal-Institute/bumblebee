package bumblebee.type

enum class ImgFileType(val signature : ByteArray) {
    BMP(byteArrayOf(66,77)),
    JPG(byteArrayOf(255.toByte(), 216.toByte())),
    PNG(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)),
    PIX(byteArrayOf(80, 73, 88)),
    TIFF_LITTLE(byteArrayOf(73, 73)),
    TIFF_BIG(byteArrayOf(77, 77)),
    ICO_ICON(byteArrayOf(0, 0, 1, 0)),
    ICO_CURSOR(byteArrayOf(0, 0, 2, 0)),
    WEBP(byteArrayOf(82, 73, 70, 70))

}