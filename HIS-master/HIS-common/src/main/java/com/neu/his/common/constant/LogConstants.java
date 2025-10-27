package com.neu.his.common.constant;

/**
 * 日志常量定义
 * 
 * @author HIS Team
 * @date 2025
 */
public class LogConstants {
    
    /**
     * 日志类型
     */
    public static class LogType {
        /** 业务日志 */
        public static final String BUSINESS = "BUSINESS";
        /** 系统日志 */
        public static final String SYSTEM = "SYSTEM";
        /** 操作日志 */
        public static final String OPERATION = "OPERATION";
        /** 异常日志 */
        public static final String EXCEPTION = "EXCEPTION";
        /** 性能日志 */
        public static final String PERFORMANCE = "PERFORMANCE";
        /** 安全日志 */
        public static final String SECURITY = "SECURITY";
        /** 访问日志 */
        public static final String ACCESS = "ACCESS";
        /** 数据库日志 */
        public static final String DATABASE = "DATABASE";
    }
    
    /**
     * 操作类型
     */
    public static class OperationType {
        /** 查询 */
        public static final String SELECT = "SELECT";
        /** 新增 */
        public static final String INSERT = "INSERT";
        /** 更新 */
        public static final String UPDATE = "UPDATE";
        /** 删除 */
        public static final String DELETE = "DELETE";
        /** 导出 */
        public static final String EXPORT = "EXPORT";
        /** 导入 */
        public static final String IMPORT = "IMPORT";
        /** 登录 */
        public static final String LOGIN = "LOGIN";
        /** 登出 */
        public static final String LOGOUT = "LOGOUT";
        /** 授权 */
        public static final String GRANT = "GRANT";
        /** 强制退出 */
        public static final String FORCE = "FORCE";
    }
    
    /**
     * 业务模块
     */
    public static class BusinessModule {
        /** 用户管理 */
        public static final String USER = "USER";
        /** 患者管理 */
        public static final String PATIENT = "PATIENT";
        /** 医生管理 */
        public static final String DOCTOR = "DOCTOR";
        /** 挂号管理 */
        public static final String REGISTRATION = "REGISTRATION";
        /** 诊断管理 */
        public static final String DIAGNOSIS = "DIAGNOSIS";
        /** 药品管理 */
        public static final String MEDICINE = "MEDICINE";
        /** 收费管理 */
        public static final String BILLING = "BILLING";
        /** 系统管理 */
        public static final String SYSTEM = "SYSTEM";
    }
    
    /**
     * 日志状态
     */
    public static class LogStatus {
        /** 成功 */
        public static final String SUCCESS = "SUCCESS";
        /** 失败 */
        public static final String FAIL = "FAIL";
        /** 异常 */
        public static final String EXCEPTION = "EXCEPTION";
    }
    
    /**
     * 敏感字段
     */
    public static class SensitiveField {
        /** 密码 */
        public static final String PASSWORD = "password";
        /** 身份证号 */
        public static final String ID_CARD = "idCard";
        /** 手机号 */
        public static final String PHONE = "phone";
        /** 银行卡号 */
        public static final String BANK_CARD = "bankCard";
        /** 邮箱 */
        public static final String EMAIL = "email";
    }
    
    /**
     * 性能阈值（毫秒）
     */
    public static class PerformanceThreshold {
        /** 慢查询阈值 */
        public static final long SLOW_QUERY = 1000L;
        /** 慢接口阈值 */
        public static final long SLOW_API = 3000L;
        /** 超时阈值 */
        public static final long TIMEOUT = 10000L;
    }
}
