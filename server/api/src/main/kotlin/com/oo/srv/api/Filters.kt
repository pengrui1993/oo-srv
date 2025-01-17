package com.oo.srv.api

import jakarta.servlet.FilterChain
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


fun traceToken():String?{
    return MDC.get(TRACE_TOKEN)
}
@Component
@WebFilter(urlPatterns = ["/**"])
@Order(Int.MAX_VALUE/2)
private class TraceFilter :OncePerRequestFilter(){
    private fun enableTrace() {
        if (StringUtils.isBlank(traceToken())) {
            MDC.put(TRACE_TOKEN, uuid())
        }
    }
    private fun disableTrace() {
        MDC.remove(TRACE_TOKEN)
    }
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        enableTrace()
        try{
            filterChain.doFilter(request,response)
        }finally {
            disableTrace()
        }
    }
}
@Component
@WebFilter(urlPatterns = ["/**"])
@Order(Int.MAX_VALUE/2+100)
private class LogFilter :OncePerRequestFilter(){
    private val log = LoggerFactory.getLogger(javaClass)
    override fun initFilterBean() {
        super.initFilterBean()
    }
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI
        val start = now();
        try{
            filterChain.doFilter(request,response)
        }finally {
            val end = now();
            log.info("uri:$uri,cast:${end-start} ms")
        }
    }

}
