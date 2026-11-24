package net.hwyz.iov.cloud.framework.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 基础异常（历史形态：int 状态码，非错误码枚举）。
 *
 * @author hwyz_leo
 * @deprecated 统一异常规范改以 {@link BusinessException} + {@link ErrorCode} 为唯一业务异常契约：
 * int 状态码无法对外登记、无法映射 HTTP 面、与 702xxx/806xxx 等模块码段不兼容。
 * 新代码禁止使用；存量调用点须迁移到 {@code XxxBaseException extends BusinessException}。
 */
@Deprecated
@Getter
@Setter
@JsonIgnoreProperties({"stackTrace", "localizedMessage", "suppressed", "cause"})
public class BaseException extends RuntimeException {

    /**
     * 错误码
     */
    private int code;
    /**
     * 错误消息
     */
    private String message;
    /**
     * 应用名称
     */
    private String app;
    /**
     * 发生时间
     */
    private Long ts;

    /**
     * 默认错误码
     */
    private static final int ERROR_CODE = 100000;
    /**
     * 错误码：入参无效
     */
    public static final int INPUT_PARAM_INVALID = 100001;


    public BaseException(String message) {
        super(message);
        this.code = ERROR_CODE;
        this.message = message;
        this.ts = System.currentTimeMillis();
    }

    public BaseException(int code) {
        super(String.valueOf(code));
        this.code = code;
        this.message = null;
        this.ts = System.currentTimeMillis();
    }

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.ts = System.currentTimeMillis();
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}
