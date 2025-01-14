package com.oo.srv.core

import java.math.BigDecimal


private fun customerSearching(lat:Double,lng:Double,type:String){
    val sql = """
        SELECT*
        ,ROUND(6378.138 * 2 * ASIN(SQRT(POW(SIN((#{lat} * PI() / 180 - lat * PI() / 180) / 2),2) + COS(40.0497810000 * PI() / 180) * COS(lat * PI() / 180) * POW(SIN((#{lon} * PI() / 180 - lng * PI() / 180) / 2),2))) * 1000) 
            AS dis
        FROM user_waitress 
        WHERE deleted=0 
            and serving_type like concat('%,',#{type},',%') 
            ORDER BY dis ASC searching_score DESC
            limit 10
    """.trimIndent()
}
object WaitressManager{

}

fun main() {
    val res = BigDecimal("1234E+2") * BigDecimal("12555.12312")
    println(res.toPlainString())
    println(res.toEngineeringString())
    println(res.toString())
}
class WaitressCurrentState(val id:WaitressId):Waitress{
    private var version = 0
    private var inServing = false
    private var canCashOut = false
    private var canServing = false
    private var bankAccount:String? = null
    private var lastCashOutErrorInfo:String? = null
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
    fun onCashOutOk(amount:BigDecimal){
        if(amount>remainFee)throw IllegalStateException()
        remainFee-=amount
        takeOutFee+=amount
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
    fun onCashOutSuccess(){
        lastCashOutErrorInfo = null
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
    fun onCashOutError(errInfo:String){
        canCashOut = false
        lastCashOutErrorInfo = errInfo
        version++
    }
}
