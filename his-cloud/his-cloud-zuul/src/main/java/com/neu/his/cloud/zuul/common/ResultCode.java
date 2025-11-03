package com.neu.his.cloud.zuul.common;

/**
 * 网关服务(Zuul)错误码
 * 错误码范围：500000-509999
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
    
    // ========== 网关服务错误码(500000-509999) ==========
    // 网关错误(500101-500199)
    ROUTE_FAILED(500101, "服务路由失败"),
    SERVICE_TIMEOUT(500102, "服务超时"),
    SERVICE_NOT_AVAILABLE(500103, "服务不可用");
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
