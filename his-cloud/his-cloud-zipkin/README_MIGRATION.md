# Zipkin 服务迁移说明

## 重要变更

从 Spring Cloud 2020.0.0 (Ilford) 开始，Spring Cloud Sleuth Zipkin 已经废弃了嵌入式 Zipkin Server 的支持。

### 原因
- `io.zipkin.java:zipkin-server` 和 `zipkin-autoconfigure-ui` 已经停止维护
- Spring Cloud 推荐使用独立的 Zipkin Server

## 迁移方案

### 方案 1: 使用独立的 Zipkin Server (推荐)

#### 1. 使用 Docker 运行 Zipkin
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

#### 2. 使用可执行 JAR 运行
```bash
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

#### 3. 配置微服务连接到 Zipkin
在各个微服务的 `application.yml` 中配置：
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### 方案 2: 使用 Micrometer Tracing

已在 `his-cloud-dependencies` 中添加了 Micrometer Tracing 依赖：
- `micrometer-tracing-bridge-brave`
- `zipkin-reporter-brave`

各个微服务需要添加以下依赖：
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

## 后续步骤

1. **停用当前的 his-cloud-zipkin 模块**
2. **部署独立的 Zipkin Server**
3. **更新所有微服务配置，指向新的 Zipkin Server**
4. **验证链路追踪功能正常**

## 参考文档
- [Zipkin Quickstart](https://zipkin.io/pages/quickstart.html)
- [Spring Cloud Sleuth Migration Guide](https://github.com/spring-cloud/spring-cloud-sleuth/wiki/Spring-Cloud-Sleuth-3.0-Migration-Guide)
- [Micrometer Tracing Documentation](https://micrometer.io/docs/tracing)
