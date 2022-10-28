package bumblebee.util

class Math {
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
    }
}