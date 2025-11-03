package com.neu.his.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * 事务切面
 * 用于增强事务管理，提供统一的事务日志记录和异常处理
 * 
 * @author HIS Team
 * @since 2025-11-03
 */
@Aspect
@Component
@Order(1) // 确保在事务切面之前执行
public class TransactionAspect {

    private static final Logger logger = LoggerFactory.getLogger(TransactionAspect.class);

    /**
     * 定义切点：拦截所有Service实现类中带有@Transactional注解的方法
     */
    @Pointcut("execution(* com.neu.his..service.impl..*ServiceImpl.*(..)) && @annotation(org.springframework.transaction.annotation.Transactional)")
    public void transactionalMethods() {}

    /**
     * 环绕通知：在事务方法执行前后进行日志记录和异常处理
     */
    @Around("transactionalMethods()")
    public Object aroundTransactionalMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        logger.info("事务开始 - 方法: {}", methodName);
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("事务提交成功 - 方法: {}, 耗时: {}ms", methodName, executionTime);
            
            // 如果执行时间过长，记录警告
            if (executionTime > 5000) {
                logger.warn("事务执行时间过长 - 方法: {}, 耗时: {}ms，建议优化", methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("事务执行失败，准备回滚 - 方法: {}, 耗时: {}ms, 异常: {}", 
                methodName, executionTime, e.getMessage(), e);
            
            // 手动标记事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            
            // 重新抛出异常，让Spring事务管理器处理
            throw e;
        }
    }
}
