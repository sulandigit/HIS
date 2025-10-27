package com.neu.his.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 * 
 * @author HIS Team
 * @date 2025
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 业务模块
     */
    String module() default "";
    
    /**
     * 操作类型
     */
    String operation() default "";
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 是否保存请求参数
     */
    boolean saveRequestData() default true;
    
    /**
     * 是否保存响应结果
     */
    boolean saveResponseData() default true;
}
