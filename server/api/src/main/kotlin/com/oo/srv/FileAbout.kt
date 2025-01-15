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
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.apache.tika.mime.MimeTypeException
import org.apache.tika.mime.MimeTypes
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer


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
            else {
                URLUtil.completeUrl(props.serverOrigin
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
@Component
class FileInfoCache(@Resource val fileRepository:UploadFileInfoRepo){
    private val loader = object: CacheLoader<Long, UploadFileInfo>(){
        override fun load(key: Long): UploadFileInfo {//load file info from database
            return fileRepository.findByIdOrNull(key)!!
        }
    }
    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.HOURS)
        .maximumSize(10000)
        .initialCapacity(300)
        .build(loader)
    fun get(fileInfoId:Long):String{
        return cache.get(fileInfoId).uriPath
    }
}

@Component
class Storage(
    @Resource val fileRepository:UploadFileInfoRepo
    ,@Resource val propertiesAccessor: PropertiesAccessor

){
    private val log = LoggerFactory.getLogger(javaClass)
    fun read(id: Long): UploadFileInfo? {
        return fileRepository.findByIdOrNull(id)
    }
    private val diskPrefix:String get() = propertiesAccessor.localUploadedPath
    fun store(mf: MultipartFile, t: Consumer<Throwable?>): UploadFileInfo {
        val sf = UploadFileInfo()
        sf.originName = mf.originalFilename
        sf.sizeInBytes = mf.size
        sf.mime = mf.contentType
        val pp: Path = Paths.get(diskPrefix).resolve(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        val f = pp.toFile()
        if (!f.exists() && !f.mkdirs()) log.warn("mkdir dir error,{}", f)
        val p = AtomicReference<Path>()
        val cleaner =  {
            try {
                if (null != p.get()) Files.deleteIfExists(p.get())
            } catch (ex: IOException) {
                log.error(ex.message, ex)
            }
            fileRepository.delete(sf)
        }
        p.set(pp.resolve(uuid() + ".tmp"))
        val fdp = p.get()!!.toFile()
        try {
            mf.transferTo(fdp)
        } catch (e: IOException) {
            cleaner()
            t.accept(e)
            throw e
        }
        sf.diskPath = diskPath(fdp)
        sf.sha1 = sha1(fdp)
        val example = Example.of(
            UploadFileInfo().apply { sizeInBytes = sf.sizeInBytes;sha1 = sf.sha1 }
            ,ExampleMatcher.matching().withIgnoreNullValues()
        )
        val page =  PageRequest.of(0, 2,Sort.by("createdTime").ascending())
        val exists = fileRepository.findAll( example,page)
        if (!exists.isEmpty) {
            val e = exists.first()
            log.warn("file conflicted,download:{},exists:{}", p.get(), e)
            fileRepository.save(sf)
            return e
        }
        try {
            sf.suffix = MimeTypes.getDefaultMimeTypes().forName(sf.mime).extension
        } catch (e: MimeTypeException) {
            cleaner()
            t.accept(e)
            throw e
        }
        var saved = fileRepository.save(sf)
        val newName = "${saved.id}${sf.suffix}"
        val newPath = File(f, newName)
        if (!p.get()!!.toFile().renameTo(newPath)) {
            val ex = IOException("file rename error")
            t.accept(ex)
            throw ex
        }
        saved.uriPath = File(f.name, newName).toString()
        saved.diskPath = diskPath(newPath)
        saved = fileRepository.save(saved)
        log.info("saved file info:{}", saved) //3113052
        return saved
    }
    fun sha1(f: File): String {
        val digest = DigestUtils(MessageDigestAlgorithms.SHA_1)
        return digest.digestAsHex(f)
    }
    fun diskPath(f: File): String {
        return try {
            f.canonicalPath
        } catch (e: IOException) {
            log.warn(e.message,e)
            f.absolutePath
        }
    }
}