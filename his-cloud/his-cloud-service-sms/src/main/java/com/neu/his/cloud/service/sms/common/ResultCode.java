package com.neu.his.cloud.service.sms.common;

/**
 * 系统管理服务(SMS)错误码
 * 错误码范围：400000-409999
 * 详见文档：/document/error-codes.md
 */
public enum ResultCode implements IErrorCode {
    // ========== 系统通用错误码(000000-009999) ==========
    SUCCESS(000000, "操作成功"),
    UNAUTHORIZED(000101, "暂未登录或token已经过期"),
    FORBIDDEN(000102, "没有相关权限"),
    VALIDATE_FAILED(000201, "参数检验失败"),
    FAILED(000501, "操作失败"),
    SERVICE_UNAVAILABLE(000502, "系统繁忙，请稍后重试"),
    TOO_MANY_REQUESTS(000701, "请求过于频繁，请稍后重试"),
    SERVICE_DEGRADED(000702, "服务降级中"),
    
    // ========== 系统管理错误码(400000-409999) ==========
    // 用户管理错误(400301-400329)
    INVALID_CREDENTIALS(400301, "用户名或密码错误"),
    USER_NOT_FOUND(400302, "用户不存在"),
    USER_ALREADY_EXISTS(400303, "用户已存在"),
    WEAK_PASSWORD(400304, "密码强度不符合要求"),
    
    // 角色权限错误(400330-400349)
    ROLE_NOT_FOUND(400330, "角色不存在"),
    
    // 科室管理错误(400350-400369)
    DEPARTMENT_NOT_FOUND(400350, "科室不存在"),
    
    // 常用项管理错误(400370-400389)
    COMMON_ITEM_NOT_FOUND(400370, "常用项不存在");
    private long code;
    private String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
