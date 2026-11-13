package com.acme.nubi.platform.profile.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.profile.domain.model.enums.CommunicativeNeed;
import com.acme.nubi.platform.profile.domain.model.enums.Gender;
import com.acme.nubi.platform.profile.domain.model.enums.SensitivityLevel;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad JPA del perfil. Es un contenedor anémico: todas las reglas de
 * negocio viven en el agregado de dominio.
 */
@Getter
@Setter
@Entity
@Table(name = "profile")
public class ProfileJpaEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "nickname")
    private String nickname;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "photo_url")
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "communicative_need", nullable = false)
    private CommunicativeNeed communicativeNeed;

    @Column(nullable = false)
    private boolean active;

    // --- Perfil sensorial (US-03) -----------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "sensory_auditory", nullable = false)
    private SensitivityLevel sensoryAuditory;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensory_visual", nullable = false)
    private SensitivityLevel sensoryVisual;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensory_tactile", nullable = false)
    private SensitivityLevel sensoryTactile;

    @Column(name = "low_stimulation_enabled", nullable = false)
    private boolean lowStimulationEnabled;

    @Column(name = "prioritize_visuals", nullable = false)
    private boolean prioritizeVisuals;

    @Column(name = "confirm_audio", nullable = false)
    private boolean confirmAudio;

    // --- Hijos ------------------------------------------------------------

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ProfileCaregiverJpaEntity> caregivers = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<TrustedContactJpaEntity> trustedContacts = new ArrayList<>();

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private DeclaredDiagnosisJpaEntity diagnosis;
}
