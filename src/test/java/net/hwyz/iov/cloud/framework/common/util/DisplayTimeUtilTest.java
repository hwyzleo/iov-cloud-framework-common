package net.hwyz.iov.cloud.framework.common.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 展示时间工具单测（FW-COMMON-DSN-CR-002，T-CTZ-02）。
 * <p>
 * 覆盖：Instant→上海/Berlin/offset、UTC 墙钟转换、DST 边界、纳秒保留、秒级展示格式、
 * 数据 null × zone null 矩阵；旧 ExplicitTimeUtil 严格 null/DST 契约不受影响。
 */
class DisplayTimeUtilTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");
    private static final Instant SAMPLE = Instant.parse("2026-10-28T04:43:32Z");

    @Test
    void toLocalDateTime保留精度且地区正确() {
        Instant instant = Instant.parse("2026-10-28T04:43:32.123456789Z");
        assertThat(DisplayTimeUtil.toLocalDateTime(instant, SHANGHAI))
                .isEqualTo(LocalDateTime.of(2026, 10, 28, 12, 43, 32, 123_456_789));
        assertThat(DisplayTimeUtil.toLocalDateTime(instant, BERLIN))
                .isEqualTo(LocalDateTime.of(2026, 10, 28, 5, 43, 32, 123_456_789));
    }

    @Test
    void format输出秒精度展示文本() {
        assertThat(DisplayTimeUtil.format(SAMPLE, SHANGHAI)).isEqualTo("2026-10-28 12:43:32");
        assertThat(DisplayTimeUtil.format(SAMPLE, BERLIN)).isEqualTo("2026-10-28 05:43:32");
        assertThat(DisplayTimeUtil.format(SAMPLE, ZoneId.of("UTC"))).isEqualTo("2026-10-28 04:43:32");
    }

    @Test
    void utcWallClockToLocal按UTC墙钟正确转换() {
        LocalDateTime utcWall = LocalDateTime.of(2026, 10, 28, 4, 43, 32);
        assertThat(DisplayTimeUtil.utcWallClockToLocal(utcWall, SHANGHAI))
                .isEqualTo(LocalDateTime.of(2026, 10, 28, 12, 43, 32));
        assertThat(DisplayTimeUtil.utcWallClockToLocal(utcWall, ZoneOffset.UTC))
                .isEqualTo(utcWall);
    }

    @Test
    void 数据null返回null且zone先校验() {
        assertThat(DisplayTimeUtil.toLocalDateTime(null, SHANGHAI)).isNull();
        assertThat(DisplayTimeUtil.format(null, SHANGHAI)).isNull();
        assertThat(DisplayTimeUtil.utcWallClockToLocal(null, SHANGHAI)).isNull();
        // zone null → NPE，即使数据为 null
        assertThatThrownBy(() -> DisplayTimeUtil.toLocalDateTime(null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DisplayTimeUtil.format(null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DisplayTimeUtil.utcWallClockToLocal(null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DisplayTimeUtil.toLocalDateTime(SAMPLE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void Berlin春季gap与秋季overlap沿用显式政策() {
        // 2026-03-29 02:30 是 Berlin 春季 gap（02:00→03:00 跳变），墙钟不存在
        LocalDateTime gapWall = LocalDateTime.of(2026, 3, 29, 2, 30);
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(gapWall, BERLIN))
                .isInstanceOf(java.time.DateTimeException.class);
        // 2026-10-25 02:30 是秋季 overlap（两个有效 offset），须显式选择
        LocalDateTime overlapWall = LocalDateTime.of(2026, 10, 25, 2, 30);
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(overlapWall, BERLIN))
                .isInstanceOf(java.time.DateTimeException.class);
        Instant early = ExplicitTimeUtil.resolveLocalDateTime(overlapWall, BERLIN,
                ZoneOffset.of("+02:00"));
        Instant late = ExplicitTimeUtil.resolveLocalDateTime(overlapWall, BERLIN,
                ZoneOffset.of("+01:00"));
        assertThat(early).isEqualTo(Instant.parse("2026-10-25T00:30:00Z"));
        assertThat(late).isEqualTo(Instant.parse("2026-10-25T01:30:00Z"));
        // 显式选择错误 offset 拒绝
        assertThatThrownBy(() -> ExplicitTimeUtil.resolveLocalDateTime(overlapWall, BERLIN,
                ZoneOffset.of("+08:00")))
                .isInstanceOf(java.time.DateTimeException.class);
    }

    @Test
    void 展示层不改变显式转换与严格解析契约() {
        // 无 offset 的事件 ISO 仍拒绝（ExplicitTimeUtil 契约不变）
        assertThatThrownBy(() -> ExplicitTimeUtil.parseInstantIso("2026-10-28T04:43:32"))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
        // 带 offset 的墙钟字符串仍拒绝
        assertThatThrownBy(() -> ExplicitTimeUtil.parseLocalDateTimeIso("2026-10-28T12:43:32+08:00"))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
    }
}
