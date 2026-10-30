package net.hwyz.iov.cloud.framework.common.util;

import cn.hutool.core.date.DateUtil;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DateTimeUtil} 旧行为 golden 契约测试（FW-COMMON-DSN-CR-001，TC-02/TC-03）。
 * <p>
 * 冻结旧兼容层：自定义方法隐含 Asia/Shanghai 语义、继承 Hutool 方法按 JVM 默认时区语义、
 * null/非法输入既有行为、弃用仅元信息（forRemoval=false）、getNowInstant/getNowDate 绝对时刻语义、
 * extends DateUtil 与公开可见性保持。本测试不因本 CR 改变任何旧断言。
 */
class DateTimeUtilLegacyContractTest {

    private static final Instant FIXED = Instant.parse("2026-10-28T04:43:32Z");
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    // ---------------- 绝对时刻入口（不弃用，语义固定） ----------------

    @Test
    void getNowInstant_绝对时刻与JVM时区无关() {
        Instant before = Instant.now();
        Instant now = DateTimeUtil.getNowInstant();
        Instant after = Instant.now();
        assertThat(now).isBetween(before, after);
    }

    @Test
    void getNowDate_绝对时刻Date不因JVM时区改变() {
        long before = System.currentTimeMillis();
        Date now = DateTimeUtil.getNowDate();
        long after = System.currentTimeMillis();
        assertThat(now.getTime()).isBetween(before, after);
        assertThat(now.toInstant()).isBetween(
                Instant.ofEpochMilli(before), Instant.ofEpochMilli(after));
    }

    // ---------------- 自定义方法：隐含 Asia/Shanghai（与 JVM 时区无关，已实证） ----------------

    @Test
    void format_Instant_固定上海墙钟() {
        assertThat(DateTimeUtil.format(FIXED)).isEqualTo("2026-10-28 12:43:32");
        assertThat(DateTimeUtil.format((Instant) null)).isNull();
    }

    @Test
    void instantToOffsetDateTime_固定加八() {
        assertThat(DateTimeUtil.instantToOffsetDateTime(FIXED))
                .isEqualTo(OffsetDateTime.parse("2026-10-28T12:43:32+08:00"));
        assertThat(DateTimeUtil.instantToOffsetDateTime(null)).isNull();
    }

    @Test
    void fromEpochMilli_toEpochMilli_上海墙钟往返() {
        assertThat(DateTimeUtil.fromEpochMilli(1793162612000L))
                .isEqualTo(LocalDateTime.parse("2026-10-28T12:43:32"));
        assertThat(DateTimeUtil.toEpochMilli(LocalDateTime.parse("2026-10-28T12:43:32")))
                .isEqualTo(1793162612000L);
    }

    @Test
    void getNowLocalDateTime_getNowOffsetDateTime_固定上海() {
        // 以显式上海墙钟对照（纳秒级连续两次调用允许亚秒误差，避免竞态）
        Instant now = DateTimeUtil.getNowInstant();
        LocalDateTime expected = LocalDateTime.ofInstant(now, SHANGHAI);
        assertThat(java.time.Duration.between(DateTimeUtil.getNowLocalDateTime(), expected).abs())
                .isLessThan(java.time.Duration.ofSeconds(1));
        OffsetDateTime odt = DateTimeUtil.getNowOffsetDateTime();
        assertThat(java.time.Duration.between(odt.toInstant(), now).abs())
                .isLessThan(java.time.Duration.ofSeconds(1));
        assertThat(odt.getOffset().getTotalSeconds()).isEqualTo(8 * 3600);
    }

    // ---------------- 纯墙钟方法（不弃用，非绝对时刻） ----------------

    @Test
    void format_LocalDateTime_纯墙钟字符串() {
        assertThat(DateTimeUtil.format(LocalDateTime.parse("2026-10-28T12:43:32")))
                .isEqualTo("2026-10-28 12:43:32");
        assertThat(DateTimeUtil.format((LocalDateTime) null)).isNull();
    }

    @Test
    void parse_墙钟解析_null返null非法抛异常() {
        assertThat(DateTimeUtil.parse("2026-10-28 12:43:32"))
                .isEqualTo(LocalDateTime.parse("2026-10-28T12:43:32"));
        assertThat(DateTimeUtil.parse(null)).isNull();
        assertThatThrownBy(() -> DateTimeUtil.parse("2026-10-28"))
                .isInstanceOf(DateTimeParseException.class);
    }

    // ---------------- 继承 Hutool 方法：按 JVM 默认时区语义冻结 ----------------

