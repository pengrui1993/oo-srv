package com.oo.srv


import jakarta.persistence.*
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Comment
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.io.File
import java.math.BigDecimal
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


open class CallValues{
    companion object{
        const val READ = 0.toByte()
        const val WRITE = 1.toByte()

        const val GUEST = 0.toByte()
        const val ACTRESS = 1.toByte()
        const val ADMIN = 2.toByte()
        const val ACCOUNTING = 1.toByte()
        const val OPERATOR = 2.toByte()
    }

}

@Entity
@Table(name="biz_api_call_log")
class BizApiCall:CallValues(){
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Final
    var id:Long = 0L
    @Final
    var reqId = ""
    @Final
    var uri = ""
    @ColumnDefault("0")
    var version:Int = 0
    @Final
    var cost = BigDecimal.ZERO
    @Final
    var uid:Long = 0L
    @Comment("0:guest,1:actress,2:manager")
    @Final
    var roleType = GUEST
    @ColumnDefault("0")
    @Comment("0:read,1:write")
    @Final
    var rw = READ
    @Final
    var startTime = LocalDateTime.now()
    var endTime:LocalDateTime? = null
    @Final
    @Column(columnDefinition = "TEXT")
    var inJson = ""
    @Final
    @Column(columnDefinition = "TEXT")
    var outJson = ""
}

@Entity
@Table(name="sys_api_call_log")
class SysApiCall:CallValues(){
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id:Long = 0L
    val reqId = ""
    val uri = ""
    @ColumnDefault("0")
    var version:Int = 0
    var cost = BigDecimal.ZERO
    var uid:Long = 0L
    @Comment("0:guest,1:accounting,2:operator")
    var roleType = GUEST
    @ColumnDefault("0")
    @Comment("0:read,1:write")
    var rw = READ
    var startTime = LocalDateTime.now()
    var endTime:LocalDateTime? = null
    @Column(columnDefinition = "TEXT")
    var inJson = ""
    @Column(columnDefinition = "TEXT")
    var outJson = ""
}


@Entity
@Table(name="biz_proc")
@Comment("procedure")
class BizProcedure(val creator:Long = 0L){
    @Id
    var id = uuid()
    var type = 0 //Procedure.Type like audit
    var status = "INIT" //WORKING|SUCCESS|ERROR
    var version = 0
    var running = TRUE
    var owner = creator
    @Column(columnDefinition = "TEXT")
    var requestData = ""
    val createdTime = LocalDateTime.now()
    var lastTimerTime = createdTime
    @Column(columnDefinition = "TEXT")
    var errorReason = "NO_ERROR"
    companion object{
        const val TRUE = 1.toByte()
        const val FALSE = 0.toByte()
    }
}
fun uuid():String{
    return UUID.randomUUID().toString().replace("-","")
}
@Entity
@Table(name="biz_tran")
class BizTransaction{
    @Id
    @Column(name="order_no")
    var order_no = uuid()
    var username = "Angela Thomas"
    var price = BigDecimal.ZERO
    var status = "success"
    var timestamp = 0L
    var version = 0
}
@Entity
@Table(name="sys_token")
class SysToken{
    @Id
    var token = "admin-token"
    @Column(columnDefinition = "TEXT")
    var data = ""
    val userId = ""
    val usersRole = "admin,editor"
    val createTime = LocalDateTime.now()
    var lastAccessTime = createTime
    var timeoutTime = Duration.ofHours(2)
}

@Entity
@Table(name="sys_config")
class SysConfig{
    @Id
    var key = ""
    var cfgGroup = ""
    var pattern = ""
    @Column(columnDefinition = "TEXT")
    var value = ""
}
@Entity
@Table(name="sys_user")
class SysUser{
    @Id
    var id = 0L
    var avatar = "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif"
    var name = "Super Admin"
    var role = "admin"
}

