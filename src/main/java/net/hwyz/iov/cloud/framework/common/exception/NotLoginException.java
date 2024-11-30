package net.hwyz.iov.cloud.framework.common.exception;

/**
 * 未能通过的登录认证异常
 *
 * @author hwyz_leo
 */
public class NotLoginException extends BaseException {

    public NotLoginException(String message) {
        super(message);
    }
}
