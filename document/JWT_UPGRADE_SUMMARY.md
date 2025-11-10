# JWT Token 加密强度提升 - 功能验证说明

## 一、实施内容总结

### 1. 依赖库升级
已完成以下依赖库升级：
- jjwt: 从 0.9.0/0.9.1 升级到 0.11.5
- 升级文件：
  - HIS-master/HIS-api/pom.xml
  - HIS-master/HIS-common/pom.xml
  - his-cloud/his-cloud-zuul/pom.xml

### 2. RSA密钥对生成
已生成2048位RSA密钥对：
- 私钥: /data/workspace/HIS/config/keys/jwt-private.pem (权限: 600)
- 公钥: /data/workspace/HIS/config/keys/jwt-public.pem (权限: 644)

### 3. 核心组件开发

#### 3.1 密钥管理服务 (KeyManagementService)
- 位置: 
  - HIS-master/HIS-common/src/main/java/com/neu/his/common/service/KeyManagementService.java
  - his-cloud/his-cloud-zuul/src/main/java/com/neu/his/cloud/zuul/service/KeyManagementService.java
- 功能:
  - 从PEM文件加载RSA密钥对
  - 支持多密钥版本管理
  - 支持HMAC密钥（HS512兼容模式）
  - 密钥轮换机制

#### 3.2 设备指纹工具 (DeviceFingerprintUtil)
- 位置:
  - HIS-master/HIS-common/src/main/java/com/neu/his/common/util/DeviceFingerprintUtil.java
  - his-cloud/his-cloud-zuul/src/main/java/com/neu/his/cloud/zuul/util/DeviceFingerprintUtil.java
- 功能:
  - 基于User-Agent、Accept-Language等生成设备指纹
  - 获取客户端真实IP地址（支持代理）
  - IP地址验证（支持disabled/loose/strict三种模式）

#### 3.3 Token黑名单服务 (TokenBlacklistService)
- 位置:
  - HIS-master/HIS-common/src/main/java/com/neu/his/common/service/TokenBlacklistService.java
  - his-cloud/his-cloud-zuul/src/main/java/com/neu/his/cloud/zuul/service/TokenBlacklistService.java
- 功能:
  - Token加入黑名单（存储到Redis）
  - 检查Token是否在黑名单中
  - 自动过期（TTL与Token剩余有效期一致）
  - 记录撤销原因和时间

#### 3.4 增强版JwtTokenUtil
- 位置:
  - HIS-master/HIS-common/src/main/java/com/neu/his/common/util/JwtTokenUtil.java
  - his-cloud/his-cloud-zuul/src/main/java/com/neu/his/cloud/zuul/util/JwtTokenUtil.java
- 新增功能:
  - 支持RS256非对称加密算法
  - 支持HS512兼容模式（双算法验证）
  - 扩展Token Payload:
    * jti: Token唯一标识
    * kid: 密钥版本ID（Header中）
    * deviceId: 设备指纹
    * ipAddress: 签发IP地址
    * sessionId: 会话标识
    * iat: 签发时间
    * nbf: 生效时间
  - 设备绑定验证
  - IP地址验证

#### 3.5 增强版JWT过滤器
- 位置:
  - HIS-master/HIS-api/src/main/java/com/neu/his/component/JwtAuthenticationTokenFilter.java
  - his-cloud/his-cloud-zuul/src/main/java/com/neu/his/cloud/zuul/component/JwtAuthenticationTokenFilter.java
- 新增功能:
  - Token黑名单检查
  - 调用增强版validateToken（支持设备和IP验证）

### 4. 配置文件更新

