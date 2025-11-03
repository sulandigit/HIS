package com.neu.his.common.cache.stats;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.neu.his.common.cache.MultiLevelCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存统计工具
 * 提供缓存命中率、驱逐率等统计信息
 */
@Component
public class CacheStatistics {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheStatistics.class);
    
    @Autowired
    private MultiLevelCacheManager cacheManager;
    
    private final DecimalFormat percentFormat = new DecimalFormat("0.00%");
    
    /**
     * 获取所有缓存统计信息
     */
    public Map<String, CacheStatsInfo> getAllCacheStats() {
        Map<String, CacheStatsInfo> statsMap = new HashMap<>();
        
        statsMap.put("defaultCache", getCacheStatsInfo(cacheManager.getDefaultCaffeineCache(), "默认缓存"));
        statsMap.put("hotDataCache", getCacheStatsInfo(cacheManager.getHotDataCache(), "热点数据缓存"));
        statsMap.put("userSessionCache", getCacheStatsInfo(cacheManager.getUserSessionCache(), "用户会话缓存"));
        statsMap.put("dictionaryCache", getCacheStatsInfo(cacheManager.getDictionaryCache(), "字典缓存"));
        
        return statsMap;
    }
    
    /**
     * 获取指定缓存的统计信息
     */
    public CacheStatsInfo getCacheStatsInfo(Cache<String, Object> cache, String cacheName) {
        CacheStats stats = cache.stats();
        
        CacheStatsInfo info = new CacheStatsInfo();
        info.setCacheName(cacheName);
        info.setSize(cache.estimatedSize());
        info.setHitCount(stats.hitCount());
        info.setMissCount(stats.missCount());
        info.setHitRate(stats.hitRate());
        info.setMissRate(stats.missRate());
        info.setLoadSuccessCount(stats.loadSuccessCount());
        info.setLoadFailureCount(stats.loadFailureCount());
        info.setEvictionCount(stats.evictionCount());
        info.setTotalLoadTime(stats.totalLoadTime());
        info.setAverageLoadPenalty(stats.averageLoadPenalty());
        
        return info;
    }
    
    /**
     * 打印所有缓存统计信息
     */
    public void printAllCacheStats() {
        logger.info("==========  缓存统计信息 ==========");
        Map<String, CacheStatsInfo> statsMap = getAllCacheStats();
        
        for (Map.Entry<String, CacheStatsInfo> entry : statsMap.entrySet()) {
            CacheStatsInfo info = entry.getValue();
            logger.info("缓存名称: {}", info.getCacheName());
            logger.info("  当前大小: {}", info.getSize());
            logger.info("  命中次数: {}", info.getHitCount());
            logger.info("  未命中次数: {}", info.getMissCount());
            logger.info("  命中率: {}", percentFormat.format(info.getHitRate()));
            logger.info("  未命中率: {}", percentFormat.format(info.getMissRate()));
            logger.info("  驱逐次数: {}", info.getEvictionCount());
            logger.info("  加载成功次数: {}", info.getLoadSuccessCount());
            logger.info("  加载失败次数: {}", info.getLoadFailureCount());
            logger.info("  平均加载时间(纳秒): {}", info.getAverageLoadPenalty());
            logger.info("-------------------------------------");
        }
    }
    
    /**
     * 缓存统计信息类
     */
    public static class CacheStatsInfo {
        private String cacheName;
        private long size;
        private long hitCount;
        private long missCount;
        private double hitRate;
        private double missRate;
        private long loadSuccessCount;
        private long loadFailureCount;
        private long evictionCount;
        private long totalLoadTime;
        private double averageLoadPenalty;
        
        public String getCacheName() {
            return cacheName;
        }
        
        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        public long getHitCount() {
            return hitCount;
        }
        
        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }
        
        public long getMissCount() {
            return missCount;
        }
        
        public void setMissCount(long missCount) {
            this.missCount = missCount;
        }
        
        public double getHitRate() {
            return hitRate;
        }
        
        public void setHitRate(double hitRate) {
            this.hitRate = hitRate;
        }
        
        public double getMissRate() {
            return missRate;
        }
        
        public void setMissRate(double missRate) {
            this.missRate = missRate;
        }
        
        public long getLoadSuccessCount() {
            return loadSuccessCount;
        }
        
        public void setLoadSuccessCount(long loadSuccessCount) {
            this.loadSuccessCount = loadSuccessCount;
        }
        
        public long getLoadFailureCount() {
            return loadFailureCount;
        }
        
        public void setLoadFailureCount(long loadFailureCount) {
            this.loadFailureCount = loadFailureCount;
        }
        
        public long getEvictionCount() {
            return evictionCount;
        }
        
        public void setEvictionCount(long evictionCount) {
            this.evictionCount = evictionCount;
        }
        
        public long getTotalLoadTime() {
            return totalLoadTime;
        }
        
        public void setTotalLoadTime(long totalLoadTime) {
            this.totalLoadTime = totalLoadTime;
        }
        
        public double getAverageLoadPenalty() {
            return averageLoadPenalty;
        }
        
        public void setAverageLoadPenalty(double averageLoadPenalty) {
            this.averageLoadPenalty = averageLoadPenalty;
        }
    }
}
