package com.oo.srv

import com.google.gson.Gson
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import java.io.File
import java.io.InputStream
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

val gson = Gson()
fun now():Long{return System.currentTimeMillis()}

fun uuid():String{
    return UUID.randomUUID().toString().replace("-","")
}
fun readInputStreamAsString(ins: InputStream):String{
    val buf = ByteArray(1024)
    var len: Int
    val sw = StringWriter()
    while(true){
        len=ins.read(buf)
        if(-1==len)break
        sw.write(String(buf,0,len))
    }
    return sw.toString()
}
fun <T> clearFieldsToNull(clazz:Class<T>, obj:T):T{
    clazz.declaredFields.forEach {
        it.isAccessible=true
        it.set(obj,null)
    }
    return obj
}

fun sha1(f: File): String {
    val digest = DigestUtils(MessageDigestAlgorithms.SHA_1)
    return digest.digestAsHex(f)
}
fun diskPath(f: File): String {
    return try {
        f.canonicalPath
    } catch (e: Throwable) {
        f.absolutePath
    }
}
fun compareSame() {
    val p1 = Paths.get("/tmp/oo-srv/upload/20250117/1.gif")
    val p2 = Paths.get("/tmp/oo-srv/upload/20250117/boom.gif")
    println(isSameFileInBytes(p1,p2))//true
}
private const val bufSize = 1024
fun isSameFileInBytes(p1: Path, p2:Path):Boolean{
    if(diskPath(p1.toFile())==diskPath(p2.toFile()))return true
    val f1 = p1.toFile()
    val f2 = p2.toFile()
    if(f1.length()!=f2.length())return false
    val buf1 = ByteArray(bufSize)
    val buf2 = ByteArray(bufSize)
    var len1: Int
    var len2: Int
    Files.newInputStream(p1).use l1@{ io1->
        Files.newInputStream(p2).use l2@{ io2->
            while(true){
                len1 = io1.read(buf1)
                len2 = io2.read(buf2)
                if(len1!=len2) return false
                if(-1==len1) return true
                for(i in 0 until len1){
                    if(buf1[i]!=buf2[i])return false
                }
            }
        }
    }
}