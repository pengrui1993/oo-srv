package com.oo.srv.core

import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GlobalCoordinates


private fun main() {
    println("经纬度距离计算结果：" + haversine(109.371319, 22.155406, 108.009758, 21.679011) + "米")
}


/*
https://en.wikipedia.org/wiki/Haversine_formula
Haversine algorithm in sql

@Select("<script>SELECT*,ROUND(6378.138 * 2 * ASIN(SQRT(POW(SIN((#{lat} * PI() / 180 - lat * PI() / 180" +
        "    ) / 2),2) + COS(40.0497810000 * PI() / 180) * COS(lat * PI() / 180) * POW(" +
        "    SIN((#{lon} * PI() / 180 - lng * PI() / 180) / 2),2))) * 1000) AS dis" +
        "    FROM yshop_store_shop WHERE deleted=0 " +
        "<if test =\"name !=''\">and name = #{name}</if>" +
        "<if test =\"shopId > 0\">and id = #{shopId}</if>" +
        " ORDER BY dis ASC</script>"
)

 */
fun haversine(longitudeFrom: Double, latitudeFrom: Double, longitudeTo: Double, latitudeTo: Double): Double {
    val source = GlobalCoordinates(latitudeFrom, longitudeFrom)
    val target = GlobalCoordinates(latitudeTo, longitudeTo)
    return GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.Sphere, source, target).ellipsoidalDistance
}
/*
SELECT
    *,
    6378.138 * 2 * ASIN(
      SQRT(
        POW(
          SIN(
            (
              '.$lat.' * PI() / 180 - lat * PI() / 180
            ) / 2
          ), 2
        ) + COS('.$lat.' * PI() / 180) * COS(lat * PI() / 180) * POW(
          SIN(
            (
              '.$lng.' * PI() / 180 - lng * PI() / 180
            ) / 2
          ), 2
        )
      )
    ) *1000 AS distance
FROM
    distance
ORDER BY
    distance ASC
 */