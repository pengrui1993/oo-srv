package com.oo.srv

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

@RestController
private object AuthController {
    //example https://app.xfdj.kft.ink/
    @PostMapping(SMS_LOGIN_URI)
    fun smsLogin():Any{
        return mapOf(
            "code" to ApiCode.OK.code
            ,"token" to UUID.randomUUID().toString().replace("-","")
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
private object FirstPageController {
    private val log = LoggerFactory.getLogger(javaClass)
    @GetMapping(FIRST_PAGE_CONFIG_URI)
    fun brand():Any{
        return ClassLoader.getSystemResourceAsStream("recentlyStates.json").use {
            readInputStreamAsString(it!!)
        }
    }
    //pageNum=1&pageSize=3&status=1&accountType=1&latitude=&longitude=&mustStatus=1
    @GetMapping(FIRST_PAGE_ACTRESS3_URI)
    fun actresses3():Any{
        return ClassLoader.getSystemResourceAsStream("actresses3.json").use {
            readInputStreamAsString(it!!)
        }
    }
    @GetMapping(FIRST_PAGE_NEAR10_URI)
    fun near10Actresses():Any{
        return ClassLoader.getSystemResourceAsStream("near10Act.json").use {
            readInputStreamAsString(it!!)
        }
    }
    @GetMapping(FIRST_PAGE_HOT10_URI)
    fun hot10Actresses():Any{
        return ClassLoader.getSystemResourceAsStream("hot10Act.json").use {
            readInputStreamAsString(it!!)
        }
    }
    @GetMapping(FIRST_PAGE_NEW10_URI)
    fun new10Actresses():Any{
        return ClassLoader.getSystemResourceAsStream("new10Act.json").use {
            readInputStreamAsString(it!!)
        }
    }
}

@RestController
private object DynamicStatesController {
    //?pageNum=1&pageSize=10&followDynamic=false&orderByColumn=createTime&isAsc=desc
    @GetMapping(DYNAMIC_STATES_PAGE_LIST_URI)
    fun listRecentlyActressesDynamicStates():Any{//每人一条
        return ClassLoader.getSystemResourceAsStream("recentlyStates.json").use {
            readInputStreamAsString(it!!)
        }
    }
    @PostMapping(DYNAMIC_STATES_PAGE_VISIT_URI)
    fun someoneVisited(customerId:Long,dynamicStatesIds:List<Long>):Any{
        return mapOf("code" to ApiCode.OK.code,
            "msg" to ApiCode.OK.msg
        )
    }
    /*
        store/dynamic/list?pageNum=1&pageSize=10&followDynamic=true&orderByColumn=createTime&isAsc=desc
     */
    @GetMapping(DYNAMIC_STATES_PAGE_FOLLOW_LIST_URI)
    fun customerFollowerList(){

    }
}

@RestController
private object StoreController {
    //store/serviceProject/list?specialStatus=true
    @GetMapping(STORE_PAGE_SEP3_LIST_URI)
    fun specialPrice(){
        val rspItem = ClassLoader.getSystemResourceAsStream("storeSpecial3.json")
    }
    //store/massageTechnician/listOfflineStore?latitude=&longitude=&searchValue=
    @GetMapping(STORE_PAGE_HOT_STORE_LIST_URI)
    fun hotStore(){
        val rspItem = ClassLoader.getSystemResourceAsStream("storeHotStore.json")
    }
    //store/massageTechnician/list?latitude=&longitude=
    @GetMapping(STORE_PAGE_HOT_ACTRESS_LIST_URI)
    fun hotWaitress(){
        val rspItem = ClassLoader.getSystemResourceAsStream("storeHotActress.json")
    }
}

@RestController
private object ActressController {
    //?pageNum=1&pageSize=10&status=1&accountType=1&latitude=&longitude=&jobTitle=%E6%8C%89%E6%91%A9
    //jobTitle=按摩
    //pageNum=1&pageSize=10&status=1&accountType=1&latitude=&longitude=&jobTitle=%E5%81%A5%E8%BA%AB
    //jobTitle=健身
    @GetMapping(ACTRESS_PAGE_LIST_BY_JOB_URI)
    fun listActressesByJobTitle(): Any {
        val types = listOf("按摩", "健身", "陪喝", "陪玩")
        return ClassLoader.getSystemResourceAsStream("actByJobTitle.json").use {
            readInputStreamAsString(it!!)
        }
    }
    //pageNum=1&pageSize=100&accountType=2&status=1
    @GetMapping(ACTRESS_PAGE_LIST_STORE_URI)
    fun listStoreForActress(): Any {
        return ClassLoader.getSystemResourceAsStream("listStoreFoAct.json").use {
            readInputStreamAsString(it!!)
        }
    }
}

@RestController
private object SharedController {
    data class FileExistReq(
        @Min(0)
        val size: Long,
        @NotBlank
        val sha1: String
    )
    @GetMapping(FILE_EXISTS)
    fun fileExists(@NotNull @Validated req: FileExistReq) {
    }
    @PostMapping(
        value = [FILE_UPLOAD_ONLY], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun upload(@Validated fs: Array<MultipartFile>): Any? {
        return null
    }
    data class ComplexUploadReq(
        @NotBlank
        val params: String
    ) {
        var files: List<MultipartFile>? = null
        fun set(files: Array<MultipartFile>) {
            this.files = files.toList()
        }
    }
    @PostMapping(
        value = [FILE_UPLOAD_PARAMS], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun upload(
        @RequestPart files: Array<MultipartFile>, params: ComplexUploadReq
    ) {
        params.set(files)
    }
    @GetMapping(FILE_DOWNLOAD)
    fun download(fileId: String, rsp: HttpServletResponse) {
        // get info by fileId
        //https://stackoverflow.com/questions/7137634/getting-mimetype-subtype-with-apache-tika
        val mime = "txt/plain" //org.apache.tika
        val fullFileName = "file"
        val urlRes = UrlResource("files///User/pengrui/files/a.txt")
        val res = FileSystemResource("/User/pengrui/files/a.txt")
        val dis = String.format("attachment; filename=\"%s\"", fullFileName)
        rsp.contentType = mime
        rsp.setHeader(HttpHeaders.CONTENT_DISPOSITION, dis)
        IOUtils.copy(res.inputStream, rsp.outputStream)
    }

    fun sha1(f: File): String {
        return DigestUtils(MessageDigestAlgorithms.SHA_1).digestAsHex(f)
    }
}

private fun test() {
    val msg = URLDecoder.decode("%E5%81%A5%E8%BA%AB", StandardCharsets.UTF_8)
    println(msg)
}
private val data1 = mapOf(
    "code" to ApiCode.OK.code,
    "rows" to listOf(
        mapOf(
            "id" to 68,
            "age" to 22,
            "isAuth" to "1",
            "nickName" to "倩敏",
            "jobTitle" to "按摩",
            "headImg" to "https://image.xfdj.kft.ink/profile/upload/2023/02/11/20230211154906A054.jpg",
            "totalOrder" to 29,
            "distance" to 11975.71,
            "storeName" to "健养汇养生馆（龙华店）"
        )
    )
)
private val data2 = mapOf("code" to ApiCode.OK.code,
    "rows" to listOf(mapOf(
        "id" to 19,
        "storeName" to "健健养生馆",
        "storeDesc" to "健健养生馆",
        "mainImg" to "https://image.xfdj.kft.ink/profile/upload/2023/06/26/20230626081429A001.jpeg",
        "status" to "0",
        "accountId" to 75,
        "serviceTime" to "00 to00-23 to59",
        "address" to "广东省深圳市龙岗区龙翔大道8033号",
        "addressDetail" to "深圳市龙岗区人民政府",
        "longitude" to "22.720828",
        "latitude" to "22.720828",
        "tel" to "13510733365",
        "storeType" to "1",
        "mobile" to "135xxxx3365"))
)

private val data3 =  mapOf("code" to ApiCode.OK.code,
    "rows" to listOf(mapOf(
        "id" to 150,
        "storeId" to 75,
        "content" to "美妙的一天",
        "imgUrl" to "https://image.xfdj.kft.ink/profile/upload/2024/03/06/20240306213439A001.jpeg",
        "longitude" to 114.059468,
        "latitude" to 22.621726,
        "visitedTimes" to 107,
        "viewNum" to 107,
        "likeNum" to 0,
        "dynamicType" to "1",
        "createTime" to "2024-03-06 21:36:18",


        "actressId" to 71,
        "headImg" to "https://image.xfdj.kft.ink/profile/upload/2023/06/07/20230607100032A002.jpeg",

        "storeId" to 19,
        "storeName" to "健健养生馆",
    ))
)
private val data4 = mapOf(
    "code" to ApiCode.OK.code
    ,"rows" to listOf(mapOf(
        "id" to 2
        ,"position" to "1"
        ,"title" to "保健按摩精油开背"
        ,"imgUrl" to "https://image.xfdj.kft.ink/profile/upload/2023/01/07/20230107143256A056.jpg"
        ,"linkUrl" to "https://app.xfdj.kft.ink/pages/technicianDetail/technicianDetail?id=68"
        ,"linkType" to "INNER_PAGE"
        ,"sortOrder" to "2"
        ,"status" to "0"
    ))
)
private val data5= mapOf(
    "code" to ApiCode.OK.code
    ,"count" to 3
    ,"rows" to listOf(mapOf(
        "id" to 72
        ,"nickName" to "青春"
        ,"mobile" to "137xxxx5847"
        ,"jobTitle" to "按摩"
        ,"distance" to 11969.95
        ,"storeId" to 73
        ,"mustStatus" to true
        ,"headImg" to "https://image.xfdj.kft.ink/profile/upload/2024/03/04/20240304214647A002.jpeg"
    ), mapOf(
        "id" to 59
        ,"nickName" to "芳芳"
        ,"mobile" to "188xxxx8587"
        ,"jobTitle" to "按摩"
        ,"distance" to 11977.29
        ,"storeId" to 38
        ,"mustStatus" to true
        ,"headImg" to "https://image.xfdj.kft.ink/profile/upload/2023/02/11/20230211145621A018.jpg"
    ))
)
private val data6= mapOf(
    "code" to ApiCode.OK.code
    ,"count" to 10
    ,"rows" to listOf(mapOf(
        "id" to 72
        ,"nickName" to "青春"
        ,"mobile" to "137xxxx5847"
        ,"jobTitle" to "按摩"
        ,"address" to "广东省深圳市龙岗区"
        ,"longitude" to "114.130271"
        ,"latitude" to "22.700883"
        ,"distance" to 11969.95
        ,"storeId" to 73
        ,"mustStatus" to true
        ,"headImg" to "https://image.xfdj.kft.ink/profile/upload/2024/03/04/20240304214647A002.jpeg"
    ))
)
private val data7= mapOf(
    "code" to ApiCode.OK.code
    ,"count" to 10
    ,"rows" to listOf(mapOf(
        "id" to 72
        ,"nickName" to "青春"
        ,"mobile" to "137xxxx5847"
        ,"jobTitle" to "按摩"
        ,"longitude" to "114.130271"
        ,"latitude" to "22.700883"
        ,"distance" to 11969.95
        ,"storeId" to 73
        ,"redStatus" to true
        ,"headImg" to "https://image.xfdj.kft.ink/profile/upload/2024/03/04/20240304214647A002.jpeg"
    ))
)
private val data8 = mapOf(
    "code" to ApiCode.OK.code
    ,"count" to 6
    ,"rows" to listOf(mapOf(
        "id" to 72
        ,"nickName" to "青春"
        ,"storeName" to "麦香园休闲"
        ,"longitude" to "114.130271"
        ,"latitude" to "22.700883"
        ,"distance" to 11969.95
        ,"storeId" to 73
        ,"newStatus" to true
        ,"headImg" to "https://image.xfdj.kft.ink/profile/upload/2024/03/04/20240304214647A002.jpeg"
    ))
)