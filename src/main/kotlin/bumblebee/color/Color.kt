package bumblebee.color

import bumblebee.type.ColorType

interface Color {
  val colorType : ColorType
  var colorArray : Array<Int>
}