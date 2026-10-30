package net.hwyz.iov.cloud.framework.common.util;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Objects;

/**
 * 显式时间工具（FW-COMMON-DSN-CR-001，RD-001/RD-003/RD-004/RD-005）。
 * <p>
 * 与 {@link DateTimeUtil}（冻结的兼容层，继承 Hutool、隐含 Asia/Shanghai 解释）不同，
 * 本工具提供 Instant-first、显式 {@link ZoneId}、可注入 {@link Clock} 与严格解析的
 * 无歧义入口：
 * <ul>
 *   <li>事件时刻一律以 {@link Instant} 表达，不读取 JVM 默认时区，不隐含业务地区；</li>
 *   <li>墙钟/offset 转换必须显式传入 {@link ZoneId}，不使用 systemDefault 或全局业务区默认值；</li>
 *   <li>解析严格且完整消费：无 offset 的事件 ISO、尾随字符、非法日期、墙钟混入 offset、
 *       纯日期混入日期时间一律拒绝，不静默截断或猜测；</li>
 *   <li>DST gap 拒绝；overlap 必须显式选择属于该区该时刻有效集合的 offset；</li>
 *   <li>epoch 固定毫秒单位，不按位数猜测；精度默认不裁剪（亚毫秒按 JDK 既定语义，见
 *       {@link #toEpochMillis(Instant)}）。</li>
 * </ul>
 * 本工具为纯 JDK 静态工具：无 Spring 自动配置、无全局状态、无可变 Clock 单例、无配置键，
 * 不新增 HTTP 端点、Jackson 模块、TypeHandler 或签名/canonical 算法。
 * <p>
 * 异常约定（均为显式失败，无兜底）：引用参数 null → {@link NullPointerException}；
 * 空/空白/非法/附带尾随字符字符串 → {@link java.time.format.DateTimeParseException}；
 * DST/offset 不匹配 → {@link DateTimeException}；epoch 溢出 → {@link ArithmeticException}。
 * 不返回当前时间、epoch 0、默认地区或猜测结果。
 *
 * @author hwyz_leo
 * @since 2026-10-30
 */
public final class ExplicitTimeUtil {

    private ExplicitTimeUtil() {
    }

