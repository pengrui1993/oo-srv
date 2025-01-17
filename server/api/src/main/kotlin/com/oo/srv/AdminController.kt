package com.oo.srv

import com.wf.captcha.SpecCaptcha
import jakarta.annotation.PostConstruct
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.data.domain.*
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.caseSensitive
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
private class AdminTransactionController(@Resource val tranRepo:BizTranRepo){
    @GetMapping(ADMIN_TRANSACTION_LIST_URI)
    fun list():Any{
        return mapOf(
            "code" to AdminApiCode.OK.code,
            "data" to mapOf(
                "total" to tranRepo.count(),
                "items" to tranRepo.findAll()
            )
        )
        //return transList
    }
    @GetMapping(ADMIN_SEARCH_USER_URI)
    fun searchUser(){

    }
}


@RestController
class AdminAuthController(
    @Resource val sessionManager:AdminSessionManager
    ,@Resource val userRepo:SysUserRepo
){
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
    //ocr-captcha
    private val log = LoggerFactory.getLogger(javaClass)
    @GetMapping(ADMIN_AUTH_CAPTCHA_URI)
    fun captcha(request:HttpServletRequest,response:HttpServletResponse){
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
    @PostMapping(ADMIN_USER_LOGIN_URI)
    fun login(@RequestBody body:Map<String,String>,request:HttpServletRequest):Any{
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
        if(Objects.isNull(session))throw AdminVerificationExpiredException()
        val cap = session!!.getAttribute(AUTH_CAPTCHA_KEY)
        if(Objects.isNull(cap))throw AdminVerificationExpiredException()
        val pair = cap  as Pair<String,LocalDateTime>
        if(pair.second.isBefore(LocalDateTime.now())) {
            session.removeAttribute(AUTH_CAPTCHA_KEY)
            throw AdminVerificationExpiredException()
        }
        if(code!=pair.first) throw AdminTokenExpiredException()
        session.removeAttribute(AUTH_CAPTCHA_KEY)
        val ex = Example.of(SysUser().clear().also { it.upwd = upwd;it.uname=uname }
            , ExampleMatcher.matching().withIgnoreNullValues()
                .withMatcher("uname",caseSensitive())
                .withMatcher("upwd",caseSensitive())
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
    fun logout(req:HttpServletRequest):Any{
        val token = req.getHeader(ADMIN_AUTH_KEY)
        sessionManager.destroyToken(token!!)
        return mapOf(
            "code" to AdminApiCode.OK.code,
            "data" to "success",
        )
    }

    private val usersRoles = lazy {
        ClassLoader.getSystemResourceAsStream("users.json")
            .use {gson.fromJson(readInputStreamAsString(it!!),HashMap::class.java) as Map<String,*>}
    }
    @GetMapping(ADMIN_USER_INFO_URI)
    fun userRoles(
//        @RequestParam token:String
        req:HttpServletRequest
    ):Any{
        val user = req.getAttribute(REQ_USER_KEY) as SysUser
        val err = {mapOf(
            "code" to AdminApiCode.ILLEGAL_TOKEN.code,
            "message" to "Login failed, unable to get user details.",
        )}
        val info = usersRoles.value[user.role]
        if(Objects.isNull(info))return err()
        return mapOf(
            "code" to AdminApiCode.OK.code,
            "data" to info,
        )
    }
}

@RestController
private object AdminRouterController{
    val allRouters = lazy {
        ClassLoader.getSystemResourceAsStream("routers.json")
            .use {readInputStreamAsString(it!!).let {routers-> gson.fromJson(routers,List::class.java) }}
    }
    @GetMapping(ADMIN_ROUTERS_URI)
    fun routers():Any{
        return mapOf(
            "code" to AdminApiCode.OK.code
            ,"data" to allRouters.value
        )
    }
}
@RestController
private class AdminRoleController(@Resource  val repo:BizApiCallRepository){
    val roleRouters = lazy {
        ClassLoader.getSystemResourceAsStream("roleRouters.json")
            .use {readInputStreamAsString(it!!).let {rolesRouters-> gson.fromJson(rolesRouters,List::class.java) }}
    }
    @GetMapping(ADMIN_ROLES_URI)
    fun rolesRouters():Any{
        return mapOf(
            "code" to AdminApiCode.OK.code
            ,"data" to roleRouters.value
        )
    }
    @PostMapping("/dev-api/vue-element-admin/role/add")
    fun add(){
        val page:Page<BizApiCall> = repo.findAll(
            Example.of(BizApiCall(), ExampleMatcher.matching().withIgnoreNullValues())
            ,PageRequest.of(0, 1
                , Sort.by(Sort.Direction.ASC, "seatNumber")))

        Sort.by(listOf(Sort.Order.asc("seatNumber")
                        ,Sort.Order.desc("createdTime")))
    }
    @PutMapping(ADMIN_ROLE_PERM_UPDATE)
    fun update(@RequestBody body:String):Any{
        println(body)// see rolePermUpdate.json
        return mapOf(
            "code" to AdminApiCode.OK.code
        )
    }
    @DeleteMapping(ADMIN_ROLE_PERM_DELETE)
    fun delete(@RequestBody body:String):Any{
        println(body)//{"role":"editor"}
        return mapOf(
            "code" to AdminApiCode.OK.code
        )
    }

}
@RestController
object AdminArticleController{
    data class PageParams (
        @Min(1) var page: Int = 1
        ,@Min(1) var limit: Int = 10
    )
    @GetMapping(ADMIN_ARTICLE_LIST)
    fun list(@Valid params:PageParams):Any{
        return ClassLoader.getSystemResourceAsStream("articleList.json")
            .use { readInputStreamAsString(it!!) }
    }
    @GetMapping("/dev-api/vue-element-admin/article/detail")
    fun detail(){

    }
    @GetMapping("/dev-api/vue-element-admin/article/pv")
    fun pv(){
    }

    @PostMapping("/dev-api/vue-element-admin/article/create")
    fun create(){
    }
    @PostMapping("/dev-api/vue-element-admin/article/update")
    fun update(){

    }
}


