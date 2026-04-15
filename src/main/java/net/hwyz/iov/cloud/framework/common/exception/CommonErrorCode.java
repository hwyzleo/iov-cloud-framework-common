package net.hwyz.iov.cloud.framework.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用错误码
 */
@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    SUCCESS("000000", "成功"),
    INVALID_PARAM("100001", "参数校验失败"),
    INTERNAL_ERROR("100002", "系统内部错误");

    private final String code;
    private final String message;

}
