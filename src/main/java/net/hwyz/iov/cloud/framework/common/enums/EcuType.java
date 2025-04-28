package net.hwyz.iov.cloud.framework.common.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * ECU类型枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum EcuType {

    /** 车联终端 **/
    TBOX,
    /** 中央计算平台 **/
    CCP,
    /** 蓝牙模块 **/
    BTM;

    public static EcuType valOf(String val) {
        return Arrays.stream(EcuType.values())
                .filter(ecuType -> ecuType.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}
