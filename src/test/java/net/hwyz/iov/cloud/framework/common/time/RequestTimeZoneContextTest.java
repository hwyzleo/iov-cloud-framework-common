package net.hwyz.iov.cloud.framework.common.time;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 请求时区上下文单测（FW-COMMON-DSN-CR-002，T-CTZ-01）。
 * <p>
 * 覆盖：无绑定 current 抛错、set null、clear 幂等、嵌套/LIFO/重复 close/错线程 close、
 * 多线程隔离、线程池快照捕获与恢复、无快照任务不继承工作线程残留。
 */
class RequestTimeZoneContextTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");
    private static final ZoneId UTC = ZoneId.of("UTC");

    private static Snapshot snapshot(ZoneId zone) {
        return new Snapshot(zone, Source.HEADER, FallbackReason.NONE);
    }

    @AfterEach
    void clearThreadLocal() {
        RequestTimeZoneContext.clear();
    }

    @Test
    void 无绑定current抛IllegalStateException且currentOptional为空() {
        assertThatThrownBy(RequestTimeZoneContext::current)
                .isInstanceOf(IllegalStateException.class);
        assertThat(RequestTimeZoneContext.currentOptional()).isEmpty();
        assertThat(RequestTimeZoneContext.capture()).isEmpty();
    }

    @Test
    void set后current返回时区且快照完整() {
        Snapshot s = snapshot(SHANGHAI);
        RequestTimeZoneContext.set(s);
        assertThat(RequestTimeZoneContext.current()).isEqualTo(SHANGHAI);
        assertThat(RequestTimeZoneContext.currentOptional()).contains(s);
        assertThat(RequestTimeZoneContext.capture()).contains(s);
        assertThat(s.zoneId()).isEqualTo(SHANGHAI);
        assertThat(s.source()).isEqualTo(Source.HEADER);
        assertThat(s.fallbackReason()).isEqualTo(FallbackReason.NONE);
    }

    @Test
    void set拒绝null() {
        assertThatThrownBy(() -> RequestTimeZoneContext.set(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void clear幂等且为remove语义() {
        RequestTimeZoneContext.set(snapshot(SHANGHAI));
        RequestTimeZoneContext.clear();
        assertThat(RequestTimeZoneContext.currentOptional()).isEmpty();
        RequestTimeZoneContext.clear();
        assertThat(RequestTimeZoneContext.currentOptional()).isEmpty();
    }

    @Test
    void open绑定新快照close恢复原绑定() {
        RequestTimeZoneContext.set(snapshot(SHANGHAI));
        try (RequestTimeZoneContext.Scope scope = RequestTimeZoneContext.open(snapshot(BERLIN))) {
            assertThat(RequestTimeZoneContext.current()).isEqualTo(BERLIN);
        }
        assertThat(RequestTimeZoneContext.current()).isEqualTo(SHANGHAI);
    }

    @Test
    void open无原绑定close后清除() {
        try (RequestTimeZoneContext.Scope scope = RequestTimeZoneContext.open(snapshot(UTC))) {
            assertThat(RequestTimeZoneContext.current()).isEqualTo(UTC);
        }
        assertThat(RequestTimeZoneContext.currentOptional()).isEmpty();
    }

    @Test
    void 嵌套作用域LIFO顺序关闭恢复() {
        RequestTimeZoneContext.set(snapshot(SHANGHAI));
        RequestTimeZoneContext.Scope outer = RequestTimeZoneContext.open(snapshot(BERLIN));
        RequestTimeZoneContext.Scope inner = RequestTimeZoneContext.open(snapshot(UTC));
        assertThat(RequestTimeZoneContext.current()).isEqualTo(UTC);
        inner.close();
        assertThat(RequestTimeZoneContext.current()).isEqualTo(BERLIN);
        outer.close();
        assertThat(RequestTimeZoneContext.current()).isEqualTo(SHANGHAI);
    }

    @Test
    void 重复close幂等() {
        RequestTimeZoneContext.Scope scope = RequestTimeZoneContext.open(snapshot(BERLIN));
        scope.close();
        scope.close();
        assertThat(RequestTimeZoneContext.currentOptional()).isEmpty();
    }

    @Test
    void 错序关闭抛IllegalStateException且状态不变() {
        RequestTimeZoneContext.set(snapshot(SHANGHAI));
        RequestTimeZoneContext.Scope outer = RequestTimeZoneContext.open(snapshot(BERLIN));
        RequestTimeZoneContext.Scope inner = RequestTimeZoneContext.open(snapshot(UTC));
        assertThatThrownBy(outer::close)
                .isInstanceOf(IllegalStateException.class);
        // 错序 close 不修改状态：当前仍是 inner 绑定
        assertThat(RequestTimeZoneContext.current()).isEqualTo(UTC);
        inner.close();
        outer.close();
        assertThat(RequestTimeZoneContext.current()).isEqualTo(SHANGHAI);
    }

    @Test
    void 异常路径下scope关闭仍恢复() {
        RequestTimeZoneContext.set(snapshot(SHANGHAI));
        RequestTimeZoneContext.Scope scope = RequestTimeZoneContext.open(snapshot(BERLIN));
        try {
            throw new IllegalStateException("模拟业务异常");
        } catch (IllegalStateException expected) {
            // 预期异常
        } finally {
            scope.close();
        }
        assertThat(RequestTimeZoneContext.current()).isEqualTo(SHANGHAI);
    }

    @Test
    void 错线程close抛IllegalStateException且不影响其他线程() throws Exception {
        RequestTimeZoneContext.set(snapshot(SHANGHAI));
        RequestTimeZoneContext.Scope scope = RequestTimeZoneContext.open(snapshot(BERLIN));
        AtomicReference<Throwable> other = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                scope.close();
            } catch (Throwable e) {
                other.set(e);
            } finally {
                done.countDown();
            }
        });
        t.start();
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(other.get()).isInstanceOf(IllegalStateException.class);
        // 本线程状态未被修改
        assertThat(RequestTimeZoneContext.current()).isEqualTo(BERLIN);
        scope.close();
        assertThat(RequestTimeZoneContext.current()).isEqualTo(SHANGHAI);
    }

    @Test
    void 多线程上下文相互隔离() throws Exception {
        RequestTimeZoneContext.set(snapshot(SHANGHAI));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch done = new CountDownLatch(2);
            AtomicReference<Throwable> failures = new AtomicReference<>();
            for (ZoneId zone : new ZoneId[]{BERLIN, UTC}) {
                pool.submit(() -> {
                    try {
                        assertThat(RequestTimeZoneContext.currentOptional()).isEmpty();
                        try (RequestTimeZoneContext.Scope scope = RequestTimeZoneContext.open(snapshot(zone))) {
                            assertThat(RequestTimeZoneContext.current()).isEqualTo(zone);
                        }
                        assertThat(RequestTimeZoneContext.currentOptional()).isEmpty();
                    } catch (Throwable e) {
                        failures.accumulateAndGet(e, (a, b) -> a == null ? b : a);
                    } finally {
                        done.countDown();
                    }
                });
            }
            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(failures.get()).isNull();
            // 主线程绑定不受影响
            assertThat(RequestTimeZoneContext.current()).isEqualTo(SHANGHAI);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void 线程池任务捕获快照后open恢复() throws Exception {
        RequestTimeZoneContext.set(snapshot(SHANGHAI));
        Optional<Snapshot> captured = RequestTimeZoneContext.capture();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            AtomicReference<ZoneId> workerZone = new AtomicReference<>();
            AtomicReference<ZoneId> afterZone = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            pool.submit(() -> {
                try (RequestTimeZoneContext.Scope scope = RequestTimeZoneContext.open(captured.orElseThrow())) {
                    workerZone.set(RequestTimeZoneContext.current());
                }
                afterZone.set(RequestTimeZoneContext.currentOptional().map(Snapshot::zoneId).orElse(null));
                done.countDown();
            });
            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(workerZone.get()).isEqualTo(SHANGHAI);
            assertThat(afterZone.get()).isNull();
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void 无快照任务不继承工作线程残留() throws Exception {
        // 工作线程先残留一个绑定（模拟上一任务未清理）
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            CountDownLatch seed = new CountDownLatch(1);
            pool.submit(() -> {
                RequestTimeZoneContext.set(snapshot(BERLIN));
                seed.countDown();
            });
            assertThat(seed.await(5, TimeUnit.SECONDS)).isTrue();

            // 无快照任务：任务期内不得看到工作线程残留，结束后恢复
            AtomicReference<Optional<Snapshot>> during = new AtomicReference<>();
            AtomicReference<Optional<Snapshot>> after = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            pool.submit(() -> {
                Optional<Snapshot> previous = RequestTimeZoneContext.capture();
                RequestTimeZoneContext.clear();
                try {
                    during.set(RequestTimeZoneContext.capture());
                } finally {
                    previous.ifPresentOrElse(RequestTimeZoneContext::set, RequestTimeZoneContext::clear);
                }
                after.set(RequestTimeZoneContext.capture());
                done.countDown();
            });
            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(during.get()).isEmpty();
            assertThat(after.get().map(Snapshot::zoneId)).contains(BERLIN);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void 快照字段全部非null() {
        assertThatThrownBy(() -> new Snapshot(null, Source.HEADER, FallbackReason.NONE))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Snapshot(SHANGHAI, null, FallbackReason.NONE))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Snapshot(SHANGHAI, Source.HEADER, null))
                .isInstanceOf(NullPointerException.class);
    }
}
