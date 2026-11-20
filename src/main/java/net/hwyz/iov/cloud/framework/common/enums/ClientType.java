package net.hwyz.iov.cloud.framework.common.enums;

/**
 * 客户端类型枚举类
 * <p>
 * IOV-TSP-DSN-CR-017：车辆侧车机（IVI）客户端类型由历史 {@code IDCM} 改为 {@code IVI}，
 * 与 CIAM 令牌 {@code client_type} 声明、SGW 车辆类型白名单保持一致。
 *
 * @author hwyz_leo
 */
public enum ClientType {

    /** 手机 **/
    MOBILE,
    /** 车载信息娱乐系统（车机） **/
    IVI,
    /** 智驾域控制模块 **/
    ADCM,
    /** 车联终端模块 **/
    TBOX,
    /** 中央计算平台 **/
    CCP,
    /** 后台管理门户 **/
    MPT;

}
