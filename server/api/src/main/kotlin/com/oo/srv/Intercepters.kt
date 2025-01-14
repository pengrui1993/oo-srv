package com.oo.srv

import com.oo.srv.core.Roles
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
class Config2(@Resource private val auth:AuthInterceptor
            ,@Resource private val adminAuth:AdminAuthInterceptor
): WebMvcConfigurer {
    val clientApiPattern = "/oo-srv/api/**"
    val adminApiPattern = "/dev-api/vue-element-admin/**"
    override fun addCorsMappings(registry: CorsRegistry) {
        listOf(clientApiPattern,adminApiPattern).forEach {
            registry.addMapping(it)
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(false)//true
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
):AsyncHandlerInterceptor{
    private val log = LoggerFactory.getLogger(AdminAuthInterceptor::class.java)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val api = AdminApi.from(request.requestURI)
        val token = request.getHeader(ADMIN_AUTH_KEY)
        if(Objects.nonNull(token))log.info("accept admin token:$token")
        if(Objects.isNull(token)&&api.auth) throw AdminAuthenticationException()
        return true
    }
}
@Component
class AuthInterceptor(@Resource val sessionManager:SessionManager) : AsyncHandlerInterceptor{
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val api = Api.from(request.requestURI)
        val token = request.getHeader(AUTH_KEY)
        val role = sessionManager.roleByToken(token)
        if(!api.isReadonly()&&api.auth&& Roles.GUEST==role){
            throw AuthenticationException()
        }
        return true
    }
}