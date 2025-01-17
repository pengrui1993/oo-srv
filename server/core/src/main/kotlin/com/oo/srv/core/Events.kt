package com.oo.srv.core

import java.util.concurrent.atomic.AtomicBoolean


class SysStartingEvent: Event {
    override val type: EventType = EventType.SYS_STARTING
    companion object{
        private val map:Map<Class<out Module>,AtomicBoolean>
        val readiedModules = mutableSetOf<Module>()
        init {
            map = mods.map { it.javaClass }.associateWith { AtomicBoolean(false) }
        }
    }
    private val modulesPrepared = map
    private val moduleCount:Int get() = modulesPrepared.keys.count()
    private val readyModuleCounter:Int get() = modulesPrepared.values.count { it.get() }
    val ready:Boolean get() = moduleCount==readyModuleCounter
    fun setReady(mod: Module, ready:Boolean){
        modulesPrepared[mod.javaClass]?.let {
            it.set(ready)
            if(ready) readiedModules +=mod
        }
    }
    fun <T> getBean(clazz:Class<T>):T{
        return BeanMgr.getBean(clazz)
    }
    fun <T> getModuleInterface(clazz:Class<T>):T?{
        for (readiedModule in readiedModules) {
            if(clazz.isAssignableFrom(readiedModule.javaClass)){
                return readiedModule as T
            }
        }
        return null;
    }
}

class SysTickEvent(val dt:Double,val ms:Long): Event {
    override val type: EventType = EventType.SYS_TICK
}
class SysStartedEvent: Event {
    override val type: EventType = EventType.SYS_STARTED
}
class SysStoppingEvent: Event {
    override val type: EventType = EventType.SYS_STOPPING
}
class SysStoppedEvent: Event {
    override val type: EventType = EventType.SYS_STOPPED
}

private fun caseDemo(ev: Event){
    when(ev){
        is SysTickEvent, is SysStartingEvent ->{}
    }
    when(ev.type){
        EventType.SYS_TICK, EventType.SYS_STARTING ->{}
        else->{}
    }
}
private fun launchDemo() {
    var counter = 0
    val ticker: EventListener = { ev->
        println(System.currentTimeMillis())
        when(ev){
            is SysStartingEvent ->{
                if(counter++>=3)ev.setReady(InvitingCodeManager,true)
            }
        }

    }
    val register: EventRegister = EventBus
    register.on(ticker)
    val publisher: EventPublisher = EventBus
    val e = SysStartingEvent()
    while(!e.ready){
        publisher.emit(e)
    }
    for(i in 0..1){
        publisher.emit(SysTickEvent(0.1,i.toLong()))
    }
    register.off(ticker)
    println(EventBus.size)

}