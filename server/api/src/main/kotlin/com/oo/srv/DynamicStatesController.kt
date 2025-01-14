package com.oo.srv

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
private object DynamicStatesController {
    //?pageNum=1&pageSize=10&followDynamic=false&orderByColumn=createTime&isAsc=desc
    @GetMapping(DYNAMIC_STATES_PAGE_LIST_URI)
    fun listRecentlyActressesDynamicStates():Any{//每人一条
        return mapOf("code" to ApiCode.OK.code,
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