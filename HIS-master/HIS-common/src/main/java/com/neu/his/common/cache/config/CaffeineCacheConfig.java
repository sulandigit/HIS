package com.neu.his.common.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存配置类
 * 提供高性能的本地缓存支持
 */
@Configuration
@ConditionalOnClass(Caffeine.class)
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class CaffeineCacheConfig {
    
    /**
     * 创建默认的Caffeine缓存实例
     * 用于通用场景的本地缓存
     */
    @Bean(name = "defaultCaffeineCache")
    public Cache<String, Object> defaultCaffeineCache(CaffeineCacheProperties properties) {
        return Caffeine.newBuilder()
                .initialCapacity(properties.getInitialCapacity())
                .maximumSize(properties.getMaximumSize())
                .expireAfterWrite(properties.getExpireAfterWrite(), TimeUnit.SECONDS)
                .expireAfterAccess(properties.getExpireAfterAccess(), TimeUnit.SECONDS)
                .recordStats()
                .build();
    }
    
    /**
     * 创建热点数据缓存实例
     * 用于高频访问的热点数据,缓存时间较短
     */
    @Bean(name = "hotDataCache")
    public Cache<String, Object> hotDataCache() {
        return Caffeine.newBuilder()
                .initialCapacity(500)
                .maximumSize(5000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(3, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
    
    /**
     * 创建用户会话缓存实例
     * 用于用户会话数据,缓存时间较长
     */
    @Bean(name = "userSessionCache")
    public Cache<String, Object> userSessionCache() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(2000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
    
    /**
     * 创建字典数据缓存实例
     * 用于系统字典等基础数据,缓存时间很长
     */
    @Bean(name = "dictionaryCache")
    public Cache<String, Object> dictionaryCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
