package bumblebee.util

import bumblebee.core.ImgPix
import bumblebee.type.ColorType
import bumblebee.util.Converter.Companion.byteToHex
import bumblebee.util.Converter.Companion.hexToInt
import bumblebee.util.Converter.Companion.hexToRGB

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

}