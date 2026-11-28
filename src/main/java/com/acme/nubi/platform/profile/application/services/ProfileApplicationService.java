package com.acme.nubi.platform.profile.application.services;

import com.acme.nubi.platform.profile.application.dto.ProfileResponses.InvitationResponse;
import com.acme.nubi.platform.iam.interfaces.acl.IamContextFacade;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.CreateProfileRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.InviteCaregiverRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.LinkTrustedContactRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.ReorderTrustedContactsRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateDiagnosisRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdatePhotoRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateProfileRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateSensoryProfileRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileResponses.CaregiverResponse;
import com.acme.nubi.platform.profile.application.dto.ProfileResponses.DiagnosisResponse;
import com.acme.nubi.platform.profile.application.dto.ProfileResponses.ProfileResponse;
import com.acme.nubi.platform.profile.application.dto.ProfileResponses.SensoryProfileResponse;
import com.acme.nubi.platform.profile.application.dto.ProfileResponses.TrustedContactResponse;
import com.acme.nubi.platform.profile.application.errors.BusinessError;
import com.acme.nubi.platform.profile.application.errors.DomainGuard;
import com.acme.nubi.platform.profile.domain.model.aggregates.NeurodivergentProfile;
import com.acme.nubi.platform.profile.domain.model.aggregates.Subscription;
import com.acme.nubi.platform.profile.domain.model.entities.ProfileCaregiver;
import com.acme.nubi.platform.profile.domain.model.entities.TrustedContact;
import com.acme.nubi.platform.profile.domain.model.enums.ConditionType;
import com.acme.nubi.platform.profile.domain.model.valueobjects.DeclaredDiagnosis;
import com.acme.nubi.platform.profile.domain.model.valueobjects.PlanLimits;
import com.acme.nubi.platform.profile.domain.model.valueobjects.SensoryProfile;
import com.acme.nubi.platform.profile.domain.repository.ProfileRepository;
import com.acme.nubi.platform.profile.domain.repository.SubscriptionRepository;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del perfil (US-01 a US-06).
 *
 * <p>Cada método devuelve {@code Result<T, BusinessError>} en lugar de lanzar
 * excepciones: el controlador traduce el error al código HTTP correspondiente.
 */
@Service
@RequiredArgsConstructor
public class ProfileApplicationService {

    private final ProfileRepository profileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IamContextFacade iam;

    // -------------------------------------------------------------------
    // US-01 — Crear y consultar
    // -------------------------------------------------------------------

    @Transactional
    public Result<ProfileResponse, BusinessError> create(
            Long accountId, CreateProfileRequest request) {
        // Regla de negocio: una cuenta acompaña a un único niño con autismo.
        int ownedProfiles = profileRepository.findByAccountId(accountId).size();
        if (ownedProfiles >= PlanLimits.SINGLE_CHILD) {
            return Result.failure(BusinessError.conflict(
                    "profile.single.child",
                    "Tu cuenta ya acompaña a un niño. Nubi permite un solo perfil por cuenta."));
        }
        subscriptionFor(accountId);

        ProfileCaregiver primary =
                ProfileCaregiver.primary(accountId, request.primaryCaregiverEmail(), Instant.now());
        Result<NeurodivergentProfile, BusinessError> created = DomainGuard.guard(() ->
                NeurodivergentProfile.create(
                        request.firstName(),
                        request.lastName(),
                        request.nickname(),
                        request.age(),
                        request.gender(),
                        request.communicativeNeed(),
                        primary,
                        Instant.now())
                        // Todo perfil nace con el diagnóstico de autismo declarado.
                        .declareDiagnosis(DeclaredDiagnosis.autism()));
        return persist(created);
    }

    /** Perfiles donde la cuenta es cuidador principal (US-01). */
    @Transactional(readOnly = true)
    public List<ProfileResponse> listOwned(Long accountId) {
        return profileRepository.findByAccountId(accountId).stream()
                .map(ProfileResponse::from)
                .toList();
    }

    /** Perfiles donde la cuenta participa, incluidos los que cuida por invitación. */
    @Transactional(readOnly = true)
    public List<ProfileResponse> listAccessible(Long accountId) {
        return profileRepository.findByCaregiverAccountId(accountId).stream()
                .map(ProfileResponse::from)
                .toList();
    }

