package com.oo.srv

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(AopLogic::class.java)
/**
 * 描述aop 调用的顺序
 */
private annotation class Sequence(val value: Int = 0)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogAop
@Aspect
@Component
class AopLogic {
    //这个方法定义了切入点
    @Pointcut("@annotation(LogAop)")
    fun pc() {}
    private fun print(msg: String) {
        println(msg)
    }
    private fun now(): Long {
        return System.currentTimeMillis()
    }
    @Sequence(1)
    @Around("pc()") //这个方法定义了具体的通知
    @Throws(Throwable::class)
    fun around(pjp: ProceedingJoinPoint): Any {
//        print("around");
        val t1 = now()
        val obj = pjp.proceed()
        val t2 = now()
        //        logService.submit(()->log.info("\nmethod:{} ,call time:{} ms",pjp.getSignature(),t2-t1));
        log.info("method:{} ,call time:{} ms", pjp.signature, t2 - t1)
        return obj
    }

    //这个方法定义了具体的通知
    @Sequence(2) //    @Before("pc()")
    fun before(joinPoint: JoinPoint?) {
        print("before")
    }

    @Sequence(4) //    @After("pc()") //这个方法定义了具体的通知
    fun after(joinPoint: JoinPoint?) {
        print("after")
    }

    //    @AfterReturning(value = "execution(public int com.test.Controller.*(int,int))",returning = "res")
    fun afterReturning(joinPoint: JoinPoint?, res: Any?) {}
    /**
     * @param joinPoint
     * @param error 参数名需要和 注解的throwing 一致
     */
    @Sequence(3) //@AfterThrowing(pointcut = "pc()",throwing = "error")
    fun thr(joinPoint: JoinPoint?, error: Throwable?) {
        print("throwing")
        //signature:String com.habf.controller.StatisticsController.hello(),error:null
        //System.out.println(String.format("signature:%s,error:%s",joinPoint.getSignature(),error.getMessage()));
    }
}
