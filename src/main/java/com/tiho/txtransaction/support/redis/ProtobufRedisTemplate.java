package com.tiho.txtransaction.support.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class ProtobufRedisTemplate extends RedisTemplate<String, byte[]> {

    public ProtobufRedisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        BytesRedisSerializer bytesRedisSerializer = new BytesRedisSerializer();
        setKeySerializer(stringSerializer);
        setHashKeySerializer(stringSerializer);
        setValueSerializer(bytesRedisSerializer);
        setHashValueSerializer(bytesRedisSerializer);
    }

    public ProtobufRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }
}
