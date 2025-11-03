package com.neu.his.common.cache.annotation;

import com.neu.his.common.cache.CacheLevel;

import java.lang.annotation.*;

/**
 * 多级缓存注解
 * 用于方法级别的声明式缓存
 * 
 * 使用示例:
 * @MultiLevelCache(namespace = "user", key = "#userId", level = CacheLevel.L1_AND_L2, expireSeconds = 300)
 * public User getUserById(Long userId) {
 *     return userMapper.selectById(userId);
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiLevelCache {
    
    /**
     * 缓存命名空间
     */
    String namespace() default "default";
    
    /**
     * 缓存键,支持SpEL表达式
     * 例如: #userId, #user.id, #p0, #a0
     */
    String key();
    
    /**
     * 缓存级别
     */
    CacheLevel level() default CacheLevel.L1_AND_L2;
    
    /**
     * 过期时间(秒),默认5分钟
     */
    long expireSeconds() default 300;
    
    /**
     * 指定使用的Caffeine缓存实例名称
     * 可选值: default, hotData, userSession, dictionary
     */
    String cacheName() default "default";
    
    /**
     * 条件表达式,支持SpEL,满足条件才缓存
     * 例如: #result != null
     */
    String condition() default "";
}
