package com.acme.nubi.platform.crisismanagement.application.internal.outboundservices.acl;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryProfileSnapshot;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Anti-Corruption Layer hacia Perfil y Personalización (ProfileContextFacade): Gestión de Crisis
 * solo necesita saber si el perfil existe y leer su perfil sensorial para personalizar la guía.
 * <p>
 * Perfil y Personalización todavía no publica su fachada en este backend. Hasta entonces se asume
 * que el perfil existe y que no tiene sensibilidades registradas (se usa la guía genérica);
 * cuando la fachada exista, solo cambia el cuerpo de estos dos métodos.
 */
@Service("crisisManagementExternalProfileService")
public class ExternalProfileService {

    public boolean existsProfile(ProfileId profileId) {
        return true;
    }

    public Optional<SensoryProfileSnapshot> fetchSensoryProfile(ProfileId profileId) {
        return Optional.empty();
    }
}
