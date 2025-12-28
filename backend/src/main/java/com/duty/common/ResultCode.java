package com.duty.common;

/**
 * 响应状态码枚举
 *
 * @author duty-system
 * @since 2025-12-26
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限访问"),

    // 业务异常码
    SHIFT_NOT_EXIST(1001, "班次不存在"),
    SCHEDULE_NOT_EXIST(1002, "排班记录不存在"),
    RECORD_NOT_EXIST(1003, "值班记录不存在"),
    TEMPLATE_NOT_EXIST(1004, "记录模板不存在"),
    PERSON_NOT_EXIST(1005, "人员不存在"),
    DUPLICATE_SCHEDULE(1006, "该人员在此时间段已存在排班");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
