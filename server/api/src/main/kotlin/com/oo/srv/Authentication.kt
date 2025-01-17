package com.oo.srv

import jakarta.annotation.Resource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.io.Serializable
import java.time.Duration
import java.util.*

interface AdminSessionManager {
    fun loadAdminUserInfo(token: String): AdminUserInfo
    fun createToken(res: AdminUserInfo): String
    fun destroyToken(token: String)
}
interface AdminUserInfo
class DummyAdminUserInfo:AdminUserInfo
@Component
private class AdminSessionManagerImpl:AdminSessionManager{
    @Resource lateinit var redisTemplate:RedisTemplate<String,AdminUserInfo>
    val vop = {redisTemplate.opsForValue()}
    val tokenPrefix = "TOKEN:ADMIN:"
    fun key(token:String):String{
        return tokenPrefix+token
    }
    override fun loadAdminUserInfo(token: String): AdminUserInfo {
        if(""==token)return DummyAdminUserInfo()
        val userInfo = vop().get(key(token))
        return if(Objects.isNull(userInfo))DummyAdminUserInfo() else userInfo!!
    }

    override fun createToken(res: AdminUserInfo): String {
        val tk = uuid()
        vop().set(key(tk),res, Duration.ofHours(1))
        return tk
    }

    override fun destroyToken(token: String) {
        redisTemplate.delete(key(token))
    }

    fun onAdminLoginSuccess(){}
    fun onAdminLoginFailWhenAlreadyLogon(){
        AdminApiCode.ALREADY_LOGON
    }
    fun onAdminLogoutSuccess(){}
}
interface AppUserInfo:Serializable
class DummyAppUserInfo:AppUserInfo
class CustomerUserInfo:AppUserInfo
class WaitressUserInfo:AppUserInfo
interface SessionManager{
    fun loadAppUserInfo(token:String):AppUserInfo
    fun createToken(res: AppUserInfo): String
    fun destroyToken(token: String)
}
@Component
private class SessionManagerImpl(
    @Resource val redisTemplate:RedisTemplate<String,Serializable>
):SessionManager{
    final val tokenPrefix = "TOKEN:APP:"
    final val wt = "w-"
    final val ct = "c-"
    final val waitressPrefix = tokenPrefix+wt
    final val customerPrefix = tokenPrefix+ct
    fun key(token:String):String{
        return tokenPrefix+token
    }
    fun isWaitressToken(token: String):Boolean{
        return token.startsWith(waitressPrefix)
    }
    fun isCustomerToken(token: String):Boolean{
        return token.startsWith(customerPrefix)
    }
    override fun loadAppUserInfo(token: String): AppUserInfo {
        if(""==token)return DummyAppUserInfo()
        return CustomerUserInfo()
    }
    val vop = {redisTemplate.opsForValue()}
    override fun createToken(res: AppUserInfo): String {
        var tk = uuid()
        when(res){
            is WaitressUserInfo->{
                tk = "$wt$tk"
                vop().set(key(tk),res, Duration.ofHours(1))
            }
            is CustomerUserInfo->{
                tk = "$ct$tk"
                vop().set(key(tk),res, Duration.ofHours(1))
            }
            else->throw IllegalArgumentException("invalid app userInfo")
        }
        return tk
    }
    override fun destroyToken(token: String) {
        redisTemplate.delete(key(token))
    }
}