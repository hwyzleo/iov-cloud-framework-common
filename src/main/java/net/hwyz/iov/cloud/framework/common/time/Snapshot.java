package net.hwyz.iov.cloud.framework.common.time;

import java.time.ZoneId;
import java.util.Objects;

/**
 * 请求时区不可变快照（FW-COMMON-DSN-CR-002，RD-CTZ-001）。
 * <p>
 * 每次逻辑请求解析完成后生成一次，之后在请求生命周期内固定不变；
 * 异步重派发/线程移交只复用该快照，不重复读取已变化的配置或用户状态。
 * 字段全部非 null，构造后不可修改。
 *
 * @param zoneId        有效时区（已校验、规范化，非 null）
 * @param source        来源（非 null）
 * @param fallbackReason 回退原因（主来源命中时为 {@link FallbackReason#NONE}，非 null）
 * @author hwyz_leo
 * @since 2026-10-31
 */
public record Snapshot(ZoneId zoneId, Source source, FallbackReason fallbackReason) {

    public Snapshot {
        Objects.requireNonNull(zoneId, "zoneId");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(fallbackReason, "fallbackReason");
    }
}
