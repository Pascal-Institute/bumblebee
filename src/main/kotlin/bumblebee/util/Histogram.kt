package bumblebee.util

import bumblebee.core.ImgPix
import bumblebee.type.ColorType

data class Histogram(val imgPix: ImgPix) {

    lateinit var channelR : MutableList<Int>
    lateinit var channelG : MutableList<Int>
    lateinit var channelB : MutableList<Int>

    init {
        extract()
    }

    private fun extract(){

        when(imgPix.colorType){

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
    }

    fun getOtsuLevel() : Int {

        val vKList = MutableList(256) { 0.0 }
        val totalCount = channelG.sum()

        var mT = 0.0
        for(i : Int in 0 until 256){
            mT += (i * channelG[i] * 1.0) / totalCount
        }
        for(k : Int in 0 until  256){
            var sum = 0
            var mK = 0.0

            for(i : Int in 0 until k+1){
                sum += channelG[i]
                mK += (i * channelG[i] * 1.0) / totalCount
            }

            var pK = (sum * 1.0) / totalCount
            var vK = ((mT * pK - mK) *  (mT * pK - mK))/(pK * (1 - pK))

            vKList[k] = vK

            if(pK == 1.0 || pK == 0.0){
                vKList[k] = 0.0
            }
        }
        return vKList.indexOf(vKList.max())
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