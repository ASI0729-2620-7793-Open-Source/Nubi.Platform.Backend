package com.acme.nubi.platform.profile.domain.model.aggregates;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.profile.domain.events.TrustedContactLinkedEvent;
import com.acme.nubi.platform.profile.domain.model.entities.ProfileCaregiver;
import com.acme.nubi.platform.profile.domain.model.entities.TrustedContact;
import com.acme.nubi.platform.profile.domain.model.enums.CaregiverRole;
import com.acme.nubi.platform.profile.domain.model.enums.CommunicativeNeed;
import com.acme.nubi.platform.profile.domain.model.enums.Gender;
import com.acme.nubi.platform.profile.domain.model.enums.InvitationStatus;
import com.acme.nubi.platform.profile.domain.model.valueobjects.DeclaredDiagnosis;
import com.acme.nubi.platform.profile.domain.model.valueobjects.SensoryProfile;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root. Perfil de un niño o adolescente neurodivergente (US-01 a US-06).
 *
 * <p>Controla la consistencia de sus hijos: solo este agregado crea el perfil
 * sensorial, el diagnóstico declarado, los cuidadores asociados y los contactos
 * de confianza. Cada comando devuelve una instancia nueva (inmutable), de modo
 * que las capas superiores nunca comparten estado parcial.
 */
public class NeurodivergentProfile extends AbstractDomainAggregateRoot {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String nickname;
    private final int age;
    private final Gender gender;
    private final String photoUrl;
    private final CommunicativeNeed communicativeNeed;
    private final boolean active;
    private final SensoryProfile sensoryProfile;
    private final DeclaredDiagnosis diagnosis;
    private final List<ProfileCaregiver> caregivers;
    private final List<TrustedContact> trustedContacts;
    private final Instant createdAt;
    private final Instant updatedAt;

    public NeurodivergentProfile(
            Long id,
            String firstName,
            String lastName,
            String nickname,
            int age,
            Gender gender,
            String photoUrl,
            CommunicativeNeed communicativeNeed,
            boolean active,
            SensoryProfile sensoryProfile,
            DeclaredDiagnosis diagnosis,
            List<ProfileCaregiver> caregivers,
            List<TrustedContact> trustedContacts,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.photoUrl = photoUrl;
        this.communicativeNeed = communicativeNeed;
        this.active = active;
        this.sensoryProfile = sensoryProfile;
        this.diagnosis = diagnosis;
        this.caregivers = List.copyOf(caregivers);
        this.trustedContacts = List.copyOf(trustedContacts);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Comando "Crear perfil" (US-01): nace activo, con perfil sensorial por defecto. */
    public static NeurodivergentProfile create(
            String firstName,
            String lastName,
            String nickname,
            int age,
            Gender gender,
            CommunicativeNeed communicativeNeed,
            ProfileCaregiver primaryCaregiver,
            Instant now) {
        if (firstName == null || firstName.isBlank()) {
            throw new DomainRuleViolation(
                    "profile.firstName.required", "El nombre es obligatorio.", "firstName");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new DomainRuleViolation(
                    "profile.lastName.required", "El apellido es obligatorio.", "lastName");
        }
        if (age <= 0) {
            throw new DomainRuleViolation(
                    "profile.age.invalid", "La edad debe ser mayor que cero.", "age");
        }
        if (primaryCaregiver == null || !primaryCaregiver.isPrincipal()) {
            throw new DomainRuleViolation(
                    "profile.primaryCaregiver.required",
                    "Todo perfil necesita un cuidador principal.",
                    "primaryCaregiver");
        }
        return new NeurodivergentProfile(
                null,
                firstName.trim(),
                lastName.trim(),
                nickname,
                age,
                gender == null ? Gender.PREFER_NOT_TO_SAY : gender,
                null,
                communicativeNeed == null ? CommunicativeNeed.PICTOGRAMS : communicativeNeed,
                true,
                SensoryProfile.withDefaultValues(),
                null,
                List.of(primaryCaregiver),
                List.of(),
                now,
                now);
    }

    // -------------------------------------------------------------------
    // Comandos
    // -------------------------------------------------------------------

    /** Comando "Editar ficha general" (US-02). */
    public NeurodivergentProfile updateGeneral(
            String firstName,
            String lastName,
            String nickname,
            int age,
            Gender gender,
            CommunicativeNeed communicativeNeed) {
        if (firstName == null || firstName.isBlank()) {
            throw new DomainRuleViolation(
                    "profile.firstName.required", "El nombre es obligatorio.", "firstName");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new DomainRuleViolation(
                    "profile.lastName.required", "El apellido es obligatorio.", "lastName");
        }
        if (age <= 0) {
            throw new DomainRuleViolation(
                    "profile.age.invalid", "La edad debe ser mayor que cero.", "age");
        }
        return copy(
                firstName.trim(),
                lastName.trim(),
                nickname,
                age,
                gender,
                photoUrl,
                communicativeNeed,
                active,
                sensoryProfile,
                diagnosis,
                caregivers,
                trustedContacts);
    }

    /** Comando "Registrar sensibilidades" (US-03): reemplaza el VO completo. */
    public NeurodivergentProfile registerSensitivities(SensoryProfile sensory) {
        if (sensory == null) {
            throw new DomainRuleViolation(
                    "sensoryProfile.required", "El perfil sensorial es obligatorio.",
                    "sensoryProfile");
        }
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, active,
                sensory, diagnosis, caregivers, trustedContacts);
    }

