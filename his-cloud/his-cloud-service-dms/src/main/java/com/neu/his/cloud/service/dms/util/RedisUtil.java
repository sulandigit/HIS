package com.neu.his.cloud.service.dms.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类
 * 提供Redis常用操作的封装，包括基础操作、过期时间管理、批量操作等
 */
@Service
public class RedisUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // =============================String操作=============================

    /**
     * 设置字符串值
     */
    public void setStr(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            logger.error("Redis setStr error, key: {}", key, e);
        }
    }

    /**
     * 设置字符串值，并指定过期时间
     */
    public void setStr(String key, String value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                stringRedisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                setStr(key, value);
            }
        } catch (Exception e) {
            logger.error("Redis setStr with expire error, key: {}", key, e);
        }
    }

    /**
     * 获取字符串值
     */
    public String getStr(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis getStr error, key: {}", key, e);
            return null;
        }
    }

    /**
     * 设置超期时间（秒）
     */
    public boolean expireStr(String key, long expire) {
        try {
            return stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Redis expireStr error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 删除字符串
     */
    public void removeStr(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            logger.error("Redis removeStr error, key: {}", key, e);
        }
    }

    // =============================Object操作=============================

    /**
     * 设置对象值
     */
    public void setObj(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            logger.error("Redis setObj error, key: {}", key, e);
        }
    }

    /**
     * 设置对象值，并指定过期时间
     */
    public void setObj(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                setObj(key, value);
            }
        } catch (Exception e) {
            logger.error("Redis setObj with expire error, key: {}", key, e);
        }
    }

    /**
     * 获取对象值
     */
    public Object getObj(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis getObj error, key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取对象值，如果不存在则返回默认值
     */
    public Object getObjWithDefault(String key, Object defaultValue) {
        Object value = getObj(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 删除对象
     */
    public void removeObj(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.error("Redis removeObj error, key: {}", key, e);
        }
    }

    // =============================通用操作=============================

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                return redisTemplate.expire(key, time, timeUnit);
            }
            return false;
        } catch (Exception e) {
            logger.error("Redis expire error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取过期时间（秒）
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Redis getExpire error, key: {}", key, e);
            return null;
        }
    }

    /**
     * 判断key是否存在
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Redis exists error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.error("Redis delete error, key: {}", key, e);
        }
    }

    /**
     * 批量删除缓存
     */
    public void delete(Collection<String> keys) {
        try {
            redisTemplate.delete(keys);
        } catch (Exception e) {
            logger.error("Redis batch delete error, keys size: {}", keys.size(), e);
        }
    }

    // =============================Hash操作=============================

    /**
     * 获取Hash中的值
     */
    public Object hget(String key, String item) {
        try {
            return redisTemplate.opsForHash().get(key, item);
        } catch (Exception e) {
            logger.error("Redis hget error, key: {}, item: {}", key, item, e);
            return null;
        }
    }

    /**
     * 设置Hash中的值
     */
    public void hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
        } catch (Exception e) {
            logger.error("Redis hset error, key: {}, item: {}", key, item, e);
        }
    }

    /**
     * 设置Hash中的值，并设置过期时间
     */
    public void hset(String key, String item, Object value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
        } catch (Exception e) {
            logger.error("Redis hset with expire error, key: {}, item: {}", key, item, e);
        }
    }

    /**
     * 删除Hash中的值
     */
    public void hdel(String key, Object... items) {
        try {
            redisTemplate.opsForHash().delete(key, items);
        } catch (Exception e) {
            logger.error("Redis hdel error, key: {}", key, e);
        }
    }

    /**
     * 判断Hash中是否存在该项的值
     */
    public boolean hHasKey(String key, String item) {
        try {
            return redisTemplate.opsForHash().hasKey(key, item);
        } catch (Exception e) {
            logger.error("Redis hHasKey error, key: {}, item: {}", key, item, e);
            return false;
        }
    }

    /**
     * 获取Hash的所有键值对
     */
    public Map<Object, Object> hmget(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            logger.error("Redis hmget error, key: {}", key, e);
            return new HashMap<>();
        }
    }

    /**
     * 设置Hash的多个键值对
     */
    public void hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            logger.error("Redis hmset error, key: {}", key, e);
        }
    }

    /**
     * 设置Hash的多个键值对，并设置过期时间
     */
    public void hmset(String key, Map<String, Object> map, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
        } catch (Exception e) {
            logger.error("Redis hmset with expire error, key: {}", key, e);
        }
    }

    // =============================List操作=============================

    /**
     * 获取List的指定范围内的值
     */
    public List<Object> lrange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            logger.error("Redis lrange error, key: {}", key, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取List的长度
     */
    public Long llen(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            logger.error("Redis llen error, key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 通过索引获取List中的值
     */
    public Object lindex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            logger.error("Redis lindex error, key: {}, index: {}", key, index, e);
            return null;
        }
    }

    /**
     * 将值放入List的右边
     */
    public void rpush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            logger.error("Redis rpush error, key: {}", key, e);
        }
    }

    /**
     * 将值放入List的右边，并设置过期时间
     */
    public void rpush(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
        } catch (Exception e) {
            logger.error("Redis rpush with expire error, key: {}", key, e);
        }
    }

    /**
     * 将值放入List的左边
     */
    public void lpush(String key, Object value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            logger.error("Redis lpush error, key: {}", key, e);
        }
    }

    /**
     * 从List的右边弹出一个值
     */
    public Object rpop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            logger.error("Redis rpop error, key: {}", key, e);
            return null;
        }
    }

    /**
     * 从List的左边弹出一个值
     */
    public Object lpop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            logger.error("Redis lpop error, key: {}", key, e);
            return null;
        }
    }

    // =============================Set操作=============================

    /**
     * 获取Set中的所有值
     */
    public Set<Object> smembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            logger.error("Redis smembers error, key: {}", key, e);
            return new HashSet<>();
        }
    }

    /**
     * 判断Set中是否存在某个值
     */
    public boolean sismember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            logger.error("Redis sismember error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 将值添加到Set中
     */
    public void sadd(String key, Object... values) {
        try {
            redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            logger.error("Redis sadd error, key: {}", key, e);
        }
    }

    /**
     * 从Set中删除值
     */
    public void srem(String key, Object... values) {
        try {
            redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            logger.error("Redis srem error, key: {}", key, e);
        }
    }

    /**
     * 获取Set的大小
     */
    public Long scard(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            logger.error("Redis scard error, key: {}", key, e);
            return 0L;
        }
    }
}
