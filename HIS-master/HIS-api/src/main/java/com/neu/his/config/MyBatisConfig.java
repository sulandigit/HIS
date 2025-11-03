package com.neu.his.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis配置类
 * 注意：事务管理已在TransactionConfig中统一配置
 */
@Configuration
@MapperScan({"com.neu.his.mbg"})//"com.neu.his.sms.dao",
public class MyBatisConfig {
}
