package com.oo.srv

import cn.hutool.core.util.URLUtil
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.micrometer.common.util.StringUtils
import jakarta.annotation.Resource
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * "1233213;3321;9998877" to
 * [
 *  "http://domain/oo-srv/static/20231212/1233213.jpg
 * ,"http://domain/oo-srv/static/20230112/3321.mp4
 * ,"http://domain/oo-srv/static/20230112/9998877.mp3
 * ]
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@JsonSerialize(using = FileIdsSplitFormat::class)
@JacksonAnnotationsInside
annotation class FilesUrlVo
/**
 * "3321" to "http://domain/oo-srv/static/2023/01/12/3321.mp4
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@JsonSerialize(using = FileIdFormat::class)
@JacksonAnnotationsInside
annotation class FileUrlVo
private const val sep = DB_FILE_SEP

private fun isNotFromLocalDisk(fileId: String): Boolean {
    return fileId.startsWith("http://") || fileId.startsWith("https://")
}
private fun getPathSuffixName(absolutePath: String): String {
    val indexOf = lastIndexOfSeparator(absolutePath, 2)
    val suffix = absolutePath.substring(indexOf)
    return suffix
}
private fun lastIndexOfSeparator(filePath: String, ln: Int): Int {
    var lastNumber = ln
    val isFileSeparator:(Char)->Boolean = {c->'/' == c || '\\' == c}
    if (StringUtils.isNotEmpty(filePath)) {
        var i = filePath.length
        while (true) {
            if (--i < 0) break
            if (isFileSeparator(filePath[i])
                &&--lastNumber <= 0) return i
        }
    }
    return -1
}

class FileIdFormat(
    @Resource val props:PropertiesAccessor
    ,@Resource val cache:FileInfoCache
): JsonSerializer<String>(){
    override fun serialize(value: String, jsonGenerator: JsonGenerator, serializers: SerializerProvider){
        val o = value
        if (!StringUtils.isEmpty(o) && (o != "true") && (o != "false")) {
            val fileId: String = o
            val url: String
            if (isNotFromLocalDisk(fileId)) {
                url = fileId
            } else {
                val absolutePath = cache.get(fileId.toLong())
                val suffixName = getPathSuffixName(absolutePath)
                url = URLUtil.completeUrl(props.serverOrigin, "${props.urlPrefix}$suffixName")
            }
            jsonGenerator.writeString(url)
        } else {
            jsonGenerator.writeString("")
        }
    }

}
class FileIdsSplitFormat(
    @Resource val props:PropertiesAccessor
    ,@Resource val cache:FileInfoCache
): JsonSerializer<String>() {
    override fun serialize(value: String, jsonGenerator: JsonGenerator, serializers: SerializerProvider) {
        if(StringUtils.isEmpty(value)
            || Objects.equals("true",value)
            || Objects.equals("false",value)
            ){
            jsonGenerator.writeArray(arrayOfNulls<String>(0), 0, 0)
            return
        }
        val urlArray = value.split(sep)
            .filter { ""!=it }
            .map { if(isNotFromLocalDisk(it)) it
                    else {URLUtil.completeUrl(props.serverOrigin
                    , props.urlPrefix + getPathSuffixName(cache.get(it.toLong())))} }
            .toTypedArray()
        jsonGenerator.writeArray(urlArray, 0, urlArray.size)
    }
}

private fun example(){
    val url = URLUtil.completeUrl("http://test", "/image/pkg.png")
    println(url) //  http://test/image/pkg.png
}
private fun cutPath() {
    val absolutePath = "/root/application/love_raise/uploads/2022-06-23/1539931626367090688.jpeg"
    val indexOf: Int = lastIndexOfSeparator(absolutePath, 3)
    val suffix = absolutePath.substring(indexOf)
    println(suffix)// /uploads/2022-06-23/1539931626367090688.jpeg
}