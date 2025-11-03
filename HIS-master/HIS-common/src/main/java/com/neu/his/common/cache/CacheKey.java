package com.neu.his.common.cache;

import java.io.Serializable;
import java.util.Objects;

/**
 * 缓存键封装类
 * 用于标准化缓存键的生成和管理
 */
public class CacheKey implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 缓存命名空间/前缀
     */
    private String namespace;
    
    /**
     * 缓存键
     */
    private String key;
    
    /**
     * 完整的缓存键(namespace:key)
     */
    private String fullKey;
    
    public CacheKey(String namespace, String key) {
        this.namespace = namespace;
        this.key = key;
        this.fullKey = namespace + ":" + key;
    }
    
    public CacheKey(String fullKey) {
        this.fullKey = fullKey;
        int index = fullKey.indexOf(":");
        if (index > 0) {
            this.namespace = fullKey.substring(0, index);
            this.key = fullKey.substring(index + 1);
        } else {
            this.namespace = "default";
            this.key = fullKey;
            this.fullKey = "default:" + fullKey;
        }
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getFullKey() {
        return fullKey;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return Objects.equals(fullKey, cacheKey.fullKey);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fullKey);
    }
    
    @Override
    public String toString() {
        return fullKey;
    }
    
    /**
     * 构建缓存键
     */
    public static CacheKey of(String namespace, String... keyParts) {
        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0; i < keyParts.length; i++) {
            if (i > 0) {
                keyBuilder.append(":");
            }
            keyBuilder.append(keyParts[i]);
        }
        return new CacheKey(namespace, keyBuilder.toString());
    }
}
