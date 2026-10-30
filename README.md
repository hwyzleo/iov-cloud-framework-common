# iov-cloud-framework-common

车联网云端框架通用部分（FW-COMMON）。

## 时间工具（FW-COMMON-DSN-CR-001）

`net.hwyz.iov.cloud.framework.common.util` 提供两类时间工具：

### 1. `ExplicitTimeUtil`（新增调用的显式策略入口）

独立 final 工具类，纯 JDK `java.time`，无 Spring/Web/MySQL 依赖、无全局状态。

- **Instant-first**：事件时刻一律 `Instant`，`nowInstant()` 委托 UTC Clock，不读取 JVM 默认时区。
- **显式 ZoneId**：墙钟/offset 转换必须显式传 `ZoneId`（`toLocalDateTime` / `toOffsetDateTime` /
  `formatWallClock`），不使用 `systemDefault` 或全局业务区默认值。
- **严格解析**：`parseInstantIso`（必须带 `Z`/数值 offset、完整消费）、`parseLocalDateTimeIso`（带
  offset 拒绝）、`parseLocalDateIso`（日期时间输入拒绝、不静默截断）；空/空白/非法/尾随字符显式失败。
- **DST**：`resolveLocalDateTime` 在 gap 拒绝；overlap 必须显式选择属于该时刻有效集合的 offset。
- **epoch**：`fromEpochMillis(long)` / `toEpochMillis(Instant)` 固定毫秒单位，不按位数猜测；
  溢出抛 `ArithmeticException`。
- **异常约定**：null → `NullPointerException`；非法字符串 → `DateTimeParseException`；
  DST/offset 不匹配 → `DateTimeException`。无任何“当前时间/epoch0/默认地区/静默修正”兜底。

### 2. `DateTimeUtil`（冻结的兼容层）

继承 `cn.hutool.core.date.DateUtil`，保留全部既有签名、继承关系与行为，**不因时间治理修改**。

- 以下六项入口隐含 Asia/Shanghai 解释，已标注 `@Deprecated(forRemoval = false)`（行为不变，无删除期限）：
  `getNowLocalDateTime()`、`getNowOffsetDateTime()`、`instantToOffsetDateTime(Instant)`、
  `format(Instant)`、`fromEpochMilli(long)`、`toEpochMilli(LocalDateTime)`。
  替代入口见各方法 Javadoc 指向的 `ExplicitTimeUtil` 方法。
- `getNowInstant()` / `getNowDate()`（绝对时刻）与 `format(LocalDateTime)` / `parse(String)`
  （纯墙钟）保持原语义，不弃用。

### 已知风险（接入 UTC 环境前须逐调用点核验）

- 继承自 Hutool 的方法（`now()`、`format(Date, pattern)`、`parseDate(...)` 等）按 **JVM 默认时区**
  解释；自定义方法隐含 **Asia/Shanghai**。两者仅在 JVM 默认时区为 Asia/Shanghai 时结果一致，
  JVM 改为 UTC 后继承方法输出相对上海早 8 小时。
- `parseDate(...)`（继承）对带时分秒的字符串会**静默截断到日期**（如 `2026-10-28 12:00:00`
  → `2026-10-28 00:00:00`）；严格解析请使用 `ExplicitTimeUtil.parseLocalDateIso`。
- 既有调用方（ciam/eiam/FW-WEB 等）本批**未迁移**，迁移由各服务配对变更分批批准执行。
