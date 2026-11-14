package com.acme.nubi.platform.supportnetwork.domain.model.aggregates;

import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateEpisodeReportCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.MarkEpisodeReportAsSharedCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "episode_reports")
public class EpisodeReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long profileId;

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    @Column(nullable = false)
    private Integer episodeCount;

    @Column(nullable = false)
    private String summary;

    private String observations;

    @Column(nullable = false)
    private Boolean shared;

    private String sharedWith;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sharedAt;

    protected EpisodeReport() {
    }

    private EpisodeReport(
            Long profileId,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            Integer episodeCount,
            String summary,
            String observations) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID must not be null");
        }
        if (periodStart == null) {
            throw new IllegalArgumentException("Period start must not be null");
        }
        if (periodEnd == null) {
            throw new IllegalArgumentException("Period end must not be null");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start must not be after period end");
        }
        if (episodeCount == null) {
            throw new IllegalArgumentException("Episode count must not be null");
        }
        if (episodeCount < 0) {
            throw new IllegalArgumentException("Episode count must not be negative");
        }
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Summary must not be blank");
        }

        this.profileId = profileId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.episodeCount = episodeCount;
        this.summary = summary;
        this.observations = observations;
        this.shared = false;
        this.createdAt = LocalDateTime.now();
    }

    public static EpisodeReport create(CreateEpisodeReportCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create command must not be null");
        }
        return new EpisodeReport(
                command.profileId(),
                command.periodStart(),
                command.periodEnd(),
                command.episodeCount(),
                command.summary(),
                command.observations());
    }

    public void markAsShared(MarkEpisodeReportAsSharedCommand command) {
        ensureCommandTargetsReport(command == null ? null : command.episodeReportId());
        if (command.sharedWith() == null || command.sharedWith().isBlank()) {
            throw new IllegalArgumentException("Shared with must not be blank");
        }

        this.shared = true;
        this.sharedWith = command.sharedWith();
        this.sharedAt = LocalDateTime.now();
    }

    private void ensureCommandTargetsReport(Long episodeReportId) {
        if (episodeReportId == null || !episodeReportId.equals(id)) {
            throw new IllegalArgumentException("Command does not target this episode report");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public String getSummary() {
        return summary;
    }

    public String getObservations() {
        return observations;
    }

    public Boolean getShared() {
        return shared;
    }

    public String getSharedWith() {
        return sharedWith;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }
}
