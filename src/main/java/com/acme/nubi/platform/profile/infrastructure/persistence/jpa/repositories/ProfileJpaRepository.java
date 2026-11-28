package com.acme.nubi.platform.profile.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.profile.domain.model.enums.CaregiverRole;
import com.acme.nubi.platform.profile.domain.model.enums.InvitationStatus;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.ProfileJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acceso a datos del perfil. Las consultas se expresan en términos de JPA. */
public interface ProfileJpaRepository extends JpaRepository<ProfileJpaEntity, Long> {

    /**
     * Perfiles activos donde la cuenta es cuidador principal.
     * Se usa EXISTS porque el filtro mezcla columnas del perfil (active) con
     * columnas del hijo (accountId, role, status), algo que las consultas
     * derivadas de Spring Data no pueden expresar.
     */
    @Query("""
            select p from ProfileJpaEntity p
             where p.active = true
               and exists (select 1 from ProfileCaregiverJpaEntity c
                            where c.profile = p
                              and c.accountId = :accountId
                              and c.role = :role
                              and c.status = :status)
            """)
    List<ProfileJpaEntity> findActiveOwnedBy(
            @Param("accountId") Long accountId,
            @Param("role") CaregiverRole role,
            @Param("status") InvitationStatus status);

    /** Perfiles activos donde la cuenta figura como cuidador (principal o invitado). */
    @Query("""
            select p from ProfileJpaEntity p
             where p.active = true
               and exists (select 1 from ProfileCaregiverJpaEntity c
                            where c.profile = p
                              and c.accountId = :accountId
                              and c.status = :status)
            """)
    List<ProfileJpaEntity> findActiveAccessibleBy(
            @Param("accountId") Long accountId,
            @Param("status") InvitationStatus status);

    @Query("""
            select p from ProfileJpaEntity p
             where p.active = true
               and exists (select 1 from ProfileCaregiverJpaEntity c
                            where c.profile = p
                              and lower(c.invitedEmail) = lower(:email)
                              and c.status = :status)
            """)
    List<ProfileJpaEntity> findActiveInvitationsFor(
            @Param("email") String email,
            @Param("status") InvitationStatus status);
}
