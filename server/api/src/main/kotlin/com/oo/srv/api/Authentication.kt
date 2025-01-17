package com.oo.srv.api

import com.wf.captcha.SpecCaptcha
import jakarta.annotation.PostConstruct
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.caseSensitive
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.AsyncHandlerInterceptor
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

interface AdminSessionManager {
    fun loadAdminUserInfo(token: String): AdminUserInfo
    fun createToken(res: AdminUserInfo): String
    fun destroyToken(token: String)
}
interface AdminUserInfo
class DummyAdminUserInfo:AdminUserInfo
@Component
private class AdminSessionManagerImpl:AdminSessionManager{
    @Resource lateinit var redisTemplate:RedisTemplate<String,AdminUserInfo>
    val vop = {redisTemplate.opsForValue()}
    val tokenPrefix = "TOKEN:ADMIN:"
    fun key(token:String):String{
        return tokenPrefix+token
    }
    override fun loadAdminUserInfo(token: String): AdminUserInfo {
        if(""==token)return DummyAdminUserInfo()
        val userInfo = vop().get(key(token))
        return if(Objects.isNull(userInfo))DummyAdminUserInfo() else userInfo!!
    }

    override fun createToken(res: AdminUserInfo): String {
        val tk = uuid()
        vop().set(key(tk),res, Duration.ofHours(1))
        return tk
    }
    override fun destroyToken(token: String) {
        redisTemplate.delete(key(token))
    }
    fun onAdminLoginSuccess(){}
    fun onAdminLoginFailWhenAlreadyLogon(){
        AdminApiCode.ALREADY_LOGON
    }
    fun onAdminLogoutSuccess(){}
}
interface AppUserInfo:Serializable
class DummyAppUserInfo:AppUserInfo
class CustomerUserInfo:AppUserInfo
class WaitressUserInfo:AppUserInfo
interface SessionManager{
    fun loadAppUserInfo(token:String):AppUserInfo
    fun createToken(res: AppUserInfo): String
    fun destroyToken(token: String)
}
@Component
private class SessionManagerImpl(
    @Resource val redisTemplate:RedisTemplate<String,Serializable>
):SessionManager{
    final val tokenPrefix = "TOKEN:APP:"
    final val wt = "w-"
    final val ct = "c-"
    final val waitressPrefix = tokenPrefix+wt
    final val customerPrefix = tokenPrefix+ct
    fun key(token:String):String{
        return tokenPrefix+token
    }
    fun isWaitressToken(token: String):Boolean{
        return token.startsWith(waitressPrefix)
    }
    fun isCustomerToken(token: String):Boolean{
        return token.startsWith(customerPrefix)
    }
    override fun loadAppUserInfo(token: String): AppUserInfo {
        if(""==token)return DummyAppUserInfo()
        return CustomerUserInfo()
    }
    val vop = {redisTemplate.opsForValue()}
    override fun createToken(res: AppUserInfo): String {
        var tk = uuid()
        when(res){
            is WaitressUserInfo->{
                tk = "$wt$tk"
                vop().set(key(tk),res, Duration.ofHours(1))
            }
            is CustomerUserInfo->{
                tk = "$ct$tk"
                vop().set(key(tk),res, Duration.ofHours(1))
            }
            else->throw IllegalArgumentException("invalid app userInfo")
        }
        return tk
    }
    override fun destroyToken(token: String) {
        redisTemplate.delete(key(token))
    }
}

@RestController
private class AppAuthController {
    //example https://app.xfdj.kft.ink/
    @PostMapping(CUSTOMER_SMS_LOGIN_URI)
    fun customerSmsLogin():Any{
        return mapOf(
            "code" to ApiCode.OK.code
            ,"token" to uuid()
        )
    }
    @PostMapping(WAITRESS_SMS_LOGIN_URI)
    fun waitressSmsLogin():Any{
        return mapOf(
            "code" to ApiCode.OK.code
            ,"token" to uuid()
        )
    }
    @PostMapping(LOGOUT_URI)
    fun logout():Any{
        return mapOf(
            "code" to ApiCode.OK.code
        )
    }
}
@RestController
class AdminAuthController(
    @Resource val sessionManager:AdminSessionManager
    ,@Resource val userRepo:SysUserRepo
){

    //ocr-captcha
    private val log = LoggerFactory.getLogger(javaClass)
    @GetMapping(ADMIN_AUTH_CAPTCHA_URI)
    fun captcha(request: HttpServletRequest, response: HttpServletResponse){
        val captcha = SpecCaptcha(130, 48, 4)
        response.contentType = "image/gif"
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setDateHeader("Expires", 0L)
        val session = request.getSession(true)
        val codeTimePair = captcha.text().lowercase(Locale.getDefault()) to LocalDateTime.now().plusMinutes(10)
        session.setAttribute(AUTH_CAPTCHA_KEY, codeTimePair)
        captcha.out(response.outputStream)
    }
    @PostConstruct
    private fun init(){
        val saved = userRepo.save(
            SysUser().also
            { it.name="tony";it.role="admin-token"
                it.uname="admin";it.upwd="111111"
                it.age = 13
            }
        )
    }
    @PostMapping(ADMIN_USER_LOGIN_URI)
    fun login(@RequestBody body:Map<String,String>, request: HttpServletRequest):Any{
        log.info("admin.login:{}",gson.toJson(body))
        val uname = body["username"]
        val upwd = body["password"]
        val code = body["verification"]!!
        val err = {
            mapOf(
                "code" to AdminApiCode.AUTH_ERR.code,
                "message" to "Account and password are incorrect.",
            )}
        if(Objects.isNull(uname)||uname!!.isEmpty())return err()
        if(Objects.isNull(upwd)||upwd!!.isEmpty())return err()
        val session = request.getSession(false)
        if(Objects.isNull(session))throw AdminVerificationExpiredException("missing session")
        val captchaHolder = session!!.getAttribute(AUTH_CAPTCHA_KEY)
        if(Objects.isNull(captchaHolder))throw AdminVerificationExpiredException("no captcha code in session")
        val pair = captchaHolder  as Pair<String, LocalDateTime>
        if(pair.second.isBefore(LocalDateTime.now())) {
            session.removeAttribute(AUTH_CAPTCHA_KEY)
            throw AdminVerificationExpiredException("captcha code expired")
        }
        if(code!=pair.first) throw AdminTokenExpiredException("captcha code no matching")
        session.removeAttribute(AUTH_CAPTCHA_KEY)
        val ex = Example.of(SysUser().clear().also { it.upwd = upwd;it.uname=uname }
            , ExampleMatcher.matching().withIgnoreNullValues()
                .withMatcher("uname", caseSensitive())
                .withMatcher("upwd", caseSensitive())
        )
        val res = userRepo.findOne(ex)
        if(res.isEmpty)return err()
        val token = sessionManager.createToken(res.get())
        session.invalidate()
        return mapOf(
            "code" to AdminApiCode.OK.code,
            "data" to token,
        )
    }
    @PostMapping(ADMIN_USER_LOGOUT_URI)
    fun logout(req: HttpServletRequest):Any{
        val token = req.getHeader(ADMIN_AUTH_KEY)
        sessionManager.destroyToken(token!!)
        return mapOf(
            "code" to AdminApiCode.OK.code,
            "data" to "success",
        )
    }
}

@Component
class AdminAuthInterceptor(
    @Resource val sessionManager:AdminSessionManager
    ,@Resource val propertiesAccessor: PropertiesAccessor
): AsyncHandlerInterceptor {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
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
) : AsyncHandlerInterceptor {
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