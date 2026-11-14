package com.acme.nubi.platform.profile.interfaces.acl;

import com.acme.nubi.platform.profile.domain.model.aggregates.NeurodivergentProfile;
import com.acme.nubi.platform.profile.domain.model.valueobjects.SensoryProfile;
import com.acme.nubi.platform.profile.domain.repository.ProfileRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fachada pública del Bounded Context Perfil y Personalización. Los demás contextos
 * (Autorregulación, Gestión de Crisis, Rutina Visual) la consumen desde su Anti-Corruption
 * Layer y solo reciben tipos primitivos: nunca las clases del dominio de Perfil.
 */
@Service
public class ProfilesContextFacade {

    private final ProfileRepository profileRepository;

    public ProfilesContextFacade(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /** Verdadero si el perfil existe y no fue archivado. */
    @Transactional(readOnly = true)
    public boolean existsActiveProfile(Long profileId) {
        if (profileId == null) {
            return false;
        }
        return profileRepository.findById(profileId).map(NeurodivergentProfile::active).orElse(false);
    }

    /** Sensibilidades del niño; vacío si el perfil no existe o está archivado. */
    @Transactional(readOnly = true)
    public Optional<SensoryData> fetchSensoryProfile(Long profileId) {
        if (profileId == null) {
            return Optional.empty();
        }
        return profileRepository.findById(profileId)
                .filter(NeurodivergentProfile::active)
                .map(profile -> {
                    SensoryProfile sensory = profile.sensoryProfile() == null
                            ? SensoryProfile.withDefaultValues()
                            : profile.sensoryProfile();
                    return new SensoryData(
                            sensory.auditory().name(),
                            sensory.visual().name(),
                            sensory.tactile().name(),
                            sensory.lowStimulationEnabled(),
                            sensory.prioritizeVisuals());
                });
    }

    /**
     * Copia de las sensibilidades expresadas con cadenas: LOW, MEDIUM, HIGH o VERY_HIGH.
     */
    public record SensoryData(
            String auditory,
            String visual,
            String tactile,
            boolean lowStimulationEnabled,
            boolean prioritizeVisuals) {
    }
}
