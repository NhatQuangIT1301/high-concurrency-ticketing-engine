package com.nhatquang.high_concurrency_ticketing_engine.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration 
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Định dạng Key dưới dạng String để dễ đọc trên Redis CLI
        template.setKeySerializer(new StringRedisSerializer());
        // Định dạng Value
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        return template;
    }

    @Bean
    public DefaultRedisScript<Long> decrementStockScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // Nạp file script từ thư mục resources
        redisScript.setLocation(new ClassPathResource("scripts/decrement_stock.lua"));
        // Kiểu dữ liệu trả về tương ứng với các số 1, 0, -1 trong script
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }
}
