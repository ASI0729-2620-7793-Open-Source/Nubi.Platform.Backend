package com.acme.nubi.platform.profile.domain.repository;

import com.acme.nubi.platform.profile.domain.model.aggregates.NeurodivergentProfile;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia del agregado NeurodivergentProfile.
 * La implementación vive en la infraestructura; el dominio solo conoce este
 * contrato.
 */
public interface ProfileRepository {

    NeurodivergentProfile save(NeurodivergentProfile profile);

    Optional<NeurodivergentProfile> findById(Long id);

    /** Perfiles activos de una cuenta, con el cuidador principal dentro. */
    List<NeurodivergentProfile> findByAccountId(Long accountId);

    /** Perfiles en los que la cuenta figura como cuidador activo. */
    List<NeurodivergentProfile> findByCaregiverAccountId(Long accountId);

    /** Cuenta que posee el perfil, leída del cuidador principal. */
    Optional<Long> findOwnerAccountId(Long profileId);

    void delete(NeurodivergentProfile profile);
}
