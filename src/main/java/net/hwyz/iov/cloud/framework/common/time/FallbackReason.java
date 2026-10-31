package net.hwyz.iov.cloud.framework.common.time;

/**
 * 请求时区回退原因（FW-COMMON-DSN-CR-002，RD-CTZ-001）。
 * <p>
 * 低基数枚举，只描述「为什么走到了当前来源」的类别化原因，用于日志/观测的
 * 低基数标签；绝不保存原始非法输入、账号、VIN、requestId 或任何原始载荷。
 *
 * @author hwyz_leo
 * @since 2026-10-31
 */
public enum FallbackReason {

    /** 主来源直接命中，无回退。 */
    NONE,

    /** 认证用户 SPI 无配置（未认证或无存储配置），继续候选来源。 */
    NO_USER_PROFILE,

    /** 认证用户 SPI 执行异常，记录后继续候选，不改变认证结果。 */
    USER_PROFILE_ERROR,

    /** 请求头缺失（随后才允许尝试参数）。 */
    MISSING_HEADER,

    /** 请求头存在但为空。 */
    EMPTY_HEADER,

    /** 请求头存在但非法（长度/字符/格式/IANA/offset 校验失败）。 */
    INVALID_HEADER,

    /** 请求头存在多个值。 */
    MULTIPLE_HEADER_VALUES,

    /** 参数缺失（header 缺失且未启用参数时也以此标识）。 */
    MISSING_PARAM,

    /** 参数存在但非法。 */
    INVALID_PARAM,

    /** 多个候选参数或单参数多值，视为歧义。 */
    AMBIGUOUS_PARAMS,

    /** 无服务/端点显式兜底可提供。 */
    NO_SERVICE_FALLBACK
}
