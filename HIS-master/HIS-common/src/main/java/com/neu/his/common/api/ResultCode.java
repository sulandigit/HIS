package com.neu.his.common.api;

/**
 * 枚举了HIS系统API操作码
 * 错误码结构：6位数字，模块码(2位)+分类码(2位)+序号码(2位)
 * 详见文档：document/error-codes.md
 */
public enum ResultCode implements IErrorCode {
    // ========== 系统通用错误码(000000-009999) ==========
    // 成功状态
    SUCCESS(000000, "操作成功"),
    
    // 认证授权错误(000101-000199)
    UNAUTHORIZED(000101, "暂未登录或token已经过期"),
    FORBIDDEN(000102, "没有相关权限"),
    
    // 参数校验错误(000201-000299)
    VALIDATE_FAILED(000201, "参数检验失败"),
    
    // 系统内部错误(000501-000599)
    FAILED(000501, "操作失败"),
    SERVICE_UNAVAILABLE(000502, "系统繁忙，请稍后重试"),
    
    // 限流熔断错误(000701-000799)
    TOO_MANY_REQUESTS(000701, "请求过于频繁，请稍后重试"),
    SERVICE_DEGRADED(000702, "服务降级中"),
    
    // ========== 门诊管理错误码(100000-109999) ==========
    // 患者管理错误(100301-100319)
    PATIENT_NOT_FOUND(100301, "患者信息不存在"),
    PATIENT_ALREADY_EXISTS(100302, "患者信息已存在"),
    
    // 挂号管理错误(100310-100329)
    REGISTRATION_NOT_FOUND(100310, "挂号信息不存在"),
    REGISTRATION_FULL(100311, "该时段号源已满"),
    DUPLICATE_REGISTRATION(100312, "重复挂号"),
    REGISTRATION_CANCELLED(100313, "挂号已取消，不可操作"),
    
    // 诊疗管理错误(100330-100349)
    DIAGNOSIS_NOT_FOUND(100330, "诊断记录不存在"),
    MEDICAL_RECORD_TEMPLATE_NOT_FOUND(100331, "病历模板不存在"),
    
    // 排班管理错误(100350-100369)
    SCHEDULE_NOT_FOUND(100350, "医生排班信息不存在"),
    SCHEDULE_CONFLICT(100351, "排班时间冲突"),
    
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
    RETURN_QUANTITY_EXCEEDED(200340, "退药数量超过已发数量"),
    
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
    INVOICE_NUMBER_DISCONTINUOUS(300370, "发票号不连续"),
    
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
    COMMON_ITEM_NOT_FOUND(400370, "常用项不存在"),
    
    // ========== 第三方服务错误码(900000-909999) ==========
    // OSS服务错误(900601-900619)
    OSS_UPLOAD_FAILED(900601, "OSS文件上传失败"),
    OSS_DELETE_FAILED(900602, "OSS文件删除失败"),
    
    // 支付服务错误(900620-900639)
    PAYMENT_API_FAILED(900620, "支付接口调用失败"),
    
    // 短信服务错误(900640-900659)
    SMS_SEND_FAILED(900640, "短信发送失败"),
    
    // 通用外部服务错误(900660-900699)
    EXTERNAL_API_TIMEOUT(900660, "外部接口超时");
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
