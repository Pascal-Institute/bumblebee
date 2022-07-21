package bumblebee

import java.io.File

class ImgPix(private val filePath : String) {

    private val file = File(filePath)
    private val byteArray = file.readBytes()
    private val chunkArray = ArrayList<Chunk>()

    init {
        extractImageInfo(byteArray)
    }

    private fun extractImageInfo(byteArray: ByteArray){

        var size = byteArray.size
        var idx = 8
        while (idx < size){

            var chunk = Chunk()
            var count  = 0

            while(count < 4){

                chunk.length[count] = byteArray[idx]

                count++
                idx++
            }

            count = 0

            while(count < 4){

                chunk.type[count] = byteArray[idx]

                count++
                idx++
            }

            //chunk.length
            //while

            while (count < 4){
                count++
                idx++
            }
            count = 0
            chunk.getLength()
            chunkArray.add(chunk)
        }
    }
}