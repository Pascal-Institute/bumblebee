package bumblebee.type

enum class ChunkType(val byte: ByteArray) {
    IHDR(byteArrayOf(73, 72, 68, 82)),
    IDAT(byteArrayOf(73, 68, 65, 84)),
    IEND(byteArrayOf(73, 69, 78, 68)),
    PLTE(byteArrayOf(80, 76, 84, 69)),
    GAMA(byteArrayOf(103, 65, 77, 65)),

}