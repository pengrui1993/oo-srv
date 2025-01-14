package com.oo.srv




enum class AdminApi(val uri:String
               ,private val rw:Boolean=READ
               ,val auth:Boolean = true
) {
    USER_INFO(ADMIN_USER_INFO_URI)
    ,TRANSACTION_LIST(ADMIN_TRANSACTION_LIST_URI)
    ,SEARCH_USER(ADMIN_SEARCH_USER_URI)
    ,USER_LOGIN(ADMIN_USER_LOGIN_URI,auth=false)
    ,USER_LOGOUT(ADMIN_USER_LOGOUT_URI)
    ,ROLES(ADMIN_ROLES_URI)
    ,ROUTERS(ADMIN_ROUTERS_URI)
    ,ARTICLE_LIST(ADMIN_ARTICLE_LIST)


    ,ROLE_PERMISSION_UPDATE(ADMIN_ROLE_PERM_UPDATE,rw= WRITE)
    ,ROLE_PERMISSION_DELETE(ADMIN_ROLE_PERM_DELETE,rw= WRITE)
    ;
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
    ,AUTH_ERR(60204,"验证错误")
    ,ILLEGAL_TOKEN(50008,"令牌错误")
    ,ALREADY_LOGGED(50012,"重复登录")
    ,TOKEN_EXPIRED(50014,"令牌过期")
    ,SERVER_ERROR(50050,"请联系管理员")
    ;
}