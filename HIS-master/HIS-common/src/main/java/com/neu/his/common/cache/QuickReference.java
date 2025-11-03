/*
 * HIS系统多级缓存策略 - 快速参考指南
 * ==========================================
 * 
 * 一、核心组件
 * -----------
 * 1. CacheLevel: 缓存级别枚举
 *    - L1_ONLY: 仅本地缓存(Caffeine)
 *    - L2_ONLY: 仅分布式缓存(Redis)
 *    - L1_AND_L2: 多级缓存
 * 
 * 2. CacheKey: 缓存键封装
 *    - CacheKey.of("namespace", "key1", "key2")
 * 
 * 3. MultiLevelCacheManager: 多级缓存管理器
 *    - get(): 获取缓存
 *    - put(): 设置缓存
 *    - evict(): 删除缓存
 * 
 * 二、使用示例
 * -----------
 * 
 * 示例1: 基本使用(注解方式)
 * -------------------------
 * @MultiLevelCache(
 *     namespace = "user",
 *     key = "#userId",
 *     level = CacheLevel.L1_AND_L2,
 *     expireSeconds = 300
 * )
 * public User getUserById(Long userId) {
 *     return userMapper.selectById(userId);
 * }
 * 
 * 示例2: 清除缓存
 * ---------------
 * @CacheEvict(
 *     namespace = "user",
 *     key = "#user.id",
 *     level = CacheLevel.L1_AND_L2
 * )
 * public void updateUser(User user) {
 *     userMapper.updateById(user);
 * }
 * 
 * 示例3: 编程方式
 * ---------------
 * @Autowired
 * private MultiLevelCacheManager cacheManager;
 * 
 * public User getUserById(Long userId) {
 *     CacheKey key = CacheKey.of("user", String.valueOf(userId));
 *     return cacheManager.get(key, CacheLevel.L1_AND_L2, 
 *         () -> userMapper.selectById(userId), 300);
 * }
 * 
 * 示例4: 热点数据缓存
 * -------------------
 * @MultiLevelCache(
 *     namespace = "hotUser",
 *     key = "#userId",
 *     cacheName = "hotData",
 *     expireSeconds = 180
 * )
 * public User getHotUser(Long userId) {
 *     return userMapper.selectById(userId);
 * }
 * 
 * 示例5: 用户会话缓存
 * -------------------
 * @MultiLevelCache(
 *     namespace = "session",
 *     key = "#token",
 *     cacheName = "userSession",
 *     expireSeconds = 1800
 * )
 * public UserSession getSession(String token) {
 *     return sessionRepository.findByToken(token);
 * }
 * 
 * 示例6: 字典数据缓存
 * -------------------
 * @MultiLevelCache(
 *     namespace = "dict",
 *     key = "#dictType",
 *     cacheName = "dictionary",
 *     expireSeconds = 3600
 * )
 * public List<DictItem> getDictByType(String dictType) {
 *     return dictMapper.selectByType(dictType);
 * }
 * 
 * 示例7: 条件缓存
 * ---------------
 * @MultiLevelCache(
 *     namespace = "user",
 *     key = "#userId",
 *     expireSeconds = 300,
 *     condition = "#result != null"
 * )
 * public User getUserById(Long userId) {
 *     return userMapper.selectById(userId);
 * }
 * 
 * 示例8: SpEL表达式
 * -----------------
 * @MultiLevelCache(
 *     namespace = "user",
 *     key = "#user.id + ':' + #user.type"
 * )
 * public UserInfo getUserInfo(User user) {
 *     return userMapper.selectInfo(user);
 * }
 * 
 * 示例9: 缓存统计
 * ---------------
 * @Autowired
 * private CacheStatistics cacheStatistics;
 * 
 * public void showStats() {
 *     cacheStatistics.printAllCacheStats();
 * }
 * 
 * 示例10: 手动操作缓存
 * --------------------
 * // 设置缓存
 * CacheKey key = CacheKey.of("user", "1001");
 * cacheManager.put(key, user, CacheLevel.L1_AND_L2, 300);
 * 
 * // 删除缓存
 * cacheManager.evict(key, CacheLevel.L1_AND_L2);
 * 
 * // 清空所有本地缓存
 * cacheManager.clear();
 * 
 * 三、配置说明
 * -----------
 * 
 * application.yml:
 * ----------------
 * his:
 *   cache:
 *     caffeine:
 *       initial-capacity: 100
 *       maximum-size: 1000
 *       expire-after-write: 300
 *       expire-after-access: 180
 *       record-stats: true
 * 
 * 四、缓存实例
 * -----------
 * 
 * | 实例名       | 容量  | 过期时间 | 适用场景      |
 * |-------------|-------|---------|--------------|
 * | default     | 1000  | 5分钟   | 通用数据      |
 * | hotData     | 5000  | 3分钟   | 热点数据      |
 * | userSession | 2000  | 30分钟  | 用户会话      |
 * | dictionary  | 1000  | 60分钟  | 字典数据      |
 * 
 * 五、注意事项
 * -----------
 * 
 * 1. Redis缓存的对象需要实现Serializable接口
 * 2. 更新数据时记得清除相关缓存
 * 3. 合理设置缓存容量和过期时间
 * 4. 定期监控缓存命中率
 * 5. 避免缓存大对象影响性能
 * 
 * 六、常用SpEL表达式
 * ------------------
 * 
 * #userId                     // 参数名
 * #user.id                    // 对象属性
 * #p0, #p1                    // 参数索引
 * #a0, #a1                    // 参数索引(别名)
 * #result                     // 返回值
 * #result != null             // 条件表达式
 * #userId + ':' + #type       // 字符串拼接
 * 
 * 七、性能指标
 * -----------
 * 
 * - Caffeine读取: < 1ms
 * - Redis读取: 1-5ms
 * - 数据库查询: 10-100ms
 * - 建议命中率: > 80%
 * 
 * 八、文档位置
 * -----------
 * 
 * - 详细文档: HIS-common/src/main/java/com/neu/his/common/cache/README.md
 * - 使用示例: CacheUsageExample.java
 * - 实现总结: document/多级缓存策略实现总结.md
 * 
 * 九、支持与反馈
 * -------------
 * 
 * 如有问题,请查看详细文档或联系开发团队
 * 
 */
