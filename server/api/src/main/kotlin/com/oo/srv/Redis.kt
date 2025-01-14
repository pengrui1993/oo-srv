package com.oo.srv

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import jakarta.annotation.Resource
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Configuration(proxyBeanMethods = false)
class Config0{
    fun public(redisTemplate: RedisTemplate<String, Serializable>){
        redisTemplate.convertAndSend("EVENT0", "1234")
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
    fun redisMessageListenerContainer(connectionFactory: RedisConnectionFactory): RedisMessageListenerContainer {
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
@Component
private class RedisListener(
    @Resource val c:RedisMessageListenerContainer
    ,@Resource var repo:BizApiCallRepository
    ,@Resource val redisTemplate:RedisTemplate<String,Serializable>
    ,@Resource val transactionTemplate: TransactionTemplate
    ,@Resource @PersistenceContext val entityManager: EntityManager
): KeyExpirationEventMessageListener(c){
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
        }
        Syncer.sync {
        }
    }
}