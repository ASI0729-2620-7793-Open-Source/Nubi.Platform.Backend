package com.acme.nubi.platform.profile.infrastructure.persistence.mappers;

import com.acme.nubi.platform.profile.domain.model.aggregates.NeurodivergentProfile;
import com.acme.nubi.platform.profile.domain.model.entities.ProfileCaregiver;
import com.acme.nubi.platform.profile.domain.model.entities.TrustedContact;
import com.acme.nubi.platform.profile.domain.model.valueobjects.DeclaredDiagnosis;
import com.acme.nubi.platform.profile.domain.model.valueobjects.SensoryProfile;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.DeclaredDiagnosisJpaEntity;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.ProfileCaregiverJpaEntity;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.ProfileJpaEntity;
import com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities.TrustedContactJpaEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Mapeo explícito entre el agregado de dominio (inmutable) y su entidad JPA.
 * Ninguna de las dos conoce a la otra: este es el único lugar donde conviven.
 */
public final class ProfileMapper {

    private ProfileMapper() {
    }

    // -------------------------------------------------------------------
    // JPA -> Dominio
    // -------------------------------------------------------------------

    public static NeurodivergentProfile toDomain(ProfileJpaEntity entity) {
        // Fuerza la inicialización de las asociaciones lazy (dentro de la
        // transacción) antes de pasar de la entidad al dominio.
        List<ProfileCaregiver> caregivers = entity.getCaregivers().stream()
                .map(ProfileMapper::toDomain)
                .toList();
        // El orden de la colección JPA es el de inserción; el orden visible es
        // el de la prioridad, que es el que el cuidador reordena.
        List<TrustedContact> contacts = entity.getTrustedContacts().stream()
                .map(ProfileMapper::toDomain)
                .sorted(Comparator.comparingInt(TrustedContact::priorityOrder))
                .toList();
        DeclaredDiagnosis diagnosis = entity.getDiagnosis() == null
                ? null
                : toDomain(entity.getDiagnosis());

        return new NeurodivergentProfile(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getNickname(),
                entity.getAge(),
                entity.getGender(),
                entity.getPhotoUrl(),
                entity.getCommunicativeNeed(),
                entity.isActive(),
                new SensoryProfile(
                        entity.getSensoryAuditory(),
                        entity.getSensoryVisual(),
                        entity.getSensoryTactile(),
                        entity.isLowStimulationEnabled(),
                        entity.isPrioritizeVisuals(),
                        entity.isConfirmAudio()),
                diagnosis,
                caregivers,
                contacts,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static ProfileCaregiver toDomain(ProfileCaregiverJpaEntity entity) {
        return new ProfileCaregiver(
                entity.getId(),
                entity.getAccountId(),
                entity.getInvitedEmail(),
                entity.getRole(),
                entity.getStatus(),
                entity.getInvitedAt(),
                entity.getAcceptedAt());
    }

    public static TrustedContact toDomain(TrustedContactJpaEntity entity) {
        return new TrustedContact(
                entity.getId(),
                entity.getFullName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getRelationship(),
                entity.getPriorityOrder());
    }

    public static DeclaredDiagnosis toDomain(DeclaredDiagnosisJpaEntity entity) {
        return new DeclaredDiagnosis(
                entity.getConditionType(),
                entity.getCustomDescription(),
                entity.getDiagnosedBy(),
                entity.getDiagnosisDate(),
                entity.getProfessionalNotes());
    }

    // -------------------------------------------------------------------
    // Dominio -> JPA
    // -------------------------------------------------------------------

    /**
     * Aplica el estado completo del agregado sobre la entidad gestionada.
     * Los hijos existentes se actualizan por id para no perder los identificadores
     * que la base de datos ya asignó; los nuevos se agregan y los ausentes se
     * eliminan (orphanRemoval).
     */
    public static void applyTo(ProfileJpaEntity target, NeurodivergentProfile source) {
        target.setFirstName(source.firstName());
        target.setLastName(source.lastName());
        target.setNickname(source.nickname());
        target.setAge(source.age());
        target.setGender(source.gender());
        target.setPhotoUrl(source.photoUrl());
        target.setCommunicativeNeed(source.communicativeNeed());
        target.setActive(source.active());

        SensoryProfile sensory = source.sensoryProfile();
        if (sensory != null) {
            target.setSensoryAuditory(sensory.auditory());
            target.setSensoryVisual(sensory.visual());
            target.setSensoryTactile(sensory.tactile());
            target.setLowStimulationEnabled(sensory.lowStimulationEnabled());
            target.setPrioritizeVisuals(sensory.prioritizeVisuals());
            target.setConfirmAudio(sensory.confirmAudio());
        }

        applyCaregivers(target, source.caregivers());
        applyTrustedContacts(target, source.trustedContacts());
        applyDiagnosis(target, source.diagnosis());
    }

    private static void applyCaregivers(
            ProfileJpaEntity target, List<ProfileCaregiver> caregivers) {
        List<ProfileCaregiverJpaEntity> rows = target.getCaregivers();
        List<Long> kept = new ArrayList<>();
        for (ProfileCaregiver domain : caregivers) {
            if (domain.id() != null) {
                rows.stream()
                        .filter(row -> domain.id().equals(row.getId()))
                        .findFirst()
                        .ifPresent(row -> {
                            row.setAccountId(domain.accountId());
                            row.setInvitedEmail(domain.invitedEmail());
                            row.setRole(domain.role());
                            row.setStatus(domain.status());
                            row.setInvitedAt(domain.invitedAt());
                            row.setAcceptedAt(domain.acceptedAt());
                        });
                kept.add(domain.id());
            } else {
                ProfileCaregiverJpaEntity row = new ProfileCaregiverJpaEntity();
                row.setProfile(target);
                row.setAccountId(domain.accountId());
                row.setInvitedEmail(domain.invitedEmail());
                row.setRole(domain.role());
                row.setStatus(domain.status());
                row.setInvitedAt(domain.invitedAt());
                row.setAcceptedAt(domain.acceptedAt());
                rows.add(row);
            }
        }
        rows.removeIf(row -> row.getId() != null && !kept.contains(row.getId()));
    }

    private static void applyTrustedContacts(
            ProfileJpaEntity target, List<TrustedContact> contacts) {
        List<TrustedContactJpaEntity> rows = target.getTrustedContacts();
        List<Long> kept = new ArrayList<>();
        for (TrustedContact domain : contacts) {
            if (domain.id() != null) {
                rows.stream()
                        .filter(row -> domain.id().equals(row.getId()))
                        .findFirst()
                        .ifPresent(row -> {
                            row.setFullName(domain.fullName());
                            row.setPhone(domain.phone());
                            row.setEmail(domain.email());
                            row.setRelationship(domain.relationship());
                            row.setPriorityOrder(domain.priorityOrder());
                        });
                kept.add(domain.id());
            } else {
                TrustedContactJpaEntity row = new TrustedContactJpaEntity();
                row.setProfile(target);
                row.setFullName(domain.fullName());
                row.setPhone(domain.phone());
                row.setEmail(domain.email());
                row.setRelationship(domain.relationship());
                row.setPriorityOrder(domain.priorityOrder());
                rows.add(row);
            }
        }
        rows.removeIf(row -> row.getId() != null && !kept.contains(row.getId()));
    }

    private static void applyDiagnosis(ProfileJpaEntity target, DeclaredDiagnosis diagnosis) {
        if (diagnosis == null) {
            target.setDiagnosis(null);
            return;
        }
        DeclaredDiagnosisJpaEntity row = target.getDiagnosis();
        if (row == null) {
            row = new DeclaredDiagnosisJpaEntity();
            row.setProfile(target);
            target.setDiagnosis(row);
        }
        row.setConditionType(diagnosis.condition());
        row.setCustomDescription(diagnosis.customDescription());
        row.setDiagnosedBy(diagnosis.diagnosedBy());
        row.setDiagnosisDate(diagnosis.diagnosisDate());
        row.setProfessionalNotes(diagnosis.professionalNotes());
    }
}
