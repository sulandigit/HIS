package com.neu.his.exception;

/**
 * 业务异常类
 * 用于标识业务逻辑异常，触发事务回滚
 * 
 * @author HIS Team
 * @since 2025-11-03
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
