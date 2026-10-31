package net.hwyz.iov.cloud.framework.common.time;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/**
 * 请求时区上下文（FW-COMMON-DSN-CR-002，RD-CTZ-001）。
 * <p>
 * 纯 Java {@link ThreadLocal} 上下文，仅依赖 JDK，无 Spring/Servlet/Feign 依赖、
 * 无默认地区配置、无静态可变默认值。WEB 边界层负责请求完成 {@link #clear()}；
 * 任务包装器在工作线程 {@link #open(Snapshot)} 并在 finally {@link Scope#close()}。
 * <p>
 * 契约：
 * <ul>
 *   <li>{@link #current()}：未绑定抛 {@link IllegalStateException}，不读取
 *       systemDefault、不隐式返回上海；</li>
 *   <li>{@link #currentOptional()}/{@link #capture()}：无绑定返回 empty，无副作用；</li>
 *   <li>{@link #set(Snapshot)} 拒绝 null；{@link #clear()} 使用 {@code ThreadLocal.remove()}
 *       且幂等（非 set(null)）；</li>
 *   <li>{@link #open(Snapshot)}：保存当前绑定并绑定新快照，返回 {@link Scope}；
 *       close 恢复原绑定（无原值则 remove）；重复 close 幂等；限同线程、LIFO 使用，
 *       错线程/错序抛 {@link IllegalStateException} 且不修改其他线程状态；</li>
 *   <li>不使用 InheritableThreadLocal，不自动注册线程池，不保存 request/principal。</li>
 * </ul>
 * 调用方不得在打开 scope 内绕过它任意 set/clear；本类属于受约束的低层 API，
 * 业务代码应优先使用 WEB {@code DisplayTimeFacade} 等边界组件。
 *
 * @author hwyz_leo
 * @since 2026-10-31
 */
public final class RequestTimeZoneContext {

    private static final ThreadLocal<Snapshot> HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Deque<Scope>> SCOPE_STACK = new ThreadLocal<>();

    private RequestTimeZoneContext() {
    }

    /**
     * 当前线程绑定的时区。
     *
     * @return 快照中的 {@link ZoneId}
     * @throws IllegalStateException 当前线程未绑定快照
     */
    public static java.time.ZoneId current() {
        Snapshot snapshot = HOLDER.get();
        if (snapshot == null) {
            throw new IllegalStateException("请求时区上下文未绑定（当前线程无请求快照）");
        }
        return snapshot.zoneId();
    }

    /**
     * 当前线程的快照（无绑定返回 empty）。
     */
    public static Optional<Snapshot> currentOptional() {
        return Optional.ofNullable(HOLDER.get());
    }

    /**
     * 绑定快照。
     *
     * @param snapshot 非 null
     * @throws NullPointerException snapshot 为 null
     */
    public static void set(Snapshot snapshot) {
        HOLDER.set(Objects.requireNonNull(snapshot, "snapshot"));
    }

    /**
     * 清除当前线程绑定（{@code ThreadLocal.remove()}，幂等，不是 set(null)）。
     */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 打开一个作用域：保存当前绑定并绑定新快照，返回可关闭的 {@link Scope}。
     *
     * @param snapshot 非 null
     * @return 作用域；close 恢复原绑定（无原值则 remove）
     * @throws NullPointerException snapshot 为 null
     */
    public static Scope open(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        return new Scope(snapshot, Thread.currentThread());
    }

    /**
     * 捕获当前不可变快照用于显式传播（异步任务/Feign 透传等）。
     *
     * @return 当前快照；无绑定返回 empty
     */
    public static Optional<Snapshot> capture() {
        return Optional.ofNullable(HOLDER.get());
    }

    /**
     * 请求时区作用域（FW-COMMON-DSN-CR-002，RD-CTZ-001）。
     * <p>
     * 限同线程、LIFO 使用：close 恢复进入前的绑定（无原值则 remove）；
     * 重复 close 幂等；错线程/错序抛 {@link IllegalStateException} 且不修改其他线程状态。
     */
    public static final class Scope implements AutoCloseable {

        private final Snapshot bound;
        private final Snapshot previous;
        private final Thread owner;
        private boolean closed;

        private Scope(Snapshot bound, Thread owner) {
            this.bound = bound;
            this.previous = HOLDER.get();
            this.owner = owner;
            HOLDER.set(bound);
            stack().push(this);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            Thread current = Thread.currentThread();
            if (current != owner) {
                throw new IllegalStateException("Scope 必须在绑定线程关闭（错线程关闭拒绝且不修改状态）");
            }
            Deque<Scope> stack = stack();
            if (stack.isEmpty() || stack.peek() != this) {
                throw new IllegalStateException("Scope 必须按 LIFO 顺序关闭（错序关闭拒绝且不修改状态）");
            }
            stack.pop();
            closed = true;
            if (previous != null) {
                HOLDER.set(previous);
            } else {
                HOLDER.remove();
            }
        }

        private static Deque<Scope> stack() {
            Deque<Scope> stack = SCOPE_STACK.get();
            if (stack == null) {
                stack = new ArrayDeque<>();
                SCOPE_STACK.set(stack);
            }
            return stack;
        }
    }
}
