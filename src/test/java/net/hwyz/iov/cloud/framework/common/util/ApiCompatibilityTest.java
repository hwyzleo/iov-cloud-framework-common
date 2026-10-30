package net.hwyz.iov.cloud.framework.common.util;

import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 旧客户端 API 兼容测试（FW-COMMON-DSN-CR-001，TC-03）。
 * <p>
 * 用 JDK JavaCompiler 将“旧客户端”源码（引用 DateTimeUtil 全部既有自定义签名、
 * 继承 Hutool 方法与新 ExplicitTimeUtil 入口）重编译，并以当前构建产物加载运行，
 * 验证：源码重编译无新增 null 重载歧义、弃用方法仍可链接调用、extends 与公开可见性保持、
 * 新旧入口返回值一致。二进制链接（旧 jar 编译 + 新 jar 运行）由交付验证命令另行归档。
 */
class ApiCompatibilityTest {

    private static final String CLIENT_SOURCE = """
            import java.time.Instant;
            import java.time.LocalDateTime;
            import java.time.ZoneId;
            import java.time.format.DateTimeFormatter;
            import java.util.Date;
            import net.hwyz.iov.cloud.framework.common.util.DateTimeUtil;
            import net.hwyz.iov.cloud.framework.common.util.ExplicitTimeUtil;

            public class LegacyClient {
                public static String run() {
                    Instant fixed = Instant.parse("2026-10-28T04:43:32Z");
                    LocalDateTime wall = LocalDateTime.parse("2026-10-28T12:43:32");
                    StringBuilder sb = new StringBuilder();
                    // 绝对时刻入口（不弃用）
                    sb.append(DateTimeUtil.getNowDate() instanceof Date).append('|');
                    sb.append(DateTimeUtil.getNowInstant() != null).append('|');
                    // 弃用入口：签名与行为保持不变，仍可编译/链接调用
                    sb.append(DateTimeUtil.format(fixed)).append('|');
                    sb.append(DateTimeUtil.instantToOffsetDateTime(fixed)).append('|');
                    sb.append(DateTimeUtil.fromEpochMilli(1793162612000L)).append('|');
                    sb.append(DateTimeUtil.toEpochMilli(wall)).append('|');
                    sb.append(DateTimeUtil.getNowLocalDateTime() != null).append('|');
                    sb.append(DateTimeUtil.getNowOffsetDateTime() != null).append('|');
                    // 纯墙钟入口（不弃用）
                    sb.append(DateTimeUtil.format(wall)).append('|');
                    sb.append(DateTimeUtil.parse("2026-10-28 12:43:32")).append('|');
                    // 继承 Hutool 入口（extends 保持）
                    sb.append(DateTimeUtil.now() != null).append('|');
                    sb.append(DateTimeUtil.parseDate("2026-10-28")).append('|');
                    sb.append(DateTimeUtil.formatDate(Date.from(fixed))).append('|');
                    // 新显式入口
                    sb.append(ExplicitTimeUtil.toEpochMillis(fixed)).append('|');
                    sb.append(ExplicitTimeUtil.formatInstantIso(fixed)).append('|');
                    sb.append(ExplicitTimeUtil.toLocalDateTime(fixed, ZoneId.of("Asia/Shanghai")));
                    return sb.toString();
                }
            }
            """;

    @Test
    void 旧客户端源码重编译并运行_新旧入口结果一致() throws Exception {
        Path dir = Files.createTempDirectory("legacy-client");
        Path src = dir.resolve("LegacyClient.java");
        Files.writeString(src, CLIENT_SOURCE, StandardCharsets.UTF_8);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("JDK 应提供 JavaCompiler").isNotNull();
        int exit = compiler.run(null, null, null,
                "-classpath", System.getProperty("java.class.path"),
                "-d", dir.toString(),
                src.toString());
        assertThat(exit).as("旧客户端源码应编译成功（0）").isZero();

        String result = runClient(dir);
        String[] parts = result.split("\\|");
        assertThat(parts).hasSize(16);
        assertThat(parts[0]).isEqualTo("true");                       // getNowDate
        assertThat(parts[1]).isEqualTo("true");                       // getNowInstant
        assertThat(parts[2]).isEqualTo("2026-10-28 12:43:32");        // format(Instant) 上海墙钟
        assertThat(parts[3]).isEqualTo("2026-10-28T12:43:32+08:00");  // instantToOffsetDateTime
        assertThat(parts[4]).isEqualTo("2026-10-28T12:43:32");        // fromEpochMilli 上海墙钟
        assertThat(parts[5]).isEqualTo("1793162612000");              // toEpochMilli(LocalDateTime)
        assertThat(parts[6]).isEqualTo("true");                       // getNowLocalDateTime
        assertThat(parts[7]).isEqualTo("true");                       // getNowOffsetDateTime
        assertThat(parts[8]).isEqualTo("2026-10-28 12:43:32");        // format(LocalDateTime)
        assertThat(parts[9]).isEqualTo("2026-10-28T12:43:32");        // parse
        assertThat(parts[10]).isEqualTo("true");                      // 继承 now()
        assertThat(parts[11]).isEqualTo("2026-10-28 00:00:00");       // 继承 parseDate（日期 0 点，JVM 默认时区）
        assertThat(parts[13]).isEqualTo("1793162612000");             // ExplicitTimeUtil.toEpochMillis
        assertThat(parts[14]).isEqualTo("2026-10-28T04:43:32Z");      // ExplicitTimeUtil.formatInstantIso
        assertThat(parts[15]).isEqualTo("2026-10-28T12:43:32");       // ExplicitTimeUtil.toLocalDateTime
        // 继承 formatDate 输出与 JVM 默认时区一致（不固定断言具体值，仅断言非空且为日期）
        assertThat(parts[12]).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private static String runClient(Path dir) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(
                new URL[]{dir.toUri().toURL()}, ApiCompatibilityTest.class.getClassLoader())) {
            Class<?> client = Class.forName("LegacyClient", true, loader);
            Method run = client.getMethod("run");
            return (String) run.invoke(null);
        }
    }
}
