package com.oo.srv

import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

@Configuration(proxyBeanMethods = false)
class Config2(@Resource private val auth:AppAuthInterceptor
            ,@Resource private val adminAuth:AdminAuthInterceptor
): WebMvcConfigurer {
    val clientApiPattern = CLIENT_API_PATTERN
    val adminApiPattern = ADMIN_API_PATTERN
    override fun addCorsMappings(registry: CorsRegistry) {
        listOf(clientApiPattern,adminApiPattern).forEach {
            registry.addMapping(it)
//                .allowedOrigins("*")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*")

        }
    }
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(auth).addPathPatterns(clientApiPattern)
        registry.addInterceptor(adminAuth).addPathPatterns(adminApiPattern)
    }
}
@Component
class AdminAuthInterceptor(
    @Resource val sessionManager:AdminSessionManager
    ,@Resource val propertiesAccessor: PropertiesAccessor
):AsyncHandlerInterceptor{
    private val log = LoggerFactory.getLogger(javaClass)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if(propertiesAccessor.debug)return true
        val api = AdminApi.from(request.requestURI)
        if(!api.auth)return true
        val token = request.getHeader(ADMIN_AUTH_KEY)
        if(Objects.isNull(token)) throw AdminAuthenticationException()
        log.info("accept admin token:$token")
        val userInfo = sessionManager.loadAdminUserInfo(token)
        if(userInfo is DummyAdminUserInfo)throw AdminTokenExpiredException()
        request.setAttribute(REQ_USER_KEY,userInfo)
        return true
    }
}
@Component
class AppAuthInterceptor(
    @Resource val sessionManager:SessionManager
    ,@Resource val propertiesAccessor: PropertiesAccessor
) : AsyncHandlerInterceptor{
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if(propertiesAccessor.debug)return true
        val api = AppApi.from(request.requestURI)
        if(!api.auth)return true
        val token = request.getHeader(AUTH_KEY)
        if(Objects.isNull(token))throw AuthenticationException()
        val appUserInfo = sessionManager.loadAppUserInfo(token)
        if(appUserInfo is DummyAppUserInfo) throw AuthenticationException()
        AppRoles.GUEST
        AppRoles.WAITRESS
        AppRoles.CUSTOMER
        return true
    }
}