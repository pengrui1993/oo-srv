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
    val tokenPrefix = "TOKEN:ADMIN:"
    fun key(token:String):String{
        return tokenPrefix+token
    }
    override fun loadAdminUserInfo(token: String): AdminUserInfo {
        if(""==token)return DummyAdminUserInfo()
        val userInfo = redisTemplate.opsForValue().get(key(token))
        return if(Objects.isNull(userInfo))DummyAdminUserInfo() else userInfo!!
    }

    override fun createToken(res: AdminUserInfo): String {
        val tk = uuid()
        redisTemplate.opsForValue().set(key(tk),res, Duration.ofHours(1))
        return tk
    }

    override fun destroyToken(tk: String) {
        redisTemplate.delete(key(tk))
    }

    fun onAdminLoginSuccess(){}
    fun onAdminLoginFailWhenAlreadyLogon(){
        AdminApiCode.ALREADY_LOGON
    }
    fun onAdminLogoutSuccess(){}
}
interface AppUserInfo{

}
class DummyAppUserInfo:AppUserInfo
class CustomerUserInfo:AppUserInfo
interface SessionManager{
    fun loadAppUserInfo(token:String):AppUserInfo
}
@Component
private class SessionManagerImpl(
    @Resource val redisTemplate:RedisTemplate<String,Serializable>
):SessionManager{
    val tokenPrefix = "TOKEN:APP:"
    fun key(token:String):String{
        return tokenPrefix+token
    }
    override fun loadAppUserInfo(token: String): AppUserInfo {
        if(""==token)return DummyAppUserInfo()
        return CustomerUserInfo()
    }
}