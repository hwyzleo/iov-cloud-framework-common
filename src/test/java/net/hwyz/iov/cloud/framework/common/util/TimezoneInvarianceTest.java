package net.hwyz.iov.cloud.framework.common.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 双 JVM 时区一致性测试（FW-COMMON-DSN-CR-001，TC-01/TC-07）。
 * <p>
 * 本类由 Surefire 三个 execution 独立 fork 进程执行：默认（继承 Maven JVM 时区）、
 * {@code tz-utc}（-Duser.timezone=UTC）、{@code tz-shanghai}（-Duser.timezone=Asia/Shanghai）。
 * 全部断言使用固定 {@link Clock} 与显式 {@link ZoneId}，不读取 JVM 默认时区，
 * 因此在任意 JVM 时区下结果必须一致——UTC 与 Asia/Shanghai 独立进程重跑相同断言。
 */
class TimezoneInvarianceTest {

    private static final Instant FIXED = Instant.parse("2026-10-28T04:43:32Z");
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED, UTC);

    @Test
    void 本fork进程的JVM默认时区与配置的userTimezone一致() {
        // 三个 Surefire execution 在独立 fork 进程运行：tz-utc/tz-shanghai 显式设置
        // user.timezone 与 tz.expect；默认 execution 未配置 tz.expect（继承环境）。
        // 配置了 tz.expect 的 execution 必须真正改变 fork JVM 默认时区（TC-07 独立
        // JVM 证据，避免进程内 TimeZone.setDefault 污染）。
        String expected = System.getProperty("tz.expect");
        if (expected != null) {
            assertThat(java.util.TimeZone.getDefault().getID())
                    .as("fork JVM 默认时区应与 tz.expect 一致")
                    .isEqualTo(expected);
        }
    }

    @Test
    void 固定Clock与显式zone转换不依赖JVM默认时区() {
        assertThat(ExplicitTimeUtil.nowInstant(FIXED_CLOCK)).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.toLocalDateTime(FIXED, SHANGHAI))
                .isEqualTo(LocalDateTime.parse("2026-10-28T12:43:32"));
        assertThat(ExplicitTimeUtil.toLocalDateTime(FIXED, UTC))
                .isEqualTo(LocalDateTime.parse("2026-10-28T04:43:32"));
        assertThat(ExplicitTimeUtil.toOffsetDateTime(FIXED, SHANGHAI).toInstant()).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.formatWallClock(FIXED, SHANGHAI)).isEqualTo("2026-10-28 12:43:32");
        assertThat(ExplicitTimeUtil.formatWallClock(FIXED, UTC)).isEqualTo("2026-10-28 04:43:32");
        assertThat(ExplicitTimeUtil.resolveLocalDateTime(
                LocalDateTime.parse("2026-10-28T12:43:32"), SHANGHAI)).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.parseInstantIso("2026-10-28T04:43:32Z")).isEqualTo(FIXED);
        assertThat(ExplicitTimeUtil.formatInstantIso(FIXED)).isEqualTo("2026-10-28T04:43:32Z");
        assertThat(ExplicitTimeUtil.toEpochMillis(FIXED)).isEqualTo(1793162612000L);
        assertThat(ExplicitTimeUtil.fromEpochMillis(1793162612000L)).isEqualTo(FIXED);
    }

    @Test
    void 旧自定义方法上海墙钟语义在任意JVM下一致() {
        // DateTimeUtil 自定义方法硬编码 Asia/Shanghai：与 JVM 默认时区无关（冻结行为）
        assertThat(DateTimeUtil.format(FIXED)).isEqualTo("2026-10-28 12:43:32");
        assertThat(DateTimeUtil.instantToOffsetDateTime(FIXED).getOffset().getTotalSeconds())
                .isEqualTo(8 * 3600);
        assertThat(DateTimeUtil.fromEpochMilli(1793162612000L))
                .isEqualTo(LocalDateTime.parse("2026-10-28T12:43:32"));
    }
}
