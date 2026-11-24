package net.hwyz.iov.cloud.framework.common.exception;

/**
 * 业务异常（历史形态：仅字符串消息，无错误码）。
 *
 * @author hwyz_leo
 * @deprecated 统一异常规范改以 {@link BusinessException} + {@link ErrorCode} 为唯一业务异常契约：
 * 本类无法携带稳定错误码，异常被全局处理器视为系统异常（HTTP 500 + 100002），调用方无法按码分支。
 * 新代码禁止使用；存量调用点须迁移到 {@code XxxBaseException extends BusinessException}。
 */
@Deprecated
public final class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     */
    private String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}