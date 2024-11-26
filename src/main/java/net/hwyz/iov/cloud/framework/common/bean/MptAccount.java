package net.hwyz.iov.cloud.framework.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理后台用户账号基本信息
 *
 * @author hwyz_leo
 */
@Data
@AllArgsConstructor
public class MptAccount implements Serializable {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户唯一ID
     */
    private String accountId;

}
