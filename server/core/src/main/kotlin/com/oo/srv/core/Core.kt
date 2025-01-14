package com.oo.srv.core

import java.time.Duration
import java.time.LocalDateTime

typealias GenericId = Long

typealias CustomerId = GenericId
typealias WaitressId = GenericId
typealias PaymentId = GenericId
typealias Position = Pair<Double,Double>

typealias SystemManager = Long

typealias OnceServingOrderId = GenericId

interface Customer
interface Waitress
interface Manager
interface Order
interface Payment
interface Coupon

interface CustomerRepository
interface ManagerRepository
interface WaitressRepository
interface PaymentRepository
interface OrderRepository
interface CouponRepository

//space
typealias Address = GenericId //xx省 xx市 xx区 xx街道 xx小区 xx栋 xx单元 xx号
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

