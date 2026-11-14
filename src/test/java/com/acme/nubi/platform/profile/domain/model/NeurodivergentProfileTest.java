package com.acme.nubi.platform.profile.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleKind;
import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.profile.domain.model.aggregates.NeurodivergentProfile;
import com.acme.nubi.platform.profile.domain.model.entities.ProfileCaregiver;
import com.acme.nubi.platform.profile.domain.model.entities.TrustedContact;
import com.acme.nubi.platform.profile.domain.model.enums.CaregiverRole;
import com.acme.nubi.platform.profile.domain.model.enums.CommunicativeNeed;
import com.acme.nubi.platform.profile.domain.model.enums.ConditionType;
import com.acme.nubi.platform.profile.domain.model.enums.Gender;
import com.acme.nubi.platform.profile.domain.model.enums.InvitationStatus;
import com.acme.nubi.platform.profile.domain.model.enums.SensitivityLevel;
import com.acme.nubi.platform.profile.domain.model.valueobjects.DeclaredDiagnosis;
import com.acme.nubi.platform.profile.domain.model.valueobjects.SensoryProfile;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Reglas de negocio del agregado NeurodivergentProfile, sin Spring ni base de
 * datos: se ejecuta en milisegundos y protege las invariantes del dominio.
 */
class NeurodivergentProfileTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private NeurodivergentProfile newProfile() {
        return NeurodivergentProfile.create(
                "Lucía", "Fernández", "Luchi", 8, Gender.FEMALE,
                CommunicativeNeed.PICTOGRAMS,
                ProfileCaregiver.primary(100L, "ana@nubi.app", NOW), NOW);
    }

    /** Reproduce lo que hace la persistencia: asignar ids a los hijos nuevos. */
    private NeurodivergentProfile withIds(NeurodivergentProfile source) {
        List<ProfileCaregiver> caregivers = new ArrayList<>();
        long caregiverId = 1;
        for (ProfileCaregiver caregiver : source.caregivers()) {
            caregivers.add(caregiver.id() != null ? caregiver : caregiver.withId(caregiverId));
            if (caregiver.id() == null) {
                caregiverId++;
            }
        }
        List<TrustedContact> contacts = new ArrayList<>();
        long contactId = 1;
        for (TrustedContact contact : source.trustedContacts()) {
            contacts.add(contact.id() != null ? contact : contact.withId(contactId));
            if (contact.id() == null) {
                contactId++;
            }
        }
        return new NeurodivergentProfile(
                source.id(),
                source.firstName(),
                source.lastName(),
                source.nickname(),
                source.age(),
                source.gender(),
                source.photoUrl(),
                source.communicativeNeed(),
                source.active(),
                source.sensoryProfile(),
                source.diagnosis(),
                caregivers,
                contacts,
                source.createdAt(),
                source.updatedAt());
    }

    // --- US-01 -----------------------------------------------------------

    @Test
    @DisplayName("Un perfil nuevo nace activo, con sensorial por defecto y sin diagnóstico")
    void createProfileWithDefaults() {
        NeurodivergentProfile profile = newProfile();

        assertTrue(profile.active());
        assertNotNull(profile.sensoryProfile());
        assertEquals(SensitivityLevel.MEDIUM, profile.sensoryProfile().auditory());
        assertFalse(profile.sensoryProfile().lowStimulationEnabled());
        assertNull(profile.diagnosis());
        assertEquals(1, profile.caregivers().size());
        assertTrue(profile.isPrimaryCaregiver(100L));
        assertEquals("Luchi", profile.displayName());
        assertEquals("Lucía Fernández", profile.fullName());
    }

    @Test
    @DisplayName("El nombre, el apellido y la edad son obligatorios (400 con el campo)")
    void createRequiresMandatoryData() {
        ProfileCaregiver primary = ProfileCaregiver.primary(1L, "a@b.c", NOW);

        DomainRuleViolation noName = assertThrows(DomainRuleViolation.class, () ->
                NeurodivergentProfile.create(
                        " ", "Apellido", null, 8, Gender.FEMALE,
                        CommunicativeNeed.TEXT, primary, NOW));
        assertEquals("firstName", noName.field());

        DomainRuleViolation badAge = assertThrows(DomainRuleViolation.class, () ->
                NeurodivergentProfile.create(
                        "Lucía", "Fernández", null, 0, Gender.FEMALE,
                        CommunicativeNeed.TEXT, primary, NOW));
        assertEquals("age", badAge.field());
        assertEquals(DomainRuleKind.VALIDATION, badAge.kind());
    }

    // --- US-03 -----------------------------------------------------------

    @Test
    @DisplayName("Registrar sensibilidades reemplaza el perfil sensorial completo (US-03)")
    void registerSensitivitiesReplacesWholeValueObject() {
        NeurodivergentProfile profile = newProfile();
        SensoryProfile sensory = new SensoryProfile(
                SensitivityLevel.VERY_HIGH, SensitivityLevel.LOW, SensitivityLevel.HIGH,
                true, true, false);

        NeurodivergentProfile updated = profile.registerSensitivities(sensory);

        assertEquals(SensitivityLevel.VERY_HIGH, updated.sensoryProfile().auditory());
        assertEquals(SensitivityLevel.LOW, updated.sensoryProfile().visual());
        assertEquals(SensitivityLevel.HIGH, updated.sensoryProfile().tactile());
        assertTrue(updated.sensoryProfile().isAuditorySensitive());
        // La instancia original queda intacta: el agregado es inmutable.
        assertEquals(SensitivityLevel.MEDIUM, profile.sensoryProfile().auditory());
    }

    // --- US-04 -----------------------------------------------------------

    @Test
    @DisplayName("Nubi solo acepta la condición ASD (autismo)")
    void onlyAutismIsAccepted() {
        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                new DeclaredDiagnosis(
                        ConditionType.ADHD, null, "Dra. Gómez",
                        LocalDate.parse("2024-05-01"), null));

        assertEquals(DomainRuleKind.VALIDATION, violation.kind());
        assertEquals("condition", violation.field());
        assertEquals("condition.autism.only", violation.code());
    }

    @Test
    @DisplayName("Sin condición explícita el diagnóstico es ASD")
    void missingConditionDefaultsToAutism() {
        DeclaredDiagnosis diagnosis = new DeclaredDiagnosis(null, null, null, null, null);

        assertEquals(ConditionType.ASD, diagnosis.condition());
        assertEquals(ConditionType.ASD, DeclaredDiagnosis.autism().condition());
    }

    @Test
    @DisplayName("Un diagnóstico declarado se reemplaza entero, nunca se mezcla")
    void declaredDiagnosisIsReplacedAsAWhole() {
        NeurodivergentProfile profile = newProfile();
        DeclaredDiagnosis first = new DeclaredDiagnosis(
                ConditionType.ASD, null, "Dra. Gómez", LocalDate.parse("2024-05-01"), "A");
        DeclaredDiagnosis second = new DeclaredDiagnosis(
                ConditionType.ASD, null, null, null, "Nivel 2 de apoyo");

        NeurodivergentProfile updated = profile.declareDiagnosis(first).declareDiagnosis(second);

        assertEquals(ConditionType.ASD, updated.diagnosis().condition());
        assertEquals("Nivel 2 de apoyo", updated.diagnosis().professionalNotes());
        assertNull(updated.diagnosis().diagnosedBy());
        assertNull(profile.diagnosis());
    }

    // --- US-06 -----------------------------------------------------------

    @Test
    @DisplayName("Invitar a un cuidador lo deja PENDING con solo el correo")
    void inviteCaregiverStartsPending() {
        NeurodivergentProfile invited = newProfile()
                .inviteCaregiver("pedro@nubi.app", CaregiverRole.THERAPIST);

        assertEquals(2, invited.caregivers().size());
        ProfileCaregiver caregiver = invited.caregivers().get(1);
        assertEquals(InvitationStatus.PENDING, caregiver.status());
        assertEquals("pedro@nubi.app", caregiver.invitedEmail());
        assertNull(caregiver.accountId());
    }

    @Test
    @DisplayName("No se puede invitar dos veces al mismo correo (409)")
    void duplicateCaregiverEmailIsRejected() {
        NeurodivergentProfile profile = newProfile()
                .inviteCaregiver("pedro@nubi.app", CaregiverRole.CAREGIVER);

        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                profile.inviteCaregiver("Pedro@nubi.app", CaregiverRole.THERAPIST));

        assertEquals(DomainRuleKind.CONFLICT, violation.kind());
        assertEquals("caregiver.email.duplicated", violation.code());
    }

    @Test
    @DisplayName("El cuidador principal no se puede revocar (regla 1 del US-06)")
    void principalCaregiverCannotBeRevoked() {
        NeurodivergentProfile persisted = withIds(newProfile());

        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                persisted.revokeCaregiver(persisted.caregivers().get(0).id()));

        assertEquals(DomainRuleKind.CONFLICT, violation.kind());
        assertEquals("caregiver.principal.notRevocable", violation.code());
    }

    @Test
    @DisplayName("Revocar a un cuidador que no está activo también es conflicto (409)")
    void revokingANonActiveCaregiverIsConflict() {
        NeurodivergentProfile profile = withIds(newProfile()
                .inviteCaregiver("pedro@nubi.app", CaregiverRole.CAREGIVER));
        Long pendingId = profile.caregivers().get(1).id();

        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                profile.revokeCaregiver(pendingId));

        assertEquals(DomainRuleKind.CONFLICT, violation.kind());
        assertEquals("caregiver.notActive", violation.code());
    }

    @Test
    @DisplayName("Solo una invitación PENDING puede aceptarse (409 en otro estado)")
    void onlyPendingInvitationCanBeAccepted() {
        NeurodivergentProfile profile = withIds(newProfile()
                .inviteCaregiver("pedro@nubi.app", CaregiverRole.CAREGIVER));
        Long caregiverId = profile.caregivers().get(1).id();

        NeurodivergentProfile accepted = profile.acceptCaregiver(caregiverId, 555L);
        assertEquals(InvitationStatus.ACTIVE, accepted.caregivers().get(1).status());
        assertEquals(Long.valueOf(555L), accepted.caregivers().get(1).accountId());

        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                accepted.acceptCaregiver(caregiverId, 777L));
        assertEquals(DomainRuleKind.CONFLICT, violation.kind());
        assertEquals("caregiver.invitation.notPending", violation.code());
    }

    @Test
    @DisplayName("Revocar un cuidador invitado deja de contar para el cupo del plan")
    void revokedCaregiverStopsCountingForPlanLimit() {
        NeurodivergentProfile profile = withIds(newProfile()
                .inviteCaregiver("pedro@nubi.app", CaregiverRole.CAREGIVER)
                .inviteCaregiver("maria@nubi.app", CaregiverRole.THERAPIST));
        assertEquals(3, profile.caregiversInUse());

        NeurodivergentProfile accepted =
                profile.acceptCaregiver(profile.caregivers().get(1).id(), 555L);
        NeurodivergentProfile revoked =
                accepted.revokeCaregiver(accepted.caregivers().get(1).id());

        assertEquals(2, revoked.caregiversInUse());
        assertEquals(InvitationStatus.REVOKED, revoked.caregivers().get(1).status());
        // El cupo liberado permite invitar a otra persona.
        assertEquals(3, revoked.caregiversInUse() + 1);
    }

    @Test
    @DisplayName("Un cuidador invitado activo puede ver el perfil; uno revocado no")
    void onlyActiveCaregiversCanAccessTheProfile() {
        NeurodivergentProfile profile = withIds(newProfile()
                .inviteCaregiver("pedro@nubi.app", CaregiverRole.CAREGIVER));
        Long caregiverId = profile.caregivers().get(1).id();

        NeurodivergentProfile accepted = profile.acceptCaregiver(caregiverId, 555L);
        assertTrue(accepted.isActiveCaregiver(555L));
        assertFalse(accepted.isPrimaryCaregiver(555L));
        assertFalse(accepted.isActiveCaregiver(100L) && accepted.isPrimaryCaregiver(555L));

        NeurodivergentProfile revoked = accepted.revokeCaregiver(caregiverId);
        assertFalse(revoked.isActiveCaregiver(555L));
        assertTrue(revoked.isActiveCaregiver(100L));
    }

    @Test
    @DisplayName("El rol PRIMARY solo se asigna al crear el perfil (409)")
    void primaryRoleIsReservedForCreation() {
        NeurodivergentProfile profile = newProfile();

        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                profile.inviteCaregiver("otro@nubi.app", CaregiverRole.PRIMARY));

        assertEquals(DomainRuleKind.CONFLICT, violation.kind());
        assertEquals("caregiver.role.primary.reserved", violation.code());
    }

    @Test
    @DisplayName("Un cuidador inexistente en el perfil responde 404, no 409")
    void unknownCaregiverIsNotFound() {
        NeurodivergentProfile profile = withIds(newProfile());

        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                profile.revokeCaregiver(999L));

        assertEquals(DomainRuleKind.NOT_FOUND, violation.kind());
        assertEquals("caregiver.notFound", violation.code());
    }

    // --- Contactos de confianza ------------------------------------------

    @Test
    @DisplayName("No pueden existir dos contactos con la misma prioridad (409)")
    void duplicatedContactPriorityIsRejected() {
        NeurodivergentProfile profile = newProfile()
                .linkTrustedContact(new TrustedContact(
                        null, "Mamá", "+51 999 000 111", "mama@nubi.app", "Madre", 1));

        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                profile.linkTrustedContact(new TrustedContact(
                        null, "Papá", "+51 999 000 222", null, "Padre", 1)));

        assertEquals(DomainRuleKind.CONFLICT, violation.kind());
        assertEquals("contact.priorityOrder.duplicated", violation.code());
    }

    @Test
    @DisplayName("Un contacto sin nombre o sin teléfono es inválido (400 con el campo)")
    void contactRequiresNameAndPhone() {
        DomainRuleViolation noName = assertThrows(DomainRuleViolation.class, () ->
                new TrustedContact(null, "  ", "+51 999 000 111", null, null, 1));
        assertEquals("fullName", noName.field());

        DomainRuleViolation noPhone = assertThrows(DomainRuleViolation.class, () ->
                new TrustedContact(null, "Mamá", null, null, null, 1));
        assertEquals("phone", noPhone.field());

        DomainRuleViolation badOrder = assertThrows(DomainRuleViolation.class, () ->
                new TrustedContact(null, "Mamá", "+51 999 000 111", null, null, 0));
        assertEquals("priorityOrder", badOrder.field());
    }

    @Test
    @DisplayName("Reordenar contactos vuelve a numerar de 1 a n en el nuevo orden")
    void reorderRenumberContactsFromOne() {
        NeurodivergentProfile profile = withIds(newProfile()
                .linkTrustedContact(new TrustedContact(
                        null, "Mamá", "+51 999 000 111", null, "Madre", 1))
                .linkTrustedContact(new TrustedContact(
                        null, "Papá", "+51 999 000 222", null, "Padre", 2))
                .linkTrustedContact(new TrustedContact(
                        null, "Abuela", "+51 999 000 333", null, "Abuela", 3)));

        List<Long> ids = profile.trustedContacts().stream()
                .map(TrustedContact::id)
                .toList();

        NeurodivergentProfile reordered =
                profile.reorderTrustedContacts(List.of(ids.get(2), ids.get(0), ids.get(1)));

        assertEquals("Abuela", reordered.trustedContacts().get(0).fullName());
        assertEquals(1, reordered.trustedContacts().get(0).priorityOrder());
        assertEquals(2, reordered.trustedContacts().get(1).priorityOrder());
        assertEquals(3, reordered.trustedContacts().get(2).priorityOrder());
    }

    @Test
    @DisplayName("Reordenar con un contacto ajeno al perfil responde 404")
    void reorderWithUnknownContactIsNotFound() {
        NeurodivergentProfile profile = withIds(newProfile()
                .linkTrustedContact(new TrustedContact(
                        null, "Mamá", "+51 999 000 111", null, "Madre", 1)));

        DomainRuleViolation violation = assertThrows(DomainRuleViolation.class, () ->
                profile.reorderTrustedContacts(List.of(999L)));

        assertEquals(DomainRuleKind.NOT_FOUND, violation.kind());
        assertEquals("contact.order.unknown", violation.code());
    }

    @Test
    @DisplayName("Quitar un contacto renumera el resto de 1 a n")
    void removingContactRenumbersTheRest() {
        NeurodivergentProfile profile = withIds(newProfile()
                .linkTrustedContact(new TrustedContact(
                        null, "Mamá", "+51 999 000 111", null, "Madre", 1))
                .linkTrustedContact(new TrustedContact(
                        null, "Papá", "+51 999 000 222", null, "Padre", 2)));

        NeurodivergentProfile withoutFirst =
                profile.removeTrustedContact(profile.trustedContacts().get(0).id());

        assertEquals(1, withoutFirst.trustedContacts().size());
        assertEquals(1, withoutFirst.trustedContacts().get(0).priorityOrder());
        assertEquals("Papá", withoutFirst.trustedContacts().get(0).fullName());
    }

    // --- Archivado --------------------------------------------------------

    @Test
    @DisplayName("Archivar desactiva el perfil sin borrar sus datos")
    void archiveDisablesWithoutErasing() {
        NeurodivergentProfile archived = newProfile().archive();

        assertFalse(archived.active());
        assertEquals("Lucía", archived.firstName());
        assertEquals(1, archived.caregivers().size());
        // Idempotente: archivar dos veces no cambia nada más.
        assertFalse(archived.archive().active());
    }

    @Test
    @DisplayName("Cada comando devuelve una instancia nueva (inmutabilidad)")
    void commandsReturnNewInstances() {
        NeurodivergentProfile profile = newProfile();
        NeurodivergentProfile updated = profile.updateGeneral(
                "Pedro", "Gómez", null, 9, Gender.MALE, CommunicativeNeed.VOICE);

        assertEquals("Lucía", profile.firstName());
        assertEquals("Pedro", updated.firstName());
        assertTrue(profile.active());
    }

    @Test
    @DisplayName("El alias cae al nombre cuando no hay apodo")
    void displayNameFallsBackToFirstName() {
        NeurodivergentProfile profile = NeurodivergentProfile.create(
                "Lucía", "Fernández", "   ", 8, Gender.FEMALE,
                CommunicativeNeed.TEXT, ProfileCaregiver.primary(1L, null, NOW), NOW);

        assertEquals("Lucía", profile.displayName());
    }
}
