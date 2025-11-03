# HIS系统多级缓存策略

## 概述

本模块实现了一套完整的多级缓存策略,结合了本地缓存(Caffeine)和分布式缓存(Redis),旨在提高系统性能并减少数据库访问压力。

## 核心特性

- **多级缓存架构**: L1(Caffeine本地缓存) + L2(Redis分布式缓存)
- **声明式缓存**: 基于注解的AOP实现,使用简单
- **编程式缓存**: 提供灵活的API,支持复杂场景
- **缓存统计**: 实时监控缓存命中率、驱逐率等指标
- **灵活配置**: 支持多种缓存级别和过期策略
- **高性能**: Caffeine提供接近最优的命中率

## 架构设计

### 缓存层级

```
┌─────────────────────────────────────────┐
│            应用层                        │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│     L1: Caffeine本地缓存                 │
│  - 速度快,容量小                         │
│  - 单机有效,不共享                       │
│  - 适合热点数据                          │
└─────────────────┬───────────────────────┘
                  │ 未命中
                  ▼
┌─────────────────────────────────────────┐
│     L2: Redis分布式缓存                  │
│  - 速度中等,容量大                       │
│  - 多实例共享                            │
│  - 适合一般数据                          │
└─────────────────┬───────────────────────┘
                  │ 未命中
                  ▼
┌─────────────────────────────────────────┐
│          数据源(数据库)                   │
└─────────────────────────────────────────┘
```

### 核心组件

1. **CacheLevel**: 缓存级别枚举
   - `L1_ONLY`: 仅本地缓存
   - `L2_ONLY`: 仅Redis缓存
   - `L1_AND_L2`: 多级缓存

2. **CacheKey**: 缓存键封装类
   - 标准化缓存键生成
   - 支持命名空间管理

3. **MultiLevelCacheManager**: 多级缓存管理器
   - 统一的缓存操作接口
   - 自动处理缓存穿透

4. **@MultiLevelCache**: 缓存注解
   - 声明式缓存
   - 支持SpEL表达式

5. **@CacheEvict**: 缓存清除注解
   - 数据更新时自动清除缓存

6. **CacheStatistics**: 缓存统计工具
   - 监控缓存性能
   - 优化缓存策略

## 快速开始

### 1. 添加依赖

在`pom.xml`中添加(已自动添加):

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>2.9.3</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### 2. 配置文件

在`application.yml`中添加配置:

```yaml
his:
  cache:
    caffeine:
      initial-capacity: 100
      maximum-size: 1000
      expire-after-write: 300
      expire-after-access: 180
      record-stats: true
```

### 3. 使用注解方式

```java
@Service
public class UserService {
    
    // 查询时使用缓存
    @MultiLevelCache(
        namespace = "user",
        key = "#userId",
        level = CacheLevel.L1_AND_L2,
        expireSeconds = 300
    )
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    // 更新时清除缓存
    @CacheEvict(
        namespace = "user",
        key = "#user.id",
        level = CacheLevel.L1_AND_L2
    )
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
}
```

### 4. 使用编程方式

```java
@Service
public class UserService {
    
    @Autowired
    private MultiLevelCacheManager cacheManager;
    
    public User getUserById(Long userId) {
        CacheKey cacheKey = CacheKey.of("user", String.valueOf(userId));
        
        return cacheManager.get(cacheKey, CacheLevel.L1_AND_L2, () -> {
            return userMapper.selectById(userId);
        }, 300);
    }
}
```

## 使用场景

### 场景1: 热点数据缓存

对于高频访问的数据,使用`hotDataCache`实例:

```java
@MultiLevelCache(
    namespace = "hotUser",
    key = "#userId",
    level = CacheLevel.L1_AND_L2,
    expireSeconds = 180,
    cacheName = "hotData"
)
public User getHotUser(Long userId) {
    return userMapper.selectById(userId);
}
```

### 场景2: 用户会话缓存

用户登录会话数据,使用`userSessionCache`实例:

