package com.oo.srv

import com.oo.srv.core.Roles
import jakarta.annotation.Resource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.Serializable
import java.util.*



interface SessionManager{
    fun roleByToken(token:String?):Roles
}
@Component
private class SessionManagerImpl(
    @Resource val template:RedisTemplate<String,Serializable>
):SessionManager{
    val tokenPrefix = "TOKEN:"
    fun key(token:String):String{
        return tokenPrefix+token
    }
    override fun roleByToken(token: String?): Roles {
        if(Objects.isNull(token))
            return Roles.GUEST
        val k = key(token!!)
        return Roles.ADMIN
    }
}