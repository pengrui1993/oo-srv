package com.oo.srv

import com.google.gson.Gson
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
private object GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    private fun defaultHandle(ex: Throwable):Any{
        log.error(ex.message,ex)
        return mapOf(
            "code" to ApiCode.SERVER_ERROR.code
            ,"msg" to ApiCode.SERVER_ERROR.msg
            ,"trace" to traceToken()
        )
    }
    private fun defaultAdminHandler(ex:Throwable):Any{
        log.error(ex.message,ex)
        return mapOf(
            "code" to AdminApiCode.SERVER_ERROR.code
            ,"msg" to AdminApiCode.SERVER_ERROR.msg
            ,"trace" to traceToken()
        )
    }
    /**
     * 处理系统异常，兜底处理所有的一切
     */
    @ExceptionHandler(value = [Throwable::class])
    fun defaultExceptionHandler(req: HttpServletRequest,ex: Throwable): Any {
        return defaultHandle(ex)
    }
    @ExceptionHandler(value = [RuntimeException::class])
    fun unknownRuntimeException(ex: RuntimeException): Any {
        return defaultHandle(ex)
    }
    @ExceptionHandler(value = [IllegalArgumentException::class])
    fun illegalArgumentException(ex: IllegalArgumentException): Any {
        return defaultHandle(ex)
    }
    @ExceptionHandler(MultipartException::class)
    fun fileUploadExceptionHandler(ex: MultipartException): Any {
        return defaultHandle(ex)
    }
    /**
     * 处理 SpringMVC 请求参数缺失
     * 例如说，接口上设置了 @RequestParam("xx") 参数，结果并未传递 xx 参数
     */
    @ExceptionHandler(value = [MissingServletRequestParameterException::class])
    fun missingServletRequestParameterExceptionHandler(ex: MissingServletRequestParameterException): Any {
        return defaultHandle(ex)
    }
    /**
     * 处理 SpringMVC 请求参数类型错误
     * 例如说，接口上设置了 @RequestParam("xx") 参数为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun methodArgumentTypeMismatchExceptionHandler(ex: MethodArgumentTypeMismatchException): Any {
        return defaultHandle(ex)
    }

    /**
     * 处理 SpringMVC 参数校验不正确
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidExceptionExceptionHandler(ex: MethodArgumentNotValidException): Any {
        return defaultHandle(ex)
    }
    /**
     * 处理 SpringMVC 参数绑定不正确，本质上也是通过 Validator 校验
     */
    @ExceptionHandler(BindException::class)
    fun bindExceptionHandler(ex: BindException): Any {
        return defaultHandle(ex)
    }
    /**
     * 处理 Validator 校验不通过产生的异常
     */
    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun constraintViolationExceptionHandler(ex: ConstraintViolationException): Any {
        val constraintViolation = ex.constraintViolations.iterator().next()
        return defaultHandle(ex)
    }
    /**
     * 处理 Dubbo Consumer 本地参数校验时，抛出的 ValidationException 异常
     */
    @ExceptionHandler(value = [ValidationException::class])
    fun validationException(ex: ValidationException): Any {
        // 无法拼接明细的错误信息，因为 Dubbo Consumer 抛出 ValidationException 异常时，是直接的字符串信息，且人类不可读
        return defaultHandle(ex)
    }

    /**
     * 处理 SpringMVC 请求地址不存在
     * 注意，它需要设置如下两个配置项：
     * 1. spring.mvc.throw-exception-if-no-handler-found 为 true
     * 2. spring.mvc.static-path-pattern 为 /statics/ **
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun noHandlerFoundExceptionHandler(req: HttpServletRequest, ex: NoHandlerFoundException): Any {
        return defaultHandle(ex)
    }
    /**
     * 处理 SpringMVC 请求方法不正确
     * 例如说，A 接口的方法为 GET 方式，结果请求方法为 POST 方式，导致不匹配
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun httpRequestMethodNotSupportedExceptionHandler(ex: HttpRequestMethodNotSupportedException): Any {
        return defaultHandle(ex)
    }

    /**
     * 处理 Spring Security 权限不足的异常
     */
    @ExceptionHandler(value = [AdminAuthenticationException::class])
    fun accessDeniedExceptionHandler(req: HttpServletRequest, ex: AdminAuthenticationException): Any {
        log.error(ex.message,ex)
        return mapOf(
            "code" to AdminApiCode.AUTH_ERR
            ,"msg" to AdminApiCode.AUTH_ERR.msg
            ,"trace" to traceToken()
        )
    }
    /**
     * 处理 Spring Security 权限不足的异常
     */
    @ExceptionHandler(value = [AuthenticationException::class])
    fun accessDeniedExceptionHandler(req: HttpServletRequest, ex: AuthenticationException): Any {
        log.error(ex.message,ex)
        return mapOf(
            "code" to ApiCode.NO_AUTH.code
            ,"msg" to ApiCode.NO_AUTH.msg
            ,"trace" to traceToken()
        )
    }
    /**
     * 处理业务异常 ServiceException
     *
     * 例如说，商品库存不足，用户手机号已存在。
     */
    @ExceptionHandler(value = [BusinessException::class])
    fun serviceExceptionHandler(ex: BusinessException): Any {
        return mapOf(
            "code" to HttpStatus.BAD_REQUEST.value()
            ,"msg" to ex.message
            ,"trace" to traceToken()
        )
    }


}

