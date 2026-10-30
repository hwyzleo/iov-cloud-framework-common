package net.hwyz.iov.cloud.framework.common.util;

import cn.hutool.core.date.DateUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类（冻结的兼容层，FW-COMMON-DSN-CR-001）。
 * <p>
 * 本类继承 {@link cn.hutool.core.date.DateUtil} 并保留自定义方法的既有签名与行为，
 * 作为既有调用的兼容入口，不因时间治理改变任何旧行为或删除继承关系。
 * 新增显式、无歧义的时间语义请使用 {@link ExplicitTimeUtil}（Instant-first、
 * 显式 {@link java.time.ZoneId}、可注入 {@link java.time.Clock}、严格解析）。
 * <p>
 * 已知风险：本类中继承自 Hutool 的方法（如 {@code now()}、{@code format(Date, pattern)}、
 * {@code parseDate(...)}）按 JVM 默认时区解释，而自定义方法隐含 Asia/Shanghai；
 * 两者仅在 JVM 默认时区为 Asia/Shanghai 时结果一致。接入 UTC 环境前须逐调用点核验。
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
     *
     * @deprecated 隐含 Asia/Shanghai 业务地区解释（FW-COMMON-DSN-CR-001，RD-002）。
     * 事件时刻请使用 {@link ExplicitTimeUtil#nowInstant()}；需要显式地区墙钟请使用
     * {@link ExplicitTimeUtil#toLocalDateTime(java.time.Instant, java.time.ZoneId)}。
     * 本方法行为与签名保持不变，仅做弃用标注，无自动删除期限。
     */
    @Deprecated(forRemoval = false)
    public static LocalDateTime getNowLocalDateTime() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    /**
     * 获取当前时间（默认时区）
     *
     * @deprecated 隐含 Asia/Shanghai 业务地区解释（FW-COMMON-DSN-CR-001，RD-002）。
     * 事件时刻请使用 {@link ExplicitTimeUtil#nowInstant()}；需要显式地区 offset 表示请使用
     * {@link ExplicitTimeUtil#toOffsetDateTime(java.time.Instant, java.time.ZoneId)}。
     * 本方法行为与签名保持不变，仅做弃用标注，无自动删除期限。
     */
    @Deprecated(forRemoval = false)
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
     *
     * @deprecated 隐含 Asia/Shanghai 业务地区解释（FW-COMMON-DSN-CR-001，RD-002）。
     * 请使用 {@link ExplicitTimeUtil#toOffsetDateTime(java.time.Instant, java.time.ZoneId)}
     * 显式传入业务地区。本方法行为与签名保持不变，仅做弃用标注，无自动删除期限。
     */
    @Deprecated(forRemoval = false)
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
     * @deprecated 隐含 Asia/Shanghai 业务地区解释（FW-COMMON-DSN-CR-001，RD-002）。
     * 展示请使用 {@link ExplicitTimeUtil#formatWallClock(java.time.Instant, java.time.ZoneId)}
     * 显式传入展示地区；无损表示请使用 {@link ExplicitTimeUtil#formatInstantIso(java.time.Instant)}。
     * 本方法行为与签名保持不变，仅做弃用标注，无自动删除期限。
     */
    @Deprecated(forRemoval = false)
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
     *
     * @deprecated 隐含 Asia/Shanghai 业务地区解释（FW-COMMON-DSN-CR-001，RD-002）。
     * 固定 epoch 毫秒 → 绝对时刻请使用 {@link ExplicitTimeUtil#fromEpochMillis(long)}；
     * 需要地区墙钟请配合 {@link ExplicitTimeUtil#toLocalDateTime(java.time.Instant, java.time.ZoneId)}。
     * 本方法行为与签名保持不变，仅做弃用标注，无自动删除期限。
     */
    @Deprecated(forRemoval = false)
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(DEFAULT_ZONE).toLocalDateTime();
    }

    /**
     * LocalDateTime 转毫秒时间戳
     *
     * @deprecated 隐含 Asia/Shanghai 业务地区解释（FW-COMMON-DSN-CR-001，RD-002）。
     * 绝对时刻 → 固定 epoch 毫秒请使用 {@link ExplicitTimeUtil#toEpochMillis(java.time.Instant)}。
     * 本方法行为与签名保持不变，仅做弃用标注，无自动删除期限。
     */
    @Deprecated(forRemoval = false)
    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    public static void main(String[] args) {
        System.out.println(cn.hutool.core.date.DateUtil.formatDate(new Date()));
    }
}
