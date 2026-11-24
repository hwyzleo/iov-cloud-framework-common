package net.hwyz.iov.cloud.framework.common.exception;

/**
 * 错误码的 HTTP 状态提示（可选能力，FW-COMMON-DSN-CR-002）。
 * <p>
 * 错误响应面分两类，由端点契约决定（见 framework-web {@code ErrorResponseSurface}）：
 * </p>
 * <ul>
 *   <li>管理面：端点声明返回 {@code ApiResponse}，业务失败固定 HTTP 200 + 业务码，前端按码提示；</li>
 *   <li>机器面：端点声明返回裸 DTO/{@code void}（Feign 等服务间契约），业务失败必须返回
 *       非 2xx，避免调用方把 {@code ApiResponse} 错误体反序列化成空数据而误判成功。</li>
 * </ul>
 * <p>
 * 机器面默认状态为 422（业务规则拒绝）；语义明确的错误码可覆盖本方法返回 4xx/5xx。
 * 实现类必须是错误码（{@link ErrorCode}）本身，禁止在异常类上另立状态字段。
 * </p>
 *
 * @author hwyz_leo
 */
public interface HttpStatusAware {

    /**
     * 机器面业务错误默认状态码：业务规则拒绝。
     */
    int DEFAULT_BUSINESS_ERROR_STATUS = 422;

    /**
     * 机器面业务错误的 HTTP 状态码；仅 4xx/5xx 生效，其他取值回退默认值。
     *
     * @return HTTP 状态码
     */
    default int getHttpStatus() {
        return DEFAULT_BUSINESS_ERROR_STATUS;
    }
}
