package com.oo.srv

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.apache.tomcat.util.http.fileupload.IOUtils
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
import java.io.IOException

@RestController
private object SharedController {
    data class FileExistReq(
        @Min(0)
        val size:Long,
        @NotBlank
        val sha1:String
    )
    @GetMapping(FILE_EXISTS)
    fun fileExists(@NotNull @Validated req:FileExistReq){

    }
    @PostMapping(value = [FILE_UPLOAD_ONLY]
        , consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun upload(@Validated fs: Array<MultipartFile>): Any? {
        return null
    }

    data class ComplexUploadReq(
        @NotBlank
        val params:String
    ){
        var files:List<MultipartFile>? = null
        fun set(files:Array<MultipartFile>){
            this.files = files.toList()
        }
    }
    @PostMapping(
        value = [FILE_UPLOAD_PARAMS]
        , consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun upload(@RequestPart files:Array<MultipartFile>
               ,params:ComplexUploadReq){
        params.set(files)
    }


    @GetMapping(FILE_DOWNLOAD)
    fun download(fileId:String,rsp: HttpServletResponse){
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