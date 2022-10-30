package bumblebee.util

import bumblebee.core.ImgPix
import bumblebee.type.ColorType

data class Histogram(val imgPix: ImgPix) {

    lateinit var channelR : MutableList<Int>
    lateinit var channelG : MutableList<Int>
    lateinit var channelB : MutableList<Int>
    var totalCount = 0

    init {
        extract()
    }

    private fun extract(){

        when(imgPix.metaData.colorType){

            ColorType.GRAY_SCALE->{
                channelG = MutableList(256) { 0 }

                imgPix.get().forEach {
                    val index = it.toUByte().toInt()
                    channelG[index] += 1
                }

            }

            ColorType.TRUE_COLOR->{
                channelR = MutableList(256) { 0 }
                channelG = MutableList(256) { 0 }
                channelB = MutableList(256) { 0 }

                imgPix.get().forEachIndexed { idx, byte ->
                    val index = byte.toUByte().toInt()

                    when(idx % 3){
                        0 -> {channelR[index] += 1}
                        1 -> {channelG[index] += 1}
                        2 -> {channelB[index] += 1}
                        else -> {channelG[index] += 1}
                    }
                }
            }

            else->{
                channelG = MutableList(256) { 0 }

                imgPix.get().forEach {
                    val index = it.toUByte().toInt()
                    channelG[index] += 1
                }
            }
        }

        channelG.forEach {
            totalCount += it
        }

    }

    fun getAverage(colorType: ColorType) : ByteArray {

        return when(colorType){

            ColorType.GRAY_SCALE -> byteArrayOf(getAverage(channelG).toByte())
            ColorType.TRUE_COLOR -> byteArrayOf(getAverage(channelR).toByte(),
                                                getAverage(channelG).toByte(),
                                                getAverage(channelB).toByte())
            else -> byteArrayOf(channelG.average().toInt().toByte())
        }
    }

    private fun getAverage(channel : MutableList<Int>) : Int{
        var sum = 0
        channel.forEachIndexed { index, it ->
            sum += index * it
        }
        return sum/channel.sum()
    }

}