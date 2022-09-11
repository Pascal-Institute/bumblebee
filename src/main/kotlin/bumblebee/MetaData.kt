package bumblebee

import bumblebee.type.ColorType

data class MetaData(var width : Int, var height : Int){
    lateinit var colorType : ColorType
}