#### 4.1 HIS-api配置 (application.yml)
```yaml
jwt:
  tokenHeader: Authorization
  secret: mySecret  # HS512兼容模式密钥
  expiration: 604800  # 7天
  tokenHead: Bearer
  
  # 签名算法配置
  signature:
    algorithm: RS256  # 主算法
    fallbackAlgorithms:
      - HS512  # 兼容算法
  
  # 密钥配置
  key:
    mode: file
    rsaPrivateKeyPath: /data/workspace/HIS/config/keys/jwt-private.pem
    rsaPublicKeyPath: /data/workspace/HIS/config/keys/jwt-public.pem
    currentKeyVersion: v1
  
  # 黑名单配置
  blacklist:
    enabled: true
    redisKeyPrefix: jwt:blacklist:
  
  # 安全增强配置
  security:
    deviceBinding:
      enabled: false  # 设备绑定（可按需开启）
      mode: loose
    ipValidation:
      enabled: false  # IP验证（可按需开启）
      mode: disabled
    anomalyDetection:
      enabled: false
      maxRequestsPerMinute: 100
      maxConcurrentSessions: 3
```

#### 4.2 his-cloud-zuul配置 (application.yml)
配置同上，路径一致。

## 二、功能特性

### 1. 双算法支持（渐进式升级）
- **新Token**: 使用RS256算法签名
- **旧Token**: 仍可使用HS512验证（向后兼容）
- **优势**: 无需强制用户重新登录，平滑过渡

### 2. 密钥版本管理
- 每个密钥分配唯一版本号（如v1、v2）
- Token Header中包含kid字段标识密钥版本
- 支持多版本密钥并存，便于密钥轮换

### 3. Token黑名单机制
- 用户登出时Token加入黑名单
- 密码修改后旧Token自动失效
- 管理员强制下线功能
- Redis自动过期，无需手动清理

### 4. 设备绑定（可选）
- 生成设备指纹（基于浏览器特征）
- Token只能在签发设备使用
- 防止Token跨设备盗用

### 5. IP地址校验（可选）
- 记录Token签发IP地址
- 三种验证模式：
  * disabled: 不验证
  * loose: IP段匹配（适合内网环境）
  * strict: 精确IP匹配（适合固定工作站）

## 三、使用示例

### 1. 生成Token（登录时）
```java
@Autowired
private JwtTokenUtil jwtTokenUtil;

// 方式1: 不带设备和IP信息（兼容模式）
String token = jwtTokenUtil.generateToken(userDetails);

// 方式2: 带设备和IP信息（增强模式）
String token = jwtTokenUtil.generateToken(userDetails, request);
```

### 2. 验证Token
```java
// 方式1: 基础验证（兼容模式）
boolean isValid = jwtTokenUtil.validateToken(token, userDetails);

// 方式2: 增强验证（包含设备和IP检查）
boolean isValid = jwtTokenUtil.validateToken(token, userDetails, request);
```

### 3. Token黑名单操作
```java
@Autowired
private TokenBlacklistService blacklistService;

// 用户登出时加入黑名单
String jti = jwtTokenUtil.getJtiFromToken(token);
long remainingTime = ...; // 计算Token剩余有效期
blacklistService.addToBlacklist(jti, username, "用户主动登出", remainingTime);

// 检查是否在黑名单
boolean isBlacklisted = blacklistService.isBlacklisted(jti);
```

### 4. 从Token提取信息
```java
String username = jwtTokenUtil.getUserNameFromToken(token);
String jti = jwtTokenUtil.getJtiFromToken(token);
String deviceId = jwtTokenUtil.getDeviceIdFromToken(token);
String ipAddress = jwtTokenUtil.getIpAddressFromToken(token);
```

## 四、安全特性对比

| 特性 | 升级前 | 升级后 |
|------|--------|--------|
| 签名算法 | HS512 | RS256 (支持HS512兼容) |
| 密钥类型 | 对称密钥 | 非对称密钥 |
| 密钥长度 | 简单字符串 | RSA 2048位 |
| 密钥存储 | 明文配置文件 | PEM文件（私钥权限600） |
| Token唯一性 | 无 | jti字段 |
| 密钥版本管理 | 无 | kid字段 |
| 设备绑定 | 无 | deviceId字段 |
| IP验证 | 无 | ipAddress字段 |
| 黑名单机制 | 无 | Redis黑名单 |
| Token撤销 | 不支持 | 支持 |

