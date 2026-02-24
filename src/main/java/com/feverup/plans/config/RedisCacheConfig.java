package com.feverup.plans.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        RedisCacheConfiguration config =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(mapper)))
                        .entryTtl(Duration.ofSeconds(60));

        RedisCacheManager redisManager = RedisCacheManager.builder(connectionFactory)
                                                          .cacheDefaults(config)
                                                          .build();

        CaffeineCacheManager fallbackManager = new CaffeineCacheManager();
        fallbackManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)));

        return new ResilientCacheManager(redisManager, fallbackManager);
    }
}
