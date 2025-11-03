# 错误码文档化实施指南

## 一、概述

本指南帮助开发人员快速了解和使用HIS系统的错误码规范。

## 二、文档位置

### 2.1 错误码参考文档

| 文档 | 路径 | 适用范围 |
|------|------|---------|
| 错误码汇总文档 | `/document/error-codes-summary.md` | 全系统概览 |
| HIS-master错误码文档 | `/HIS-master/document/error-codes.md` | 单体应用 |
| his-cloud错误码文档 | `/his-cloud/document/error-codes.md` | 微服务架构 |

### 2.2 错误码代码位置

| 模块 | 文件路径 |
|------|---------|
| HIS-master | `/HIS-master/HIS-common/src/main/java/com/neu/his/common/api/ResultCode.java` |
| 门诊管理服务(PMS) | `/his-cloud/his-cloud-service-pms/src/main/java/com/neu/his/cloud/service/pms/common/ResultCode.java` |
| 药品管理服务(DMS) | `/his-cloud/his-cloud-service-dms/src/main/java/com/neu/his/cloud/service/dms/common/ResultCode.java` |
| 收费管理服务(BMS) | `/his-cloud/his-cloud-service-bms/src/main/java/com/neu/his/cloud/service/bms/common/ResultCode.java` |
| 系统管理服务(SMS) | `/his-cloud/his-cloud-service-sms/src/main/java/com/neu/his/cloud/service/sms/common/ResultCode.java` |
| 网关服务(Zuul) | `/his-cloud/his-cloud-zuul/src/main/java/com/neu/his/cloud/zuul/common/ResultCode.java` |
| PC端API | `/his-cloud/his-cloud-api-pc/src/main/java/com/neu/his/cloud/api/pc/common/ResultCode.java` |
| APP端API | `/his-cloud/his-cloud-api-app/src/main/java/com/neu/his/cloud/api/app/common/ResultCode.java` |

## 三、错误码使用指南

### 3.1 错误码结构

```
错误码格式：AABBCC (6位数字)
├── AA: 模块码 (00=通用, 10=门诊, 20=药品, 30=收费, 40=系统, 50=网关, 90=第三方)
├── BB: 分类码 (00=成功, 01=认证, 02=参数, 03=业务, 04=数据, 05=系统, 06=外部, 07=限流)
└── CC: 序号码 (01-99)
```

### 3.2 代码示例

#### 3.2.1 返回成功

```java
// 无数据返回
return CommonResult.success(null);

// 返回数据
return CommonResult.success(patient);

// 返回列表
return CommonResult.success(patientList);
```

#### 3.2.2 返回业务错误

```java
// 使用预定义错误码
if (patient == null) {
    return CommonResult.failed(ResultCode.PATIENT_NOT_FOUND);
}

// 使用错误码和自定义消息
if (registration.getStatus() == RegistrationStatus.CANCELLED) {
    return CommonResult.failed(ResultCode.REGISTRATION_CANCELLED);
}
```

#### 3.2.3 抛出业务异常

```java
// 抛出异常，由全局异常处理器统一处理
if (stock < quantity) {
    throw new ApiException(ResultCode.DRUG_STOCK_INSUFFICIENT);
}
```

### 3.3 前端处理示例

#### 3.3.1 统一错误拦截

```javascript
// axios响应拦截器
axios.interceptors.response.use(
    response => {
        const res = response.data;
        
        // 成功
        if (res.code === 000000) {
            return res;
        }
        
        // 认证失败，跳转登录
        if (res.code === 000101) {
            router.push('/login');
            return Promise.reject(new Error('未登录'));
        }
        
        // 其他错误，统一提示
        Message.error(res.message);
        return Promise.reject(new Error(res.message));
    },
    error => {
        Message.error('网络错误');
        return Promise.reject(error);
    }
);
```

#### 3.3.2 业务错误处理

```javascript
// 特定业务错误处理
patientApi.getPatient(id).then(res => {
    // 处理成功
    this.patient = res.data;
}).catch(error => {
    // 根据错误码做特殊处理
    if (error.code === 100301) {
        this.$message.warning('患者不存在，请检查ID');
        this.$router.push('/patient/list');
    }
});
```

## 四、开发规范

### 4.1 新增错误码流程

1. **查阅文档**：先查看是否已有合适的错误码
2. **确定编号**：按照模块码和分类码规则确定错误码编号
3. **更新文档**：在对应的错误码文档中添加新错误码定义
4. **添加枚举**：在ResultCode枚举类中添加错误码常量
5. **代码评审**：提交PR时需包含文档更新

### 4.2 错误码命名规范

```java
// ✅ 推荐：使用描述性的枚举常量名
PATIENT_NOT_FOUND(100301, "患者信息不存在")
DRUG_STOCK_INSUFFICIENT(200303, "药品库存不足")
CHARGE_SETTLED(300302, "该费用已结算，不可修改")

// ❌ 不推荐：使用不明确的名称
ERROR_001(100301, "错误")
PATIENT_ERROR(100301, "患者错误")
```

