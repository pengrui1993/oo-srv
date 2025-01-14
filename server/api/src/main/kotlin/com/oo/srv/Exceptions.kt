package com.oo.srv


open class AuthenticationException:RuntimeException()
class AdminAuthenticationException:AuthenticationException()
class BusinessException(msg:String):RuntimeException(msg)