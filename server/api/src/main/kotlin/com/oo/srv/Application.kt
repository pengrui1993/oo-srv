package com.oo.srv
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.context.event.SpringApplicationEvent
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.support.ServletRequestHandledEvent
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadLocalRandom

@ServletComponentScan
@SpringBootApplication
class Application
lateinit var ctx:ConfigurableApplicationContext
lateinit var app:SpringApplication
fun start(vararg args:String):()->Unit{
    ctx = runApplication<Application>(*args,init = {
        app = this
        addListeners(ApplicationPidFileWriter())
    })
    return {ctx.stop()}
}
@RestController
private class HelloController{
    @GetMapping("/")
    @LogAop
    fun home():Any{
        if(ThreadLocalRandom.current().nextBoolean())throw AuthenticationException()
        return ApiCode.OK.toJson()
    }
}

object Syncer{
    private val creator = Thread.currentThread()
    private val queue = LinkedBlockingQueue<()->Unit>()
    private val log = LoggerFactory.getLogger(Syncer::class.java)
    private val worker = Thread{
        while(true){
            try{
                queue.take()()
            }catch (e:Throwable){
                log.error(e.message,e)
            }
        }
    }
    init{
        worker.isDaemon = true
        worker.start()
    }
    fun sync(run:()->Unit){
        if(Thread.currentThread()==worker){
            run()
        }else{
            queue.put(run)
        }
    }
}

interface PropertiesAccessor{
    val localUploadedPath:String
    val urlPrefix:String
    val serverOrigin:String
}
@Component
class ApplicationProperties(
     @Value("\${sys.origin:http://habf.com}")
     var origin:String
    ,@Value("\${sys.uploaded.url.path.prefix:/oo-srv/static/upload}") //TODO nginx 配置
     var uploadedFilesUrlPathPrefix:String
    ,@Value("\${sys.disk.upload.location:/home/user/data/static/upload}")
     var uploadedFilesPathPosition:String
):ApplicationListener<ApplicationEvent> ,PropertiesAccessor{
    override val localUploadedPath: String
        get() = uploadedFilesPathPosition
    override val urlPrefix: String
        get() = uploadedFilesUrlPathPrefix
    override val serverOrigin: String
        get() = origin
    private val log = LoggerFactory.getLogger(ApplicationProperties::class.java)
    override fun onApplicationEvent(event: ApplicationEvent) {
        when(event){
            is ApplicationStartedEvent->{
                //reload config from database
            }
            else->{
               log.info("event type:${event::class.java} from ${event.source}")
            }
        }
        val initTimeClass = setOf(
            ApplicationStartedEvent::class.java
            ,ApplicationReadyEvent::class.java
            ,ServletWebServerInitializedEvent::class.java
            ,AvailabilityChangeEvent::class.java //state:CORRECT ACCEPTING_TRAFFIC
            ,ContextRefreshedEvent::class.java
        )
        val runtimeClass = setOf(
            ServletRequestHandledEvent::class.java
        )
    }


}