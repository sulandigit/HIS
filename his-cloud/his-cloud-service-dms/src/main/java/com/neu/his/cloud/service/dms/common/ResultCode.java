package com.neu.his.cloud.service.dms.common;

/**
 * 药品管理服务(DMS)错误码
 * 错误码范围：200000-209999
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
    
    // ========== 药品管理错误码(200000-209999) ==========
    // 药品信息错误(200301-200319)
    DRUG_NOT_FOUND(200301, "药品信息不存在"),
    DRUG_EXPIRED(200302, "药品已过期"),
    DRUG_STOCK_INSUFFICIENT(200303, "药品库存不足"),
    
    // 处方管理错误(200320-200339)
    PRESCRIPTION_NOT_FOUND(200320, "处方不存在"),
    PRESCRIPTION_DISPENSED(200321, "处方已发药，不可修改"),
    PRESCRIPTION_TEMPLATE_NOT_FOUND(200322, "处方模板不存在"),
    INVALID_DOSES(200323, "草药处方剂数不合法"),
    
    // 发药退药错误(200340-200359)
    RETURN_QUANTITY_EXCEEDED(200340, "退药数量超过已发数量");
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
