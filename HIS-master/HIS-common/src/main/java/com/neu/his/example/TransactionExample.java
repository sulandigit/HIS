package com.neu.his.example;

import com.neu.his.exception.BusinessException;
import com.neu.his.exception.DatabaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 事务使用示例
 * 演示如何在Service层正确使用事务注解和异常处理
 * 
 * @author HIS Team
 * @since 2025-11-03
 */
@Service
public class TransactionExample {

    /**
     * 示例1: 简单事务操作
     * - 超时时间: 20秒
     * - 回滚策略: 所有异常
     */
    @Transactional(timeout = 20, rollbackFor = Exception.class)
    public void simpleTransaction() {
        // 1. 数据验证
        // 2. 数据库操作
        // 3. 业务处理
    }

    /**
     * 示例2: 带业务验证的事务
     * 抛出BusinessException会自动触发事务回滚
     */
    @Transactional(timeout = 30, rollbackFor = Exception.class)
    public int transactionWithValidation(int amount) {
        // 业务验证
        if (amount <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "金额必须大于0");
        }
        
        // 数据库操作
        // ...
        
        return 1;
    }

    /**
     * 示例3: 复杂业务事务
     * 包含多个数据库操作，确保原子性
     */
    @Transactional(timeout = 60, rollbackFor = Exception.class)
    public void complexTransaction() {
        try {
            // 步骤1: 插入主记录
            // insertMainRecord();
            
            // 步骤2: 插入关联记录
            // insertRelatedRecords();
            
            // 步骤3: 更新统计信息
            // updateStatistics();
            
        } catch (Exception e) {
            // 记录日志
            // logger.error("复杂事务执行失败", e);
            
            // 包装为DatabaseException并抛出，触发回滚
            throw new DatabaseException("复杂事务执行失败", e);
        }
    }

    /**
     * 示例4: 只读事务（查询操作）
     * 使用readOnly=true可以提高性能
     */
    @Transactional(readOnly = true, timeout = 10)
    public Object queryData(Long id) {
        // 查询操作
        return null;
    }

    /**
     * 错误示例1: 不要捕获异常而不重新抛出
     * 这样会导致事务不回滚！
     */
    @Transactional(timeout = 30, rollbackFor = Exception.class)
    public void wrongExample1() {
        try {
            // 数据库操作
        } catch (Exception e) {
            // ❌ 错误：捕获了异常但没有重新抛出
            // logger.error("操作失败", e);
            // 事务不会回滚！
        }
    }

    /**
     * 正确示例: 捕获异常后重新抛出
     */
    @Transactional(timeout = 30, rollbackFor = Exception.class)
    public void correctExample() {
        try {
            // 数据库操作
        } catch (Exception e) {
            // ✓ 正确：记录日志后重新抛出
            // logger.error("操作失败", e);
            throw new DatabaseException("操作失败", e);
        }
    }

    /**
     * 错误示例2: 不要在事务中执行耗时的非数据库操作
     */
    @Transactional(timeout = 30, rollbackFor = Exception.class)
    public void wrongExample2() {
        // 数据库操作1
        
        // ❌ 错误：在事务中调用外部API
        // callExternalApi();
        
        // ❌ 错误：在事务中进行复杂计算
        // performComplexCalculation();
        
        // 数据库操作2
    }

    /**
     * 正确示例: 将非数据库操作移到事务外
     */
    public void correctExample2() {
        // 1. 先执行非数据库操作
        // callExternalApi();
        // performComplexCalculation();
        
        // 2. 再执行数据库事务
        executeTransaction();
    }
    
    @Transactional(timeout = 30, rollbackFor = Exception.class)
    private void executeTransaction() {
        // 只包含数据库操作
    }
}
