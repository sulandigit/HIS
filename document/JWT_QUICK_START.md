# JWT Token 加密强度提升 - 快速启动指南

## 一、前置要求

### 1. 环境检查
- [x] Java 8+
- [x] Redis服务运行中
- [x] MySQL数据库运行中

### 2. 密钥文件
密钥文件已生成在：
```
/data/workspace/HIS/config/keys/
  ├── jwt-private.pem  (RSA私钥, 权限600)
  └── jwt-public.pem   (RSA公钥, 权限644)
```

## 二、快速启动步骤

### 步骤1: 验证密钥文件
```bash
ls -lh /data/workspace/HIS/config/keys/
# 应该看到两个.pem文件
```

### 步骤2: 配置验证
检查application.yml配置：
```yaml
jwt:
  signature:
    algorithm: RS256  # 使用RS256算法
  key:
    mode: file
    rsaPrivateKeyPath: /data/workspace/HIS/config/keys/jwt-private.pem
    rsaPublicKeyPath: /data/workspace/HIS/config/keys/jwt-public.pem
```

### 步骤3: 启动Redis
```bash
# 确保Redis服务运行
redis-cli ping
# 应返回: PONG
```

### 步骤4: 编译项目
```bash
cd /data/workspace/HIS/HIS-master
# 如果有Maven: mvn clean install -DskipTests
```

### 步骤5: 启动应用
```bash
# HIS-master模式
cd /data/workspace/HIS/HIS-master/HIS-api
# java -jar target/HIS-api-1.0-SNAPSHOT.jar

# 或 his-cloud模式
cd /data/workspace/HIS/his-cloud/his-cloud-zuul
# java -jar target/his-cloud-zuul-1.0-SNAPSHOT.jar
```

## 三、功能验证

### 1. 测试登录（生成Token）
```bash
curl -X POST http://localhost:9999/staff/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

期望响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJraWQiOiJ2MSIsImFsZyI6IlJTMjU2In0...",
    "tokenHead": "Bearer "
  }
}
```

**注意事项**:
- Token Header中应包含`kid: v1`（密钥版本）
- Token Payload中应包含`jti`（Token唯一标识）

### 2. 测试Token验证
```bash
TOKEN="从登录响应获取的token"

curl -X GET http://localhost:9999/staff/info \
  -H "Authorization: Bearer $TOKEN"
```

### 3. 测试黑名单（登出）
```bash
curl -X POST http://localhost:9999/staff/logout \
  -H "Authorization: Bearer $TOKEN"
```

再次使用相同Token访问：
```bash
curl -X GET http://localhost:9999/staff/info \
  -H "Authorization: Bearer $TOKEN"
```

期望：应该返回401未授权

## 四、验证新功能

### 1. 验证RS256算法
查看Token的Header部分：
```bash
# Token格式: header.payload.signature
TOKEN="your_token_here"
HEADER=$(echo $TOKEN | cut -d'.' -f1)
echo $HEADER | base64 -d
```

应该看到：
```json
{
  "kid": "v1",
  "alg": "RS256"
}
```

### 2. 验证扩展Payload
```bash
PAYLOAD=$(echo $TOKEN | cut -d'.' -f2)
echo $PAYLOAD | base64 -d
```

应该包含：
```json
{
  "sub": "username",
  "jti": "uuid",
  "created": 1234567890,
  "sessionId": "uuid",
  "iat": 1234567890,
  "nbf": 1234567890,
  "exp": 1234567890
}
```

如果启用了设备绑定和IP验证：
```json
{
  "deviceId": "md5_hash",
  "ipAddress": "192.168.1.1"
}
```

### 3. 验证黑名单
检查Redis中的黑名单数据：
```bash
redis-cli
> KEYS jwt:blacklist:*
> GET jwt:blacklist:{jti}
```

### 4. 兼容性测试（可选）
如果有旧Token（HS512算法），验证是否仍可使用：
```bash
# 使用旧Token访问
curl -X GET http://localhost:9999/staff/info \
  -H "Authorization: Bearer $OLD_TOKEN"
```

应该仍然可以正常访问（向后兼容）。

## 五、启用可选安全功能

### 1. 启用设备绑定
修改application.yml：
```yaml
jwt:
  security:
    deviceBinding:
      enabled: true
      mode: loose  # 或 strict
```

重启应用后，Token将包含deviceId字段，并在验证时检查设备一致性。

### 2. 启用IP验证
```yaml
jwt:
  security:
    ipValidation:
      enabled: true
      mode: loose  # disabled/loose/strict
```

