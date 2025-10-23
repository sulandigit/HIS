# Java 升级指南

## 升级概述

本项目已从 **Java 8** 升级到 **Java 21**，同时升级了 Spring Boot 和 Spring Cloud 到最新稳定版本。

## 版本变更

### Java 版本
- **旧版本**: Java 1.8
- **新版本**: Java 21 (LTS)

### Spring Boot 版本
- **HIS-master**:
  - 旧版本: 2.1.3.RELEASE
  - 新版本: 3.3.5

- **his-cloud**:
  - 旧版本: 2.0.3.RELEASE
  - 新版本: 3.3.5

### Spring Cloud 版本
- **旧版本**: Finchley.RELEASE
- **新版本**: 2023.0.3

### 其他重要组件升级
- **Spring Boot Admin**: 2.0.1 → 3.3.4
- **Zipkin**: 2.10.1 → 3.4.2
- **Maven Compiler Plugin**: 3.8.0 → 3.13.0

## 升级后的配置变更

### 1. HIS-master/pom.xml
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <java.version>21</java.version>
</properties>
```

### 2. his-cloud/his-cloud-dependencies/pom.xml
```xml
<properties>
    <java.version>21</java.version>
    <spring-cloud.version>2023.0.3</spring-cloud.version>
</properties>
```

### 3. 所有子模块
- HIS-demo
- HIS-service (及其所有子服务)
- HIS-api

已统一更新为 Java 21 配置。

## 需要注意的重大变更

### 1. Spring Boot 3.x 重大变更

#### Javax → Jakarta 命名空间变更
Spring Boot 3.0+ 要求使用 Jakarta EE 9+，所有 `javax.*` 包已迁移到 `jakarta.*`：

**需要修改的导入包：**
```java
// 旧版本
import javax.servlet.*;
import javax.persistence.*;
import javax.validation.*;

// 新版本
import jakarta.servlet.*;
import jakarta.persistence.*;
import jakarta.validation.*;
```

#### 受影响的主要包：
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.annotation.*` → `jakarta.annotation.*`

### 2. Spring Cloud 组件变更

#### Netflix 组件弃用
- **Zuul**: 已在 Spring Cloud 2020+ 中移除，建议迁移到 Spring Cloud Gateway
- **Hystrix**: 已停止维护，建议迁移到 Resilience4j
- **Ribbon**: 已弃用，建议使用 Spring Cloud LoadBalancer

#### 当前项目受影响的模块：
- `his-cloud-zuul`: 需要迁移到 Spring Cloud Gateway
- 使用 Hystrix 的模块: 需要迁移到 Resilience4j

### 3. Swagger 升级需求

当前使用的 Swagger 2.x 不兼容 Spring Boot 3.x，需要升级：

**建议升级到 SpringDoc OpenAPI:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 4. MySQL 驱动

当前使用的 `mysql-connector-java` 已弃用，建议升级：
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>
```

### 5. Java 21 新特性可用

升级后可以使用 Java 21 的新特性：
- **Record Classes** (Java 14+)
- **Pattern Matching for switch** (Java 17+)
- **Sealed Classes** (Java 17+)
- **Virtual Threads** (Java 21)
- **String Templates** (Preview in Java 21)

## 下一步行动计划

### 必须执行的任务：

1. **代码迁移 (高优先级)**
   - [ ] 将所有 `javax.*` 导入替换为 `jakarta.*`
   - [ ] 更新 Servlet 配置和过滤器
   - [ ] 更新 JPA/Hibernate 相关代码

2. **组件迁移 (高优先级)**
   - [ ] 将 Zuul 迁移到 Spring Cloud Gateway
   - [ ] 将 Hystrix 迁移到 Resilience4j
   - [ ] 升级 Swagger 到 SpringDoc OpenAPI

3. **依赖更新 (中优先级)**
   - [ ] 更新 MySQL 驱动
   - [ ] 更新 MyBatis 相关依赖到兼容版本
   - [ ] 更新其他第三方库到兼容 Jakarta EE 的版本

4. **测试验证 (高优先级)**
   - [ ] 运行所有单元测试
   - [ ] 执行集成测试
   - [ ] 进行完整的功能测试

### 建议执行的任务：

5. **性能优化**
   - [ ] 利用 Java 21 Virtual Threads 优化高并发场景
   - [ ] 使用新的 Pattern Matching 简化代码

6. **代码现代化**
   - [ ] 使用 Records 替换简单的 POJO
   - [ ] 使用 Sealed Classes 提升类型安全

## 环境要求

### 开发环境
- **JDK**: 21 或更高版本
- **Maven**: 3.8.0 或更高版本
- **IDE**: 
  - IntelliJ IDEA 2023.2+
  - Eclipse 2023-09+

### 运行环境
- **Java Runtime**: JRE 21 或更高版本
- **应用服务器**: 支持 Jakarta EE 9+ 的服务器
  - Tomcat 10.1+
  - Jetty 11+

## 迁移工具

### 自动化迁移工具
1. **OpenRewrite**: 可自动化迁移 javax 到 jakarta
   ```bash
   mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
     -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:LATEST \
     -Drewrite.activeRecipes=org.openrewrite.java.migrate.JavaxMigrationToJakarta
   ```

2. **IDE 自动重构**: IntelliJ IDEA 和 Eclipse 都提供了迁移工具

## 回滚方案

如果升级过程中遇到问题，可以通过 Git 回滚：
```bash
git checkout HEAD -- .
```

或者手动恢复以下关键配置：
- Java 版本改回 1.8
- Spring Boot 改回 2.1.3.RELEASE / 2.0.3.RELEASE
- Spring Cloud 改回 Finchley.RELEASE

## 技术支持

如遇到升级问题，请参考：
- [Spring Boot 3.0 迁移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Cloud 2023.x 文档](https://spring.io/projects/spring-cloud)
- [Java 21 发布说明](https://openjdk.org/projects/jdk/21/)

## 更新日期

**2025-10-23**: 完成 Java 版本和 Spring 框架升级配置
