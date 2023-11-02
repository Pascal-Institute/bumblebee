package bumblebee.color

data class YCBCR(var y : Double, var cb : Double, var cr : Double){
    //ITU-R BT.601
    companion object {
        val kr = 0.299
        val kg = 0.587
        val kb = 0.114
    }
}
