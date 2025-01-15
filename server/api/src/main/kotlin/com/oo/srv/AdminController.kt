package com.oo.srv

import com.google.gson.Gson
import jakarta.annotation.Resource
import jakarta.validation.Valid
import org.springframework.data.domain.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
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



interface AdminSessionManager
@Component
class AdminSessionManagerImpl:AdminSessionManager{
}
@RestController
class AdminAuthController(
    @Resource val tokenRepo:SysTokenRepo
    ,@Resource val roleRepo:SysRoleRepo
){
    @PostMapping(ADMIN_USER_LOGIN_URI)
    fun login(@RequestBody body:Map<String,String>):Any{
        val username = body["username"]
        val err = {mapOf(
            "code" to AdminApiCode.AUTH_ERR.code,
            "message" to "Account and password are incorrect.",
        )}
        if(Objects.isNull(username))return err()
        val token = roleRepo.findByIdOrNull(username)
        if(Objects.isNull(token))return err()
        return mapOf(
            "code" to AdminApiCode.OK.code,
            "data" to Gson().fromJson(token!!.testToken,HashMap::class.java),
        )
    }
    @PostMapping(ADMIN_USER_LOGOUT_URI)
    fun logout():Any{
        return mapOf(
            "code" to AdminApiCode.OK.code,
            "data" to "success",
        )
    }
    @GetMapping(ADMIN_USER_INFO_URI)
    fun userInfo(@RequestParam token:String):Any{
        val err = {mapOf(
            "code" to AdminApiCode.ILLEGAL_TOKEN.code,
            "message" to "Login failed, unable to get user details.",
        )}
        val info = tokenRepo.findByIdOrNull(token)
        if(Objects.isNull(info))return err()
        return mapOf(
            "code" to AdminApiCode.OK.code,
            "data" to Gson().fromJson(info!!.data,HashMap::class.java),
        )
    }
}

@RestController
object AdminRouterController{
    @GetMapping(ADMIN_ROUTERS_URI)
    fun routers():Any{
        return ClassLoader.getSystemResourceAsStream("routersList.json")
            .use {readInputStreamAsString(it!!)}
    }
}
@RestController
private class AdminRoleController(@Resource  val repo:BizApiCallRepository){

    @GetMapping(ADMIN_ROLES_URI)
    fun roles():Any{
        return ClassLoader.getSystemResourceAsStream("roleRouters.json")
            .use {readInputStreamAsString(it!!)}
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
    class PageParams {
        var page: Int = 1
        var limit: Int = 10
    }
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


