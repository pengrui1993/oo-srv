package com.oo.srv.core

import java.util.*

val randomCode = { uuid().substring(0,8)}

object InvitingCodeManager: Module {
    @Final
    private lateinit var invitingCodeRepo: InvitingCodeRepository
    @Final
    private lateinit var waitressAccessor: WaitressAccessor
    private val listener: EventListener = { it->
        when(it){
            is SysStartingEvent ->{
                it.getBean(InvitingCodeRepository::class.java).let{ bean->
                    invitingCodeRepo = bean
                    it.getModuleInterface(WaitressAccessor::class.java)?.let { mod->
                        waitressAccessor = mod
                        it.setReady(this,true)
                    }
                }
            }
        }
    }
    init{
        EventBus.on(listener)
    }
    private val map = mutableMapOf<String, InvitingCodeImpl>()
    private var enablePropagating = true
    private var usedCodeTimeoutDurationDay = 30L
    fun onManagerToggleSwitcher(id:Long){

        enablePropagating =!enablePropagating
    }
    fun onWaitressRegisterWithInvitingCode(cum: WaitressId, code:String
                                           , now: TimePoint
                                           , invitingDone:(InvitingDoneContext)->Unit){
        val item = map[code] ?: return
        item.onWaitressRegisterWithInvitingCode(cum,now, usedCodeTimeoutDurationDay)
        var i = 0
        var j = 0
        if(enablePropagating){
            while(true){
                j++
                val newCode = randomCode()
                if(!map.contains(newCode)){
                    map[newCode] = InvitingCodeImpl(newCode,cum)
                    i++
                }
                if(i>=3)break
                if(j>=1000)throw IllegalStateException("db error")
            }
        }
        invitingDone(item)
    }
    fun onTick(now: TimePoint){
        //delete from t where t.id in(select id from t where consumed and timeout )
        map.values.filter{ Objects.nonNull(it.consumeTime)}
            .filter { now.isAfter(it.usedCodeTimeoutTime!!)}
            .forEach { map -=it.id }
        map.values.filter { now.isAfter(it.timeoutTime) }
            .forEach { map -=it.id }
        map.values.filter { it.raised0 }
            .forEach { map -=it.id }
    }
}
interface WaitressAccessor
object WaitressManager: Module, WaitressAccessor {
    private val listener: EventListener = { it->
        when(it){
            is SysStartingEvent ->{
                it.setReady(this,true)
            }
        }
    }
    init{
        EventBus.on(listener)
    }
}
object CustomerManager: Module {
    private val listener: EventListener = { it->
        when(it){
            is SysStartingEvent ->{
                it.setReady(this,true)
            }
        }
    }
    init{
        EventBus.on(listener)
    }
}
object OrderManager: Module {
    @Final
    private lateinit var orderRepository: OrderRepository
    private val listener: EventListener = { it->
        when(it){
            is SysStartingEvent ->{
                it.getBean(OrderRepository::class.java).let { bean->
                    orderRepository = bean
                    it.setReady(this,true)
                }
            }
        }
    }
    init{
        EventBus.on(listener)
    }
}

object PaymentManager: Module {
    private val listener: EventListener = { it->
        when(it){
            is SysStartingEvent ->{
                it.setReady(this,true)
            }
            is SysStoppedEvent ->{
            }
        }
    }
    init{
        EventBus.on(listener)
    }
}
object CouponManager: Module {
    private val listener: EventListener = { it->
        when(it){
            is SysStartingEvent ->{
                it.setReady(this,true)
            }
        }
    }
    init{
        EventBus.on(listener)
    }
}
