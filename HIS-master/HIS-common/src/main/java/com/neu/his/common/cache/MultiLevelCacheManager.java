package com.neu.his.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.neu.his.common.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存管理器
 * 实现L1(Caffeine本地缓存) + L2(Redis分布式缓存)的多级缓存策略
 */
@Component
public class MultiLevelCacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiLevelCacheManager.class);
    
    @Autowired
    @Qualifier("defaultCaffeineCache")
    private Cache<String, Object> defaultCaffeineCache;
    
    @Autowired
    @Qualifier("hotDataCache")
    private Cache<String, Object> hotDataCache;
    
    @Autowired
    @Qualifier("userSessionCache")
    private Cache<String, Object> userSessionCache;
    
    @Autowired
    @Qualifier("dictionaryCache")
    private Cache<String, Object> dictionaryCache;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取缓存值(多级缓存策略)
     * 先从L1缓存获取,未命中则从L2缓存获取,再未命中则执行数据加载器
     * 
     * @param cacheKey 缓存键
     * @param cacheLevel 缓存级别
     * @param loader 数据加载器
     * @param expireSeconds 过期时间(秒)
     * @return 缓存值
     */
    public <T> T get(CacheKey cacheKey, CacheLevel cacheLevel, Supplier<T> loader, long expireSeconds) {
        String key = cacheKey.getFullKey();
        
        switch (cacheLevel) {
            case L1_ONLY:
                return getFromL1Only(key, loader);
                
            case L2_ONLY:
                return getFromL2Only(key, loader, expireSeconds);
                
            case L1_AND_L2:
            default:
                return getFromMultiLevel(key, loader, expireSeconds);
        }
    }
    
    /**
     * 仅从L1缓存获取
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromL1Only(String key, Supplier<T> loader) {
        try {
            return (T) defaultCaffeineCache.get(key, k -> {
                T value = loader.get();
                logger.debug("L1缓存未命中,从数据源加载: {}", key);
                return value;
            });
        } catch (Exception e) {
            logger.error("从L1缓存获取数据失败: {}", key, e);
            return loader.get();
        }
    }
    
    /**
     * 仅从L2缓存获取
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromL2Only(String key, Supplier<T> loader, long expireSeconds) {
        try {
            Object value = redisUtil.getObj(key);
            if (value != null) {
                logger.debug("L2缓存命中: {}", key);
                return (T) value;
            }
            
            // L2缓存未命中,从数据源加载
            logger.debug("L2缓存未命中,从数据源加载: {}", key);
            T result = loader.get();
            if (result != null) {
                redisUtil.setObj(key, result);
                if (expireSeconds > 0) {
                    redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("从L2缓存获取数据失败: {}", key, e);
            return loader.get();
        }
    }
    
    /**
     * 多级缓存获取
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromMultiLevel(String key, Supplier<T> loader, long expireSeconds) {
        try {
            // 1. 先查询L1缓存
            Object l1Value = defaultCaffeineCache.getIfPresent(key);
            if (l1Value != null) {
                logger.debug("L1缓存命中: {}", key);
                return (T) l1Value;
            }
            
            // 2. L1未命中,查询L2缓存
            Object l2Value = redisUtil.getObj(key);
            if (l2Value != null) {
                logger.debug("L2缓存命中,回填L1缓存: {}", key);
                defaultCaffeineCache.put(key, l2Value);
                return (T) l2Value;
            }
            
            // 3. L1、L2都未命中,从数据源加载
            logger.debug("多级缓存未命中,从数据源加载: {}", key);
            T result = loader.get();
            if (result != null) {
                // 同时写入L1和L2缓存
                defaultCaffeineCache.put(key, result);
                redisUtil.setObj(key, result);
                if (expireSeconds > 0) {
                    redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("从多级缓存获取数据失败: {}", key, e);
            return loader.get();
        }
    }
    
    /**
     * 使用指定的Caffeine缓存实例
     */
    @SuppressWarnings("unchecked")
    public <T> T get(CacheKey cacheKey, String cacheName, Supplier<T> loader, long expireSeconds) {
        Cache<String, Object> caffeineCache = getCaffeineCache(cacheName);
        String key = cacheKey.getFullKey();
        
        try {
            // 1. 先查询L1缓存
            Object l1Value = caffeineCache.getIfPresent(key);
            if (l1Value != null) {
                logger.debug("L1缓存({})命中: {}", cacheName, key);
                return (T) l1Value;
            }
            
            // 2. L1未命中,查询L2缓存
            Object l2Value = redisUtil.getObj(key);
            if (l2Value != null) {
                logger.debug("L2缓存命中,回填L1缓存({}): {}", cacheName, key);
                caffeineCache.put(key, l2Value);
                return (T) l2Value;
            }
            
            // 3. L1、L2都未命中,从数据源加载
            logger.debug("多级缓存未命中,从数据源加载: {}", key);
            T result = loader.get();
            if (result != null) {
                caffeineCache.put(key, result);
                redisUtil.setObj(key, result);
                if (expireSeconds > 0) {
                    redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("从多级缓存获取数据失败: {}", key, e);
            return loader.get();
        }
    }
    
    /**
     * 设置缓存
     */
    public void put(CacheKey cacheKey, Object value, CacheLevel cacheLevel, long expireSeconds) {
        String key = cacheKey.getFullKey();
        
        switch (cacheLevel) {
            case L1_ONLY:
                defaultCaffeineCache.put(key, value);
                break;
                
            case L2_ONLY:
                redisUtil.setObj(key, value);
                if (expireSeconds > 0) {
                    redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
                }
                break;
                
            case L1_AND_L2:
            default:
                defaultCaffeineCache.put(key, value);
                redisUtil.setObj(key, value);
                if (expireSeconds > 0) {
                    redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
                }
                break;
        }
        logger.debug("设置缓存成功: {}, 级别: {}", key, cacheLevel);
    }
    
    /**
     * 删除缓存
     */
    public void evict(CacheKey cacheKey, CacheLevel cacheLevel) {
        String key = cacheKey.getFullKey();
        
        switch (cacheLevel) {
            case L1_ONLY:
                defaultCaffeineCache.invalidate(key);
                break;
                
            case L2_ONLY:
                redisUtil.removeObj(key);
                break;
                
            case L1_AND_L2:
            default:
                defaultCaffeineCache.invalidate(key);
                redisUtil.removeObj(key);
                break;
        }
        logger.debug("删除缓存成功: {}, 级别: {}", key, cacheLevel);
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        defaultCaffeineCache.invalidateAll();
        hotDataCache.invalidateAll();
        userSessionCache.invalidateAll();
        dictionaryCache.invalidateAll();
        logger.info("清空所有本地缓存成功");
    }
    
    /**
     * 根据名称获取Caffeine缓存实例
     */
    private Cache<String, Object> getCaffeineCache(String cacheName) {
        switch (cacheName) {
            case "hotData":
                return hotDataCache;
            case "userSession":
                return userSessionCache;
            case "dictionary":
                return dictionaryCache;
            default:
                return defaultCaffeineCache;
        }
    }
    
    /**
     * 获取默认Caffeine缓存
     */
    public Cache<String, Object> getDefaultCaffeineCache() {
        return defaultCaffeineCache;
    }
    
    /**
     * 获取热点数据缓存
     */
    public Cache<String, Object> getHotDataCache() {
        return hotDataCache;
    }
    
    /**
     * 获取用户会话缓存
     */
    public Cache<String, Object> getUserSessionCache() {
        return userSessionCache;
    }
    
    /**
     * 获取字典缓存
     */
    public Cache<String, Object> getDictionaryCache() {
        return dictionaryCache;
    }
}
