package bumblebee

import java.io.File

class ImgPix(private val filePath : String) {

    private val file = File(filePath)
    private val byteArray = file.readBytes()
    private val chunkArray = ArrayList<Chunk>()

    var width = 0
    var height = 0

    init {
        extractImageInfo(byteArray)

        chunkArray.forEach{
            if(it.convertByteToHex(it.type) == "49484452"){
               width = it.getWidth(it.data.sliceArray(0..3))
               height = it.getHeight(it.data.sliceArray(4..7))
            }
        }
    }

    private fun extractImageInfo(byteArray: ByteArray){

        var size = byteArray.size
        var idx = 8

        while (idx < size){

            var chunk = Chunk()

            //length 4 byte
            var count  = 0
            while(count < 4){
                chunk.length[count] = byteArray[idx]
                count++
                idx++
            }

            //type 4 byte
            count = 0
            while(count < 4){
                chunk.type[count] = byteArray[idx]
                count++
                idx++
            }

            //data ?byte
            count = 0
            val length = chunk.getLength()
            chunk.initData(length)
            while (count < length){
                try{
                    chunk.data[count] = byteArray[idx]
                    count++
                    idx++
                }catch (e : Exception){
                    error("extract failed")
                    break
                }

            }

            //crc 4byte
            count = 0
            while (count < 4){

                chunk.crc[count] = byteArray[idx]
                count++
                idx++
            }
            chunkArray.add(chunk)
        }
    }
}