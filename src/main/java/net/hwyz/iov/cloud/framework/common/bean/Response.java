package net.hwyz.iov.cloud.framework.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

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

}