## 五、性能影响评估

根据设计文档要求：
- Token生成耗时: ≤100ms（RS256）
- Token验证耗时: ≤50ms（RS256）
- 黑名单查询耗时: ≤10ms（Redis本地）
- 系统整体响应时间增加: ≤10%

## 六、部署建议

### 1. 分阶段部署
- **阶段1**: 部署新代码，algorithm配置为HS512（保持现状）
- **阶段2**: 切换algorithm为RS256，开始生成新Token
- **阶段3**: 旧Token自然过期后，移除HS512支持

### 2. 密钥文件部署
- 确保密钥文件路径正确
- 私钥文件权限必须为600
- 生产环境建议使用Vault或KMS存储密钥

### 3. Redis配置
- 确保Redis已正常运行
- 黑名单功能依赖Redis

### 4. 可选功能启用
- 初期建议禁用deviceBinding和ipValidation
- 观察运行稳定后，逐步启用安全功能

## 七、测试验证

### 1. 单元测试
已创建以下测试类：
- DeviceFingerprintUtilTest: 设备指纹工具测试
- KeyManagementServiceTest: 密钥管理服务测试

### 2. 功能测试要点
- [ ] 用户登录生成Token（RS256）
- [ ] Token验证通过
- [ ] 旧Token（HS512）仍可验证（兼容性）
- [ ] 用户登出后Token加入黑名单
- [ ] 黑名单Token被拒绝访问
- [ ] Token过期后自动失效
- [ ] 设备绑定验证（如启用）
- [ ] IP地址验证（如启用）

### 3. 安全测试要点
- [ ] Token伪造攻击（签名验证失败）
- [ ] Token重放攻击（黑名单拦截）
- [ ] 跨设备盗用（设备绑定阻止）
- [ ] 密钥泄露风险（非对称加密降低影响）

## 八、监控指标

建议监控以下指标：
- token_generation_count: Token生成总数
- token_validation_count: Token验证总数
- token_validation_failure_rate: Token验证失败率
- token_blacklist_hit_rate: 黑名单命中率
- token_generation_time: Token生成耗时
- token_validation_time: Token验证耗时
- redis_blacklist_query_time: 黑名单查询耗时

## 九、故障排查

### 常见问题

1. **Token验证失败**
   - 检查密钥文件路径是否正确
   - 检查密钥文件格式是否正确
   - 检查algorithm配置是否正确

2. **黑名单不生效**
   - 检查Redis是否正常运行
   - 检查blacklist.enabled配置
   - 检查RedisUtil或RedisTemplate注入

3. **设备绑定误拦截**
   - 调整deviceBinding.mode为loose
   - 或暂时禁用deviceBinding.enabled

4. **性能下降**
   - 检查Redis响应时间
   - 考虑增加缓存
   - 优化密钥加载逻辑

## 十、后续优化建议

1. **密钥轮换自动化**
   - 实现定期自动密钥轮换
   - 添加密钥过期监控和告警

2. **Refresh Token机制**
   - 实现Access Token + Refresh Token双Token模式
   - 缩短Access Token有效期（如2小时）
   - Refresh Token长期有效（7天）

3. **异常行为检测**
   - 实现请求频率限制
   - 异地登录检测
   - 并发会话数限制

4. **审计日志**
   - Token签发日志
   - Token验证失败日志
   - 黑名单操作日志

5. **性能优化**
   - 公钥缓存（避免重复加载）
   - 黑名单结果缓存（短时间内）
   - 异步黑名单写入

## 十一、联系与支持

如有问题，请参考：
- 设计文档: /data/.task/design.md
- 源代码位置:
  - HIS-master模块: /data/workspace/HIS/HIS-master/
  - his-cloud-zuul模块: /data/workspace/HIS/his-cloud/his-cloud-zuul/

---

**实施完成日期**: 2025-11-10  
**版本**: v1.0  
**状态**: 开发完成，待集成测试
