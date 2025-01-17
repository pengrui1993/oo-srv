package com.oo.srv.api

import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.springdoc.core.annotations.ParameterObject
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
private class SharedController(
    @Resource private val storage: com.oo.srv.api.Storage
    ,@Resource private val tt: TransactionTemplate
) {
    /**
     * @see /server/src/test/java/digest/file.html
     */
    data class FileExistReq(
        @Min(0) val size: Long,
        @NotBlank @Size(min=1, max = 200)
        val sha1: String
    )
    @GetMapping(FILE_EXISTS)
    fun fileExists(@NotNull @Validated req: FileExistReq):Any {
        return mapOf("code" to 500,"data" to storage.exists(req.size,req.sha1))
    }
    @PostMapping(
        value = [FILE_UPLOAD_ONLY], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun upload(@Validated files: Array<MultipartFile>): Any {
        val req = """
            curl -X 'POST' \
          'http://localhost:8080/oo-srv/api/shared/file/upload-only' \
          -H 'accept: */*' \
          -H 'Content-Type: multipart/form-data' \
          -F 'files=@boom.gif;type=image/gif'
        """.trimIndent()
        val list = files.map { storage.store(it) }
        return mapOf("code" to 500,"data" to list)
    }
    data class Params(@Min(0) val age:Int, val name:String)
    @PostMapping(
        value = [FILE_UPLOAD_PARAMS]
        , consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun upload(
        @RequestPart files: Array<MultipartFile>
        ,@Validated @ParameterObject params: Params
    ):Any{
        val req = """
          curl -X 'POST' \
          'http://localhost:8080/oo-srv/api/shared/file/upload-params?age=11&name=abc' \
          -H 'accept: */*' \
          -H 'Content-Type: multipart/form-data' \
          -F 'files=@ddd-demo-requirement.png;type=image/png'
        """.trimIndent()

        return upload(files)
    }
    @GetMapping(FILE_DOWNLOAD)
    fun download(file:FileInfoId, rsp: HttpServletResponse) {
        download(file,rsp,storage)
    }
}
fun download(file:FileInfoId, rsp: HttpServletResponse,storage: com.oo.srv.api.Storage){
    // get info by fileId
    //https://stackoverflow.com/questions/7137634/getting-mimetype-subtype-with-apache-tika
    storage.fetch(file)?.let {
//        val urlRes = UrlResource("files///User/pengrui/files/a.txt")
        val res = FileSystemResource(it.diskPath!!)
        val dis = String.format("attachment; filename=\"%s\"", it.name)
        rsp.setHeader(HttpHeaders.CONTENT_DISPOSITION, dis)
        rsp.contentType = it.mime //org.apache.tika
        IOUtils.copy(res.inputStream, rsp.outputStream)
    }
}
private val uploadRsp = """
    {
      "code": 500,
      "data": [
        {
          "id": 1,
          "name": "1.png",
          "refCount": 0,
          "suffix": ".png",
          "mime": "image/png",
          "originName": "ddd-demo-requirement.png",
          "description": "files",
          "diskPath": "/private/tmp/oo-srv/upload/20250117/1.png",
          "uriPath": "20250117/1.png",
          "sha1": "b7d0e0679b1eadbb73542c3551ae8fa37d60ea12",
          "sizeInBytes": 720559,
          "createdTime": "2025-01-17 05:51:02",
          "updatedTime": "2025-01-17 05:51:02",
          "uploadDuration": "PT0S"
        }
      ]
    }
""".trimIndent()