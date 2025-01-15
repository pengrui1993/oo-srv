package com.oo.srv

import java.io.InputStream
import java.io.StringWriter

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