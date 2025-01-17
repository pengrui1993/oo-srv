package com.oo.srv


open class AuthenticationException:RuntimeException()
class AdminAuthenticationException:RuntimeException()
class AdminTokenExpiredException(msg:String? = null):RuntimeException(msg)
class AdminVerificationExpiredException(msg:String? = null):RuntimeException(msg)
class BusinessException(msg:String):RuntimeException(msg)
class AdminBusinessException(msg:String):RuntimeException(msg)