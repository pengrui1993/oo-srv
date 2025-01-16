package com.oo.srv

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import jakarta.annotation.Resource
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class TestInitRunner(
//    @Resource val tokenRepo:SysTokenRepo
//    ,@Resource val roleRepo:SysRoleRepo
    @Resource val transRepo:BizTranRepo
//    ,@Resource val userRepo:SysUserRepo
):CommandLineRunner{
    override fun run(vararg args: String?) {
        val transList = ClassLoader.getSystemResourceAsStream("transList.json")
                            .use {readInputStreamAsString(it!!)}
        val obj = gson.fromJson(transList,JsonObject::class.java)
        obj.getAsJsonObject("data").getAsJsonArray("items").forEach {
            val jo = it.asJsonObject
            val item = BizTransaction()
            item.order_no = jo.getAsJsonPrimitive("order_no").asString
            item.timestamp = jo.getAsJsonPrimitive("timestamp").asLong
            item.username = jo.getAsJsonPrimitive("username").asString
            item.price = jo.getAsJsonPrimitive("price").asBigDecimal
            item.status = jo.getAsJsonPrimitive("status").asString
            val res = transRepo.save(item)
            println(res.order_no)
        }

    }
}