    /** 展示用墙钟格式（秒精度，仅用于已明确地区的展示，不用于无损存储或 wire）。 */
    private static final DateTimeFormatter WALL_CLOCK_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    /** 严格 ISO_LOCAL_DATE_TIME（STRICT：非法日期/字段拒绝；offset 输入拒绝）。 */
    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME_STRICT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);

    /** 严格 ISO_LOCAL_DATE（STRICT：非法日期拒绝；日期时间输入拒绝）。 */
    private static final DateTimeFormatter ISO_LOCAL_DATE_STRICT =
            DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT);

    /**
     * 获取当前绝对时刻。
     * <p>
     * 委托 UTC {@link Clock} 入口，不读取 JVM 默认地区；结果与 JVM 时区无关。
     *
     * @return 当前 {@link Instant}
     */
    public static Instant nowInstant() {
        return Instant.now(Clock.systemUTC());
    }

    /**
     * 获取指定 {@link Clock} 的当前绝对时刻（可注入固定 Clock 测试）。
     *
     * @param clock Clock，非 null
     * @return clock 的 {@link Instant}，保留 Clock 精度；Clock 携带的地区不改变其 instant 含义
     * @throws NullPointerException clock 为 null
     */
    public static Instant nowInstant(Clock clock) {
        return Objects.requireNonNull(clock, "clock").instant();
    }

    /**
     * 将绝对时刻转换为指定地区的墙钟时间，不截断精度。
     *
     * @param instant 绝对时刻，非 null
     * @param zone    业务地区，非 null
     * @return 该地区的墙钟时间
     * @throws NullPointerException 任一参数为 null
     */
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zone) {
        return LocalDateTime.ofInstant(Objects.requireNonNull(instant, "instant"),
                Objects.requireNonNull(zone, "zone"));
    }

    /**
     * 将绝对时刻转换为指定地区对应的 {@link OffsetDateTime}，保持原 {@link Instant}。
     * <p>
     * 不默认 +08:00；offset 由 {@code zone} 在此时刻的规则决定。
     *
     * @param instant 绝对时刻，非 null
     * @param zone    业务地区，非 null
     * @return 该地区的 OffsetDateTime（与入参 instant 同一时刻）
     * @throws NullPointerException 任一参数为 null
     */
    public static OffsetDateTime toOffsetDateTime(Instant instant, ZoneId zone) {
        return Objects.requireNonNull(instant, "instant")
                .atZone(Objects.requireNonNull(zone, "zone"))
                .toOffsetDateTime();
    }

    /**
     * 将地区墙钟时间解析为绝对时刻，仅当该墙钟在该地区具有唯一有效 offset 时成功。
     * <p>
     * DST gap（0 个有效 offset）与 overlap（2 个有效 offset 且未选择）一律拒绝，
     * 不自动平移 gap、不默认选择较早/较晚 offset。
     *
     * @param local 地区墙钟时间，非 null
     * @param zone  业务地区，非 null
     * @return 唯一有效 offset 对应的绝对时刻
     * @throws NullPointerException 任一参数为 null
     * @throws DateTimeException    墙钟处于 DST gap 或 overlap（无唯一 offset）
     */
    public static Instant resolveLocalDateTime(LocalDateTime local, ZoneId zone) {
        Objects.requireNonNull(local, "local");
        Objects.requireNonNull(zone, "zone");
        List<ZoneOffset> validOffsets = zone.getRules().getValidOffsets(local);
        if (validOffsets.size() != 1) {
            throw new DateTimeException(
                    "当地墙钟 " + local + " 在地区 " + zone + " 无唯一有效 offset（有效数=" + validOffsets.size()
                            + "，gap 或 overlap 需显式选择有效 offset）");
        }
        return local.toInstant(validOffsets.iterator().next());
    }

    /**
     * 将地区墙钟时间解析为绝对时刻，使用调用方显式选择的 offset。
     * <p>
     * {@code selectedOffset} 必须属于该墙钟在该地区的有效 offset 集合；gap（0 个有效
     * offset）始终拒绝，即使传入任意 offset。
     *
     * @param local         地区墙钟时间，非 null
     * @param zone          业务地区，非 null
     * @param selectedOffset 调用方选择的 offset，非 null
     * @return 选择 offset 对应的绝对时刻
     * @throws NullPointerException 任一参数为 null
     * @throws DateTimeException    墙钟处于 DST gap，或 selectedOffset 不属于该时刻有效集合
     */
    public static Instant resolveLocalDateTime(LocalDateTime local, ZoneId zone, ZoneOffset selectedOffset) {
        Objects.requireNonNull(local, "local");
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(selectedOffset, "selectedOffset");
        List<ZoneOffset> validOffsets = zone.getRules().getValidOffsets(local);
        if (!validOffsets.contains(selectedOffset)) {
            throw new DateTimeException(
                    "选择的 offset " + selectedOffset + " 不属于墙钟 " + local + " 在地区 " + zone
                            + " 的有效集合 " + validOffsets + "（gap 或无效 offset 拒绝）");
        }
        return local.toInstant(selectedOffset);
    }

    /**
     * 固定 epoch 毫秒 → 绝对时刻。
     * <p>
     * 直接构造 Instant，不按位数猜测秒/毫秒，不映射任何地区墙钟。
     *
     * @param epochMillis epoch 毫秒
     * @return 对应 {@link Instant}
     */
    public static Instant fromEpochMillis(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }

    /**
     * 绝对时刻 → epoch 毫秒，遵循 JDK {@link Instant#toEpochMilli()} 语义：
     * 亚毫秒余量按 JDK 既定算法（截断到毫秒），结果超出 long 可表示范围时抛
     * {@link ArithmeticException}。不全局降精度、不改变调用方签名或 digest 输入。
     *
     * @param instant 绝对时刻，非 null
     * @return epoch 毫秒
     * @throws NullPointerException instant 为 null
     * @throws ArithmeticException  结果溢出
     */
    public static long toEpochMillis(Instant instant) {
        return Objects.requireNonNull(instant, "instant").toEpochMilli();
    }

    /**
     * 标准 Instant ISO-8601 字符串（含 {@code Z}），小数秒按实际值保留、不强制 {@code .000}。
     * <p>
     * 仅作为可读/展示表示，不作为新的 canonical/digest 规范化算法。
     *
     * @param instant 绝对时刻，非 null
     * @return 如 {@code 2026-10-28T04:43:32Z} 或 {@code 2026-10-28T04:43:32.123456789Z}
     * @throws NullPointerException instant 为 null
     */
    public static String formatInstantIso(Instant instant) {
        return Objects.requireNonNull(instant, "instant").toString();
    }

    /**
     * 严格解析事件 ISO-8601 字符串为绝对时刻。
     * <p>
     * 必须含 {@code Z} 或数值 offset（如 {@code +08:00}），完整消费输入；
     * 无 offset、空/空白、非法或附带尾随字符一律拒绝（不 trim、不猜测）。
     *
     * @param text 事件 ISO 字符串，非 null
     * @return 对应 {@link Instant}
     * @throws NullPointerException                                      text 为 null
     * @throws java.time.format.DateTimeParseException                   非法/无 offset/未完整消费
     */
    public static Instant parseInstantIso(String text) {
        Objects.requireNonNull(text, "text");
        return Instant.parse(text);
    }

    /**
     * 显式地区的墙钟展示文本（{@code uuuu-MM-dd HH:mm:ss}，秒精度）。
     * <p>
     * 秒精度是该展示方法的显式契约，不用于无损存储或 wire；无损表示请使用
     * {@link #formatInstantIso(Instant)} 或保留 {@link Instant}。
     *
     * @param instant 绝对时刻，非 null
     * @param zone    展示地区，非 null
     * @return 该地区墙钟展示文本
     * @throws NullPointerException 任一参数为 null
     */
    public static String formatWallClock(Instant instant, ZoneId zone) {
        return WALL_CLOCK_FORMATTER.withZone(Objects.requireNonNull(zone, "zone"))
                .format(Objects.requireNonNull(instant, "instant"));
    }

    /**
     * 严格解析 ISO_LOCAL_DATE_TIME 墙钟字符串。
     * <p>
     * 完整消费、保留合法小数精度（最多纳秒）；带 offset（如 {@code +08:00} 或 {@code Z}）
     * 的字符串属于绝对时刻语义，拒绝并提示，不静默丢弃 offset。
     *
     * @param text 无 offset 的 ISO 墙钟字符串，非 null
     * @return 对应 {@link LocalDateTime}
     * @throws NullPointerException                                    text 为 null
     * @throws java.time.format.DateTimeParseException                 非法/带 offset/未完整消费
     */
    public static LocalDateTime parseLocalDateTimeIso(String text) {
        Objects.requireNonNull(text, "text");
        return LocalDateTime.parse(text, ISO_LOCAL_DATE_TIME_STRICT);
    }

    /**
     * 严格解析 ISO_LOCAL_DATE 纯日期字符串。
     * <p>
     * 完整消费；日期时间输入（如 {@code 2026-10-28T00:00:00}）拒绝，不静默截断到日期。
     *
     * @param text 纯日期字符串，非 null
     * @return 对应 {@link LocalDate}
     * @throws NullPointerException                                    text 为 null
     * @throws java.time.format.DateTimeParseException                 非法/日期时间输入/未完整消费
     */
    public static LocalDate parseLocalDateIso(String text) {
        Objects.requireNonNull(text, "text");
        return LocalDate.parse(text, ISO_LOCAL_DATE_STRICT);
    }
}
