package net.hwyz.iov.cloud.framework.common.enums;

import lombok.Getter;

/**
 * 管理后台用户状态
 *
 * @author hwyz_leo
 */
@Getter
public enum MptUserStatus {
    OK("0", "正常"), DISABLE("1", "停用"), DELETED("2", "删除");

    private final String code;
    private final String info;

    MptUserStatus(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
