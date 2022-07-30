package bumblebee

import java.io.File

class FileManager {

    companion object {



        fun read(filePath : String) : ImgPix{

            return kernel(File(filePath))
        }

        private fun kernel(file : File) : ImgPix{

            val byteArray = file.readBytes()
            var fileSignature = byteArray.slice(0 until 8)

            when (fileSignature){
                else ->{
                    return PNG(byteArray)
                }
            }

        }
    }

}