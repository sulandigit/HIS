# Spring Cloud Gateway 迁移指南

## 概述

从 Spring Cloud 2020.0.0 (Ilford) 开始，Zuul 1.x 不再被支持。推荐使用 Spring Cloud Gateway 作为 API 网关。

## Zuul vs Spring Cloud Gateway

| 特性 | Zuul 1.x | Spring Cloud Gateway |
|------|---------|---------------------|
| 基础架构 | 基于 Servlet (阻塞式 I/O) | 基于 Spring WebFlux (非阻塞式 I/O) |
| 性能 | 较低 | 更高 |
| 路由配置 | properties/yaml | Fluent API 或 yaml |
| 过滤器 | ZuulFilter | GatewayFilter |
| 断言 | 简单路径匹配 | 丰富的路由断言工厂 |
| 维护状态 | 停止维护 | 活跃维护 |

## 创建 Spring Cloud Gateway 模块

### 1. 创建新的 Maven 模块

在 `his-cloud` 目录下创建 `his-cloud-gateway` 模块。

#### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.neu.his</groupId>
        <artifactId>his-cloud-dependencies</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../his-cloud-dependencies/pom.xml</relativePath>
    </parent>

    <artifactId>his-cloud-gateway</artifactId>
    <packaging>jar</packaging>

    <name>his-cloud-gateway</name>
    <description>Spring Cloud Gateway - API Gateway</description>

    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- Micrometer Tracing -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>

        <!-- Spring Boot Admin Client -->
        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-client</artifactId>
        </dependency>

        <!-- Spring Boot Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Resilience4j (熔断限流) -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot2</artifactId>
        </dependency>

        <!-- Redis (限流存储) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt</artifactId>
            <version>0.9.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>com.neu.his.cloud.gateway.GatewayApplication</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. 主启动类

```java
package com.neu.his.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

### 3. 配置文件 application.yml

#### 基于 Zuul 的原配置
```yaml
# his-cloud-zuul 原配置
zuul:
  routes:
    api-b:
      path: /app/**
      serviceId: his-cloud-api-app
```

#### 迁移到 Spring Cloud Gateway
```yaml
spring:
  application:
    name: his-cloud-gateway
  cloud:
    gateway:
      # 服务发现路由配置
      discovery:
        locator:
          enabled: true  # 启用服务发现
          lower-case-service-id: true  # 服务名小写
      
      # 路由配置
      routes:
        # 路由 1: App API
        - id: his-cloud-api-app
          uri: lb://his-cloud-api-app  # lb: 负载均衡
          predicates:
            - Path=/app/**
          filters:
            - StripPrefix=1  # 去除路径前缀
            - name: CircuitBreaker
              args:
                name: appCircuitBreaker
                fallbackUri: forward:/fallback/app
        
        # 路由 2: PC API
        - id: his-cloud-api-pc
          uri: lb://his-cloud-api-pc
          predicates:
            - Path=/pc/**
          filters:
            - StripPrefix=1
            - name: CircuitBreaker
              args:
                name: pcCircuitBreaker
                fallbackUri: forward:/fallback/pc
      
      # 全局 CORS 配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:8080"
              - "https://docs.spring.io"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600

# Eureka 配置
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS

# Resilience4j 熔断器配置
resilience4j:
  circuitbreaker:
    instances:
      appCircuitBreaker:
        sliding-window-size: 10  # 滑动窗口大小
        failure-rate-threshold: 50  # 失败率阈值
        wait-duration-in-open-state: 10000  # 熔断器打开后等待时间
        permitted-number-of-calls-in-half-open-state: 5  # 半开状态允许的调用数
      pcCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 5

# 日志配置
logging:
  level:
    root: info
    com.neu.his: debug
    org.springframework.cloud.gateway: debug
```

## 路由配置迁移对照表

| Zuul 配置项 | Gateway 配置项 | 说明 |
|------------|---------------|------|
| zuul.routes.{name}.path | predicates: - Path | 路径匹配 |
| zuul.routes.{name}.serviceId | uri: lb://{serviceId} | 服务 ID |
| zuul.sensitive-headers | N/A (使用过滤器) | 敏感头处理 |
| zuul.prefix | filters: - StripPrefix | 路径前缀处理 |
| zuul.strip-prefix | filters: - StripPrefix={n} | 去除前缀 |

## 过滤器迁移

### Zuul 过滤器示例
```java
public class AuthFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return "pre";
    }
    
    @Override
    public int filterOrder() {
        return 0;
    }
    
    @Override
    public boolean shouldFilter() {
        return true;
    }
    
    @Override
    public Object run() {
        // 认证逻辑
        return null;
    }
}
```

### Gateway 过滤器示例
```java
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {
    
    public AuthGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 认证逻辑
            ServerHttpRequest request = exchange.getRequest();
            String token = request.getHeaders().getFirst("Authorization");
            
            if (token == null || !validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            return chain.filter(exchange);
        };
    }
    
    private boolean validateToken(String token) {
        // JWT 验证逻辑
        return true;
    }
    
    public static class Config {
        // 配置属性
    }
}
```

## 全局过滤器

```java
@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 全局认证逻辑
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 白名单路径
        if (path.startsWith("/public/")) {
            return chain.filter(exchange);
        }
        
        // JWT 验证
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -100;  // 优先级
    }
    
    private boolean validateToken(String token) {
        // JWT 验证逻辑
        return true;
    }
}
```

## 熔断降级处理

```java
@RestController
public class FallbackController {
    
    @GetMapping("/fallback/app")
    public Mono<Map<String, Object>> appFallback() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", "App 服务暂时不可用，请稍后重试");
        return Mono.just(result);
    }
    
    @GetMapping("/fallback/pc")
    public Mono<Map<String, Object>> pcFallback() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", "PC 服务暂时不可用，请稍后重试");
        return Mono.just(result);
    }
}
```

## 迁移步骤

### 第一阶段：并行运行
1. 创建 `his-cloud-gateway` 模块
2. 配置相同的路由规则
3. 修改端口（如 Gateway 使用 10023，Zuul 保持 10022）
4. 同时启动两个网关
5. 逐步切换流量到 Gateway

### 第二阶段：全量切换
1. 更新客户端配置，指向 Gateway
2. 监控 Gateway 运行状态
3. 确认无问题后停止 Zuul

### 第三阶段：清理
1. 移除 `his-cloud-zuul` 模块
2. 清理相关配置

## 注意事项

1. **响应式编程**: Gateway 基于 WebFlux，不能使用阻塞式 API
2. **依赖冲突**: 不要同时引入 `spring-boot-starter-web` 和 `spring-boot-starter-webflux`
3. **数据库访问**: 如果需要数据库操作，使用响应式驱动（如 R2DBC）
4. **性能调优**: 合理配置连接池和线程池参数
5. **监控**: 配置 Actuator 和 Micrometer 进行监控

## 参考资料

- [Spring Cloud Gateway 官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [从 Zuul 迁移到 Gateway 指南](https://spring.io/blog/2019/06/18/getting-started-with-spring-cloud-gateway)
- [Gateway 路由断言和过滤器](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-request-predicates-factories)
