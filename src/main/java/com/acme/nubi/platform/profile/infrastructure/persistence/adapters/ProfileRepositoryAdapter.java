package com.acme.nubi.platform.profile.infrastructure.persistence.adapters;

import com.acme.nubi.platform.profile.domain.model.aggregates.NeurodivergentProfile;
import com.acme.nubi.platform.profile.domain.model.enums.CaregiverRole;
import com.acme.nubi.platform.profile.domain.model.enums.InvitationStatus;
import com.acme.nubi.platform.profile.domain.repository.ProfileRepository;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.ProfileJpaEntity;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.repositories.ProfileJpaRepository;
import com.acme.nubi.platform.profile.infrastructure.persistence.mappers.ProfileMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de persistencia del agregado NeurodivergentProfile.
 * Traduce entre el lenguaje del dominio y el de Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class ProfileRepositoryAdapter implements ProfileRepository {

    private final ProfileJpaRepository jpaRepository;

    @Override
    @Transactional
    public NeurodivergentProfile save(NeurodivergentProfile profile) {
        ProfileJpaEntity entity = profile.id() == null
                ? new ProfileJpaEntity()
                : jpaRepository.findById(profile.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Perfil " + profile.id() + " ya no existe."));
        ProfileMapper.applyTo(entity, profile);
        ProfileJpaEntity saved = jpaRepository.saveAndFlush(entity);
        // Se relee desde la entidad guardada para devolver los ids que la base
        // de datos acaba de asignar a los hijos nuevos.
        return ProfileMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NeurodivergentProfile> findById(Long id) {
        return jpaRepository.findById(id).map(ProfileMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NeurodivergentProfile> findByAccountId(Long accountId) {
        return jpaRepository
                .findActiveOwnedBy(accountId, CaregiverRole.PRIMARY, InvitationStatus.ACTIVE)
                .stream()
                .map(ProfileMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NeurodivergentProfile> findByCaregiverAccountId(Long accountId) {
        return jpaRepository
                .findActiveAccessibleBy(accountId, InvitationStatus.ACTIVE)
                .stream()
                .map(ProfileMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findOwnerAccountId(Long profileId) {
        return findById(profileId)
                .flatMap(profile -> profile.caregivers().stream()
                        .filter(caregiver -> caregiver.isPrincipal() && caregiver.isActive())
                        .map(caregiver -> caregiver.accountId())
                        .findFirst());
    }

    @Override
    @Transactional
    public void delete(NeurodivergentProfile profile) {
        if (profile.id() == null) {
            return;
        }
        jpaRepository.findById(profile.id()).ifPresent(jpaRepository::delete);
    }
}
