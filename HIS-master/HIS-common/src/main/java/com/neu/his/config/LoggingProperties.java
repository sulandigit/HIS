package com.neu.his.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 * 日志配置属性类
 * 
 * @author HIS Team
 * @date 2025
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "his.logging")
public class LoggingProperties {
    
    /**
     * 是否启用请求日志
     */
    private boolean enableRequestLog = true;
    
    /**
     * 是否启用性能日志
     */
    private boolean enablePerformanceLog = true;
    
    /**
     * 是否启用SQL日志
     */
    private boolean enableSqlLog = true;
    
    /**
     * 是否启用异常日志
     */
    private boolean enableExceptionLog = true;
    
    /**
     * 是否对敏感信息进行脱敏
     */
    private boolean enableDesensitization = true;
    
    /**
     * 慢接口阈值(毫秒)
     */
    private long slowApiThreshold = 3000;
    
    /**
     * 慢SQL阈值(毫秒)
     */
    private long slowSqlThreshold = 1000;
    
    /**
     * 日志保留天数
     */
    private int retentionDays = 30;
    
    /**
     * 单个日志文件最大大小
     */
    private String maxFileSize = "100MB";
    
    /**
     * 日志总大小上限
     */
    private String totalSizeCap = "10GB";
    
    /**
     * 是否启用异步日志
     */
    private boolean enableAsyncLog = true;
    
    /**
     * 异步日志队列大小
     */
    private int asyncQueueSize = 512;
    
    /**
     * 是否输出到ELK
     */
    private boolean enableElk = false;
    
    /**
     * Logstash地址
     */
    private String logstashHost = "localhost:4560";
}
