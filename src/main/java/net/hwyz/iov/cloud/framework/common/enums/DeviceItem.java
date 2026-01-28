package net.hwyz.iov.cloud.framework.common.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 设备项枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum DeviceItem {

    ACU("安全气囊控制单元", "Airbag Control Unit"),
    ADCM("自动驾驶域控模块", "Autopilot Domain Control Module"),
    AMP("功放", "Amplifier"),
    ASCM("航空座椅控制模块", "Aero Seat Control Module"),
    ATC("自动温度控制器", "Automatic Temperature Controller"),
    AVAS("电动车低速提示音系统", "Acoustic Vehicle Alert System"),
    BMS("高压电池管理系统", "Battery Management system"),
    BTM("蓝牙模块", "Bluetooth Module"),
    CCP("中央计算平台", "Center Compute Platform"),
    CDC("连续减震控制系统", "Continuous Damping Control System"),
    EMS("发动机管理系统", "Engine Management System"),
    EPS("电子助力转向", "Electronic Power Steering"),
    ESC("电子稳定控制", "Electronic Stability Control"),
    MC("电机控制器", "Motor Controller"),
    GCU("发电机控制器", "Generator Control Unit"),
    HCM("前大灯控制模块", "Headlamp Control Module"),
    IB("电动机械助力器", "iBooster"),
    IBCM("集成式车身控制模块", "Integrated Body Control Module"),
    IBS("智能蓄电池管理系统", "Intelligent Battery System"),
    IDCM("信息娱乐功能模块", "Infotainment Domain Control Module"),
    MDCM("动力域控制模块", "Mobility Domain Control Module"),
    MRR("中程毫米波雷达", "Mid Range Radar"),
    PMS("高压能量管理系统", "Power Management System"),
    SCM("座椅控制模块", "Seat Control Module"),
    SCU("挡位控制单元", "Shifter Control Unit"),
    SRR("短程毫米波雷达", "Short Range Radar"),
    TBOX("车联终端", "Telematics Box"),
    TLCM("后尾灯控制模块", "Tail Lamp Control Module"),
    WCM("无线充电模块", "Wireless Charging Module");

    public final String label;
    public final String labelEn;

    public static DeviceItem valOf(String val) {
        return Arrays.stream(DeviceItem.values())
                .filter(deviceItem -> deviceItem.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}
