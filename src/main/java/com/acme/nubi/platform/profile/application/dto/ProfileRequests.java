package com.acme.nubi.platform.profile.application.dto;

import com.acme.nubi.platform.profile.domain.model.enums.CaregiverRole;
import com.acme.nubi.platform.profile.domain.model.enums.CommunicativeNeed;
import com.acme.nubi.platform.profile.domain.model.enums.ConditionType;
import com.acme.nubi.platform.profile.domain.model.enums.Gender;
import com.acme.nubi.platform.profile.domain.model.enums.PlanType;
import com.acme.nubi.platform.profile.domain.model.enums.SensitivityLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * Contratos de escritura. Las anotaciones de Jakarta Validation cubren los
 * datos obligatorios (400 con el campo en el detalle); las reglas de negocio
 * las resuelve el dominio.
 */
public final class ProfileRequests {

    private ProfileRequests() {
    }

    /** POST /profiles — US-01. */
    public record CreateProfileRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String nickname,
            @NotNull @Min(1) Integer age,
            Gender gender,
            CommunicativeNeed communicativeNeed,
            @Email String primaryCaregiverEmail) {
    }

    /** PUT /profiles/{id} — US-02. */
    public record UpdateProfileRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String nickname,
            @NotNull @Min(1) Integer age,
            Gender gender,
            CommunicativeNeed communicativeNeed) {
    }

    /** PUT /profiles/{id}/photo — US-05. */
    public record UpdatePhotoRequest(@NotBlank String photoUrl) {
    }

    /** PUT /profiles/{id}/sensory-profile — US-03. */
    public record UpdateSensoryProfileRequest(
            @NotNull SensitivityLevel auditory,
            @NotNull SensitivityLevel visual,
            @NotNull SensitivityLevel tactile,
            @NotNull Boolean lowStimulationEnabled,
            @NotNull Boolean prioritizeVisuals,
            @NotNull Boolean confirmAudio) {
    }

    /** PUT /profiles/{id}/diagnosis — US-04. La condición es opcional: siempre es ASD (autismo). */
    public record UpdateDiagnosisRequest(
            ConditionType condition,
            String customDescription,
            String diagnosedBy,
            LocalDate diagnosisDate,
            String professionalNotes) {
    }

    /** POST /profiles/{id}/caregivers — US-06. */
    public record InviteCaregiverRequest(
            @NotBlank @Email String email,
            @NotNull CaregiverRole role) {
    }

    /** PATCH /profiles/{id}/caregivers/{caregiverId} — el invitado acepta (status = ACCEPTED). */
    public record UpdateCaregiverStatusRequest(@NotBlank String status) {
    }

    /** POST /profiles/{id}/trusted-contacts. */
    public record LinkTrustedContactRequest(
            @NotBlank String fullName,
            @NotBlank String phone,
            @Email String email,
            String relationship,
            @NotNull @Min(1) Integer priorityOrder) {
    }

    /** PUT /profiles/{id}/trusted-contacts/order. */
    public record ReorderTrustedContactsRequest(@NotEmpty List<Long> order) {
    }

    /** PUT /subscriptions/plan — cambio de plan. */
    public record ChangePlanRequest(@NotNull PlanType plan) {
    }
}
