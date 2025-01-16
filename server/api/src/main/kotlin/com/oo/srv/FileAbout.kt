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
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MimeTypeException
import org.apache.tika.mime.MimeTypes
import org.apache.tika.parser.AutoDetectParser
import org.slf4j.LoggerFactory
import org.springframework.core.io.AbstractResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.UrlResource
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

private fun isNotFromLocalDisk(fileId: String): Boolean {
    return fileId.startsWith("http://") || fileId.startsWith("https://")
}
/*
    in: "/root/application/love_raise/uploads/2022-06-23/1539931626367090688.jpeg"
    out:"/2022-06-23/1539931626367090688.jpeg"
 */
private fun getPathSuffixName(absolutePath: String): String {
    val indexOf = lastIndexOfSeparator(absolutePath, 2)
    val suffix = absolutePath.substring(indexOf)
    return suffix
}

private fun cutPath() {
    val absolutePath = "/root/application/love_raise/uploads/2022-06-23/1539931626367090688.jpeg"
    val indexOf: Int = lastIndexOfSeparator(absolutePath, 3)
    val suffix = absolutePath.substring(indexOf)
    println(suffix)// /uploads/2022-06-23/1539931626367090688.jpeg
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
        if (!StringUtils.isEmpty(value) && (value != "true") && (value != "false")) {
            val url: String
            if (isNotFromLocalDisk(value)) {
                url = value
            } else {
                val absolutePath = cache.get(value.toLong())
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
    private val sep = DB_FILE_SEP
    override fun serialize(value: String, jsonGenerator: JsonGenerator, serializers: SerializerProvider) {
        if(StringUtils.isEmpty(value)
            || Objects.equals("true",value)
            || Objects.equals("false",value)
        ){
            jsonGenerator.writeArray(arrayOf<String>(), 0, 0)
            return
        }
        val urlArray = value.split(sep).stream().map{it.trim()}.filter { it.isNotEmpty() }
            .map { if(isNotFromLocalDisk(it)) it
                    else { URLUtil.completeUrl(props.serverOrigin
                , props.urlPrefix + getPathSuffixName(cache.get(it.toLong())))} }
            .toList().toTypedArray()
        jsonGenerator.writeArray(urlArray, 0, urlArray.size)
    }
}

private fun example(){
    val url1 = URLUtil.completeUrl("http://test", "image/pkg.png")
    val url2 = URLUtil.completeUrl("http://test/abc/", "/image/pkg.png")
    if(url1==url2) println(url2) //  http://test/image/pkg.png
}
fun UploadFileInfo.empty():UploadFileInfo{
    uriPath = "/TODO/IF/ERROR/FileAbout.kt"//TODO
    return this
}
@Component
class FileInfoCache(@Resource val fileRepository:UploadFileInfoRepo){
    private val empty = UploadFileInfo().empty()
    private val loader = object: CacheLoader<Long, UploadFileInfo>(){
        override fun load(key: Long): UploadFileInfo {//load file info from database
            return fileRepository.findByIdOrNull(key)?:empty
        }
    }
    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.HOURS)
        .maximumSize(10000)
        .initialCapacity(300)
        .build(loader)
    fun getFileInfo(id:FileInfoId):UploadFileInfo?{
        val res = cache.get(id)
        return if(res!=empty)res else null
    }
    fun get(fileInfoId:FileInfoId):String{
        return cache.get(fileInfoId).uriPath!!
    }
}
interface Storage{
    fun exists(size: Long, sha1: String): Boolean
    fun fetch(id: FileInfoId): UploadFileInfo?
    fun store(mf: MultipartFile): UploadFileInfo
}

@Component
class StorageImpl(
    @Resource val fileRepository:UploadFileInfoRepo
    ,@Resource val propertiesAccessor: PropertiesAccessor
    ,@Resource val fileInfoCache: FileInfoCache
):Storage{
    private val log = LoggerFactory.getLogger(javaClass)
    override fun exists(size: Long, sha1: String): Boolean {
        return fileRepository.count(exampleBy(size,sha1))>0
    }
    override fun fetch(id: FileInfoId): UploadFileInfo? {
        return fileInfoCache.getFileInfo(id)
    }
    private val diskPrefix:String get() = propertiesAccessor.localUploadedPath
    private val now = {System.currentTimeMillis()}
    override fun store(mf: MultipartFile): UploadFileInfo {
        val ufi = UploadFileInfo()
        ufi.originName = mf.originalFilename?:""
        ufi.refCount = 0L
        ufi.sizeInBytes = mf.size
        ufi.mime = mf.contentType
        ufi.description = mf.name
        ufi.suffix = suffixString(ufi.originName!!,ufi.mime)
        val subDirName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) //20201212
        val uploadFileParentDir: Path = Paths.get(diskPrefix).resolve(subDirName)
        val targetFileParentDir = uploadFileParentDir.toFile()
        if (!targetFileParentDir.exists() && !targetFileParentDir.mkdirs())  log.warn("mkdir dir error,{}", targetFileParentDir)
        var saved = fileRepository.save(ufi)
        val newName = "${saved.id}${saved.suffix}"
        val newFullPath = File(targetFileParentDir, newName)
        run {//save to disk
            val start = now()
            mf.transferTo(newFullPath)
            saved.uploadDuration = Duration.ofMillis(now()-start)
        }
        saved.sha1 = sha1(newFullPath)
        run l0@{//check file exists
            val example = exampleBy(saved.sizeInBytes!!,saved.sha1!!)
            val page =  PageRequest.of(0, 5,Sort.by("createdTime").descending())
            val exists = fileRepository.findAll(example,page)
            val sameSizeAndSha1Files = exists.content
            for (exist in sameSizeAndSha1Files) {
                if(Objects.isNull(exist)||Objects.isNull(exist.diskPath))continue
                val p1 = Paths.get(exist.diskPath!!)
                if(!Files.exists(p1)||!Files.isRegularFile(p1)) continue
                if(isSameFileInBytes(p1,newFullPath.toPath())){
                    log.warn("file conflicted,new file path:{},exists first path:{},exists size:{}"
                        , newFullPath, exist.diskPath,sameSizeAndSha1Files.size)
                    newFullPath.delete()
                    fileRepository.delete(saved)
                    return exist
                }
            }
        }
        saved.name = newName
        saved.diskPath = diskPath(newFullPath)
        saved.uriPath =  "$subDirName/$newName"//20201212/123213.txt
        saved.mime = saved.mime?:mimeFound(FileSystemResource(saved.diskPath!!))
        saved.updatedTime = LocalDateTime.now()
        saved = fileRepository.save(saved)
        log.info("saved file path:{},id:{}", saved.diskPath,saved.id)
        return saved
    }
    private fun suffixString(filename:String,mime:String?): String {
        if(filename.contains('.')){
            return filename.substring(filename.lastIndexOf('.'))
        }
        if(Objects.isNull(mime))return ""
        try {
            return MimeTypes.getDefaultMimeTypes().forName(mime!!).extension
        } catch (e: MimeTypeException) {
            log.warn(e.message,e)
            return ""
        }
    }

    private fun exampleBy(size: Long, sha1: String):Example<UploadFileInfo>{
        return Example.of(
            clearFieldsToNull(UploadFileInfo::class.java,UploadFileInfo())
                .apply { sizeInBytes = size;this.sha1 = sha1 }
            ,ExampleMatcher.matching().withIgnoreNullValues()
        )
    }
}
private fun mimeFound(resource: AbstractResource):String{
    val metadata = Metadata()
//    metadata.set(Metadata.RESOLUTION_UNIT, resource.getFile().getName());//RESOURCE_NAME_KEY
   TikaInputStream.get(resource.inputStream).use {
       val mediaType = AutoDetectParser().detector.detect(it, metadata)
       metadata[Metadata.CONTENT_TYPE] = mediaType.toString()
       return mediaType.toString()
   }
}
fun demoForTestMime() {
    val tika = TikaConfig()
    val myListOfFiles = listOf(Paths.get("/tmp/springfox-core-2.5.0.jar"))
    for (f in myListOfFiles) {
        val metadata = Metadata()
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
            val metadata = Metadata()
            //if you know the file name, it is a good idea to
            //set it in the metadata, e.g.
            //metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, "somefile.pdf");
            val mimetype: String = tika.detector.detect(
                TikaInputStream.get(`is`), metadata
            ).toString()
            println("Stream $`is` is $mimetype")
        }
    }
}
fun testFileMime() {
    val v = mimeFound(UrlResource("file:///tmp/oo-srv/upload/20250117/1.png"))//image/png
    val v2 = mimeFound(UrlResource("file:///Users/pengrui/Downloads/中国银行线上收银台接口规范1.2(1).docx"))//application/x-tika-ooxml
    println(v)
    println(v2)
}
fun testFileToString() {
    println(File("20201212","abc.txt").toString())//20201212/abc.txt
}
fun testExtension() {
    var ext = MimeTypes.getDefaultMimeTypes().forName("text/html").extension
    println(ext)//.html
    ext = MimeTypes.getDefaultMimeTypes().forName("application/octet-stream").extension
    println(ext)//.bin
}
fun detectApiUse() {
    println(diskPath(File("docs")))//f.canonicalPath
}
