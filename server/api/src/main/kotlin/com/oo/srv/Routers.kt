package com.oo.srv

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import java.util.*


class SysPowerToRouter(powerId:Long = 0, powerPid:Long = 0){
    var id  = powerId
    var pid = powerPid
    var type = "MENU|PAGE|API"
    //    var front = "view.login"
//    var uri = "/view/login"
    var level = 0
    var path = "/redirect"
    var name:String? = "DirectivePermission"
    var component:String? = "views/permission/directive"
    var redirect:String? = "dashboard"
    var alias:String? = null
    var metaTitle:String? = "Dashboard"
    var metaIcon:String?  = "dashboard"
    var metaAffix:String?  = "true"

    var metaNoCache:String?  = "true"
    var caseSensitive:String? = "true"

    var children:List<Long>? = listOf(1L,2L)
    var metaRoles:List<String>?  = listOf("admin","editor") // select t.role from sys_role_power as t where t.pid = #{powerId}

    var hidden:String? = "true"
    var alwaysShow:String? = "true" //see client's code
    override fun toString(): String {
        return "SysPowerToRouter(id=$id, pid=$pid, type='$type', level=$level, path='$path', name=$name, component=$component, redirect=$redirect, alias=$alias, metaTitle=$metaTitle, metaIcon=$metaIcon, metaAffix=$metaAffix, metaNoCache=$metaNoCache, caseSensitive=$caseSensitive, children=$children, metaRoles=$metaRoles, hidden=$hidden, alwaysShow=$alwaysShow)"
    }
}
fun testToRouter() {
    val target = SysPowerToRouter(0L).apply { name = null }
    val map = listOf(
        target
        ,SysPowerToRouter(1L).apply {
            pid = 0L
            level = 1
            children = null
        }
        ,SysPowerToRouter(2L).apply {
            pid = 0L
            level = 1
            children = null
        }
    ).associateBy { it.id }
    val r = powerToRouter(target, map)
    println(GsonBuilder().setPrettyPrinting().create().toJson(r))
}
fun powerToRouter(p:SysPowerToRouter, idToPower:Map<Long,SysPowerToRouter>):VueRouter{
    val c = SysPowerToRouter::class
    val r = VueRouter()
    val mt = VueRouterMeta()
    val map = r.javaClass.declaredFields.associateBy { it.name }
    val mm = VueRouterMeta::class.java.declaredFields.associateBy { it.name }
    c.java.declaredFields.forEach {
        if(it.name.startsWith("meta")){
            val ss = it.name.split("meta")[1]
            val routerMetaName = ss[0].lowercase(Locale.getDefault())+ss.substring(1)
            mm[routerMetaName]?.let l1@{ metaField->
                if(routerMetaName=="roles"){
                    it.isAccessible = true
                    val listStr = it.get(p) ?: return@l1;
                    mt.roles = listStr.toString()
                        .split(",")
                        .stream().filter{id-> Objects.nonNull(id)}
                        .map { id->id.trim() }.filter{id-> id.isNotEmpty() }
                        .toList()
                }else{
                    metaField.isAccessible = true
                    it.isAccessible = true
                    if(it.type==metaField.type){
                        metaField.set(mt,it.get(p))
                    }else if(metaField.type==java.lang.Boolean::class.java){
                        metaField.set(mt,it.get(p).toString().toBoolean())
                    }else{
                        throw NotImplementedError()
                    }
                }
            }
        }else{
            map[it.name]?.let l0@{ routerFiled->
                routerFiled.isAccessible = true
                it.isAccessible = true
                if(routerFiled.name=="children"){
                    val ids = it.get(p)
                    if(Objects.isNull(ids))return@l0
                    r.children = (ids as List<*>)
                        .mapNotNull { i -> idToPower[i] }
                        .map{i->powerToRouter(i,idToPower)}
                        .toList()
                }else if(it.type==routerFiled.type){
                    routerFiled.set(r,it.get(p))
                }else {
                    if(routerFiled.type == java.lang.Boolean::class.java){
                        routerFiled.set(r,it.get(p).toString().toBoolean())
                    }else{
                        throw NotImplementedError()
                    }
                }
            }
        }
    }
    if(!mt.isAllEmpty())r.meta = mt
    return r
}
class VueRouterMeta{
    var title:String?=null
    var icon:String?=null
    var affix:Boolean? = null
    var noCache:Boolean? = null
    var roles:List<String>? = null
    fun isAllEmpty():Boolean{
        for (f in javaClass.declaredFields) if(Objects.nonNull(f.get(this)))return false
        return true
    }
}

class VueRouter{
    //sub router only
    var name:String? = "DirectivePermission"
    var path = "/redirect"
    var redirect = "dashboard"
    var component = "layout/Layout"
    var hidden:Boolean? = true
    var alwaysShow:Boolean? = true
    var children:List<VueRouter>? = null
    var caseSensitive:Boolean? = null
    /*
   "meta":{
          "title": "Dashboard",
          "icon": "dashboard",
          "affix": true
          "noCache": true
    }
   */
    var meta:VueRouterMeta? = null
}

