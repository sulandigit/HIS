package com.neu.his.cloud.service.bms.common;

/**
 * 收费管理服务(BMS)错误码
 * 错误码范围：300000-309999
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
    
    // ========== 收费管理错误码(300000-309999) ==========
    // 收费管理错误(300301-300329)
    CHARGE_NOT_FOUND(300301, "收费记录不存在"),
    CHARGE_SETTLED(300302, "该费用已结算，不可修改"),
    PAYMENT_METHOD_NOT_SUPPORTED(300303, "支付方式不支持"),
    
    // 退费管理错误(300330-300349)
    REFUND_AMOUNT_EXCEEDED(300330, "退费金额超过实收金额"),
    
    // 日结管理错误(300350-300369)
    DAILY_SETTLEMENT_NOT_FOUND(300350, "日结记录不存在"),
    DAILY_SETTLEMENT_NOT_DONE(300351, "该收费员尚未日结"),
    
    // 发票管理错误(300370-300389)
    INVOICE_NUMBER_DISCONTINUOUS(300370, "发票号不连续");
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
