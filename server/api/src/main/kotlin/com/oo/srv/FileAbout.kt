package com.oo.srv

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
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
    private val log = LoggerFactory.getLogger(Storage::class.java)
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