package net.hwyz.iov.cloud.framework.common.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 基础领域对象
 *
 * @author hwyz_leo
 */
@Getter
@SuperBuilder
public abstract class BaseDo<ID> {

    /**
     * 主键
     */
    protected ID id;

    /**
     * 领域对象状态
     */
    private DoState state;

    /**
     * 状态初始化
     */
    protected void stateInit() {
        state = DoState.NEW;
    }

    /**
     * 被读取时状态
     */
    public void stateLoad() {
        state = DoState.UNCHANGED;
    }

    /**
     * 状态改变
     */
    protected void stateChange() {
        if (state == DoState.UNCHANGED) {
            state = DoState.CHANGED;
        }
    }

    /**
     * 状态删除
     */
    protected void stateDelete() {
        state = DoState.DELETED;
    }

    /**
     * 状态已持久化
     *
     * @param id ID
     */
    public void statePersistent(ID id) {
        this.id = id;
        state = DoState.UNCHANGED;
    }

}
