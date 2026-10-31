package net.hwyz.iov.cloud.framework.common.time;

/**
 * 请求时区快照来源（FW-COMMON-DSN-CR-002，RD-CTZ-001）。
 * <p>
 * 低基数枚举，仅标识时区值由哪条解析链产出，不保存原始输入、用户标识或请求详情。
 *
 * @author hwyz_leo
 * @since 2026-10-31
 */
public enum Source {

    /** 已认证用户的服务端存储配置（认证 principal 之后才允许读取）。 */
    USER_PROFILE,

    /** 被允许信任的请求头 {@code X-Timezone}（trust-header=true 时）。 */
    HEADER,

    /** 显式启用的 query/form 参数（param-names 非空且 header 缺失时）。 */
    PARAM,

    /** 服务/端点显式兜底（如 TSP Excel 的 tsp.display.zone-id），不是请求输入。 */
    SERVICE_FALLBACK,

    /** 框架 {@code iov.web.timezone.default-zone-id} 最终兜底，仅展示/本地日历语义。 */
    DEFAULT
}
