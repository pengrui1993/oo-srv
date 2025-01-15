package com.oo.srv.core

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*


private fun numbers() {
    val res = BigDecimal("1234E+2") * BigDecimal("12555.12312")
    println(res.toPlainString())
    println(res.toEngineeringString())
    println(res.toString())

    println(DayOfWeek.of(7))
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
    private var weekCalcStartTime:LocalDateTime? = null
    private var thisWeekCashOutTimes = 0

    private var feePercent = 0.5
    private var remainFee = BigDecimal.ZERO
    private var takeOutFee =  BigDecimal.ZERO
    private var servingTypes = ",三国杀,LOL,"
    private var scoreForSearching = 0.0
    private var lng = 0.0
    private var lat = 0.0
    private var raiseTimes = 0

    val canTakeOut:Double get() = remainFee.toDouble()
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
        if(Objects.isNull(weekCalcStartTime)){
            weekCalcStartTime = time
            thisWeekCashOutTimes = 1
        }else{
            if(time.second-weekCalcStartTime!!.second>7*24*60*60){
                thisWeekCashOutTimes = 1
                weekCalcStartTime = time
            }else{
                thisWeekCashOutTimes++
            }
        }
        version++
    }
    val busy get() = inServing
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
