package com.acme.nubi.platform.iam.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.iam.infrastructure.persistence.jpa.entities.UserAccountPersistenceEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountPersistenceRepository extends JpaRepository<UserAccountPersistenceEntity, Long> {

    Optional<UserAccountPersistenceEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
