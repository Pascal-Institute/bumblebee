package bumblebee.util

import org.jetbrains.annotations.NotNull
import java.util.Objects


class ImgHeader : HashMap<String, ByteArray>(){
    override operator fun get(key : String) : ByteArray{
        return this.getOrDefault(key, byteArrayOf())
    }
}