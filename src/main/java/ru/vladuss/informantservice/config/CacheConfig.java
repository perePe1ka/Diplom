package ru.vladuss.informantservice.config;


import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${redis.host:redis}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    @Value("${cache.ttl.minutes:10}")
    private long ttlMinutes;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Connecting to Redis {}:{}", redisHost, redisPort);
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        log.info("Initializing RedisCacheManager (TTL={} min)", ttlMinutes);

        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(ttlMinutes))
                .disableCachingNullValues()
                .prefixCacheNameWith("informant::");

        return RedisCacheManager.builder(cf)
                .cacheDefaults(cfg)
                .transactionAware()
                .build();
    }

    @Bean
    public SimpleCacheErrorHandler cacheErrorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex,
                                               org.springframework.cache.Cache cache, Object key) {
                log.warn("Redis cache GET error ({} → {})", key, ex.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException ex,
                                               org.springframework.cache.Cache cache, Object key, Object val) {
                log.warn("Redis cache PUT error ({} → {})", key, ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex,
                                                 org.springframework.cache.Cache cache, Object key) {
                log.warn("Redis cache EVICT error ({} → {})", key, ex.getMessage());
            }
        };
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
