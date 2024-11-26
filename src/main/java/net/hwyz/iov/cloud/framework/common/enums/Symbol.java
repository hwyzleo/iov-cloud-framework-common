package net.hwyz.iov.cloud.framework.common.enums;

import lombok.AllArgsConstructor;

/**
 * 符号枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum Symbol {

    /** 下划线 **/
    UNDERSCORE("_"),
    /** 中划线 **/
    HYPHEN("-"),
    /** 冒号 **/
    COLON(":");

    public final String value;

}
