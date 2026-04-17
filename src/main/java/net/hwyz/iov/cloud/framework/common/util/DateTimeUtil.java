package net.hwyz.iov.cloud.framework.common.util;

import cn.hutool.core.date.DateUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类
 *
 * @author hwyz_leo
 */
public class DateTimeUtil extends DateUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 获取当前日期对象
     *
     * @return 当前日期对象
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前时间（默认时区）
     */
    public static LocalDateTime getNowLocalDateTime() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    /**
     * 获取当前时间（默认时区）
     */
    public static OffsetDateTime getNowOffsetDateTime() {
        return OffsetDateTime.now(DEFAULT_ZONE);
    }

    /**
     * 获取当前时间
     */
    public static Instant getNowInstant() {
        return Instant.now();
    }

    /**
     * Instant 转 OffsetDateTime
     */
    public static OffsetDateTime instantToOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atZone(DEFAULT_ZONE).toOffsetDateTime();
    }

    /**
     * 格式化为 yyyy-MM-dd HH:mm:ss
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * 格式化为 yyyy-MM-dd HH:mm:ss
     *
     * @param instant Instant
     * @return String
     */
    public static String format(Instant instant) {
        return instant == null ? null : instant.atZone(DEFAULT_ZONE).toLocalDateTime().format(DEFAULT_FORMATTER);
    }

    /**
     * 解析 yyyy-MM-dd HH:mm:ss 格式字符串
     */
    public static LocalDateTime parse(String text) {
        return text == null ? null : LocalDateTime.parse(text, DEFAULT_FORMATTER);
    }

    /**
     * 毫秒时间戳转 LocalDateTime
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(DEFAULT_ZONE).toLocalDateTime();
    }

    /**
     * LocalDateTime 转毫秒时间戳
     */
    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    public static void main(String[] args) {
        System.out.println(cn.hutool.core.date.DateUtil.formatDate(new Date()));
    }
}
