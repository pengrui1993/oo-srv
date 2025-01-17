package com.oo.srv.api

import jakarta.annotation.Resource
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

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