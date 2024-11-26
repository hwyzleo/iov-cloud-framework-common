package net.hwyz.iov.cloud.framework.common.domain;

import java.util.Optional;

/**
 * 基础领域仓库接口
 *
 * @author hwyz_leo
 */
public interface BaseRepository<ID, DomainObj> {

    /**
     * 根据主键获取领域对象
     *
     * @param id 主键
     * @return 领域对象
     */
    Optional<DomainObj> getById(ID id);

    /**
     * 保存领域对象
     *
     * @param domainObj 领域对象
     * @return 是否成功
     */
    boolean save(DomainObj domainObj);

}
