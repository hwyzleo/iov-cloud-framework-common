package net.hwyz.iov.cloud.framework.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * 展示时间工具（FW-COMMON-DSN-CR-002，RD-CTZ-002）。
 * <p>
 * 在 {@link ExplicitTimeUtil}（CR-001，显式 ZoneId、严格解析、DST gap 拒绝/overlap
 * 显式 offset）之上的薄封装，仅补充「数据参数 null → null」的展示友好语义；
 * 不复制第二套日期解析器，不改 ExplicitTimeUtil 的 null→NPE、精度、严格解析与
 * gap/overlap 政策。
 * <p>
 * 契约：
 * <ul>
 *   <li>zone 参数先 {@code requireNonNull}（即使数据为 null 也先校验 zone，zone null → NPE）；</li>
 *   <li>数据参数 null → 返回 null；非 null 数据委托 ExplicitTimeUtil，保留精度；</li>
 *   <li>{@link #format(Instant, ZoneId)} 输出 {@code uuuu-MM-dd HH:mm:ss} 秒精度展示文本，
 *       仅用于展示，不得从该文本重建摘要、入库时刻或安全截止时间；</li>
 *   <li>{@link #utcWallClockToLocal(LocalDateTime, ZoneId)} 仅接受已确认 UTC DATETIME
 *       墙钟，未知来源时区禁止调用。</li>
 * </ul>
 * 本工具不读取 {@code RequestTimeZoneContext}；effective zone 由 WEB facade 解析后显式传入。
 *
 * @author hwyz_leo
 * @since 2026-10-31
 */
public final class DisplayTimeUtil {

    private DisplayTimeUtil() {
    }

    /**
     * 绝对时刻 → 指定地区墙钟时间（保留精度）。
     *
     * @param value Instant，null 返回 null
     * @param zone  展示地区，非 null
     * @return 该地区墙钟时间；value 为 null 返回 null
     * @throws NullPointerException zone 为 null（即使 value 为 null）
     */
    public static LocalDateTime toLocalDateTime(Instant value, ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        if (value == null) {
            return null;
        }
        return ExplicitTimeUtil.toLocalDateTime(value, zone);
    }

    /**
     * 绝对时刻 → 指定地区墙钟展示文本（{@code uuuu-MM-dd HH:mm:ss} 秒精度，仅展示用）。
     *
     * @param value Instant，null 返回 null
     * @param zone  展示地区，非 null
     * @return 展示文本；value 为 null 返回 null
     * @throws NullPointerException zone 为 null（即使 value 为 null）
     */
    public static String format(Instant value, ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        if (value == null) {
            return null;
        }
        return ExplicitTimeUtil.formatWallClock(value, zone);
    }

    /**
     * 已确认 UTC DATETIME 墙钟 → 指定地区墙钟（保留精度）。
     * <p>
     * 仅接受语义确认为 UTC 存储的 {@link LocalDateTime}：先按 {@link ZoneOffset#UTC}
     * 严格解析为绝对时刻（gap/overlap 政策同 ExplicitTimeUtil，UTC 恒唯一），再转换到
     * 目标地区。未知来源时区（如业务上海墙上时间误读为 UTC）禁止调用本方法。
     *
     * @param value UTC 墙钟时间，null 返回 null
     * @param zone  展示地区，非 null
     * @return 目标地区墙钟时间；value 为 null 返回 null
     * @throws NullPointerException zone 为 null（即使 value 为 null）
     */
    public static LocalDateTime utcWallClockToLocal(LocalDateTime value, ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        if (value == null) {
            return null;
        }
        Instant instant = ExplicitTimeUtil.resolveLocalDateTime(value, ZoneOffset.UTC);
        return ExplicitTimeUtil.toLocalDateTime(instant, zone);
    }
}
