package com.oo.srv


open class AuthenticationException:RuntimeException()
class AdminAuthenticationException:RuntimeException()
class AdminTokenExpiredException:RuntimeException()
class AdminVerificationExpiredException:RuntimeException()
class BusinessException(msg:String):RuntimeException(msg)
class AdminBusinessException(msg:String):RuntimeException(msg)