package net.hwyz.iov.cloud.framework.common.bean;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页对象
 *
 * @author hwyz_leo
 */
@Data
public class Page<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<T> rows;

    /**
     * 表格数据对象
     */
    public Page() {
    }

    /**
     * 分页
     *
     * @param rawlist 原始列表数据
     * @param list    列表数据
     */
    public Page(List rawlist, List<T> list) {
        this.total = (new PageInfo(rawlist)).getTotal();
        this.rows = list;
    }

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public Page(List<T> list, long total) {
        this.rows = list;
        this.total = total;
    }
}