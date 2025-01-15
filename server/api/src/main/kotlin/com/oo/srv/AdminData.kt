package com.oo.srv

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import jakarta.annotation.Resource
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class TestInitRunner(
    @Resource val tokenRepo:SysTokenRepo
    ,@Resource val roleRepo:SysRoleRepo
    ,@Resource val transRepo:BizTranRepo
):CommandLineRunner{
    override fun run(vararg args: String?) {
        val gson = Gson()
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
        val users = ClassLoader.getSystemResourceAsStream("users.json")
            .use { readInputStreamAsString(it!!) }
            .let { Gson().fromJson(it,LinkedTreeMap::class.java) } as Map<String,*>
        users.entries.forEach {
            val st = SysToken()
            st.token = it.key
            st.data = gson.toJson(it.value)
            val res = tokenRepo.save(st)
            println(res.createTime)
        }
        val tokens = ClassLoader.getSystemResourceAsStream("tokens.json")
            .use { readInputStreamAsString(it!!) }
            .let { Gson().fromJson(it,LinkedTreeMap::class.java) } as Map<String,*>
        tokens.entries.forEach {
            val rs = SysRole()
            rs.role = it.key
            when(rs.role){
                "editor"->{
                    rs.introduction = "I am a editor"
                }
            }
            rs.testToken = gson.toJson(it.value)
            val res = roleRepo.save(rs)
            println(res.createTime)
        }
    }
}
private fun objToJson() {
    val g = GsonBuilder().setPrettyPrinting().create()
    val tokens = ClassLoader.getSystemResourceAsStream("tokens.json")
        .use { readInputStreamAsString(it!!) }
        .let { Gson().fromJson(it,LinkedTreeMap::class.java) } as Map<String,*>
    println(g.toJson(tokens))
    val users = ClassLoader.getSystemResourceAsStream("users.json")
        .use { readInputStreamAsString(it!!) }
        .let { Gson().fromJson(it,LinkedTreeMap::class.java) } as Map<String,*>
    println(g.toJson(users))
}