    /** Comando "Registrar diagnóstico" (US-04): reemplaza o limpia el VO. */
    public NeurodivergentProfile declareDiagnosis(DeclaredDiagnosis declared) {
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, active,
                sensoryProfile, declared, caregivers, trustedContacts);
    }

    /** Comando "Subir foto" (US-05): la URL ya fue validada por el almacenamiento. */
    public NeurodivergentProfile changePhoto(String photoUrl) {
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, active,
                sensoryProfile, diagnosis, caregivers, trustedContacts);
    }

    /** Comando "Invitar cuidador" (US-06): nace PENDING con solo el correo. */
    public NeurodivergentProfile inviteCaregiver(String email, CaregiverRole role) {
        if (role == CaregiverRole.PRIMARY) {
            throw DomainRuleViolation.conflict(
                    "caregiver.role.primary.reserved",
                    "El cuidador principal se asigna al crear el perfil.",
                    "role");
        }
        String normalized = email == null ? "" : email.trim().toLowerCase();
        boolean alreadyLinked = caregivers.stream()
                .filter(item -> item.status() != InvitationStatus.REVOKED)
                .anyMatch(item -> item.invitedEmail() != null
                        && item.invitedEmail().equalsIgnoreCase(normalized));
        if (alreadyLinked) {
            throw DomainRuleViolation.conflict(
                    "caregiver.email.duplicated",
                    "Ese correo ya está vinculado al perfil.",
                    "email");
        }
        ProfileCaregiver invited = ProfileCaregiver.invited(normalized, role, Instant.now());
        List<ProfileCaregiver> next = new ArrayList<>(caregivers);
        next.add(invited);
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, active,
                sensoryProfile, diagnosis, next, trustedContacts);
    }

    /** Comando "Aceptar invitación" (US-06): PENDING -> ACTIVE. */
    public NeurodivergentProfile acceptCaregiver(Long caregiverId, Long accountId) {
        return mapCaregiver(caregiverId, caregiver -> caregiver.accept(accountId));
    }

    /** Comando "Revocar acceso" (US-06): ACTIVE -> REVOKED (nunca el principal). */
    public NeurodivergentProfile revokeCaregiver(Long caregiverId) {
        return mapCaregiver(caregiverId, ProfileCaregiver::revoke);
    }

    /** Comando "Vincular contacto de confianza": publica TrustedContactLinkedEvent. */
    public NeurodivergentProfile linkTrustedContact(TrustedContact contact) {
        if (contact == null) {
            throw new DomainRuleViolation(
                    "contact.required", "El contacto de confianza es obligatorio.");
        }
        boolean duplicated = trustedContacts.stream()
                .anyMatch(item -> item.priorityOrder() == contact.priorityOrder());
        if (duplicated) {
            throw DomainRuleViolation.conflict(
                    "contact.priorityOrder.duplicated",
                    "Ya existe un contacto con esa prioridad.",
                    "priorityOrder");
        }
        TrustedContact linked = contact.withId(null);
        List<TrustedContact> next = new ArrayList<>(trustedContacts);
        next.add(linked);
        registerEvent(new TrustedContactLinkedEvent(
                id, null, linked.fullName(), Instant.now()));
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, active,
                sensoryProfile, diagnosis, caregivers, next);
    }

    /** Comando "Reordenar contactos": recibe los ids en el nuevo orden 1..n. */
    public NeurodivergentProfile reorderTrustedContacts(List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.size() != trustedContacts.size()) {
            throw new DomainRuleViolation(
                    "contact.order.incomplete",
                    "El orden debe incluir todos los contactos del perfil.",
                    "order");
        }
        List<TrustedContact> reordered = new ArrayList<>();
        for (int index = 0; index < orderedIds.size(); index++) {
            Long contactId = orderedIds.get(index);
            TrustedContact match = trustedContacts.stream()
                    .filter(item -> Objects.equals(item.id(), contactId))
                    .findFirst()
                    .orElseThrow(() -> DomainRuleViolation.notFound(
                            "contact.order.unknown",
                            "El contacto " + contactId + " no pertenece al perfil."));
            reordered.add(match.withPriorityOrder(index + 1));
        }
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, active,
                sensoryProfile, diagnosis, caregivers, reordered);
    }

    /** Comando "Desvincular contacto de confianza": lo quita de la red. */
    public NeurodivergentProfile removeTrustedContact(Long contactId) {
        List<TrustedContact> remaining = new ArrayList<>(trustedContacts);
        boolean removed =
                remaining.removeIf(item -> item.id() != null && item.id().equals(contactId));
        if (!removed) {
            throw DomainRuleViolation.notFound(
                    "contact.notFound", "El contacto no pertenece a este perfil.");
        }
        List<TrustedContact> renumbered = new ArrayList<>();
        for (int index = 0; index < remaining.size(); index++) {
            renumbered.add(remaining.get(index).withPriorityOrder(index + 1));
        }
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, active,
                sensoryProfile, diagnosis, caregivers, renumbered);
    }

    /** Comando "Archivar perfil" (DELETE): active = false, no borra historial. */
    public NeurodivergentProfile archive() {
        if (!active) {
            return this;
        }
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, false,
                sensoryProfile, diagnosis, caregivers, trustedContacts);
    }

    // -------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------

    public String fullName() {
        return firstName + " " + lastName;
    }

    public String displayName() {
        return nickname != null && !nickname.isBlank() ? nickname : firstName;
    }

    /** Cuidadores que ocupan cupo del plan: todos menos los revocados. */
    public int caregiversInUse() {
        return (int) caregivers.stream()
                .filter(item -> item.status() != InvitationStatus.REVOKED)
                .count();
    }

    public boolean isActiveCaregiver(Long accountId) {
        return caregivers.stream()
                .filter(item -> item.accountId() != null)
                .filter(ProfileCaregiver::isActive)
                .anyMatch(item -> item.accountId().equals(accountId));
    }

    public boolean isPrimaryCaregiver(Long accountId) {
        return caregivers.stream()
                .filter(ProfileCaregiver::isPrincipal)
                .filter(ProfileCaregiver::isActive)
                .anyMatch(item -> item.accountId() != null && item.accountId().equals(accountId));
    }

    public ProfileCaregiver findCaregiver(Long caregiverId) {
        return caregivers.stream()
                .filter(item -> item.id() != null && item.id().equals(caregiverId))
                .findFirst()
                .orElseThrow(() -> DomainRuleViolation.notFound(
                        "caregiver.notFound",
                        "El cuidador no pertenece a este perfil."));
    }

    public Long id() {
        return id;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String nickname() {
        return nickname;
    }

    public int age() {
        return age;
    }

    public Gender gender() {
        return gender;
    }

    public String photoUrl() {
        return photoUrl;
    }

    public CommunicativeNeed communicativeNeed() {
        return communicativeNeed;
    }

    public boolean active() {
        return active;
    }

    public SensoryProfile sensoryProfile() {
        return sensoryProfile;
    }

    public DeclaredDiagnosis diagnosis() {
        return diagnosis;
    }

    public List<ProfileCaregiver> caregivers() {
        return caregivers;
    }

    public List<TrustedContact> trustedContacts() {
        return trustedContacts;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    // -------------------------------------------------------------------
    // Internos
    // -------------------------------------------------------------------

    private NeurodivergentProfile mapCaregiver(
            Long caregiverId, java.util.function.UnaryOperator<ProfileCaregiver> mutation) {
        List<ProfileCaregiver> next = new ArrayList<>();
        boolean found = false;
        for (ProfileCaregiver caregiver : caregivers) {
            if (caregiver.id() != null && caregiver.id().equals(caregiverId)) {
                next.add(mutation.apply(caregiver));
                found = true;
            } else {
                next.add(caregiver);
            }
        }
        if (!found) {
            throw DomainRuleViolation.notFound(
                    "caregiver.notFound",
                    "El cuidador no pertenece a este perfil.");
        }
        return copy(
                firstName, lastName, nickname, age, gender, photoUrl, communicativeNeed, active,
                sensoryProfile, diagnosis, next, trustedContacts);
    }

    private NeurodivergentProfile copy(
            String firstName,
            String lastName,
            String nickname,
            int age,
            Gender gender,
            String photoUrl,
            CommunicativeNeed communicativeNeed,
            boolean active,
            SensoryProfile sensoryProfile,
            DeclaredDiagnosis diagnosis,
            List<ProfileCaregiver> caregivers,
            List<TrustedContact> trustedContacts) {
        return new NeurodivergentProfile(
                id,
                firstName,
                lastName,
                nickname,
                age,
                gender,
                photoUrl,
                communicativeNeed,
                active,
                sensoryProfile,
                diagnosis,
                caregivers,
                trustedContacts,
                createdAt,
                Instant.now());
    }
}
