package net.hwyz.iov.cloud.framework.common.util;

import java.util.Date;

/**
 * 日期时间工具类
 *
 * @author hwyz_leo
 */
public class DateUtil extends cn.hutool.core.date.DateUtil {

    /**
     * 获取当前日期对象
     *
     * @return 当前日期对象
     */
    public static Date getNowDate() {
        return new Date();
    }

    public static void main(String[] args) {
        System.out.println(cn.hutool.core.date.DateUtil.formatDate(new Date()));
    }
}