### 3. 测试设备绑定
```bash
# 1. 登录获取Token
TOKEN1=$(curl -s -X POST http://localhost:9999/staff/login \
  -H "Content-Type: application/json" \
  -H "User-Agent: Mozilla/5.0 (Windows)" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')

# 2. 使用不同User-Agent访问（模拟不同设备）
curl -X GET http://localhost:9999/staff/info \
  -H "Authorization: Bearer $TOKEN1" \
  -H "User-Agent: Chrome/90.0"
```

在strict模式下，应该被拒绝访问。

## 六、监控检查

### 1. 查看日志
```bash
# 查看Token生成日志
tail -f logs/application.log | grep "JwtTokenUtil"

# 查看黑名单操作日志
tail -f logs/application.log | grep "TokenBlacklistService"

# 查看密钥加载日志
tail -f logs/application.log | grep "KeyManagementService"
```

### 2. Redis监控
```bash
redis-cli
> INFO stats
> DBSIZE
> KEYS jwt:blacklist:*
```

## 七、常见问题排查

### 问题1: Token验证失败
**错误**: JWT格式验证失败

**排查步骤**:
1. 检查密钥文件是否存在
   ```bash
   ls -l /data/workspace/HIS/config/keys/
   ```

2. 检查密钥文件权限
   ```bash
   chmod 600 /data/workspace/HIS/config/keys/jwt-private.pem
   ```

3. 检查配置文件中的路径
   ```bash
   grep -A5 "jwt:" /path/to/application.yml
   ```

4. 查看应用日志
   ```bash
   tail -100 logs/application.log | grep -i error
   ```

### 问题2: 黑名单不生效
**症状**: 登出后Token仍可使用

**排查步骤**:
1. 检查Redis连接
   ```bash
   redis-cli ping
   ```

2. 检查黑名单配置
   ```yaml
   jwt:
     blacklist:
       enabled: true  # 确保为true
   ```

3. 检查jti是否存在
   ```bash
   # Token中应该包含jti字段
   echo $PAYLOAD | base64 -d | jq '.jti'
   ```

4. 手动检查Redis
   ```bash
   redis-cli
   > KEYS jwt:blacklist:*
   ```

### 问题3: 密钥加载失败
**错误**: 密钥管理服务初始化失败

**解决方案**:
1. 检查PEM文件格式
   ```bash
   head -1 /data/workspace/HIS/config/keys/jwt-private.pem
   # 应该是: -----BEGIN PRIVATE KEY-----
   ```

2. 重新生成密钥
   ```bash
   cd /data/workspace/HIS/config/keys
   openssl genrsa -out jwt-private.pem 2048
   openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
   chmod 600 jwt-private.pem
   ```

### 问题4: 性能问题
**症状**: 请求响应变慢

**排查步骤**:
1. 检查Redis响应时间
   ```bash
   redis-cli --latency
   ```

2. 检查Token验证耗时（查看日志）
   ```bash
   tail -f logs/application.log | grep "token_validation_time"
   ```

3. 考虑禁用可选功能
   ```yaml
   jwt:
     security:
       deviceBinding:
         enabled: false
       ipValidation:
         enabled: false
   ```

## 八、回滚方案

如果升级出现问题，可以快速回滚到HS512模式：

### 方式1: 配置回滚（推荐）
修改application.yml：
```yaml
jwt:
  signature:
    algorithm: HS512  # 改回HS512
```

重启应用即可。

### 方式2: 代码回滚
如果需要完全回滚代码，使用git：
```bash
cd /data/workspace/HIS
git log --oneline  # 查看提交历史
git revert <commit_hash>  # 回滚到指定版本
```

## 九、下一步

升级完成后，建议：

1. **观察运行状态**（1-2周）
   - 监控Token验证失败率
   - 监控系统响应时间
   - 收集用户反馈

2. **逐步启用安全功能**
   - 先启用黑名单（已默认启用）
   - 观察稳定后启用设备绑定（loose模式）
   - 最后根据需要启用IP验证

3. **清理旧Token**
   - 等待旧HS512 Token自然过期（7天）
   - 可选：强制用户重新登录

4. **实施密钥轮换**
   - 规划90天一次的密钥轮换
   - 准备密钥轮换脚本
   - 测试密钥轮换流程

## 十、技术支持

如遇到问题，请：
1. 查阅完整文档: `/data/workspace/HIS/document/JWT_UPGRADE_SUMMARY.md`
2. 查看设计文档: `/data/.task/design.md`
3. 检查源代码注释

---

**最后更新**: 2025-11-10  
**版本**: v1.0
