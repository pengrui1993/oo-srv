package com.oo.srv.core

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


private fun numbers() {
    val res = BigDecimal("1234E+2") * BigDecimal("12555.12312")
    println(res.toPlainString())
    println(res.toEngineeringString())
    println(res.toString())

    println(DayOfWeek.of(7))
}
class WaitressAddresses{
    val id = 0L
    var wid:WaitressId = 0L
    var contact = "联系人"
    var phone = "联系人手机号"
    var major = "四川省重庆市南岸区"
    var minor = "福田街道恒大天雨3栋3单元2002号"
    var default = false
    var lng = 0.0
    var lat = 0.0
    fun onSetAsDefaultAddress(notifyWaitressSetDefaultAddress:(WaitressAddresses)->Unit){
        default = true
        notifyWaitressSetDefaultAddress(this)
    }
}
fun onSetAsDefaultAddress(wa:WaitressAddresses,map:Map<WaitressId,WaitressImpl>){
    wa.onSetAsDefaultAddress { map[it.wid]?.onSetLngLat(it.lng,it.lat) }
}
open class WaitressCurrentState(val id:WaitressId){
    private var version = 0
    private var inServing = false
    private var canCashOut = false
    private var canServing = false
    private var bankAccount:String? = null
    private var lastCashOutErrorInfo:String? = null
    private var lastCashOutTime:LocalDateTime? = null

    private var todayCashOutTimes = 0
    private var dailyCashOutLimit = 1
    private var lastDayChangedDate: LocalDate? = null
    private var cashOutMinLimit = 1
    private var cashOutMaxLimit = 50000
    private var feePercent = 0.5
    private var remainFee = BigDecimal.ZERO
    private var takeOutFee =  BigDecimal.ZERO
    private var servingTypes = ",三国杀,LOL,"
    private var scoreForSearching = 0.0
    private var lng = 0.0
    private var lat = 0.0
    private var raiseTimes = 0

    val busy get() = inServing
    val canTakeOut:Boolean get() = canCashOut&&todayCashOutTimes<dailyCashOutLimit
    val canTakeOutAmount:Int
        get() = if(!canTakeOut) 0
        else remainFee.toInt().let { if(it>cashOutMaxLimit)cashOutMaxLimit else it }
    fun onDayChange(time:LocalDateTime){
        if(Objects.isNull(lastCashOutTime)
            ||Objects.isNull(lastDayChangedDate)){
            todayCashOutTimes = 0
            lastDayChangedDate = time.toLocalDate()
        }else{
            val curDay = time.toLocalDate()
            val lastDay = lastDayChangedDate!!
            val changed = curDay.year!=lastDay.year||curDay.dayOfYear!=lastDay.dayOfYear
            if(changed){
                todayCashOutTimes = 0
                lastDayChangedDate = curDay
            }
        }
        version++
    }
    fun onSetLngLat(ng:Double,at:Double){
        lng = ng
        lat = at
        version++
    }
    fun onOnceServicingDone(amount:BigDecimal,fireRaise:(WaitressId)->Unit){
        if(raiseTimes>0){
            raiseTimes--
            remainFee+=amount * BigDecimal.valueOf(feePercent+0.1)
            fireRaise(id)
        }else{
            remainFee+=amount * BigDecimal.valueOf(feePercent)
        }
        version++

    }
    fun onCashOutError(errInfo:String){
        canCashOut = false
        lastCashOutErrorInfo = errInfo
        version++
    }
    fun onCashOutSuccess(amount:BigDecimal,time:LocalDateTime){
        if(amount>remainFee)throw IllegalStateException()
        lastCashOutErrorInfo = null
        remainFee-=amount
        takeOutFee+=amount
        if(Objects.isNull(lastCashOutTime)){
            todayCashOutTimes = 1
        }
        lastCashOutTime = time
        version++
    }
    fun onAccepted(){
        if(!canServing)throw IllegalStateException()
        inServing = true
        version++
    }
    fun onServingDone(){
        if(!canServing)throw IllegalStateException()
        inServing = false
        version++
    }

    fun onSelfBindBankAccount(account:String){
        bankAccount = account
        canCashOut = true
        version++
    }
    fun onAdminAuditServingInfoOk(){
        canServing = true
        if(null!=bankAccount){
            canCashOut = true
        }
        version++
    }

}
