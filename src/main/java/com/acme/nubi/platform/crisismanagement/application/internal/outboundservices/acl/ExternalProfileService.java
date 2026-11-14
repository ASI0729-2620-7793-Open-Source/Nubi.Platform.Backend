package com.acme.nubi.platform.crisismanagement.application.internal.outboundservices.acl;

import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensitivityLevel;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryProfileSnapshot;
import com.acme.nubi.platform.profile.interfaces.acl.ProfilesContextFacade;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Anti-Corruption Layer hacia Perfil y Personalización (ProfilesContextFacade): Gestión de Crisis
 * necesita saber si el perfil existe y leer su perfil sensorial para ordenar la guía según el
 * sentido más sensible del niño.
 */
@Service("crisisManagementExternalProfileService")
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
                        SensitivityLevel.valueOf(data.auditory()),
                        SensitivityLevel.valueOf(data.visual()),
                        SensitivityLevel.valueOf(data.tactile())));
    }
}