    @Test
    void 继承_format_Date_pattern_跟随JVM默认时区() {
        // 冻结：继承方法按 JVM 默认时区解释（不是固定上海）
        Instant now = Instant.now();
        String expected = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault()).format(now);
        assertThat(DateTimeUtil.format(Date.from(now), "yyyy-MM-dd HH:mm:ss")).isEqualTo(expected);
    }

    @Test
    void 继承_now_跟随JVM默认时区() {
        Instant now = Instant.now();
        String expected = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault()).format(now);
        assertThat(DateTimeUtil.now()).isEqualTo(expected);
    }

    @Test
    void 继承_parseDate_日期解析与带时分秒静默截断冻结() {
        // 纯日期：按 JVM 默认时区解释 0 点（继承方法环境依赖语义冻结）
        Date parsed = DateTimeUtil.parseDate("2026-10-28");
        Date expected = Date.from(java.time.LocalDate.parse("2026-10-28")
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertThat(parsed.getTime()).isEqualTo(expected.getTime());
        // 带时分秒：静默截断到日期（既有行为，本 CR 冻结，不"修复"）
        assertThat(DateTimeUtil.parseDate("2026-10-28 12:43:32").getTime())
                .isEqualTo(DateTimeUtil.parseDate("2026-10-28").getTime());
    }

    // ---------------- 弃用元信息（仅元信息，行为不变） ----------------

    private static final List<String> DEPRECATED_METHODS = Arrays.asList(
            "getNowLocalDateTime",
            "getNowOffsetDateTime",
            "instantToOffsetDateTime",
            "fromEpochMilli",
            "toEpochMilli");

    private static final List<String> NOT_DEPRECATED_METHODS = Arrays.asList(
            "getNowInstant",
            "getNowDate",
            "parse");

    @Test
    void 弃用方法_仅元信息且forRemoval为false() throws Exception {
        for (String name : DEPRECATED_METHODS) {
            for (Method m : DateTimeUtil.class.getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    Deprecated d = m.getAnnotation(Deprecated.class);
                    assertThat(d).as("方法 %s 应标注 @Deprecated", name).isNotNull();
                    assertThat(d.forRemoval()).as("方法 %s forRemoval 应为 false", name).isFalse();
                }
            }
        }
        // 精确命中：六项弃用方法必须全部存在；format 仅 Instant 重载弃用，LocalDateTime 重载不弃用
        assertThat(countDeprecatedDeclared()).isEqualTo(6);
        assertThat(DateTimeUtil.class.getMethod("format", Instant.class).getAnnotation(Deprecated.class))
                .isNotNull();
        assertThat(DateTimeUtil.class.getMethod("format", LocalDateTime.class).getAnnotation(Deprecated.class))
                .isNull();
    }

    private static long countDeprecatedDeclared() {
        return Arrays.stream(DateTimeUtil.class.getDeclaredMethods())
                .filter(m -> m.getAnnotation(Deprecated.class) != null)
                .count();
    }

    @Test
    void 不弃用方法_保持原语义() throws Exception {
        // 精确按签名判定：六项弃用（含 format(Instant)），其余声明方法不弃用（main 除外）
        for (Method m : DateTimeUtil.class.getDeclaredMethods()) {
            if (m.getName().equals("main")) {
                continue;
            }
            if (isExpectedDeprecated(m)) {
                assertThat(m.getAnnotation(Deprecated.class))
                        .as("方法 %s 应被弃用", m).isNotNull();
            } else {
                assertThat(m.getAnnotation(Deprecated.class))
                        .as("方法 %s 不应被弃用", m).isNull();
            }
        }
        // getNowInstant/getNowDate/parse 等入口仍可反射命中
        assertThat(DateTimeUtil.class.getMethod("getNowInstant")).isNotNull();
        assertThat(DateTimeUtil.class.getMethod("getNowDate")).isNotNull();
        assertThat(DateTimeUtil.class.getMethod("parse", String.class)).isNotNull();
        // format(LocalDateTime) 与 format(Instant) 两个墙钟/展示重载仍存在
        assertThat(DateTimeUtil.class.getMethod("format", LocalDateTime.class)).isNotNull();
        assertThat(DateTimeUtil.class.getMethod("format", Instant.class)).isNotNull();
    }

    private static boolean isExpectedDeprecated(Method m) {
        if (m.getName().equals("format")) {
            return m.getParameterCount() == 1 && m.getParameterTypes()[0] == Instant.class;
        }
        return DEPRECATED_METHODS.contains(m.getName());
    }

    // ---------------- extends 与公开可见性保持 ----------------

    @Test
    void 继承关系与公开可见性保持() {
        assertThat(DateTimeUtil.class.getSuperclass()).isEqualTo(DateUtil.class);
        assertThat(DateTimeUtil.class.getModifiers() & java.lang.reflect.Modifier.PUBLIC).isNotZero();
        // 无新增 public 构造器之外的可见性变化：仍为 public 类 + 默认构造
        assertThat(DateTimeUtil.class.getConstructors()).hasSize(1);
        // 公开静态方法计数与冻结基线一致（弃用仅注解，不增减方法）
        long publicStaticMethods = Arrays.stream(DateTimeUtil.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .filter(m -> java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                .count();
        assertThat(publicStaticMethods).isEqualTo(11); // getNowDate/getNowLocalDateTime/getNowOffsetDateTime/
        // getNowInstant/instantToOffsetDateTime/format×2/parse/fromEpochMilli/toEpochMilli/main
    }

    @Test
    void 无新增null重载歧义_弃用方法签名不变() {
        // format 的两个重载仍按既有签名解析；null 需显式类型，与冻结基线一致
        assertThat(DateTimeUtil.format((LocalDateTime) null)).isNull();
        assertThat(DateTimeUtil.format((Instant) null)).isNull();
        // 旧方法签名逐一保持（反射对比，防止误加 null 兼容重载）
        Method[] methods = DateTimeUtil.class.getDeclaredMethods();
        long overloadsOfFormat = Arrays.stream(methods).filter(m -> m.getName().equals("format")).count();
        assertThat(overloadsOfFormat).isEqualTo(2);
    }

    @Test
    void 弃用注解不含runtimeRetention影响() throws Exception {
        // 确保 @Deprecated 是标准注解且不改变运行行为
        Method m = DateTimeUtil.class.getDeclaredMethod("getNowLocalDateTime");
        Annotation[] annotations = m.getAnnotations();
        assertThat(annotations).extracting(a -> a.annotationType().getSimpleName())
                .containsExactly("Deprecated");
    }
}
