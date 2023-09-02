package bumblebee.type

enum class FileType(val signature : ByteArray, val fileName : String) {
    BMP(byteArrayOf(66,77), "bmp"),
    JPG(byteArrayOf(255.toByte(), 216.toByte()),"jpg"),
    PNG(byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10), "png"),
    PIX(byteArrayOf(80, 73, 88), "pix"),
    TIFF_LITTLE(byteArrayOf(73, 73), "tiff"),
    TIFF_BIG(byteArrayOf(77, 77), "tiff"),
    ICO_ICON(byteArrayOf(0, 0, 1, 0), "ico"),
    ICO_CURSOR(byteArrayOf(0, 0, 2, 0), "ico"),
    WEBP(byteArrayOf(82, 73, 70, 70), "webp")

}