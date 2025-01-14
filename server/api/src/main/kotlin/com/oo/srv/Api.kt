package com.oo.srv

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.oo.srv.Api.entries
import org.slf4j.LoggerFactory



enum class Api(val uri:String
    ,private val rw:Boolean=READ
    ,val auth:Boolean = false
) {
     LOGIN(SMS_LOGIN_URI,WRITE)
    ,LOGOUT(LOGOUT_URI,WRITE)
    ,FIRST_PAGE_1(FIRST_PAGE_CONFIG_URI)
    ,FIRST_PAGE_2(FIRST_PAGE_ACTRESS3_URI)
    ,FIRST_PAGE_3(FIRST_PAGE_NEAR10_URI)
    ,FIRST_PAGE_4(FIRST_PAGE_HOT10_URI)
    ,FIRST_PAGE_5(FIRST_PAGE_NEW10_URI)

    ,ACTRESS_PAGE_1(ACTRESS_PAGE_LIST_BY_JOB_URI)
    ,ACTRESS_PAGE_2(ACTRESS_PAGE_LIST_STORE_URI)

    ,DYNAMIC_STATES_1(DYNAMIC_STATES_PAGE_LIST_URI)
    ,DYNAMIC_STATES_2(DYNAMIC_STATES_PAGE_VISIT_URI,WRITE)
    ,DYNAMIC_STATES_3(DYNAMIC_STATES_PAGE_FOLLOW_LIST_URI,auth=true)
    ;
    fun isReadonly():Boolean{
        return rw == READ;
    }
    companion object{
        fun from(uri:String):Api{
            for (entry in entries) {
                if(entry.uri==uri)
                    return entry
            }
            throw IllegalArgumentException("invalid uri:$uri")
        }

    }
}
private val log = LoggerFactory.getLogger(Api::class.java)
private val init_ =  object{
    init{
        if(entries.map { a->a.uri }.toSet().size!=entries.size){
            log.warn("same url in api :{}",Api.entries.toList())
        }
    }
}
private val gson = Gson()

enum class ApiCode(val code:Int,val msg:String){
    OK(200,"成功")
    ,NO_AUTH(401,"无权限")
    ,SERVER_ERROR(500,"请联系管理员")
    ;
    fun toJson():String{
        val obj = JsonObject()
        obj.addProperty("code",code)
        obj.addProperty("msg",msg)
        return gson.toJson(obj)
    }
}
private fun test() {
    for (entry in Api.entries) {
        println(entry)
    }
    println(Api.LOGIN.uri)
}


