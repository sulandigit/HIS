package com.neu.his.common.cache.example;

import com.neu.his.common.cache.CacheKey;
import com.neu.his.common.cache.CacheLevel;
import com.neu.his.common.cache.MultiLevelCacheManager;
import com.neu.his.common.cache.annotation.CacheEvict;
import com.neu.his.common.cache.annotation.MultiLevelCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 多级缓存使用示例
 * 展示如何使用多级缓存策略
 */
@Service
public class CacheUsageExample {
    
    @Autowired
    private MultiLevelCacheManager cacheManager;
    
    // 模拟数据库
    private static final Map<Long, User> userDatabase = new HashMap<>();
    
    static {
        userDatabase.put(1L, new User(1L, "张三", 28));
        userDatabase.put(2L, new User(2L, "李四", 32));
        userDatabase.put(3L, new User(3L, "王五", 25));
    }
    
    /**
     * 示例1: 使用注解方式实现多级缓存
     * 推荐使用此方式,简洁且易于维护
     */
    @MultiLevelCache(
        namespace = "user",
        key = "#userId",
        level = CacheLevel.L1_AND_L2,
        expireSeconds = 300,
        cacheName = "default"
    )
    public User getUserById(Long userId) {
        System.out.println("从数据库加载用户: " + userId);
        return userDatabase.get(userId);
    }
    
    /**
     * 示例2: 使用热点数据缓存
     * 适用于高频访问的数据
     */
    @MultiLevelCache(
        namespace = "hotUser",
        key = "#userId",
        level = CacheLevel.L1_AND_L2,
        expireSeconds = 180,
        cacheName = "hotData"
    )
    public User getHotUser(Long userId) {
        System.out.println("从数据库加载热点用户: " + userId);
        return userDatabase.get(userId);
    }
    
    /**
     * 示例3: 仅使用本地缓存
     * 适用于单机环境或不需要分布式共享的数据
     */
    @MultiLevelCache(
        namespace = "localUser",
        key = "#userId",
        level = CacheLevel.L1_ONLY,
        cacheName = "default"
    )
    public User getUserLocalOnly(Long userId) {
        System.out.println("从数据库加载用户(仅本地缓存): " + userId);
        return userDatabase.get(userId);
    }
    
    /**
     * 示例4: 仅使用Redis缓存
     * 适用于需要跨实例共享但不频繁访问的数据
     */
    @MultiLevelCache(
        namespace = "redisUser",
        key = "#userId",
        level = CacheLevel.L2_ONLY,
        expireSeconds = 600
    )
    public User getUserRedisOnly(Long userId) {
        System.out.println("从数据库加载用户(仅Redis缓存): " + userId);
        return userDatabase.get(userId);
    }
    
    /**
     * 示例5: 更新用户时清除缓存
     */
    @CacheEvict(
        namespace = "user",
        key = "#user.id",
        level = CacheLevel.L1_AND_L2
    )
    public void updateUser(User user) {
        System.out.println("更新用户: " + user.getId());
        userDatabase.put(user.getId(), user);
    }
    
    /**
     * 示例6: 编程式使用多级缓存
     * 提供更灵活的控制
     */
    public User getUserByIdProgrammatic(Long userId) {
        CacheKey cacheKey = CacheKey.of("user", String.valueOf(userId));
        
        return cacheManager.get(cacheKey, CacheLevel.L1_AND_L2, () -> {
            System.out.println("从数据库加载用户(编程式): " + userId);
            return userDatabase.get(userId);
        }, 300);
    }
    
    /**
     * 示例7: 使用指定的缓存实例
     */
    public User getUserWithSpecificCache(Long userId) {
        CacheKey cacheKey = CacheKey.of("user", String.valueOf(userId));
        
        return cacheManager.get(cacheKey, "userSession", () -> {
            System.out.println("从数据库加载用户(指定缓存实例): " + userId);
            return userDatabase.get(userId);
        }, 1800);
    }
    
    /**
     * 示例8: 手动设置缓存
     */
    public void setUserCache(User user) {
        CacheKey cacheKey = CacheKey.of("user", String.valueOf(user.getId()));
        cacheManager.put(cacheKey, user, CacheLevel.L1_AND_L2, 300);
        System.out.println("手动设置用户缓存: " + user.getId());
    }
    
    /**
     * 示例9: 手动删除缓存
     */
    public void deleteUserCache(Long userId) {
        CacheKey cacheKey = CacheKey.of("user", String.valueOf(userId));
        cacheManager.evict(cacheKey, CacheLevel.L1_AND_L2);
        System.out.println("手动删除用户缓存: " + userId);
    }
    
    /**
     * 示例10: 条件缓存
     * 只有当结果不为null时才缓存
     */
    @MultiLevelCache(
        namespace = "user",
        key = "#userId",
        level = CacheLevel.L1_AND_L2,
        expireSeconds = 300,
        condition = "#result != null"
    )
    public User getUserWithCondition(Long userId) {
        System.out.println("从数据库加载用户(条件缓存): " + userId);
        return userDatabase.get(userId);
    }
    
    /**
     * 用户实体类
     */
    public static class User {
        private Long id;
        private String name;
        private Integer age;
        
        public User() {
        }
        
        public User(Long id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Integer getAge() {
            return age;
        }
        
        public void setAge(Integer age) {
            this.age = age;
        }
        
        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
