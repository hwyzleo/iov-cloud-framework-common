package net.hwyz.iov.cloud.framework.common.enums;

import lombok.AllArgsConstructor;

/**
 * 自定义Headers
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum CustomHeaders {

    /** 客户端ID **/
    CLIENT_ID("clientId"),
    /** 令牌 **/
    TOKEN("token"),
    /** 客户端账号 **/
    CLIENT_ACCOUNT("clientAccount");

    public final String value;

}
