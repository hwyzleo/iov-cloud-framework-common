package net.hwyz.iov.cloud.framework.common.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ExplicitTimeUtil} 新 API 语义测试（FW-COMMON-DSN-CR-001，TC-01/TC-04/TC-05/TC-06）。
 * <p>
 * 覆盖：Clock 注入、显式 ZoneId 转换、严格 ISO 解析矩阵、DST gap/overlap 显式消歧、
 * 固定 epoch 毫秒单位与精度、null/异常显式失败（无兜底）。
 */
class ExplicitTimeUtilTest {

    private static final Instant FIXED = Instant.parse("2026-10-28T04:43:32Z");
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED, UTC);

    // ---------------- nowInstant / Clock ----------------

    @Test
    void nowInstant_固定Clock返回同一时刻且不读JVM默认时区() {
        assertThat(ExplicitTimeUtil.nowInstant(FIXED_CLOCK)).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.nowInstant(Clock.fixed(FIXED, ZoneId.of("America/New_York"))))
                .isEqualTo(FIXED); // Clock 携带的地区不改变 instant 含义
        assertThat(ExplicitTimeUtil.nowInstant()).isNotNull();
        assertThatThrownBy(() -> ExplicitTimeUtil.nowInstant(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ---------------- 显式 ZoneId 转换 ----------------

    @Test
    void toLocalDateTime_显式地区墙钟不截断精度() {
        assertThat(ExplicitTimeUtil.toLocalDateTime(FIXED, SHANGHAI))
                .isEqualTo(LocalDateTime.parse("2026-10-28T12:43:32"));
        assertThat(ExplicitTimeUtil.toLocalDateTime(FIXED, UTC))
                .isEqualTo(LocalDateTime.parse("2026-10-28T04:43:32"));
        Instant nano = Instant.parse("2026-10-28T04:43:32.123456789Z");
        assertThat(ExplicitTimeUtil.toLocalDateTime(nano, UTC))
                .isEqualTo(LocalDateTime.parse("2026-10-28T04:43:32.123456789"));
        assertThatThrownBy(() -> ExplicitTimeUtil.toLocalDateTime(null, SHANGHAI))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.toLocalDateTime(FIXED, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toOffsetDateTime_显式地区offset保持同一Instant() {
        OffsetDateTime shanghai = ExplicitTimeUtil.toOffsetDateTime(FIXED, SHANGHAI);
        assertThat(shanghai).isEqualTo(OffsetDateTime.parse("2026-10-28T12:43:32+08:00"));
        assertThat(shanghai.toInstant()).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.toOffsetDateTime(FIXED, UTC))
                .isEqualTo(OffsetDateTime.parse("2026-10-28T04:43:32Z"));
        assertThatThrownBy(() -> ExplicitTimeUtil.toOffsetDateTime(null, SHANGHAI))
                .isInstanceOf(NullPointerException.class);
    }

    // ---------------- DST 消歧 ----------------

    @Test
    void resolveLocalDateTime_唯一有效offset成功() {
        // Berlin 夏令时常规时刻：唯一 +02:00
        Instant resolved = ExplicitTimeUtil.resolveLocalDateTime(
                LocalDateTime.parse("2026-06-01T12:00:00"), BERLIN);
        assertThat(resolved).isEqualTo(Instant.parse("2026-06-01T10:00:00Z"));
        // Asia/Shanghai 无 DST，唯一 +08:00
        assertThat(ExplicitTimeUtil.resolveLocalDateTime(
                LocalDateTime.parse("2026-10-28T12:43:32"), SHANGHAI)).isEqualTo(FIXED);
    }

    @Test
    void resolveLocalDateTime_gap拒绝不自动平移() {
        // Berlin 2026-03-29 02:30 处于 DST gap（02:00 → 03:00）
        LocalDateTime gap = LocalDateTime.parse("2026-03-29T02:30:00");
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(gap, BERLIN))
                .isInstanceOf(DateTimeException.class)
                .hasMessageContaining("无唯一有效 offset");
    }

    @Test
    void resolveLocalDateTime_overlap无选择拒绝() {
        // Berlin 2026-10-25 02:30 处于 overlap（+02:00 与 +01:00 均有效）
        LocalDateTime overlap = LocalDateTime.parse("2026-10-25T02:30:00");
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(overlap, BERLIN))
                .isInstanceOf(DateTimeException.class)
                .hasMessageContaining("无唯一有效 offset");
    }

    @Test
    void resolveLocalDateTime_显式有效offset成功且相差一小时() {
        LocalDateTime overlap = LocalDateTime.parse("2026-10-25T02:30:00");
        Instant withSummer = ExplicitTimeUtil.resolveLocalDateTime(overlap, BERLIN, ZoneOffset.ofHours(2));
        Instant withWinter = ExplicitTimeUtil.resolveLocalDateTime(overlap, BERLIN, ZoneOffset.ofHours(1));
        assertThat(withSummer).isEqualTo(Instant.parse("2026-10-25T00:30:00Z"));
        assertThat(withWinter).isEqualTo(Instant.parse("2026-10-25T01:30:00Z"));
        assertThat(withSummer).isNotEqualTo(withWinter);
    }

    @Test
    void resolveLocalDateTime_非法offset拒绝_gap始终拒绝() {
        LocalDateTime overlap = LocalDateTime.parse("2026-10-25T02:30:00");
        // 不属于有效集合的 offset
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(overlap, BERLIN, ZoneOffset.UTC))
                .isInstanceOf(DateTimeException.class)
                .hasMessageContaining("不属于");
        // gap 即使显式选择任意 offset 也拒绝
        LocalDateTime gap = LocalDateTime.parse("2026-03-29T02:30:00");
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(gap, BERLIN, ZoneOffset.ofHours(2)))
                .isInstanceOf(DateTimeException.class);
        // null
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(overlap, BERLIN, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(null, BERLIN, ZoneOffset.ofHours(2)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(overlap, null, ZoneOffset.ofHours(2)))
                .isInstanceOf(NullPointerException.class);
    }

    // ---------------- 固定 epoch 毫秒 ----------------

    @Test
    void fromEpochMillis_固定毫秒单位不猜秒() {
        assertThat(ExplicitTimeUtil.fromEpochMillis(0)).isEqualTo(Instant.parse("1970-01-01T00:00:00Z"));
        assertThat(ExplicitTimeUtil.fromEpochMillis(1793162612000L)).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.fromEpochMillis(-1L)).isEqualTo(Instant.parse("1969-12-31T23:59:59.999Z"));
    }

    @Test
    void toEpochMillis_JDK语义且溢出拒绝() {
        assertThat(ExplicitTimeUtil.toEpochMillis(FIXED)).isEqualTo(1793162612000L);
        assertThat(ExplicitTimeUtil.toEpochMillis(Instant.parse("2026-10-28T04:43:32.123456789Z")))
                .isEqualTo(1793162612123L); // 亚毫秒按 JDK 既定算法截断到毫秒
        assertThatThrownBy(() -> ExplicitTimeUtil.toEpochMillis(Instant.MAX))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.toEpochMillis(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ---------------- ISO 格式/解析 ----------------

    @Test
    void formatInstantIso_带Z且小数秒不强制点000() {
        assertThat(ExplicitTimeUtil.formatInstantIso(FIXED)).isEqualTo("2026-10-28T04:43:32Z");
        assertThat(ExplicitTimeUtil.formatInstantIso(Instant.parse("2026-10-28T04:43:32.123456789Z")))
                .isEqualTo("2026-10-28T04:43:32.123456789Z");
        assertThatThrownBy(() -> ExplicitTimeUtil.formatInstantIso(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void parseInstantIso_严格offset完整消费() {
        assertThat(ExplicitTimeUtil.parseInstantIso("2026-10-28T04:43:32Z")).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.parseInstantIso("2026-10-28T12:43:32+08:00")).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.parseInstantIso("2026-10-28T04:43:32.123456789Z"))
                .isEqualTo(Instant.parse("2026-10-28T04:43:32.123456789Z"));
        // 无 offset 拒绝
        assertThatThrownBy(() -> ExplicitTimeUtil.parseInstantIso("2026-10-28T04:43:32"))
                .isInstanceOf(DateTimeParseException.class);
        // 空/空白/尾随/前导/非法日期拒绝，不 trim
        assertThatThrownBy(() -> ExplicitTimeUtil.parseInstantIso(""))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseInstantIso(" "))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseInstantIso("2026-10-28T04:43:32Z "))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseInstantIso(" 2026-10-28T04:43:32Z"))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseInstantIso("2026-02-30T04:43:32Z"))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseInstantIso(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void formatWallClock_显式地区秒精度展示() {
        assertThat(ExplicitTimeUtil.formatWallClock(FIXED, SHANGHAI)).isEqualTo("2026-10-28 12:43:32");
        assertThat(ExplicitTimeUtil.formatWallClock(FIXED, UTC)).isEqualTo("2026-10-28 04:43:32");
        assertThatThrownBy(() -> ExplicitTimeUtil.formatWallClock(null, SHANGHAI))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.formatWallClock(FIXED, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void parseLocalDateTimeIso_严格墙钟拒绝offset与非法() {
        assertThat(ExplicitTimeUtil.parseLocalDateTimeIso("2026-10-28T12:43:32"))
                .isEqualTo(LocalDateTime.parse("2026-10-28T12:43:32"));
        assertThat(ExplicitTimeUtil.parseLocalDateTimeIso("2026-10-28T04:43:32.123456789"))
                .isEqualTo(LocalDateTime.parse("2026-10-28T04:43:32.123456789"));
        // 带 offset（绝对时刻语义）拒绝，不静默丢弃
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateTimeIso("2026-10-28T12:43:32+08:00"))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateTimeIso("2026-10-28T12:43:32Z"))
                .isInstanceOf(DateTimeParseException.class);
        // 非法日期/尾随/空白拒绝
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateTimeIso("2026-02-30T10:00:00"))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateTimeIso("2026-10-28T12:43:32 "))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateTimeIso(" "))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateTimeIso(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void parseLocalDateIso_严格纯日期拒绝日期时间不截断() {
        assertThat(ExplicitTimeUtil.parseLocalDateIso("2026-10-28"))
                .isEqualTo(LocalDate.parse("2026-10-28"));
        // 日期时间输入拒绝，不静默截断到日期
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateIso("2026-10-28T00:00:00"))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateIso("2026-10-28 00:00:00"))
                .isInstanceOf(DateTimeParseException.class);
        // 非法日期/尾随/空白拒绝
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateIso("2026-02-29"))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateIso("2026-04-31"))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateIso("2026-10-28 "))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateIso(null))
                .isInstanceOf(NullPointerException.class);
    }
}
