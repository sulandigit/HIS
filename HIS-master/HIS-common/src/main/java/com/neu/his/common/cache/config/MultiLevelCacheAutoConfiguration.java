package com.neu.his.common.cache.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 多级缓存自动配置类
 * 自动扫描缓存相关组件并启用AOP
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan("com.neu.his.common.cache")
public class MultiLevelCacheAutoConfiguration {
    // 自动配置,无需额外代码
}
