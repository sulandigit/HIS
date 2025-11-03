package com.neu.his.common.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Caffeine缓存配置属性
 */
@ConfigurationProperties(prefix = "his.cache.caffeine")
public class CaffeineCacheProperties {
    
    /**
     * 初始容量
     */
    private int initialCapacity = 100;
    
    /**
     * 最大容量
     */
    private long maximumSize = 1000;
    
    /**
     * 写入后过期时间(秒)
     */
    private long expireAfterWrite = 300;
    
    /**
     * 访问后过期时间(秒)
     */
    private long expireAfterAccess = 180;
    
    /**
     * 是否启用统计
     */
    private boolean recordStats = true;
    
    public int getInitialCapacity() {
        return initialCapacity;
    }
    
    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }
    
    public long getMaximumSize() {
        return maximumSize;
    }
    
    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }
    
    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }
    
    public void setExpireAfterWrite(long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }
    
    public long getExpireAfterAccess() {
        return expireAfterAccess;
    }
    
    public void setExpireAfterAccess(long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }
    
    public boolean isRecordStats() {
        return recordStats;
    }
    
    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }
}
