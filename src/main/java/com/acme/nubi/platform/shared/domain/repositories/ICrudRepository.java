package com.acme.nubi.platform.shared.domain.repositories;

import java.util.Optional;

public interface ICrudRepository<T, ID> {
    T save(T entity);

    Optional<T> findById(ID id);

    boolean existsById(ID id);
}
