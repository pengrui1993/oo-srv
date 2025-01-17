package com.oo.srv.core

import java.time.LocalDateTime
import java.util.*


class CustomerImpl: Customer {

}
class WaitressImpl(id: WaitressId): WaitressCurrentState(id), Waitress
class CouponImpl: Coupon {

}
class PaymentImpl: Payment {

}
class OrderImpl(id: OrderId, pos: Position): OnceServing(id,pos), Order

interface InvitingDoneContext{
    val provider: WaitressId
    val consumer0: WaitressId
    val score0:Double;
    val raised0:Boolean
}
class InvitingCodeImpl(val id: InvitingCodeId, override val provider: WaitressId): InvitingCode, InvitingDoneContext {
    private var version = 0
    private val createdTime: LocalDateTime = TimePoint.now()
    val timeoutTime: LocalDateTime = createdTime.plusDays(15)
    var consumer: WaitressId? = null;
    var consumeTime: TimePoint? = null
    var usedCodeTimeoutTime: TimePoint? = null
    private val invitingScore = 10.0
    private var raised = false
    fun onWaitressRegisterWithInvitingCode(cum: WaitressId, now: TimePoint, usedCodeTimeoutDurationDay: Long){
        val item = this
        item.consumer = cum
        item.consumeTime = now
        item.usedCodeTimeoutTime = now.plusDays(usedCodeTimeoutDurationDay)
        version++
    }

    fun onWaitressUsedRaisingScore(who: WaitressId){
        if(who!=provider)return
        raised = true
        version++
    }

    override val raised0: Boolean
        get() = raised
    override val consumer0: WaitressId get() = consumer!!
    override val score0: Double
        get() = invitingScore
}

private fun main() {
    for(i in 0..3)
        println(UUID.randomUUID().toString().replace("-",""))
}

