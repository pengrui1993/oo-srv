package com.oo.srv

import de.codecentric.boot.admin.server.config.EnableAdminServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.Scanner
import kotlin.system.exitProcess


@SpringBootApplication
@EnableAdminServer
class Monitor
fun main() {
    val ctx = runApplication<Monitor>()
    while(true){
        val line = readln()
        when(line){
            "quit","exit"->{
                try{
                    ctx.stop()
                }finally {
                    exitProcess(0)
                }
            }
            else->{}
        }
    }
}