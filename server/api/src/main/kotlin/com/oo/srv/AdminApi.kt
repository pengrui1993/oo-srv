package com.oo.srv

import com.oo.srv.core.Roles


enum class AdminApi(val uri:String
               ,private val rw:Boolean=READ
               ,val auth:Boolean = true
) {
    USER_INFO(ADMIN_USER_INFO_URI)
    ,TRANSACTION_LIST(ADMIN_TRANSACTION_LIST_URI)
    ,SEARCH_USER(ADMIN_SEARCH_USER_URI)
    ,USER_LOGIN(ADMIN_USER_LOGIN_URI,auth=false)
    ,AUTH_CAPTCHA(ADMIN_AUTH_CAPTCHA_URI,auth=false)
    ,USER_LOGOUT(ADMIN_USER_LOGOUT_URI)
    ,ROLES(ADMIN_ROLES_URI)
    ,ROUTERS(ADMIN_ROUTERS_URI)
    ,ARTICLE_LIST(ADMIN_ARTICLE_LIST)


    ,ROLE_PERMISSION_UPDATE(ADMIN_ROLE_PERM_UPDATE,rw= WRITE)
    ,ROLE_PERMISSION_DELETE(ADMIN_ROLE_PERM_DELETE,rw= WRITE)
    ;
    fun isReadonly():Boolean{
        return rw == READ;
    }
    companion object {
        fun from(uri:String):AdminApi{
            for (entry in entries) {
                if(entry.uri==uri)
                    return entry
            }
            throw IllegalArgumentException("invalid uri for admin:$uri")
        }
    }
}
enum class AdminApiCode(val code:Int,val msg:String){
    OK(20000,"成功")
    ,AUTH_ERR(60204,"用户密码验证错误")
    ,ILLEGAL_TOKEN(50008,"令牌错误")
    ,ALREADY_LOGON(50012,"重复登录")
    ,TOKEN_EXPIRED(50014,"令牌过期")
    ,VER_CODE_EXPIRED(51111,"令牌过期")
    ,SERVER_ERROR(50050,"请联系管理员")
    ;
}
enum class AdminRoles{
    ADMIN,ACCOUNTANT,OPERATOR,THIRD_SERVER//WX_PAY_SERVER_CALLBACK
    ;
    fun toCoreRole(){
        when(this){
            ADMIN-> Roles.ADMIN
            ACCOUNTANT-> Roles.ACCOUNTANT
            OPERATOR-> Roles.OPERATOR
            THIRD_SERVER-> Roles.THIRD_SERVER
        }
    }
}