    /** Invitaciones que la cuenta todavía no aceptó, buscadas por su correo (US-06). */
    @Transactional(readOnly = true)
    public List<InvitationResponse> listPendingInvitations(Long accountId) {
        String email = iam.fetchEmail(accountId).orElse(null);
        if (email == null) {
            return List.of();
        }
        return profileRepository.findByPendingInvitationEmail(email).stream()
                .map(ProfileResponse::from)
                .flatMap(profile -> profile.caregivers().stream()
                        .filter(caregiver -> "PENDING".equals(caregiver.status())
                                && email.equalsIgnoreCase(caregiver.invitedEmail()))
                        .map(caregiver -> new InvitationResponse(
                                profile.id(), caregiver.id(), profile.displayName(),
                                caregiver.role(), caregiver.invitedAt())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Result<ProfileResponse, BusinessError> get(Long accountId, Long profileId) {
        return DomainGuard.map(loadFor(accountId, profileId), ProfileResponse::from);
    }

    // -------------------------------------------------------------------
    // US-02 — Editar ficha general
    // -------------------------------------------------------------------

    @Transactional
    public Result<ProfileResponse, BusinessError> update(
            Long accountId, Long profileId, UpdateProfileRequest request) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        return persist(DomainGuard.guard(() ->
                profile.updateGeneral(
                        request.firstName(),
                        request.lastName(),
                        request.nickname(),
                        request.age(),
                        request.gender(),
                        request.communicativeNeed())));
    }

    // -------------------------------------------------------------------
    // US-03 — Sensibilidades sensoriales
    // -------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Result<SensoryProfileResponse, BusinessError> getSensitivities(
            Long accountId, Long profileId) {
        return DomainGuard.map(loadFor(accountId, profileId), profile -> {
            SensoryProfile sensory = profile.sensoryProfile();
            return SensoryProfileResponse.from(
                    sensory == null ? SensoryProfile.withDefaultValues() : sensory);
        });
    }

    @Transactional
    public Result<SensoryProfileResponse, BusinessError> updateSensitivities(
            Long accountId, Long profileId, UpdateSensoryProfileRequest request) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        SensoryProfile sensory = new SensoryProfile(
                request.auditory(),
                request.visual(),
                request.tactile(),
                request.lowStimulationEnabled(),
                request.prioritizeVisuals(),
                request.confirmAudio());
        Result<NeurodivergentProfile, BusinessError> updated =
                DomainGuard.guard(() -> profile.registerSensitivities(sensory));
        Result<ProfileResponse, BusinessError> saved = persist(updated);
        return DomainGuard.map(saved, ProfileResponse::sensoryProfile);
    }

