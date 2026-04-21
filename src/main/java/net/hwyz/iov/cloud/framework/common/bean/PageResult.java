package net.hwyz.iov.cloud.framework.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果通用包装。
 *
 * @param <T> 结果项类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 结果列表
     */
    private List<T> items;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码（从 0 开始）
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 构建空结果。
     */
    public static <T> PageResult<T> empty(int page, int size) {
        return PageResult.<T>builder()
                .items(Collections.emptyList())
                .total(0)
                .page(page)
                .size(size)
                .build();
    }
}