@Entity
@Table(name="sys_role_powers", indexes = [
    Index(name = "role_index_name",  columnList="role", unique = false)
    ,Index(name = "power_index_name",  columnList="power", unique = false)
])
class SysRolePowers{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id = 0L
    @Column(name="role", nullable = false)
    var role = "admin"
    @Column(name="power", nullable = false)
    val power = 0L
    val powerType = "MENU|PAGE|API"
}
@Entity
@Table(name="sys_role")
class SysRole{
    @Id
    var role = "admin"
    var testToken = ""
    var introduction = "I am a super administrator"
    val createTime = LocalDateTime.now()
}
@Entity
@Table(name="sys_power")
class SysPower(initId:Long = 0, initPid:Long = 0){
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id  = initId
    var pid = initPid
    var level = 0
    var path = "/redirect"
    var type = "MENU|PAGE|API"
    var name:String? = null//"DirectivePermission"
    var component:String? = null//"views/permission/directive"
    var redirect:String? = null//"dashboard"
    var alias:String? = null
    var metaTitle:String? = null //"Dashboard"
    var metaIcon:String?  = null //dashboard
    var metaAffix:String?  = null //"true"
    var metaNoCache:String?  = null//"true"
    var caseSensitive:String? = null//"true"
    var hidden:String? = null//"true"
    var alwaysShow:String? = null//"true"
    //[path, redirect, hidden, component, children, meta, title, icon, name, alwaysShow, noCache, roles, affix]
}


@Entity
@Table(name="upload_file_info"
    , indexes = [
        Index(name = "sha1_size_index_name", columnList = "sha1, sizeInBytes", unique = false)
        ,Index(name = "sha1_index_name",  columnList="sha1", unique = false)
    ]
)
class UploadFileInfo{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id = 0L
    var name = "12321321"
    var refCount = 0L
    var suffix = ".png"
    var mime = "image/png"
    var originName = "girl.png"
    var description = "that is a normal file"
    var diskPath = "/data/oosrv/static/upload/20200101/12321321.png"
    var uriPath = "/2020/01/01/12321321.png"
    var sha1 = "133123131"
    var sizeInBytes = 0L
    var createdTime = LocalDateTime.now()
    var updatedTime:LocalDateTime = createdTime
    var uploadDuration:Duration = Duration.ofSeconds(1)

    override fun toString(): String {
        return "UploadFileInfo(id=$id, name='$name', refCount=$refCount, suffix='$suffix', mime='$mime', originName='$originName', description='$description', diskPath='$diskPath', uriPath='$uriPath', sha1='$sha1', sizeInBytes=$sizeInBytes, createdTime=$createdTime, updatedTime=$updatedTime, uploadDuration=$uploadDuration)"
    }
}

typealias Meta = org.apache.tika.metadata.Metadata
fun demoForTestMime() {
    val tika = TikaConfig()
    val myListOfFiles = listOf(Paths.get("/tmp/springfox-core-2.5.0.jar"))
    for (f in myListOfFiles) {
        val metadata = Meta()
        //TikaInputStream sets the TikaCoreProperties.RESOURCE_NAME_KEY
        //when initialized with a file or path
        val v = TikaInputStream.get(f, metadata)
        val mimetype = tika.detector.detect(v, metadata).toString()
        println("File $f is $mimetype")//File /tmp/j1.jpg is image/jpeg
    }
    val myListOfStreams = listOf(
        URI.create("file:///tmp/j1.jpg").toURL().openStream()
        ,Files.newInputStream(Paths.get("/tmp/dicts.xlsx"))
    )
    for (`is` in myListOfStreams) {
        `is`.use {
            val metadata = Meta()
            //if you know the file name, it is a good idea to
            //set it in the metadata, e.g.
            //metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, "somefile.pdf");
            val mimetype: String = tika.getDetector().detect(
                TikaInputStream.get(`is`), metadata
            ).toString()
            println("Stream $`is` is $mimetype")
        }
    }
}
@Entity
@Table(name="biz_payment")
class BizPayment{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id = 0L
    var type = 0
}
@Entity
@Table(name="biz_order")
class BizOrder{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id = 0L
    var state = 0
    var type = 0
    var paymentId = 0L
    var version = 0
}
@Entity
@Table(name="biz_user")
class BizUser{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id = 0L
    var name = "Super Admin"
    var phone = "13333333333"
    var avatar = "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif"
    var openId = "234432"
    var roleCustomer = 0
    var roleActress = 0
}
@Entity
@Table(name="biz_csm_user")
class CustomerUser{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id = 0L
    var name = "Super Admin"
    var phone = "13333333333"
    var avatar = "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif"
    var openId = "234432"
}
@Entity
@Table(name="biz_act_user")
class ActressUser{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id = 0L
    var name = "Super Admin"
    var phone = "13333333333"
    var avatar = "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif"
    var openId = "234432"
    var gender = "female"
}
