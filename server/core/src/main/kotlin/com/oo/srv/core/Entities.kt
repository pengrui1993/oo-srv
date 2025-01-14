package com.oo.srv.core

import java.time.LocalDateTime
import java.util.*



interface InvitingDoneContext{
    val provider:WaitressId
    val consumer0:WaitressId
    val score0:Double;
    val raised0:Boolean
}
class InvitingCode(val id:String,override val provider:WaitressId):InvitingDoneContext{
    private var version = 0
    private val createdTime: LocalDateTime = TimePoint.now()
    val timeoutTime: LocalDateTime = createdTime.plusDays(15)
    var consumer:WaitressId? = null;
    var consumeTime:TimePoint? = null
    var usedCodeTimeoutTime:TimePoint? = null
    private val invitingScore = 10.0
    private var raised = false
    fun onWaitressRegisterWithInvitingCode(cum: WaitressId, now: TimePoint, usedCodeTimeoutDurationDay: Long){
        val item = this
        item.consumer = cum
        item.consumeTime = now
        item.usedCodeTimeoutTime = now.plusDays(usedCodeTimeoutDurationDay)
        version++
    }

    fun onWaitressUsedRaisingScore(who:WaitressId){
        if(who!=provider)return
        raised = true
        version++
    }

    override val raised0: Boolean
        get() = raised
    override val consumer0:WaitressId get() = consumer!!
    override val score0: Double
        get() = invitingScore
}
private val randomCode = {UUID.randomUUID().toString().replace("-","").substring(0,8)}

class InvitingCodeManager{
    private val map = mutableMapOf<String,InvitingCode>()
    private var enablePropagating = true
    private var usedCodeTimeoutDurationDay = 30L
    fun onManagerToggleSwitcher(id:SystemManager){
        enablePropagating=!enablePropagating
    }
    fun onWaitressRegisterWithInvitingCode(cum:WaitressId,code:String
                                           ,now:TimePoint
                                           ,invitingDone:(InvitingDoneContext)->Unit){
        val item = map[code] ?: return
        item.onWaitressRegisterWithInvitingCode(cum,now,usedCodeTimeoutDurationDay)
        var i = 0
        var j = 0
        if(enablePropagating){
            while(true){
                j++
                val newCode = randomCode()
                if(!map.contains(newCode)){
                    map[newCode] = InvitingCode(newCode,cum)
                    i++
                }
                if(i>=3)break
                if(j>=1000)throw IllegalStateException("db error")
            }
        }
        invitingDone(item)
    }
    fun onTick(now:TimePoint){
        //delete from t where t.id in(select id from t where consumed and timeout )
        map.values.filter{ Objects.nonNull(it.consumeTime)}
            .filter { now.isAfter(it.usedCodeTimeoutTime!!)}
            .forEach { map-=it.id }
        map.values.filter { now.isAfter(it.timeoutTime) }
            .forEach { map-=it.id }
        map.values.filter { it.raised0 }
            .forEach { map-=it.id }
    }
}

private fun main() {
    for(i in 0..3)
        println(UUID.randomUUID().toString().replace("-",""))
}