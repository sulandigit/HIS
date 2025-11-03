package com.neu.his.common.cache.aspect;

import com.neu.his.common.cache.CacheKey;
import com.neu.his.common.cache.MultiLevelCacheManager;
import com.neu.his.common.cache.annotation.CacheEvict;
import com.neu.his.common.cache.annotation.MultiLevelCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 多级缓存切面
 * 实现@MultiLevelCache和@CacheEvict注解的AOP处理
 */
@Aspect
@Component
public class MultiLevelCacheAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiLevelCacheAspect.class);
    
    @Autowired
    private MultiLevelCacheManager cacheManager;
    
    private final ExpressionParser parser = new SpelExpressionParser();
    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
    
    /**
     * 处理@MultiLevelCache注解
     */
    @Around("@annotation(com.neu.his.common.cache.annotation.MultiLevelCache)")
    public Object handleMultiLevelCache(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MultiLevelCache annotation = method.getAnnotation(MultiLevelCache.class);
        
        // 解析缓存键
        String cacheKeyStr = parseKey(annotation.key(), method, joinPoint.getArgs());
        CacheKey cacheKey = new CacheKey(annotation.namespace(), cacheKeyStr);
        
        logger.debug("处理多级缓存注解,缓存键: {}, 级别: {}", cacheKey.getFullKey(), annotation.level());
        
        // 从缓存获取数据
        Object result;
        if ("default".equals(annotation.cacheName())) {
            result = cacheManager.get(cacheKey, annotation.level(), () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException("执行目标方法失败", throwable);
                }
            }, annotation.expireSeconds());
        } else {
            result = cacheManager.get(cacheKey, annotation.cacheName(), () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException("执行目标方法失败", throwable);
                }
            }, annotation.expireSeconds());
        }
        
        // 检查条件表达式
        if (StringUtils.hasText(annotation.condition())) {
            boolean shouldCache = evaluateCondition(annotation.condition(), method, joinPoint.getArgs(), result);
            if (!shouldCache && result != null) {
                cacheManager.evict(cacheKey, annotation.level());
            }
        }
        
        return result;
    }
    
    /**
     * 处理@CacheEvict注解
     */
    @Around("@annotation(com.neu.his.common.cache.annotation.CacheEvict)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheEvict annotation = method.getAnnotation(CacheEvict.class);
        
        // 解析缓存键
        String cacheKeyStr = parseKey(annotation.key(), method, joinPoint.getArgs());
        CacheKey cacheKey = new CacheKey(annotation.namespace(), cacheKeyStr);
        
        logger.debug("处理缓存清除注解,缓存键: {}, 级别: {}", cacheKey.getFullKey(), annotation.level());
        
        // 方法执行前清除
        if (annotation.beforeInvocation()) {
            cacheManager.evict(cacheKey, annotation.level());
        }
        
        Object result = joinPoint.proceed();
        
        // 方法执行后清除
        if (!annotation.beforeInvocation()) {
            cacheManager.evict(cacheKey, annotation.level());
        }
        
        return result;
    }
    
    /**
     * 解析缓存键(支持SpEL表达式)
     */
    private String parseKey(String key, Method method, Object[] args) {
        if (!key.contains("#")) {
            return key;
        }
        
        // 获取方法参数名
        String[] paramNames = discoverer.getParameterNames(method);
        if (paramNames == null) {
            return key;
        }
        
        // 创建SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
        }
        
        try {
            Expression expression = parser.parseExpression(key);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            logger.error("解析缓存键失败: {}", key, e);
            return key;
        }
    }
    
    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, Method method, Object[] args, Object result) {
        if (!StringUtils.hasText(condition)) {
            return true;
        }
        
        // 获取方法参数名
        String[] paramNames = discoverer.getParameterNames(method);
        
        // 创建SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        context.setVariable("result", result);
        
        try {
            Expression expression = parser.parseExpression(condition);
            Boolean value = expression.getValue(context, Boolean.class);
            return value != null && value;
        } catch (Exception e) {
            logger.error("评估条件表达式失败: {}", condition, e);
            return true;
        }
    }
}
