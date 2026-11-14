package com.acme.nubi.platform.selfregulation.application.internal.outboundservices.acl;

import com.acme.nubi.platform.profile.interfaces.acl.ProfilesContextFacade;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.SensitivityLevel;
import com.acme.nubi.platform.selfregulation.domain.model.valueobjects.SensoryProfileSnapshot;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Anti-Corruption Layer hacia Perfil y Personalización: Autorregulación solo necesita saber si
 * el perfil existe y leer su perfil sensorial para ocultar los estímulos que podrían alterar al
 * niño (por ejemplo, sonidos si tiene sensibilidad auditiva alta).
 */
@Service("selfRegulationExternalProfileService")
public class ExternalProfileService {

    private final ProfilesContextFacade profilesContextFacade;

    public ExternalProfileService(ProfilesContextFacade profilesContextFacade) {
        this.profilesContextFacade = profilesContextFacade;
    }

    public boolean existsProfile(ProfileId profileId) {
        return profilesContextFacade.existsActiveProfile(profileId.value());
    }

    public Optional<SensoryProfileSnapshot> fetchSensoryProfile(ProfileId profileId) {
        return profilesContextFacade.fetchSensoryProfile(profileId.value())
                .map(data -> new SensoryProfileSnapshot(
                        profileId,
                        toLevel(data.auditory()),
                        toLevel(data.visual()),
                        toLevel(data.tactile()),
                        data.prioritizeVisuals()));
    }

    /** Autorregulación usa una escala de tres niveles: VERY_HIGH cuenta como HIGH. */
    private static SensitivityLevel toLevel(String level) {
        return switch (level) {
            case "HIGH", "VERY_HIGH" -> SensitivityLevel.HIGH;
            case "MEDIUM" -> SensitivityLevel.MEDIUM;
            default -> SensitivityLevel.LOW;
        };
    }
}
