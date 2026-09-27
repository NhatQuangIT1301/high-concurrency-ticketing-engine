package com.nhatquang.high_concurrency_ticketing_engine.config;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
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
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Cấu hình Cache: Thời gian sống (TTL) mặc định là 60 phút, và format dữ liệu thành JSON
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                .fromSerializer(RedisSerializer.json())
            );

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(cacheConfig).build();
    }

    //Cộng vé
    @Bean
    public DefaultRedisScript<Long> incrementStockScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // Nạp file script hoàn vé
        redisScript.setLocation(new ClassPathResource("scripts/increment_stock.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    //Trừ vé
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