    // -------------------------------------------------------------------
    // US-04 — Diagnóstico declarado
    // -------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Result<DiagnosisResponse, BusinessError> getDiagnosis(
            Long accountId, Long profileId) {
        return DomainGuard.flatMap(
                loadFor(accountId, profileId),
                profile -> profile.diagnosis() == null
                        ? Result.failure(BusinessError.notFound(
                                "diagnosis.notFound", "El perfil no tiene diagnóstico declarado."))
                        : Result.success(DiagnosisResponse.from(profile.diagnosis())));
    }

    @Transactional
    public Result<DiagnosisResponse, BusinessError> updateDiagnosis(
            Long accountId, Long profileId, UpdateDiagnosisRequest request) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        Result<DeclaredDiagnosis, BusinessError> diagnosis = DomainGuard.guard(() ->
                new DeclaredDiagnosis(
                        request.condition() == null ? ConditionType.ASD : request.condition(),
                        request.customDescription(),
                        request.diagnosedBy(),
                        request.diagnosisDate(),
                        request.professionalNotes()));
        if (diagnosis.isFailure()) {
            return Result.failure(diagnosis.failure().orElseThrow());
        }
        Result<ProfileResponse, BusinessError> saved =
                persist(DomainGuard.guard(() -> profile.declareDiagnosis(diagnosis.success().orElseThrow())));
        return DomainGuard.map(saved, ProfileResponse::diagnosis);
    }

    // -------------------------------------------------------------------
    // US-05 — Foto de perfil
    // -------------------------------------------------------------------

    @Transactional
    public Result<ProfileResponse, BusinessError> updatePhoto(
            Long accountId, Long profileId, UpdatePhotoRequest request) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        return persist(DomainGuard.guard(() -> profile.changePhoto(request.photoUrl())));
    }

    // -------------------------------------------------------------------
    // US-06 — Cuidadores asociados
    // -------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Result<List<CaregiverResponse>, BusinessError> listCaregivers(
            Long accountId, Long profileId) {
        return DomainGuard.map(
                loadFor(accountId, profileId),
                profile -> profile.caregivers().stream()
                        .map(CaregiverResponse::from)
                        .toList());
    }

    @Transactional
    public Result<CaregiverResponse, BusinessError> inviteCaregiver(
            Long accountId, Long profileId, InviteCaregiverRequest request) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();

        Result<NeurodivergentProfile, BusinessError> invited = DomainGuard.guard(() ->
                profile.inviteCaregiver(request.email(), request.role()));
        if (invited.isFailure()) {
            return Result.failure(invited.failure().orElseThrow());
        }

        // El límite se evalúa sobre el estado previo, después de validar el correo.
        Subscription subscription = subscriptionFor(accountId);
        if (!subscription.allowsAnotherCaregiver(profile.caregiversInUse())) {
            return Result.failure(BusinessError.conflict(
                    "subscription.caregiver.limit",
                    "El plan "
                            + subscription.plan()
                            + " permite hasta "
                            + subscription.limits().maxCaregiversPerProfile()
                            + " cuidadores por perfil."));
        }

        return DomainGuard.flatMap(
                persist(invited), response -> caregiverByEmail(response, request.email()));
    }

    /** El cuidador invitado acepta con su propia cuenta: no necesita acceso previo. */
    @Transactional
    public Result<CaregiverResponse, BusinessError> acceptCaregiver(
            Long accountId, Long profileId, Long caregiverId) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadIfExists(profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        // Solo la cuenta del correo invitado puede aceptar la invitación.
        String email = iam.fetchEmail(accountId).orElse(null);
        boolean someoneElse = email != null && ProfileResponse.from(profile).caregivers().stream()
                .anyMatch(caregiver -> caregiverId.equals(caregiver.id())
                        && !email.equalsIgnoreCase(caregiver.invitedEmail()));
        if (someoneElse) {
            return Result.failure(BusinessError.conflict(
                    "caregiver.invitation.otherAccount",
                    "La invitación corresponde a otro correo."));
        }
        Result<NeurodivergentProfile, BusinessError> accepted = DomainGuard.guard(() ->
                profile.acceptCaregiver(caregiverId, accountId));
        return DomainGuard.flatMap(
                persist(accepted), response -> caregiverById(response, caregiverId));
    }

    @Transactional
    public Result<CaregiverResponse, BusinessError> revokeCaregiver(
            Long accountId, Long profileId, Long caregiverId) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        Result<NeurodivergentProfile, BusinessError> revoked = DomainGuard.guard(() ->
                profile.revokeCaregiver(caregiverId));
        return DomainGuard.flatMap(
                persist(revoked), response -> caregiverById(response, caregiverId));
    }

    // -------------------------------------------------------------------
    // Contactos de confianza
    // -------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Result<List<TrustedContactResponse>, BusinessError> listTrustedContacts(
            Long accountId, Long profileId) {
        return DomainGuard.map(
                loadFor(accountId, profileId),
                profile -> profile.trustedContacts().stream()
                        .map(TrustedContactResponse::from)
                        .toList());
    }

    @Transactional
    public Result<TrustedContactResponse, BusinessError> linkTrustedContact(
            Long accountId, Long profileId, LinkTrustedContactRequest request) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        Result<TrustedContact, BusinessError> contact = DomainGuard.guard(() ->
                new TrustedContact(
                        null,
                        request.fullName(),
                        request.phone(),
                        request.email(),
                        request.relationship(),
                        request.priorityOrder()));
        if (contact.isFailure()) {
            return Result.failure(contact.failure().orElseThrow());
        }
        Result<NeurodivergentProfile, BusinessError> linked = DomainGuard.guard(() ->
                profile.linkTrustedContact(contact.success().orElseThrow()));
        return DomainGuard.flatMap(
                persist(linked), response -> contactByPriority(response, request.priorityOrder()));
    }

    @Transactional
    public Result<List<TrustedContactResponse>, BusinessError> reorderTrustedContacts(
            Long accountId, Long profileId, ReorderTrustedContactsRequest request) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        Result<NeurodivergentProfile, BusinessError> reordered = DomainGuard.guard(() ->
                profile.reorderTrustedContacts(request.order()));
        return DomainGuard.map(persist(reordered), ProfileResponse::trustedContacts);
    }

    @Transactional
    public Result<List<TrustedContactResponse>, BusinessError> removeTrustedContact(
            Long accountId, Long profileId, Long contactId) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        Result<NeurodivergentProfile, BusinessError> removed = DomainGuard.guard(() ->
                profile.removeTrustedContact(contactId));
        return DomainGuard.map(persist(removed), ProfileResponse::trustedContacts);
    }

    /** Archivar (DELETE lógico): el perfil deja de estar activo (US-01). */
    @Transactional
    public Result<ProfileResponse, BusinessError> archive(Long accountId, Long profileId) {
        Result<NeurodivergentProfile, BusinessError> loaded = loadFor(accountId, profileId);
        if (loaded.isFailure()) {
            return Result.failure(loaded.failure().orElseThrow());
        }
        NeurodivergentProfile profile = loaded.success().orElseThrow();
        return persist(DomainGuard.guard(profile::archive));
    }

    // -------------------------------------------------------------------
    // Internos
    // -------------------------------------------------------------------

    /** Carga el perfil y exige que la cuenta sea cuidador activo. */
    private Result<NeurodivergentProfile, BusinessError> loadFor(
            Long accountId, Long profileId) {
        Result<NeurodivergentProfile, BusinessError> found = loadIfExists(profileId);
        if (found.isFailure()) {
            return found;
        }
        NeurodivergentProfile profile = found.success().orElseThrow();
        if (!profile.isActiveCaregiver(accountId)) {
            // No se revela la existencia del perfil a cuentas ajenas.
            return Result.failure(BusinessError.notFound(
                    "profile.notAccessible",
                    "El perfil no existe o no está disponible para esta cuenta."));
        }
        return found;
    }

    private Result<NeurodivergentProfile, BusinessError> loadIfExists(Long profileId) {
        return profileRepository.findById(profileId)
                .<Result<NeurodivergentProfile, BusinessError>>map(Result::success)
                .orElseGet(() -> Result.failure(BusinessError.notFound(
                        "profile.notFound", "El perfil no existe.")));
    }

    /** Guarda el agregado y publica los eventos de dominio que haya emitido. */
    private Result<ProfileResponse, BusinessError> persist(
            Result<NeurodivergentProfile, BusinessError> source) {
        if (source.isFailure()) {
            return Result.failure(source.failure().orElseThrow());
        }
        NeurodivergentProfile profile = source.success().orElseThrow();
        return DomainGuard.guard(() -> {
            NeurodivergentProfile saved = profileRepository.save(profile);
            publishEvents(profile);
            return ProfileResponse.from(saved);
        });
    }

    private void publishEvents(AbstractDomainAggregateRoot aggregate) {
        aggregate.pullDomainEvents().forEach(eventPublisher::publishEvent);
    }

    /** Suscripción vigente de la cuenta; si no existe, nace FREEMIUM. */
    private Subscription subscriptionFor(Long accountId) {
        return subscriptionRepository.findByAccountId(accountId)
                .orElseGet(() -> subscriptionRepository.save(
                        Subscription.freemium(accountId, Instant.now())));
    }

    private static Result<CaregiverResponse, BusinessError> caregiverById(
            ProfileResponse response, Long caregiverId) {
        return response.caregivers().stream()
                .filter(caregiver -> caregiverId.equals(caregiver.id()))
                .findFirst()
                .<Result<CaregiverResponse, BusinessError>>map(Result::success)
                .orElseGet(() -> Result.failure(BusinessError.notFound(
                        "caregiver.notFound", "El cuidador no pertenece a este perfil.")));
    }

    private static Result<CaregiverResponse, BusinessError> caregiverByEmail(
            ProfileResponse response, String email) {
        return response.caregivers().stream()
                .filter(caregiver -> caregiver.invitedEmail() != null
                        && caregiver.invitedEmail().equalsIgnoreCase(email))
                .reduce((first, second) -> second)
                .<Result<CaregiverResponse, BusinessError>>map(Result::success)
                .orElseGet(() -> Result.failure(BusinessError.notFound(
                        "caregiver.notFound", "No se pudo registrar el cuidador.")));
    }

    private static Result<TrustedContactResponse, BusinessError> contactByPriority(
            ProfileResponse response, int priorityOrder) {
        return response.trustedContacts().stream()
                .filter(contact -> contact.priorityOrder() == priorityOrder)
                .findFirst()
                .<Result<TrustedContactResponse, BusinessError>>map(Result::success)
                .orElseGet(() -> Result.failure(BusinessError.notFound(
                        "contact.notFound", "No se pudo registrar el contacto.")));
    }
}
