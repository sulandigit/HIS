package com.neu.his.bo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: WebLog
 * @description: Web访问日志实体类
 * @author Zain
 * @date 2019/5/319:58
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作用户
     */
    private String username;

    /**
     * 操作时间
     */
    private Long startTime;

    /**
     * 消耗时间(毫秒)
     */
    private Integer spendTime;

    /**
     * 根路径
     */
    private String basePath;

    /**
     * URI
     */
    private String uri;

    /**
     * URL
     */
    private String url;

    /**
     * 请求类型
     */
    private String method;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 请求参数
     */
    private Object parameter;

    /**
     * 返回结果
     */
    private Object result;
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 浏览器
     */
    private String browser;
    
    /**
     * 异常信息
     */
    private String exceptionMessage;
}
