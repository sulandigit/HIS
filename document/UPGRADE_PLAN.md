# HIS 后端依赖升级实施记录

## 升级日期
2025-10-23

## 项目现状分析

### 当前技术栈

#### HIS-master (单体应用)
- **Spring Boot**: 2.1.3.RELEASE
- **Java 编译版本**: 1.8 (实际), 1.7 (配置冲突)
- **PageHelper**: 1.2.10
- **Druid**: 1.1.10
- **Swagger**: 2.7.0
- **JUnit**: 4.13.1
- **MyBatis**: 3.5.6

#### his-cloud (微服务架构)
- **Spring Boot**: 2.0.3.RELEASE
- **Spring Cloud**: Finchley.RELEASE
- **Java 版本**: 1.8
- **Zipkin**: 2.10.1
- **Spring Boot Admin**: 2.0.1
- **Swagger**: 2.7.0
- **PageHelper**: 1.2.10
- **Druid**: 1.1.13
- **MyBatis**: 1.1.1 (mybatis-spring-boot-starter)

### 主要问题识别

1. ✗ **版本不一致**: HIS-master (Spring Boot 2.1.3) vs his-cloud (Spring Boot 2.0.3)
2. ✗ **JDK 配置混乱**: HIS-master pom.xml 中 maven.compiler.source/target 设置为 1.7，但插件配置为 1.8
3. ✗ **停止维护**: Spring Cloud Finchley 已于 2019 年停止维护
4. ✗ **安全风险**: 旧版本可能存在已知安全漏洞
5. ✗ **Zuul 已弃用**: Spring Cloud 已不再维护 Zuul 1.x

## 升级方案

### 目标版本

| 组件 | 当前版本 | 目标版本 | 说明 |
|------|---------|---------|------|
| Java | 1.8 | 11 LTS | 长期支持版本 |
| Spring Boot (统一) | 2.0.3/2.1.3 | 2.7.18 | 2.x 系列最稳定版本 |
| Spring Cloud | Finchley.RELEASE | 2021.0.8 (Jubilee) | 支持 Spring Boot 2.7.x |
| PageHelper | 1.2.10 | 1.4.7 | 最新稳定版 |
| Druid | 1.1.10/1.1.13 | 1.2.20 | 最新稳定版 |
| MyBatis Spring Boot Starter | 1.1.1 | 2.3.2 | 适配 Spring Boot 2.7 |
| Swagger | 2.7.0 | SpringDoc OpenAPI 1.7.0 | 迁移到 OpenAPI 3 |
| JUnit | 4.13.1 | 5.9.3 (Jupiter) | 新一代测试框架 |
| Spring Boot Admin | 2.0.1 | 2.7.15 | 与 Spring Boot 版本匹配 |
| Maven Compiler Plugin | 3.8.0 | 3.11.0 | 支持 Java 11 |

### 升级策略

1. **Java 向后兼容**: 升级到 Java 11，但保持 Java 8 语言特性和字节码兼容
2. **统一版本管理**: 在父 POM 中统一管理所有依赖版本
3. **渐进式升级**: 先升级 HIS-master，再升级 his-cloud
4. **组件替换**:
   - Zuul → Spring Cloud Gateway
   - Hystrix → Resilience4j
   - Spring Cloud Zipkin → Micrometer Tracing + Zipkin

## 实施计划

### 阶段 1: HIS-master 升级 ✓
- [x] 分析当前依赖配置
- [ ] 升级父 POM (Java 11 + Spring Boot 2.7.18)
- [ ] 更新核心依赖版本
- [ ] 替换 Swagger 为 SpringDoc OpenAPI
- [ ] 升级 JUnit 到 JUnit 5
- [ ] 编译验证
- [ ] 单元测试验证

### 阶段 2: his-cloud 基础设施升级
- [ ] 升级 his-cloud-dependencies 父 POM
- [ ] 升级 Eureka 服务注册中心
- [ ] 升级 Config Server 配置中心
- [ ] 创建 Spring Cloud Gateway 替代 Zuul
- [ ] 升级 Zipkin 到 Micrometer Tracing
- [ ] 升级 Spring Boot Admin Monitor

### 阶段 3: his-cloud 业务服务升级
- [ ] 升级 his-cloud-service-pms (患者管理服务)
- [ ] 升级 his-cloud-service-dms (药品管理服务)
- [ ] 升级 his-cloud-service-bms (收费管理服务)
- [ ] 升级 his-cloud-service-sms (短信服务)
- [ ] 升级 API 网关服务 (api-pc, api-app)

### 阶段 4: 测试与验证
- [ ] 依赖冲突检查 (mvn dependency:tree)
- [ ] 编译验证 (mvn clean install)
- [ ] 单元测试验证
- [ ] 集成测试验证
- [ ] 安全扫描

## 升级记录

### 2025-10-23
- ✓ 完成项目结构分析
- ✓ 完成依赖版本清单梳理
- ✓ 创建升级计划文档
- → 准备开始 HIS-master 升级

## 风险与应对

### 主要风险
1. **依赖冲突**: 使用 dependency:tree 分析，通过 exclusions 解决
2. **运行时异常**: 充分的单元测试和集成测试覆盖
3. **性能退化**: 性能基线对比，JVM 参数调优
4. **业务功能异常**: 快速回滚机制，保留旧版本部署包

### 应急预案
- 保留原始代码分支
- 数据库备份
- 分阶段升级，每个阶段验证后再继续
- 遇到重大问题立即回滚

## 注意事项

1. ✓ Java 11 移除了部分 Java EE 模块（JAXB、JAX-WS），需添加单独依赖
2. ✓ Spring Boot 2.7.x 配置项可能有变化，需检查 application.yml
3. ✓ Zuul 迁移到 Gateway 需要重写路由配置和过滤器
4. ✓ JUnit 4 升级到 JUnit 5 需要修改测试注解和断言
5. ✓ Swagger 迁移到 OpenAPI 3 需要更新所有 API 注解

## 参考资料

- [Spring Boot 2.7 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes)
- [Spring Cloud 2021.0.x Documentation](https://docs.spring.io/spring-cloud/docs/2021.0.x/reference/html/)
- [Java 11 Migration Guide](https://docs.oracle.com/en/java/javase/11/migrate/index.html)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
