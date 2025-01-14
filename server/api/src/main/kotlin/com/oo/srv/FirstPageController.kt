package com.oo.srv

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ThreadLocalRandom

@RestController
private object FirstPageController {
    private val log = LoggerFactory.getLogger(FirstPageController::class.java)
    @GetMapping(FIRST_PAGE_CONFIG_URI)
    fun brand():Any{
        return mapOf(
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
    }
    //pageNum=1&pageSize=3&status=1&accountType=1&latitude=&longitude=&mustStatus=1
    /*
     {
            "id": 72,
            "mobile": "137xxxx5847",
            "openId": null,
            "wecatAccount": "123456789",
            "nickName": "青春",
            "jobTitle": "按摩",
            "headImg": "/profile/upload/2024/03/04/20240304214647A002.jpeg",
            "isAuth": "0",
            "score": 4.9,
            "isHeadAuth": "0",
            "fansNum": 3,
            "selfIntroduction": "青春靓丽，善解人意",
            "storeId": 73,
            "address": "广东省深圳市龙华区熙和路",
            "longitude": "114.034087",
            "latitude": "22.662552",
            "totalIntegral": 0.00,
            "storeLevel": "0",
            "province": "广东省",
            "city": "深圳市",
            "status": "1",
            "totalOrder": 58,
            "viewNum": 131,
            "sex": "女",
            "age": 22,
            "recentPhoto": "/profile/upload/2024/03/04/20240304214720A003.jpeg,/profile/upload/2024/03/04/20240304214734A004.jpeg,/profile/upload/2024/03/04/20240304214753A005.jpeg,/profile/upload/2024/03/04/20240304214805A006.jpeg",
            "code": "Q7NIMLF5",
            "totalIncome": 1276.30,
            "balance": 1106.30,
            "giftBalance": 2470.00,
            "giftTotal": 2470.00,
            "accountType": "1",
            "licenseImg": "",
            "personLabel": "御姐",
            "hobbies": "瑜伽,弹钢琴,穿搭,羽毛球",
            "publicStatus": true,
            "mustStatus": true,
            "newStatus": true,
            "redStatus": false,
            "reliableStatus": false,
            "discountsStatus": false,
            "createTime": "2023-06-07 10:44:33",
            "distance": 11969.95,
            "storeName": "麦香园休闲",
        },
     */
    @GetMapping(FIRST_PAGE_ACTRESS3_URI)
    fun actresses3():Any{
        return mapOf(
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
    }
    @GetMapping(FIRST_PAGE_NEAR10_URI)
    fun near10Actresses():Any{
        return mapOf(
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
    }
    @GetMapping(FIRST_PAGE_HOT10_URI)
    fun hot10Actresses():Any{
        return mapOf(
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
    }
    @GetMapping(FIRST_PAGE_NEW10_URI)
    fun new10Actresses():Any{
        return mapOf(
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
    }

}