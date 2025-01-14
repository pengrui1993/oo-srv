package com.oo.srv.core

import java.util.concurrent.ThreadLocalRandom

fun main() {
    for(i in 0..3){
        println(
            String.format("%04d",ThreadLocalRandom.current().nextInt(9999)))
    }
}
data class CustomerAppraiseWaitress(
    val id:Long
    ,val cid:Long
    ,val wid:Long
    ,val timePoint:TimePoint
){


}
class CustomersAddress{}