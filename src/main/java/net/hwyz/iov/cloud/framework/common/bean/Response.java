package net.hwyz.iov.cloud.framework.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.hwyz.iov.cloud.framework.common.constant.Constants;

/**
 * 响应对象
 *
 * @param <T>
 * @author hwyz_leo
 */
@Data
@AllArgsConstructor
public class Response<T> {

    /**
     * 结果代码
     */
    private int code;
    /**
     * 响应结果描述
     */
    private String message;
    /**
     * 时间戳
     */
    private Long ts;
    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功
     */
    public static final int SUCCESS = Constants.SUCCESS;

    /**
     * 失败
     */
    public static final int FAIL = Constants.FAIL;

    public Response() {
        this.ts = System.currentTimeMillis();
    }

    public Response(T data) {
        this.data = data;
        this.ts = System.currentTimeMillis();
    }

    public Response(String message, T data) {
        this.message = message;
        this.ts = System.currentTimeMillis();
        this.data = data;
    }

    public static <T> Response<T> ok() {
        return restResult(null, SUCCESS, null);
    }

    public static <T> Response<T> ok(T data) {
        return restResult(data, SUCCESS, null);
    }

    public static <T> Response<T> ok(T data, String msg) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> Response<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> Response<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> Response<T> restResult(T data, int code, String msg) {
        Response<T> apiResult = new Response<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMessage(msg);
        return apiResult;
    }

}
