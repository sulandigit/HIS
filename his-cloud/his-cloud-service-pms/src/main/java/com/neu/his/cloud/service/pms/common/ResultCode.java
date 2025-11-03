package com.neu.his.cloud.service.pms.common;

/**
 * 门诊管理服务(PMS)错误码
 * 错误码范围：100000-109999
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
    
    // ========== 门诊管理错误码(100000-109999) ==========
    // 患者管理错误(100301-100319)
    PATIENT_NOT_FOUND(100301, "患者信息不存在"),
    PATIENT_ALREADY_EXISTS(100302, "患者信息已存在"),
    
    // 挂号管理错误(100320-100339)
    REGISTRATION_NOT_FOUND(100320, "挂号信息不存在"),
    REGISTRATION_FULL(100321, "该时段号源已满"),
    DUPLICATE_REGISTRATION(100322, "重复挂号"),
    REGISTRATION_CANCELLED(100323, "挂号已取消，不可操作"),
    
    // 诊疗管理错误(100340-100359)
    DIAGNOSIS_NOT_FOUND(100340, "诊断记录不存在"),
    MEDICAL_RECORD_TEMPLATE_NOT_FOUND(100341, "病历模板不存在"),
    
    // 排班管理错误(100360-100379)
    SCHEDULE_NOT_FOUND(100360, "医生排班信息不存在"),
    SCHEDULE_CONFLICT(100361, "排班时间冲突");
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
