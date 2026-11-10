package com.acme.nubi.platform.profile.application.dto;

import com.acme.nubi.platform.profile.domain.model.aggregates.NeurodivergentProfile;
import com.acme.nubi.platform.profile.domain.model.entities.ProfileCaregiver;
import com.acme.nubi.platform.profile.domain.model.entities.TrustedContact;
import com.acme.nubi.platform.profile.domain.model.enums.CommunicativeNeed;
import com.acme.nubi.platform.profile.domain.model.enums.Gender;
import com.acme.nubi.platform.profile.domain.model.enums.SensitivityLevel;
import com.acme.nubi.platform.profile.domain.model.valueobjects.DeclaredDiagnosis;
import com.acme.nubi.platform.profile.domain.model.valueobjects.SensoryProfile;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Contratos de lectura del Bounded Context. Son registros inmutables: el
 * dominio nunca expone sus propias clases fuera del backend.
 */
public final class ProfileResponses {

    private ProfileResponses() {
    }

    /** Perfil completo, incluidos sus hijos (aggregate root). */
    public record ProfileResponse(
            Long id,
            String firstName,
            String lastName,
            String nickname,
            String displayName,
            Integer age,
            Gender gender,
            String photoUrl,
            CommunicativeNeed communicativeNeed,
            boolean active,
            SensoryProfileResponse sensoryProfile,
            DiagnosisResponse diagnosis,
            List<CaregiverResponse> caregivers,
            List<TrustedContactResponse> trustedContacts,
            Instant createdAt,
            Instant updatedAt) {

        public static ProfileResponse from(NeurodivergentProfile profile) {
            return new ProfileResponse(
                    profile.id(),
                    profile.firstName(),
                    profile.lastName(),
                    profile.nickname(),
                    profile.displayName(),
                    profile.age(),
                    profile.gender(),
                    profile.photoUrl(),
                    profile.communicativeNeed(),
                    profile.active(),
                    profile.sensoryProfile() == null
                            ? null
                            : SensoryProfileResponse.from(profile.sensoryProfile()),
                    profile.diagnosis() == null
                            ? null
                            : DiagnosisResponse.from(profile.diagnosis()),
                    profile.caregivers().stream().map(CaregiverResponse::from).toList(),
                    profile.trustedContacts().stream()
                            .map(TrustedContactResponse::from).toList(),
                    profile.createdAt(),
                    profile.updatedAt());
        }
    }

    /** Perfil sensorial completo, reemplazado en bloque por PUT (US-03). */
    public record SensoryProfileResponse(
            SensitivityLevel auditory,
            SensitivityLevel visual,
            SensitivityLevel tactile,
            boolean lowStimulationEnabled,
            boolean prioritizeVisuals,
            boolean confirmAudio) {

        public static SensoryProfileResponse from(SensoryProfile sensory) {
            return new SensoryProfileResponse(
                    sensory.auditory(),
                    sensory.visual(),
                    sensory.tactile(),
                    sensory.lowStimulationEnabled(),
                    sensory.prioritizeVisuals(),
                    sensory.confirmAudio());
        }
    }

    /** Diagnóstico declarado por el cuidador (US-04). */
    public record DiagnosisResponse(
            Long id,
            String condition,
            String customDescription,
            String diagnosedBy,
            LocalDate diagnosisDate,
            String professionalNotes) {

        public static DiagnosisResponse from(DeclaredDiagnosis diagnosis) {
            return new DiagnosisResponse(
                    null,
                    diagnosis.displayCondition(),
                    diagnosis.customDescription(),
                    diagnosis.diagnosedBy(),
                    diagnosis.diagnosisDate(),
                    diagnosis.professionalNotes());
        }
    }

    /** Cuidador asociado al perfil (US-06). */
    public record CaregiverResponse(
            Long id,
            String invitedEmail,
            Long accountId,
            String role,
            String status,
            Instant invitedAt,
            Instant acceptedAt) {

        public static CaregiverResponse from(ProfileCaregiver caregiver) {
            return new CaregiverResponse(
                    caregiver.id(),
                    caregiver.invitedEmail(),
                    caregiver.accountId(),
                    caregiver.role().name(),
                    caregiver.status().name(),
                    caregiver.invitedAt(),
                    caregiver.acceptedAt());
        }
    }

    /** Contacto de confianza y su prioridad de escalamiento. */
    public record TrustedContactResponse(
            Long id,
            String fullName,
            String phone,
            String email,
            String relationship,
            int priorityOrder) {

        public static TrustedContactResponse from(TrustedContact contact) {
            return new TrustedContactResponse(
                    contact.id(),
                    contact.fullName(),
                    contact.phone(),
                    contact.email(),
                    contact.relationship(),
                    contact.priorityOrder());
        }
    }
}
