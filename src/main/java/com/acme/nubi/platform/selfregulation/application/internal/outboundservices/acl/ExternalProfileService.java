package com.acme.nubi.platform.selfregulation.application.internal.outboundservices.acl;

import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.SensoryProfileSnapshot;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Anti-Corruption Layer hacia Perfil y Personalización: Autorregulación solo necesita saber si
 * el perfil existe y leer su perfil sensorial.
 * <p>
 * Perfil y Personalización todavía no publica su ProfilesContextFacade en este backend. Hasta
 * entonces se asume que el perfil existe y que no tiene restricciones sensoriales; cuando la
 * fachada exista, solo cambia el cuerpo de estos dos métodos.
 */
@Service("selfRegulationExternalProfileService")
public class ExternalProfileService {

    public boolean existsProfile(ProfileId profileId) {
        return true;
    }

    public Optional<SensoryProfileSnapshot> fetchSensoryProfile(ProfileId profileId) {
        return Optional.empty();
    }
}
