package com.oo.srv

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
private object StoreController {
    //store/serviceProject/list?specialStatus=true
    /*
    {
            "id": 25,
            "storeId": 60,
            "serviceClassityId": null,
            "projectName": "上班族肩颈疲劳缓解",
            "projectImg": "/profile/upload/2023/02/11/20230211164819A072.png",
            "projectDesc": "【项目介绍】 \n肩颈推拿调理（60分钟）  \n1、放松肩颈（腰）（5分钟） \n2、经络按摩（20分钟） \n3、深度松解肩颈部位关节、肌肉、筋膜（20分钟） \n4、肩颈热敷（15分钟） 疏通经络，缓解肌肉僵硬、疲劳，促进气血运行，改善颈肩疼痛、头晕头痛，失眠多梦等 \n\n【适宜人群】 针对办公室久坐、体力劳动者人群进行经络疏通，缓解肌肉疲劳，缓解体弱虚疲、长期超负荷工作；疲劳、压力大、精神紧张等人士。 \n\n【门店设施】 \n☆拖鞋☆：一次一换一洗一消毒。 \n☆浴巾☆：一次一换一洗一消毒。 \n☆服装☆：一次一换一洗一消毒。 \n☆茶水点心\n☆：精美果盘、养生茶水。\n\n【关于我们】 坤泰堂的理疗师已经服务上千人次，获得良好口碑推荐。在专业医生团队指导下，享受医疗级服务，养生保健，防病的效果。 我们的理疗师有多年从业经验，专注专业，定期考核，不会向您收取任何小费（服务费），让您身体舒畅，充满活力！ 坤泰堂以优雅的环境、舒适的设施、专业的服务团队成为集中医诊疗、休闲SPA、艾灸推拿、养生中药泡浴、综合保健与理疗于一身的新型中医馆。  \n\n【温馨提示】 我们非常注重服务、重视态度，若您有任何不适，可随时告知门店前台或店长，请您给我们建议。 \n【服务功效】\n帮助睡眠,畅通血脉,调理肩颈不适,放松身心,改善睡眠,缓解肌肉劳损\n【按摩部位】\n肩部,颈部\n【服务用品】\n芳香精油,一次性按摩布,一次性床单,一次性防油裤\n【服务姿势】\n卧姿\n【适用人群】\n免疫力差,全身乏力,缺乏锻炼,身体疲劳\n【不宜人群】\n哺乳期,儿童,高龄者,高血压,骨折,骨质疏松,精神病患者,生理期孕妇,心脏病,醉酒者\n",
            "linePrice": 396.00,
            "sellPrice": 198.00,
            "serviceTime": 60,
            "callService": null,
            "offStore": null,
            "projectLabel": null,
            "sellNum": 45,
            "specialStatus": true,
            "addTimeLen": null,
            "addTimePrice": null,
            "status": "0",
            "updateTime": "2023-02-13 16:45:36",
            "createTime": null,
            "storeName": "坤泰堂.中医养生.艾灸推拿",
            "technicianId": null,
            "technicianName": null,
            "priceMin": null,
            "priceMax": null,
            "serviceTimeMin": null,
            "serviceTimeMax": null,
            "storeIds": null,
            "longitude": null,
            "latitude": null,
            "technicianList": null
        },
     */
    @GetMapping(STORE_PAGE_SEP3_LIST_URI)
    fun specialPrice(){

    }
    //store/massageTechnician/listOfflineStore?latitude=&longitude=&searchValue=
    /*
        {
            "searchValue": null,
            "createBy": "13267031549",
            "createTime": "2023-02-08 14:24:14",
            "updateBy": "admin",
            "updateTime": "2023-02-11 12:07:18",
            "id": 8,
            "storeName": "飞宏本草养生堂",
            "storeDesc": "简约小清新  走的是大气轻奢风  走进这里，就可以开始享受奢侈养生之旅了~   ",
            "mainImg": "/profile/upload/2023/02/08/20230208142229A041.jpeg",
            "sliderImg": "/profile/upload/2023/02/11/20230211120702A014.jpg",
            "status": "0",
            "accountId": 38,
            "serviceTime": "10:00-22:00",
            "address": "广东省深圳市龙岗区中心城龙岗大道龙城广场地铁站D出口",
            "addressDetail": "万汇大厦",
            "longitude": "22.717557",
            "latitude": "22.717557",
            "tel": "13267031549",
            "storeType": "1",
            "projects": [],
            "distance": 3436.8,
            "mobile": null
        },
     */
    @GetMapping(STORE_PAGE_HOT_STORE_LIST_URI)
    fun hotStore(){

    }
    //store/massageTechnician/list?latitude=&longitude=
    /*
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
            "headAuthRate": null,
            "remark": null,
            "address": "广东省深圳市龙华区熙和路",
            "longitude": "114.034087",
            "latitude": "22.662552",
            "totalIntegral": 0.00,
            "storeLevel": "0",
            "province": "广东省",
            "city": "深圳市",
            "area": null,
            "agentCommissionRate": null,
            "serviceMiles": null,
            "status": "1",
            "totalOrder": 58,
            "viewNum": 131,
            "sex": "女",
            "age": 22,
            "recentPhoto": "/profile/upload/2024/03/04/20240304214720A003.jpeg,/profile/upload/2024/03/04/20240304214734A004.jpeg,/profile/upload/2024/03/04/20240304214753A005.jpeg,/profile/upload/2024/03/04/20240304214805A006.jpeg",
            "code": "Q7NIMLF5",
            "promoterCode": null,
            "promoterId": null,
            "promoterPath": null,
            "totalIncome": 1276.30,
            "balance": 1106.30,
            "giftBalance": 2470.00,
            "giftTotal": 2470.00,
            "accountType": "1",
            "licenseImg": "",
            "licenseNum": null,
            "legalPerson": null,
            "personLabel": "御姐",
            "hobbies": "瑜伽,弹钢琴,穿搭,羽毛球",
            "voiceLabel": null,
            "videoAuth": null,
            "publicStatus": true,
            "mustStatus": true,
            "newStatus": true,
            "redStatus": false,
            "reliableStatus": false,
            "discountsStatus": false,
            "createTime": "2023-06-07 10:44:33",
            "updateTime": null,
            "agentCreateTime": null,
            "agentUpdateTime": null,
            "projectId": null,
            "distance": 11969.95,
            "customerVisit": null,
            "squareVisit": null,
            "offlineStatus": null,
            "storeIds": null,
            "storeName": "麦香园休闲",
            "storeCode": null,
            "storeType": null,
            "directCount": null,
            "sellAmount": null,
            "frontImg": null,
            "backgroundImg": null,
            "realName": null,
            "idCard": null,
            "bizType": null,
            "params": {}
     */
    @GetMapping(STORE_PAGE_HOT_ACTRESS_LIST_URI)
    fun hotWaitress(){

    }
}