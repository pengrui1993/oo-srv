package com.oo.srv

import jakarta.annotation.Resource
import jakarta.servlet.*
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.annotation.WebListener
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.catalina.SessionEvent
import org.apache.catalina.SessionListener
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ThreadLocalRandom


//@Component //  that is no working when use redis to manage the session
private object TomcatSessionManager: SessionListener{
    private val log = LoggerFactory.getLogger(TomcatSessionManager::class.java)
    override fun sessionEvent(e: SessionEvent) {
        log.info(e.toString())
    }
}

@Component
private object RequestManager: ServletRequestListener{
    private val log = LoggerFactory.getLogger(RequestManager::class.java)
    override fun requestDestroyed(sre: ServletRequestEvent) {
        log.info(sre.toString())
    }
    override fun requestInitialized(sre: ServletRequestEvent) {
        log.info(sre.toString())
    }
}

//@WebServlet(urlPatterns = ["/demo"])//must be public class
open class DemoServlet(@Resource val env: Environment): HttpServlet(){
    @LogAop
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        if(ThreadLocalRandom.current().nextBoolean()){
            val session = req.getSession(true)
            resp.writer.println("session.id:${session.id},profile:${env.activeProfiles.toList()}")
        }else{
            throw AuthenticationException()
        }
    }
}
@WebListener//must be public class
class DemoListener: ServletContextListener {
    private val log = LoggerFactory.getLogger(DemoListener::class.java)
    override fun contextInitialized(sce: ServletContextEvent?) {
        log.info(sce.toString())
    }
    override fun contextDestroyed(sce: ServletContextEvent?) {
        log.info(sce.toString())
    }
}
//@Component//must be having the annotation if you want it is working
@WebFilter(urlPatterns = ["/**"])
private object DemoFilter : OncePerRequestFilter(){
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        filterChain.doFilter(request,response)
    }
}