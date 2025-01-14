package com.oo.srv

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RestController
private object ActressController {
    //?pageNum=1&pageSize=10&status=1&accountType=1&latitude=&longitude=&jobTitle=%E6%8C%89%E6%91%A9
    //jobTitle=按摩
    //pageNum=1&pageSize=10&status=1&accountType=1&latitude=&longitude=&jobTitle=%E5%81%A5%E8%BA%AB
    //jobTitle=健身
    @GetMapping(ACTRESS_PAGE_LIST_BY_JOB_URI)
    fun listActressesByJobTitle():Any{
        val types = listOf("按摩","健身","陪喝","陪玩")
        return mapOf("code" to ApiCode.OK.code,
            "rows" to listOf(mapOf(
            "id" to 68,
            "age" to 22,
            "isAuth" to "1",
            "nickName" to "倩敏",
            "jobTitle" to "按摩",
            "headImg" to "https://image.xfdj.kft.ink/profile/upload/2023/02/11/20230211154906A054.jpg",
            "totalOrder" to 29,
            "distance" to 11975.71,
            "storeName" to "健养汇养生馆（龙华店）"))
        )
    }

    //pageNum=1&pageSize=100&accountType=2&status=1
    @GetMapping(ACTRESS_PAGE_LIST_STORE_URI)
    fun listStoreForActress():Any{
        return mapOf("code" to ApiCode.OK.code,
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
    }
}

private fun test() {
    val msg = URLDecoder.decode("%E5%81%A5%E8%BA%AB",StandardCharsets.UTF_8)
    println(msg)
}