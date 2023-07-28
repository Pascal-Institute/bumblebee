package bumblebee.util

class Operator {
    companion object{
        fun ByteArray.contains(other: ByteArray): Boolean {
            if (other.isEmpty()) {
                return true
            }

            for (i in 0 until size - other.size + 1) {
                var found = true
                for (j in other.indices) {
                    if (this[i + j] != other[j]) {
                        found = false
                        break
                    }
                }
                if (found) {
                    return true
                }
            }

            return false
        }

        fun ByteArray.invert() : ByteArray{
            return this.reversedArray()
        }
    }
}