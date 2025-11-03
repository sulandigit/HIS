package com.neu.his.common.cache.annotation;

import com.neu.his.common.cache.CacheLevel;

import java.lang.annotation.*;

/**
 * 缓存清除注解
 * 用于方法执行后清除指定的缓存
 * 
 * 使用示例:
 * @CacheEvict(namespace = "user", key = "#userId", level = CacheLevel.L1_AND_L2)
 * public void updateUser(Long userId, User user) {
 *     userMapper.updateById(user);
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvict {
    
    /**
     * 缓存命名空间
     */
    String namespace() default "default";
    
    /**
     * 缓存键,支持SpEL表达式
     */
    String key();
    
    /**
     * 缓存级别
     */
    CacheLevel level() default CacheLevel.L1_AND_L2;
    
    /**
     * 是否在方法执行前清除缓存,默认在执行后清除
     */
    boolean beforeInvocation() default false;
}
