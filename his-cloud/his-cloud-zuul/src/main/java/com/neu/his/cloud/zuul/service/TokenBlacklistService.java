package com.neu.his.cloud.zuul.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务（Zuul网关版本）
 * 负责管理被撤销的Token
 */
@Service
public class TokenBlacklistService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBlacklistService.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.blacklist.enabled:true}")
    private boolean blacklistEnabled;

    @Value("${jwt.blacklist.redisKeyPrefix:jwt:blacklist:}")
    private String redisKeyPrefix;

    /**
     * 将Token加入黑名单
     *
     * @param tokenId    Token唯一标识（jti）
     * @param username   用户名
     * @param reason     撤销原因
     * @param expireTime Token剩余有效期（秒）
     */
    public void addToBlacklist(String tokenId, String username, String reason, long expireTime) {
        if (!blacklistEnabled || redisTemplate == null) {
            LOGGER.debug("黑名单功能未启用");
            return;
        }

        String key = redisKeyPrefix + tokenId;
        BlacklistEntry entry = new BlacklistEntry(username, reason, System.currentTimeMillis());
        
        try {
            // 将黑名单信息存储到Redis，设置过期时间与Token剩余有效期一致
            redisTemplate.opsForValue().set(key, entry, expireTime, TimeUnit.SECONDS);
            LOGGER.info("Token已加入黑名单 - tokenId: {}, username: {}, reason: {}", 
                       tokenId, username, reason);
        } catch (Exception e) {
            LOGGER.error("添加Token到黑名单失败 - tokenId: {}", tokenId, e);
        }
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param tokenId Token唯一标识（jti）
     * @return true表示在黑名单中，false表示不在
     */
    public boolean isBlacklisted(String tokenId) {
        if (!blacklistEnabled || redisTemplate == null) {
            return false;
        }

        if (tokenId == null || tokenId.isEmpty()) {
            return false;
        }

        String key = redisKeyPrefix + tokenId;
        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (exists != null && exists) {
                LOGGER.debug("Token在黑名单中 - tokenId: {}", tokenId);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("检查Token黑名单状态失败 - tokenId: {}", tokenId, e);
            // 出错时为安全起见，返回true拒绝访问
            return true;
        }
    }

    /**
     * 从黑名单中移除Token（一般不需要，因为Redis会自动过期）
     *
     * @param tokenId Token唯一标识（jti）
     */
    public void removeFromBlacklist(String tokenId) {
        if (!blacklistEnabled || redisTemplate == null) {
            return;
        }

        String key = redisKeyPrefix + tokenId;
        try {
            redisTemplate.delete(key);
            LOGGER.info("Token已从黑名单中移除 - tokenId: {}", tokenId);
        } catch (Exception e) {
            LOGGER.error("从黑名单移除Token失败 - tokenId: {}", tokenId, e);
        }
    }

    /**
     * 获取黑名单信息
     *
     * @param tokenId Token唯一标识（jti）
     * @return 黑名单条目信息
     */
    public BlacklistEntry getBlacklistEntry(String tokenId) {
        if (!blacklistEnabled || redisTemplate == null) {
            return null;
        }

        String key = redisKeyPrefix + tokenId;
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj instanceof BlacklistEntry) {
                return (BlacklistEntry) obj;
            }
        } catch (Exception e) {
            LOGGER.error("获取黑名单信息失败 - tokenId: {}", tokenId, e);
        }
        return null;
    }

    /**
     * 黑名单条目类
     */
    public static class BlacklistEntry implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String username;
        private String reason;
        private long revokedAt;

        public BlacklistEntry() {
        }

        public BlacklistEntry(String username, String reason, long revokedAt) {
            this.username = username;
            this.reason = reason;
            this.revokedAt = revokedAt;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public long getRevokedAt() {
            return revokedAt;
        }

        public void setRevokedAt(long revokedAt) {
            this.revokedAt = revokedAt;
        }
    }
}
