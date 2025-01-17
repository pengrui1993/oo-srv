package com.oo.srv


import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

object CallValues{
        const val READ = 0.toByte()
        const val WRITE = 1.toByte()

        const val GUEST = 0.toByte()
        const val ACTRESS = 1.toByte()
        const val ADMIN = 2.toByte()
        const val ACCOUNTING = 1.toByte()
        const val OPERATOR = 2.toByte()
}

@Entity
@Table(name="biz_api_call_log")
class BizApiCall{
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
    var roleType = CallValues.GUEST
    @ColumnDefault("0")
    @Comment("0:read,1:write")
    @Final
    var rw = CallValues.READ
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
class SysApiCall{
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
    var roleType = CallValues.GUEST
    @ColumnDefault("0")
    @Comment("0:read,1:write")
    var rw = CallValues.READ
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
class BizProcedure(var creator:Long = 0L){
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
//@Entity
//@Table(name="sys_token")
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
//    @GeneratedValue(generator = "custom-generator",strategy = GenerationType.IDENTITY)
//    @Column(name = "_key")
    var key = ""//h2 , key is no working
    @Column(columnDefinition = "TEXT")
    var value = ""
    var cfgGroup = ""
    var pattern = ""

}
@Entity
@Table(name="sys_user")
class SysUser:AdminUserInfo{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id:Long? = null
    var avatar:String? = null//"https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif"
    @Column(unique = true)
    var uname:String? = null
    var upwd:String? = null
    var name:String? = null
    var role:String? = null
    var age:Int? = null
    var curToken:String? = null
    fun clear():SysUser{
        return clearFieldsToNull(SysUser::class.java,this)
    }

}

//@Entity
//@Table(name="sys_role_powers", indexes = [
//    Index(name = "role_index_name",  columnList="role", unique = false)
//    ,Index(name = "power_index_name",  columnList="power", unique = false)
//])
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
//@Entity
//@Table(name="sys_role")
class SysRole{
    @Id
    var role = "admin"
    var testToken = ""
    var introduction = "I am a super administrator"
    val createTime = LocalDateTime.now()
}
//@Entity
//@Table(name="sys_power")
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

typealias FileInfoId = Long
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
    var id:FileInfoId? = null//1
    var name:String? = null//"1.png"
    var refCount:Long? = null//0L
    var suffix:String? = null//".png"
    var mime:String?= null//"image/png"
    var originName:String? = null//"girl.png"
    var description:String? = null//"that is a normal file"
    var diskPath:String? = null//"/data/oo-srv/static/upload/20200101/2.png"
    var uriPath:String? = null//"20200101/2.png"
    var sha1:String? = null//"d1882b6063a512f60de9cdbc7e77dc2f66754a26"
    var sizeInBytes:Long? = null//1627848
    var createdTime:LocalDateTime? = LocalDateTime.now() //2025-01-17 05:15:46.164
    var updatedTime:LocalDateTime? = createdTime//2025-01-17 05:15:46.164
    var uploadDuration:Duration? = null//Duration.ofSeconds(1) 1000000
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

class TableVersionManager{
    private val mainTableId ="TableVersion"
    private val record = mutableMapOf(
        mainTableId to TableVersion(mainTableId)
        ,"sys_user" to TableVersion("sys_user")
    )
    private val dirtyTables = mutableSetOf<String>()
    fun onTableStateChange(id:String){
        if(id==mainTableId)return
        record[id]?.let {
            it.version++
            record[mainTableId]!!.version++
            dirtyTables+=id
            dirtyTables+=mainTableId
        }
    }
    fun onTick(){
        if(dirtyTables.isEmpty())return
        //flush dirtyTables data to storage
        dirtyTables.clear()
    }

}

class TableVersion(val name:String = "TableVersion"){
    var version = 0
}