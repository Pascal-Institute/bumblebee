package bumblebee.core


open class Packet : HashMap<String, ByteArray>(){
    override operator fun get(key : String) : ByteArray{
        return this.getOrDefault(key, byteArrayOf())
    }
}