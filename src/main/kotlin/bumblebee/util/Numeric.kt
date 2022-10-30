package bumblebee.util

import kotlin.math.exp

class Numeric {
    companion object{
        fun isPrimeNumber(number : Int) : Boolean{

            if(number <= 1){
                return false
            }

            var seedNum = 2
            while (seedNum * seedNum <= number){
                if(number % seedNum == 0){
                    return false
                }
                seedNum++
            }
            return true
        }
        fun factorizePrime(number: Int): IntArray {

            var numCopy = number
            val answer = mutableListOf<Int>()

            var seedNum = 2
            var disposableFlag = true

            while (numCopy > 1) {
                if (numCopy % seedNum == 0) {
                    if (disposableFlag && isPrimeNumber(seedNum)) {
                        answer.add(seedNum)
                        disposableFlag = false
                    }
                    numCopy /= seedNum
                } else {
                    seedNum++
                    disposableFlag = true
                }
            }
            return answer.toIntArray()
        }

        fun softMax(number : DoubleArray) : DoubleArray{

            val answer = DoubleArray(number.size)
            var sum = 0.0
            number.forEach {
                sum += exp(it)
            }

            for(i : Int in answer.indices){
                answer[i] = (exp(number[i]) / sum)
            }

            return answer
        }
    }
}