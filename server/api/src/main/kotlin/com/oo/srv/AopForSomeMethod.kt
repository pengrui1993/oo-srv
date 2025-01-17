package com.oo.srv

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


/**
 * 注意拦截后相关逻辑造成影响
 * 最好只对自己当前开发业务模块作出状态改变
 */

const val CONTROLLER_METHOD = AOP_CONTROLLER_MATCH
@Aspect
@Component
class AspectControllerMethod {
    private val log = LoggerFactory.getLogger(javaClass)
    // 定义一个切入点
    @Pointcut(CONTROLLER_METHOD)
    fun controllerMethods() {}
    // 在切入点匹配的方法执行后执行这个增强
    @Around("controllerMethods()") //这个方法定义了具体的通知
    @Throws(Throwable::class)
    fun allXXXControllerMethodAround(pjp: ProceedingJoinPoint): Any? {
        val obj = pjp.proceed()
        log.info("signature:{} ", pjp.signature)
        return obj
    }
//    @After("controllerMethods()")
//    fun logAfter() {
//        println("A method in the controller package has been executed.")
//    }
}