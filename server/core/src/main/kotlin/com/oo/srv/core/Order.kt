package com.oo.srv.core

import java.time.Duration
import java.util.*
import java.util.concurrent.ThreadLocalRandom

private fun genCode():String{
    return String.format("%04d", ThreadLocalRandom.current().nextInt(9999))
}

interface OnceServingPaybackContext
interface OnceServingOnTickContext
interface OnceServingOnTickCallback{
    fun onPayTimeout(ctx:OnceServingOnTickContext)
    fun onWaitressAcceptingTimeout(onceServing: OnceServingOnTickContext)
}

open class OnceServing(val id:OrderId,pos:Position)
    :OnceServingPaybackContext
    ,OnceServingOnTickContext{
    enum class FlowState{
        CUSTOMER_NEW_ORDER
        ,PAY_FAIL//no
        ,CUSTOMER_PAY_DONE,WAITRESS_ACCEPTED
        ,CUSTOMER_CANCEL_ORDER_WHEN_ARRIVED//no
        ,WAITRESS_CANCEL_ORDER_WHEN_ARRIVED//no
        ,WAITRESS_ARRIVED
        ;
        companion object{
            fun from(value:Int):FlowState{
                for (entry in entries) {
                    if(entry.ordinal==value)
                        return entry;
                }
                throw IllegalArgumentException()
            }
        }
    }
    private val customer:CustomerId = 0L
    private val waitress:WaitressId = 0L
    private var payment:PaymentId = 0L
    private var version = 0
    private var flowState = FlowState.CUSTOMER_NEW_ORDER.ordinal
    private var payTimeout = false
    private var acceptTimeout = false;
    private var cwHandShakeCode:String = genCode()
    private val customerCreateOrderTimePoint = TimePoint.now()
    private val customerCreateOrderPosition = pos
    private var customerCreatePaymentTime:TimePoint? = null
    private var customerPayDoneTime:TimePoint? = null
    private var waitressAcceptedTime:TimePoint? = null
    private var waitressAcceptedPosition:Position? = null
    private var waitressArrivedTimePoint:TimePoint? = null
    private var waitressArrivedPosition:Position? = null
    private val payTimeoutDuration = Duration.ofMinutes(20)
    private val waitressAcceptTimeoutDuration = Duration.ofMinutes(30)

    private var cancelTime:TimePoint? = null
    private var waitressCancelWhenArrivedPosition:Position? = null
    private var customerCancelWhenArrivedPosition:Position? = null




    val cancelPosition:Position? get() =
            if(Objects.nonNull(waitressCancelWhenArrivedPosition))
                waitressCancelWhenArrivedPosition else {
                    customerCancelWhenArrivedPosition
                }
    val isWaitressCanceled:Boolean get() = Objects.nonNull(waitressCancelWhenArrivedPosition)
    val isCustomerCanceled:Boolean get() = Objects.nonNull(customerCancelWhenArrivedPosition)
    val isCanceled:Boolean get() = isWaitressCanceled||isCustomerCanceled
    val customerProviderVerifyCodeToWaitressForHandshake:String get() = cwHandShakeCode
    val customerPayDone:Boolean get() = Objects.nonNull(customerPayDoneTime)
    val customerWaitressAcceptDone:Boolean get() = Objects.nonNull(waitressAcceptedTime)
    fun onTick(time:TimePoint,cb:OnceServingOnTickCallback){
        when(FlowState.from(flowState)){
            FlowState.CUSTOMER_NEW_ORDER -> {
                if((time.second-customerCreateOrderTimePoint.second)>payTimeoutDuration.seconds){
                    payTimeout = true
                    version++
                    cb.onPayTimeout(this)
                }
            }
            FlowState.CUSTOMER_PAY_DONE -> {
                if(time.second-customerPayDoneTime!!.second>waitressAcceptTimeoutDuration.seconds){
                    acceptTimeout = true
                    version++
                    cb.onWaitressAcceptingTimeout(this)
                }
            }
            else->{

            }
        }
    }
    fun onCreatePayment(pay:PaymentId,time:TimePoint){
        if(flowState!=FlowState.CUSTOMER_NEW_ORDER.ordinal)return
        customerCreatePaymentTime = time
        payment = pay
        version++
    }
    fun onPayCallbackFailure(pay:PaymentId,time:TimePoint){
        if(pay!=payment)return
        flowState = FlowState.PAY_FAIL.ordinal
        version++
    }
    fun onPayCallbackSuccess(pay:PaymentId,time:TimePoint){
        if(pay!=payment)return
        if(flowState!=FlowState.CUSTOMER_NEW_ORDER.ordinal)return
        customerPayDoneTime = time
        flowState = FlowState.CUSTOMER_PAY_DONE.ordinal
        version++
    }
    fun onWaitressAccepted(wts:WaitressId,time:TimePoint,pos:Position
                           ,acceptedCallback:()->Unit){
        if(wts!=waitress)return
        if(flowState!=FlowState.CUSTOMER_PAY_DONE.ordinal)return
        waitressAcceptedTime = time
        flowState = FlowState.WAITRESS_ACCEPTED.ordinal
        waitressAcceptedPosition = pos
        version++
        acceptedCallback()
    }
    fun onWaitressArrivedTargetAddress(wts:WaitressId,verifyCode:String,time:TimePoint,pos:Position){
        if(wts!=waitress)return
        if(flowState!=FlowState.WAITRESS_ACCEPTED.ordinal)return
        if(cwHandShakeCode!=verifyCode)throw IllegalArgumentException()
        flowState = FlowState.WAITRESS_ARRIVED.ordinal
        waitressArrivedTimePoint = time
        waitressArrivedPosition = pos
        version++
    }
    fun onCustomerCancelOrder(cus:CustomerId,time:TimePoint,pos:Position
                              ,paybackAllMoneyCallback:(OnceServingPaybackContext)->Unit = {}){
        if(cus!=customer)return
        when(FlowState.from(flowState)){
            FlowState.CUSTOMER_PAY_DONE->{
                flowState = FlowState.CUSTOMER_CANCEL_ORDER_WHEN_ARRIVED.ordinal
                customerCancelWhenArrivedPosition = pos
                cancelTime = time
                version++
                //无责任取消 全额退款
                paybackAllMoneyCallback(this)
            }
            FlowState.WAITRESS_ACCEPTED->{
                flowState = FlowState.CUSTOMER_CANCEL_ORDER_WHEN_ARRIVED.ordinal
                customerCancelWhenArrivedPosition = pos
                cancelTime = time
                //服务员在路程中 需要付给服务员路费
                val from = waitressAcceptedPosition!!
                val current = pos
                val to = customerCreateOrderPosition
                val total = haversine(from,to)
                val now = haversine(from,current)
                version++
            }
            else->{

            }
        }


    }
    fun onWaitressCancelOrder(wts:WaitressId,time:TimePoint,pos:Position
            ,whenSomeReasonToCancelCallback:()->Unit){
        if(wts!=waitress)return
        when(FlowState.from(flowState)){
            FlowState.CUSTOMER_PAY_DONE->{
                //不可力抗原因取消 轻度惩罚并记录
            }
            FlowState.WAITRESS_ACCEPTED->{
                flowState = FlowState.WAITRESS_CANCEL_ORDER_WHEN_ARRIVED.ordinal
                waitressCancelWhenArrivedPosition = pos
                cancelTime = time
                version++
            }
            else->{

            }
        }

    }
}
