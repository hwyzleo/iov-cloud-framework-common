package net.hwyz.iov.cloud.framework.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用错误码
 * <p>
 * 平台级错误码，供各服务在“非业务语义”的基础设施错误上复用；模块业务错误码使用各自命名空间。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    SUCCESS("000000", "成功"),
    INVALID_PARAM("100001", "参数校验失败"),
    INTERNAL_ERROR("100002", "系统内部错误"),
    METHOD_NOT_ALLOWED("100003", "请求方法不支持"),
    RESOURCE_NOT_FOUND("100004", "访问资源不存在");

    private final String code;
    private final String message;

}
