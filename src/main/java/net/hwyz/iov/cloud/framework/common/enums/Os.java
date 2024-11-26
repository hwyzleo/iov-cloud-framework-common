package net.hwyz.iov.cloud.framework.common.enums;

import java.util.Arrays;

/**
 * 操作系统枚举类
 *
 * @author hwyz_leo
 */
public enum Os {

    /** 安卓 **/
    ANDROID,
    /** 苹果 **/
    IOS,
    /** 鸿蒙 **/
    HARMONYOS,
    /** 其他 **/
    OTHER;

    public static Os valOf(String osStr) {
        return Arrays.stream(Os.values())
                .filter(os -> os.name().equals(osStr))
                .findFirst()
                .orElse(OTHER);
    }

}
