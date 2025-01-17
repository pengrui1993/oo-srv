package com.oo.srv

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import jakarta.annotation.Resource
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.apache.commons.lang3.time.DurationFormatUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import java.io.Serializable
import java.time.*
import java.time.format.DateTimeFormatter


@Configuration(proxyBeanMethods = false)
class Config0{
    fun publish(redisTemplate: RedisTemplate<String, Serializable>){
        redisTemplate.convertAndSend("EVENT0", "1234")
    }

    @Bean
    fun lettuceConnectionFactory(): LettuceConnectionFactory {
        val config = RedisStandaloneConfiguration()
        config.hostName = "127.0.0.1"
        config.port = 6379 // Default port for Redis
//        config.setPassword("") // Redis password, not set here
        config.database = 3 // Redis database index
        val poolingClientConfig: LettuceClientConfiguration = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(3000))
            .build()
        return LettuceConnectionFactory(config, poolingClientConfig)
    }

    @Bean
    fun redisTemplate(connectionFactory: LettuceConnectionFactory):RedisTemplate<String,Serializable>{
        val redisTemplate = RedisTemplate<String, Serializable>()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        redisTemplate.hashKeySerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        redisTemplate.connectionFactory = connectionFactory
        return redisTemplate
    }
    @Bean
    fun redisMessageListenerContainer(connectionFactory: LettuceConnectionFactory): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.connectionFactory = connectionFactory
        return container
    }
    /**
     * https://stackoverflow.com/questions/46823579/register-javatimemodule-with-the-default-objectmapper-in-springboot
     * https://www.cnblogs.com/magicpose/p/12133599.html
     */
    @Bean
    fun javaTimeModule(): Module {
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(
            LocalDateTime::class.java,
            LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        javaTimeModule.addSerializer(
            LocalDate::class.java,
            LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
        javaTimeModule.addSerializer(
            LocalTime::class.java,
            LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )
        return javaTimeModule
    }

}
private fun durationFmt() {
    val startInstant = Instant.parse("2015-05-01T00:00:00Z") //ISO 8601 表示法
    val endInstant = Instant.parse("2015-05-03T02:30:00Z")
    val duration = Duration.between(startInstant, endInstant) //得到兩個Instant差的Duration
    val resultDurationString1 = DurationFormatUtils.formatDuration(duration.toMillis(), "dd'天'HH'時'mm'分'ss'秒'")
    println(resultDurationString1) //輸出: 02天02時30分00秒
    val resultDurationString2 =
        DurationFormatUtils.formatDuration(duration.toMillis(), "dd'天'HH'時'mm'分'ss'秒'", false)
    println(resultDurationString2) //輸出: 2天2時30分0秒  //(10以下的數字不補0)
    val resultDurationString3 = DurationFormatUtils.formatDuration(duration.toMillis(), "HH'時'mm'分'ss'秒'")
    println(resultDurationString3) //輸出: 50時30分00秒  //(沒有設定小時以上的單位，小時以上全部由小時表示:50小時)
    val resultDurationString4 = DurationFormatUtils.formatDurationISO(duration.toMillis())
    println(resultDurationString4) //輸出: P0Y0M2DT2H30M0.000S //ISO 8601標準格式
}
@Component
private class RedisListener(
    @Resource val c:RedisMessageListenerContainer
    ,@Resource var repo:BizApiCallRepository
    ,@Resource val redisTemplate:RedisTemplate<String,Serializable>
    ,@Resource val transactionTemplate: TransactionTemplate
    ,@Resource @PersistenceContext val entityManager: EntityManager
): KeyExpirationEventMessageListener(c){
    private val log = LoggerFactory.getLogger(javaClass)
    private val topic = ChannelTopic("EVENT0")
    override fun doRegister(listenerContainer: RedisMessageListenerContainer) {
        super.doRegister(listenerContainer)
        listenerContainer.addMessageListener(this, topic)
    }
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val topic = redisTemplate.keySerializer.deserialize(message.channel)//EVENT0 or __keyevent@0__:expired
        val keyOrMessage = String(message.body)//template.key or message body [1234]
        if(null!=pattern){
            val p = String(pattern) //EVENT0 or __keyevent@*__:expired
            log.info("pattern:{}",redisTemplate.keySerializer.deserialize(pattern))
        }
        log.info("rcv data from redis,channel:{},msg.body:{}",topic,keyOrMessage)
        Syncer.sync {
            val res = transactionTemplate.execute {
//                entityManager.flush()
                1
            }
        }
    }
    fun test(){
        val active = TransactionSynchronizationManager.isSynchronizationActive()
        val status = TransactionAspectSupport.currentTransactionStatus()
        val actual = TransactionSynchronizationManager.isActualTransactionActive()
    }
}