```java
@MultiLevelCache(
    namespace = "session",
    key = "#token",
    level = CacheLevel.L1_AND_L2,
    expireSeconds = 1800,
    cacheName = "userSession"
)
public UserSession getSession(String token) {
    return sessionRepository.findByToken(token);
}
```

### 场景3: 字典数据缓存

系统字典等基础数据,使用`dictionaryCache`实例:

```java
@MultiLevelCache(
    namespace = "dict",
    key = "#dictType",
    level = CacheLevel.L1_AND_L2,
    expireSeconds = 3600,
    cacheName = "dictionary"
)
public List<DictItem> getDictByType(String dictType) {
    return dictMapper.selectByType(dictType);
}
```

### 场景4: 单机缓存

对于不需要分布式共享的数据:

```java
@MultiLevelCache(
    namespace = "local",
    key = "#key",
    level = CacheLevel.L1_ONLY
)
public Object getLocalData(String key) {
    return dataSource.load(key);
}
```

### 场景5: 条件缓存

只有满足条件才缓存:

```java
@MultiLevelCache(
    namespace = "user",
    key = "#userId",
    level = CacheLevel.L1_AND_L2,
    expireSeconds = 300,
    condition = "#result != null && #result.status == 1"
)
public User getActiveUser(Long userId) {
    return userMapper.selectById(userId);
}
```

## 缓存监控

### 查看缓存统计

```java
@Autowired
private CacheStatistics cacheStatistics;

// 打印所有缓存统计信息
cacheStatistics.printAllCacheStats();

// 获取统计数据
Map<String, CacheStatsInfo> stats = cacheStatistics.getAllCacheStats();
```

### 统计指标说明

- **命中率**: 缓存命中次数 / 总请求次数
- **未命中率**: 缓存未命中次数 / 总请求次数
- **驱逐次数**: 缓存满时驱逐的条目数
- **加载时间**: 从数据源加载数据的平均时间

## 性能优化建议

### 1. 合理设置缓存容量

根据业务数据量调整`maximum-size`:
- 热点数据: 5000-10000
- 一般数据: 1000-5000
- 字典数据: 100-1000

### 2. 设置合适的过期时间

- 热点数据: 3-5分钟
- 用户会话: 30-60分钟
- 字典数据: 1-2小时
- 静态数据: 无限期

### 3. 监控缓存命中率

定期查看缓存统计,命中率低于80%需要优化:
- 增加缓存容量
- 调整过期时间
- 优化缓存键设计

### 4. 避免缓存穿透

对于可能不存在的数据,也要缓存null值:

```java
@MultiLevelCache(
    namespace = "user",
    key = "#userId",
    level = CacheLevel.L1_AND_L2,
    expireSeconds = 60  // 短时间缓存
)
public User getUserById(Long userId) {
    User user = userMapper.selectById(userId);
    // 即使user为null也会缓存,防止穿透
    return user;
}
```

## 最佳实践

1. **优先使用注解方式**: 代码简洁,易于维护
2. **设置合理的命名空间**: 便于管理和批量清除
3. **避免缓存大对象**: 影响性能和内存
4. **及时清除过期缓存**: 使用`@CacheEvict`注解
5. **监控缓存性能**: 定期查看统计数据
6. **测试缓存逻辑**: 确保缓存更新正确

## 注意事项

1. **序列化问题**: Redis缓存的对象需要实现`Serializable`
2. **缓存一致性**: 更新数据时要同步清除缓存
3. **内存管理**: 合理设置缓存容量,避免OOM
4. **并发问题**: 多级缓存管理器是线程安全的
5. **分布式环境**: L1缓存在各实例独立,注意数据一致性

## 示例代码

详细示例请参考: `CacheUsageExample.java`

## 技术选型

- **Caffeine**: 基于Java 8的高性能缓存库
- **Redis**: 分布式缓存,支持集群
- **Spring AOP**: 实现声明式缓存
- **SpEL**: 动态表达式支持

## 版本历史

- v1.0.0: 初始版本,支持多级缓存基本功能
