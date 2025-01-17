package com.oo.srv.api

import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.*
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
@RestController
private class RoleController{
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
private class AdminRouterController{
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
    @Write
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
class AdminArticleController{
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