fun testMain() {
//    testToRouter()
    parseRoutersToSysPowerList()
}

private fun parseRoutersToSysPowerList(){
    val msg = ClassLoader.getSystemResourceAsStream("routers.json").use{
        readInputStreamAsString(it!!)
    }
    val root = gson.fromJson(msg, JsonElement::class.java)
    val queue = LinkedList<JsonElement>()
    val gen = mutableMapOf<Long,SysPowerToRouter>()
    var idGen = 0L
    val nextId = {++idGen}
    queue.push(root)
    while(!queue.isEmpty()){
        val cur = queue.poll()
        val adapter = SysPowerToRouter()
        if(cur.isJsonArray){
            cur.asJsonArray.forEach {
                val id = nextId()
                it.asJsonObject.addProperty("id",id)
                it.asJsonObject.addProperty("pid",id)
                it.asJsonObject.addProperty("level",0)
                queue.push(it)
            }
        }else if(cur.isJsonObject){
            val obj = cur.asJsonObject
            // //[path, redirect, hidden, component, children,  name, alwaysShow    ,meta,title,icon,noCache, roles, affix]
            val id = obj.get("id")
            val pid = obj.get("pid")
            val path = obj.get("path")
            val redirect = obj.get("redirect")
            val hidden = obj.get("hidden")
            val component = obj.get("component")
            val children = obj.get("children")
            val name = obj.get("name")
            val alwaysShow = obj.get("alwaysShow")
            val level = obj.get("level").asInt
            adapter.level = level
            if(null!=id)adapter.id = id.asLong
            if(null!=pid)adapter.pid = pid.asLong
            if(null!=path)adapter.path = path.asString
            if(null!=redirect)adapter.redirect = redirect.asString
            if(null!=hidden)adapter.hidden = hidden.asString
            if(null!=component)adapter.component = component.asString
            if(null!=children)adapter.children = children.asJsonArray.toList()
                .stream().map{it.asJsonObject}.filter{!it.get("path").asString.startsWith("http")}
                .map{it.get("id")}.filter{Objects.nonNull(it)}
                .map { if(Objects.nonNull(it)) it.asLong else 0L  }.toList()
            if(null!=name)adapter.name = name.asString
            if(null!=alwaysShow)adapter.alwaysShow = alwaysShow.asString
            val meta = obj.get("meta")
            if(null!=meta){
                val m = meta.asJsonObject
                val title = m.get("title")
                val icon = m.get("icon")
                val noCache = m.get("noCache")
                val roles = m.get("roles")
                val affix = m.get("affix")
                if(null!=title)adapter.metaTitle = title.asString
                if(null!=icon)adapter.metaIcon = icon.asString
                if(null!=noCache)adapter.metaNoCache = noCache.asString
                if(null!=roles)adapter.metaRoles = roles.asJsonArray.toList() as List<String>
                if(null!=affix)adapter.metaAffix = affix.asString
            }
            println(adapter) //copy obj.data to power
            val ch = obj.get("children")
            if(Objects.nonNull(ch)&&ch.isJsonArray){
                ch.asJsonArray.forEach {
                    val childId = nextId()
                    val obj2 = it.asJsonObject
                    obj2.addProperty("id",childId)
                    it.asJsonObject.addProperty("level",1+level)
                    if(null!=pid)obj2.addProperty("pid",id.asLong)
                    queue.push(it)
                }
            }
        }
    }
}
private fun takeKeys() {
    val msg = ClassLoader.getSystemResourceAsStream("routers.json").use{
        readInputStreamAsString(it!!)
    }
    val root = gson.fromJson(msg, JsonElement::class.java)
    val queue = LinkedList<JsonElement>()
    queue.push(root)
    val keys = mutableSetOf<String>()
    while(!queue.isEmpty()){
        val cur = queue.poll()
        if(cur.isJsonArray){
            cur.asJsonArray.forEach {
                queue.push(it)
            }
        }else if(cur.isJsonObject){
            val obj = cur.asJsonObject
            obj.keySet().forEach{
                keys+=it
                val el = obj.get(it)
                if(el.isJsonArray||el.isJsonObject){
                    queue.push(el)
                }
            }
        }
    }
    println(keys)
}
private fun templateField(){
    val msg = "[path, redirect, hidden, component, children,  name, alwaysShow    ,meta,title,icon,noCache, roles, affix]"
    gson.fromJson(msg, JsonArray::class.java).forEach {
        val f = it.asString
        println(String.format("val %s = obj.get(\"%s\")",f,f))
    }
}
private fun templateBuilder(){
    val msg = "[path, redirect, hidden, component, children,  name, alwaysShow    ,meta,title,icon,noCache, roles, affix]"
    gson.fromJson(msg, JsonArray::class.java).forEach {
        val f = it.asString
        println(String.format("if(null!=%s)builder.append(%s).append(' ')",f,f))
    }
}