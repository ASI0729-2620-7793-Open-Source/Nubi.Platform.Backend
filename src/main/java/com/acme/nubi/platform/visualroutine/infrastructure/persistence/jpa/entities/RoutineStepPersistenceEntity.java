package com.acme.nubi.platform.visualroutine.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "routine_steps")
public class RoutineStepPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(nullable = false, length = 60)
    private String title;

    @Column(nullable = false, length = 16)
    private String icon;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Column(nullable = false)
    private int position;

    @Column(name = "last_completed_on")
    private LocalDate lastCompletedOn;
}
