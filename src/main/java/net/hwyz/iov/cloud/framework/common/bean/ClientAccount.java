package net.hwyz.iov.cloud.framework.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 客户端用户账号基本信息
 *
 * @author hwyz_leo
 */
@Data
@AllArgsConstructor
public class ClientAccount implements Serializable {

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 用户唯一ID
     */
    private String accountId;

}
