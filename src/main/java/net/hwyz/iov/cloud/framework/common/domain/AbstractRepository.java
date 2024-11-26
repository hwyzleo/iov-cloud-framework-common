package net.hwyz.iov.cloud.framework.common.domain;

import java.util.Optional;

/**
 * 领域仓库抽象类
 *
 * @param <ID>        主键
 * @param <DomainObj> 领域对象
 * @author hwyz_leo
 */
public abstract class AbstractRepository<ID, DomainObj> implements BaseRepository<ID, DomainObj> {
    public abstract Optional<DomainObj> getById(ID id);

    public abstract boolean save(DomainObj obj);

}
