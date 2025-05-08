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

    /** 安全气囊控制单元 Airbag Control Unit **/
    ACU,
    /** 自动驾驶域控模块 Autopilot Domain Control Module **/
    ADCM,
    /** 功放 Amplifier **/
    AMP,
    /** 左后航空座椅控制模块 Aero Seat Control Module Rear Left **/
    ASCM_RL,
    /** 右后航空座椅控制模块 Aero Seat Control Module Rear Right **/
    ASCM_RR,
    /** 自动温度控制器 Automatic Temperature Controller **/
    ATC,
    /** 电动车低速提示音系统 Acoustic Vehicle Alert System **/
    AVAS,
    /** 高压电池管理系统 Battery Management system **/
    BMS,
    /** 蓝牙模块 Bluetooth Module **/
    BTM,
    /** 中央计算平台 Center Compute Platform **/
    CCP,
    /** 连续减震控制系统 Continuous Damping Control System **/
    CDC,
    /** 发动机管理系统 Engine Management System **/
    EMS,
    /** 电子助力转向 Electronic Power Steering **/
    EPS,
    /** 电子稳定控制 Electronic Stability Control **/
    ESC,
    /** 前电机控制器 Front Motor Controller **/
    FMC,
    /** 发电机控制器 Generator Control Unit **/
    GCU,
    /** 左前大灯控制模块 Headlamp Control Module Left **/
    HCM_L,
    /** 右前大灯控制模块 Headlamp Control Module Right **/
    HCM_R,
    /** 电动机械助力器 iBooster **/
    IB,
    /** 集成式车身控制模块 Integrated Body Control Module **/
    IBCM,
    /** 智能蓄电池管理系统 Intelligent Battery System **/
    IBS,
    /** 信息娱乐模块 Infotainment Domain Control Module **/
    IDCM,
    /** 动力域控制模块 Mobility Domain Control Module **/
    MDCM,
    /** 前中程雷达 Mid Range Radar **/
    MRR_F,
    /** 高压能量管理系统 Power Management System **/
    PMS,
    /** 后电机控制器 Rear Motor Controller **/
    RMC,
    /** 左侧座椅控制模块 Seat Control Module Left **/
    SCM_L,
    /** 右侧座椅控制模块 Seat Control Module Right **/
    SCM_R,
    /** 挡位控制单元 Shifter Control Unit **/
    SCU,
    /** 左前毫米波雷达 Short Range Radar Front Left **/
    SRR_FL,
    /** 右前毫米波雷达 Short Range Radar Front Right **/
    SRR_FR,
    /** 左后毫米波雷达 Short Range Radar Rear Left **/
    SRR_RL,
    /** 右后毫米波雷达 Short Range Radar Rear Right **/
    SRR_RR,
    /** 车联终端 Telematics Box **/
    TBOX,
    /** 左侧后尾灯控制模块 Tail Lamp Control Module Left **/
    TLCM_L,
    /** 右侧后尾灯控制模块 Tail Lamp Control Module Right **/
    TLCM_R,
    /** 左侧无线充电模块 Wireless Charging Module Left **/
    WCM_L,
    /** 右侧无线充电模块 Wireless Charging Module Right **/
    WCM_R;

    public static EcuType valOf(String val) {
        return Arrays.stream(EcuType.values())
                .filter(ecuType -> ecuType.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}
