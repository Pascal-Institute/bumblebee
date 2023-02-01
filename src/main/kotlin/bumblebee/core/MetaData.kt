package bumblebee.core

import bumblebee.type.ColorType

data class MetaData(var width: Int, var height: Int, val grayScale: ColorType){
    var colorType : ColorType = grayScale
}
