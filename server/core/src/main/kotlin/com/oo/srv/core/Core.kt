package com.oo.srv.core

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CopyOnWriteArrayList
internal val mods = setOf(
    InvitingCodeManager
    ,WaitressManager
    ,CustomerManager
    ,OrderManager
    ,PaymentManager
    ,CouponManager
)
var initFlags = false
fun coreInit(bm:BeanManager){
    if(initFlags)throw IllegalStateException("core init must be once")
    initFlags = true
    BeanMgr.delegate = bm
    val e = SysStartingEvent()
    while(!e.ready){
        EventBus.emit(e)
    }
    EventBus.emit(SysStartedEvent())
}
fun coreDestroy(bm:BeanManager){
    if(initFlags&&bm===BeanMgr.delegate){
        initFlags = false
        EventBus.emit(SysStoppingEvent())
        EventBus.emit(SysStoppedEvent())
    }

}
interface BeanManager{
    /**
     * NoSuchBeanException
     * @see org.springframework.beans.factory.BeanFactory
     */
    fun <T> getBean(clazz:Class<T>):T
}
internal object BeanMgr:BeanManager{
    lateinit var delegate:BeanManager
    override fun <T> getBean(clazz: Class<T>): T {
        return delegate.getBean(clazz)
    }
}

interface Module
typealias CustomerId = Long
typealias WaitressId = Long
typealias OrderId = Long
typealias PaymentId = Long
typealias CouponId = Long
typealias InvitingCodeId = String


interface Customer
interface Waitress
interface Order
interface Payment
interface Coupon
interface InvitingCode

interface CustomerRepository
interface ManagerRepository
interface WaitressRepository
interface PaymentRepository
interface OrderRepository
interface CouponRepository
interface InvitingCodeRepository
//space
typealias Address = Long //xx省 xx市 xx区 xx街道 xx小区 xx栋 xx单元 xx号
typealias Position = Pair<Double,Double>
//time
typealias TimePoint = LocalDateTime
typealias TimeDuration = Duration

enum class Roles{
    GUEST,WAITRESS,CUSTOMER,ADMIN,WX_PAY_SERVER,SELF_SERVER_TIMER
}

/*
ARTIFICER：工匠
MECHANIC：机械工/机械师
TECHNICIAN：技术人员/技师
waitress:女服务员
actress:女演员
 */
enum class Progress{
    CUSTOMER_CHOICE_WAITRESS_TO_SERVICE_ITSELF

    ,WAITRESS_CASH_OUT
}

enum class Action{
    CUSTOMER_REGISTER_BY_INVITED_WITH_PHONE
    ,CUSTOMER_LOGIN,CUSTOMER_LOGOUT

    ,WAITRESS_REGISTER_BY_INVITED_WITH_PHONE
    ,WAITRESS_LOGIN,WAITRESS_LOGOUT
    ,WAITRESS_REBIND_PHONE_NUMBER
    ,WAITRESS_FILL_INFORMATION
    ,WAITRESS_PROVIDE_SERVED_ADDRESS //服务地点及其半径
    ,WAITRESS_START_SERVING //上线
    ,WAITRESS_STOP_SERVING  //下线
    ,WAITRESS_ACCEPT_ORDER  //接单

    ,WAITRESS_CANCEL_ORDER  //取消订单

    ,ADMIN_CHANGE_WX_OFFICIAL_ACCOUNT
}
enum class EventType{
    SYS_STARTING,SYS_STARTED,SYS_STOPPING,SYS_STOPPED,SYS_TICK
}
interface Event{
    val type:EventType
}


typealias EventListener = (Event)->Unit
interface EventPublisher {
    fun emit(ev:Event)
}
interface EventRegister{
    fun on(l:EventListener)
    fun off(l:EventListener)
}
object EventBus:EventRegister,EventPublisher{
    val size:Int get() = listeners.size
    private val listeners = CopyOnWriteArrayList<EventListener>()
    override fun on(l: EventListener){ listeners+=l}
    override fun off(l: EventListener){ listeners-=l}
    override fun emit(ev: Event) {
        listeners.forEach { it(ev) }
    }
}
