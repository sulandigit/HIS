package com.neu.his.common.cache;

/**
 * 缓存级别枚举
 * 定义多级缓存的层级结构
 */
public enum CacheLevel {
    /**
     * 仅使用一级本地缓存(Caffeine)
     * 适用于单机环境或不需要分布式共享的数据
     */
    L1_ONLY("L1_ONLY", "仅本地缓存"),
    
    /**
     * 仅使用二级分布式缓存(Redis)
     * 适用于需要跨实例共享但不频繁访问的数据
     */
    L2_ONLY("L2_ONLY", "仅分布式缓存"),
    
    /**
     * 使用多级缓存(Caffeine + Redis)
     * 先查询本地缓存,未命中则查询Redis,最后查询数据库
     * 适用于热点数据和高并发场景
     */
    L1_AND_L2("L1_AND_L2", "多级缓存");
    
    private final String code;
    private final String description;
    
    CacheLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
