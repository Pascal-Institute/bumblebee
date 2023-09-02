package bumblebee.core

import bumblebee.type.ColorType
import bumblebee.type.FileType

data class MetaData(var fileType : FileType, var width: Int, var height: Int, var colorType : ColorType)