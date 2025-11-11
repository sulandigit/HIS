package com.neu.his.exception;

/**
 * 数据库异常类
 * 用于标识数据库操作异常，触发事务回滚
 * 
 * @author HIS Team
 * @since 2025-11-03
 */
public class DatabaseException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
