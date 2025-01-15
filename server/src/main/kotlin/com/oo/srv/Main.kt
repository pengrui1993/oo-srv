package com.oo.srv

import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val stopper:()->Unit
    try{
        stopper= start(*args)
    }catch (e:Throwable){
        e.printStackTrace()
        exitProcess(-1)
    }
    var line: String
    while(true){
        line = readln().trim()
        when(line){
            "quit","exit"->{
                stopper()
                exitProcess(0)
            }
            else->{
                println("unknown cmd:$line")
            }
        }
    }
}
