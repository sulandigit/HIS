package com.neu.his.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;

/**
 * 事务配置类
 * 提供统一的事务管理配置，包括超时设置和回滚策略
 * 
 * @author HIS Team
 * @since 2025-11-03
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

    private final DataSource dataSource;

    public TransactionConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 配置事务管理器
     * 设置默认超时时间和回滚策略
     */
    @Bean
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        // 设置默认事务超时时间为30秒
        transactionManager.setDefaultTimeout(30);
        // 设置全局回滚策略：遇到任何异常都回滚
        transactionManager.setGlobalRollbackOnParticipationFailure(true);
        return transactionManager;
    }
}