### 4.3 错误信息编写规范

```java
// ✅ 推荐：清晰描述问题，不包含敏感信息
"患者信息不存在"
"药品库存不足"
"该时段号源已满"

// ❌ 不推荐：信息过于技术化或包含敏感信息
"NullPointerException in PatientService.getById()"
"数据库查询失败：SELECT * FROM patient WHERE id=123"
```

### 4.4 HTTP状态码匹配规范

| 业务场景 | HTTP状态码 | 示例错误码 |
|---------|-----------|-----------|
| 操作成功 | 200 | 000000 |
| 参数错误 | 400 | 000201, 200323 |
| 未认证 | 401 | 000101, 400301 |
| 无权限 | 403 | 000102 |
| 资源不存在 | 404 | 100301, 200301 |
| 资源冲突 | 409 | 100311, 200303 |
| 限流 | 429 | 000701 |
| 系统错误 | 500 | 000501, 900601 |
| 服务不可用 | 503 | 000502, 000702 |
| 网关超时 | 504 | 500102, 900660 |

## 五、常见问题

### 5.1 为什么使用6位数字而不是HTTP状态码？

- **HTTP状态码**：表示HTTP协议层面的状态（如200、404、500）
- **业务错误码**：表示业务逻辑层面的错误（如患者不存在、库存不足）
- **两者配合**：HTTP状态码 + 业务错误码，提供更精准的错误信息

### 5.2 如何处理旧代码中的错误码？

现有代码中的旧错误码（200、401、403、404、500）已经映射到新错误码：

```java
// 旧代码（兼容）
SUCCESS(200, "操作成功")  -> 映射为 SUCCESS(000000, "操作成功")
FAILED(500, "操作失败")   -> 映射为 FAILED(000501, "操作失败")
```

建议：
- 新代码直接使用新错误码
- 旧代码逐步重构，不强制立即修改

### 5.3 微服务间调用如何处理错误码？

```java
// 服务A调用服务B
@FeignClient("pms-service")
public interface PatientService {
    @GetMapping("/patient/{id}")
    CommonResult<Patient> getPatient(@PathVariable Long id);
}

// Controller中处理
CommonResult<Patient> result = patientService.getPatient(id);
if (result.getCode() != ResultCode.SUCCESS.getCode()) {
    // 直接透传下游服务的错误码
    return CommonResult.failed(result.getCode(), result.getMessage());
}
```

### 5.4 如何避免错误码冲突？

1. **模块隔离**：每个微服务只使用自己模块码范围的错误码
2. **文档先行**：新增错误码前先更新文档，避免重复
3. **代码评审**：PR必须包含错误码文档更新

## 六、最佳实践

### 6.1 异常转错误码

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ApiException.class)
    public CommonResult handleApiException(ApiException e) {
        return CommonResult.failed(e.getErrorCode());
    }
    
    @ExceptionHandler(BindException.class)
    public CommonResult handleValidException(BindException e) {
        return CommonResult.failed(ResultCode.VALIDATE_FAILED);
    }
    
    @ExceptionHandler(Exception.class)
    public CommonResult handleException(Exception e) {
        log.error("系统异常", e);
        return CommonResult.failed(ResultCode.FAILED);
    }
}
```

### 6.2 日志记录

```java
@Slf4j
public class PatientServiceImpl implements PatientService {
    
    public Patient getById(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            // 记录业务日志
            log.warn("患者不存在, patientId={}", id);
            throw new ApiException(ResultCode.PATIENT_NOT_FOUND);
        }
        return patient;
    }
}
```

### 6.3 监控告警

```java
// 关键错误码监控
if (result.getCode() == ResultCode.DRUG_STOCK_INSUFFICIENT.getCode()) {
    // 触发库存不足告警
    alertService.sendAlert("药品库存不足", drugId);
}
```

## 七、文档维护

### 7.1 更新频率

- **日常更新**：新增业务功能时同步更新
- **月度Review**：每月检查文档完整性
- **季度发布**：每季度发布正式版本

### 7.2 变更流程

1. 在对应的错误码文档中添加/修改错误码
2. 在ResultCode枚举中同步修改
3. 提交PR，包含文档和代码变更
4. Code Review确认无冲突
5. 合并代码

### 7.3 版本管理

错误码文档随代码一起进行版本控制，每次重大变更时：

1. 更新文档版本号（遵循语义化版本）
2. 在变更历史中记录变更内容
3. 通知相关团队（前端、测试）

## 八、参考资料

- [HTTP状态码规范](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status)
- [RESTful API设计指南](https://restfulapi.net/)
- [错误码设计最佳实践](https://cloud.google.com/apis/design/errors)

---

**文档维护**：后端开发团队  
**最后更新**：2025-11-03  
**版本**：v1.0.0
