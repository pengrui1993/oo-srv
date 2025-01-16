package com.oo.srv.core

object Aggregation1{
    enum class Progress{
        CUSTOMER_CHOICE_WAITRESS_TO_SERVICE_ITSELF_AND_PAY
        , WAITRESS_REGISTER_AND_SUBMIT_INFO_THEN_AUDITING

        ,WAITRESS_CASH_OUT
    }
    enum class OrderStatus{
        WAIT_CTM_PAY,WAIT_WTS_ACCEPT,WAIT_WTS_SRV,WAIT_SRV_ING,WAIT_CTM_APR,DONE
    }
    enum class Action{
        WAITRESS_REGISTER_BY_PHONE_WITH_INVITED_CODE

        ,WAITRESS_PROVIDE_SERVED_ADDRESS //服务地点 [也许]及其半径【可以根据服务位置运算】
        ,WAITRESS_START_SERVING //上线
        ,WAITRESS_STOP_SERVING  //下线
        ,WAITRESS_ACCEPT_ORDER  //接单
        ,WAITRESS_CANCEL_ORDER  //取消订单
        ,ADMIN_CHANGE_WX_OFFICIAL_ACCOUNT
    }
}
class ActionData1
fun onGuestRegisterOrCustomerLoginByPhone(data:ActionData1){
    Action.CUSTOMER_REGISTER_OR_LOGIN_BY_PHONE
}
class ActionData20
fun onCustomerLogout(data:ActionData20){
    Action.CUSTOMER_LOGOUT
}
class ActionData21
fun onCustomerRequestUnregister(data:ActionData21){
    Action.CUSTOMER_REQUEST_UNREGISTER
}
class ActionData22
fun onCustomerCancelUnregister(data:ActionData22){
    Action.CUSTOMER_CANCEL_UNREGISTER
}
class ActionData3
fun onCustomerBindWxOpenId(data:ActionData3){
    Action.CUSTOMER_BIND_WX_OPENID
}
class ActionData4
fun onCustomerCreateOrder(data:ActionData4){
    Action.CUSTOMER_CREATE_ORDER
}
class ActionData5
fun onCustomerPayForOrder(data:ActionData5){
    Action.CUSTOMER_PAY_FOR_ORDER
}
class ActionData6
fun onCustomerCancelOrder(data:ActionData6){
    Action.CUSTOMER_CANCEL_ORDER
}
class ActionData7
fun onCustomerAppraiseWaitressPostOrderDone(data:ActionData6){
    Action.CUSTOMER_APPRAISE_WAITRESS
}

class ActionData80
fun onWaitressRegisterByPhoneMaybeWithInvitedCodeFromOtherWaitress(data:ActionData80){
    Action.WAITRESS_REGISTER_BY_PHONE_WITH_INVITED_CODE
}
class ActionData81
fun onWaitressLoginByPhone(data:ActionData81){
    Action.WAITRESS_LOGIN_BY_PHONE
}
class ActionData82
fun onWaitressLogout(data:ActionData82){
    Action.WAITRESS_LOGOUT
}
class ActionData83
fun onWaitressRebindPhone(data:ActionData83){
    Action.WAITRESS_REBIND_PHONE_NUMBER
}
class ActionData84
fun onWaitressRebindWxOpenId(data:ActionData84){
    Action.WAITRESS_REBIND_WX_OPENID
}
class ActionData85{
    var wid:WaitressId = 0L
    var nikeName = ""
    var realName = "刘丽"
    var idCard1 = "fileInfo.id"
    var idCard2 = "fileInfo.id"
    var gender = ""
    var age = 10
    var wechatAccount = ""
    var hobbies = ",旅游,看电影,玩游戏,跳舞,拍摄,美食,"
    var tags = ",纯御,萝莉,清新,高冷,"
    var jobs = ",按摩,推背,spa,刮痧,"
    var label = "御姐"
    var headImage = "fileInfo.id"
    var recentlyPhoto = "111,333,555"
    //address
    var contact = "联系人"
    var phone = "联系人手机号"
    var major = "四川省重庆市南岸区"
    var minor = "福田街道恒大天雨3栋3单元2002号"
    var lng = 0.0
    var lat = 0.0
    var radius = 10 //服务半径
}
fun onWaitressFillInformation(data:ActionData85){
    Action.WAITRESS_FILL_INFORMATION
}





