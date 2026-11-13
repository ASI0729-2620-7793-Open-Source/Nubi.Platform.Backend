package com.acme.nubi.platform.supportnetwork.domain.model.aggregates;

import com.acme.nubi.platform.supportnetwork.domain.model.commands.AddEpisodeNoteCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateEpisodeRecordCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "episode_records")
public class EpisodeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long profileId;

    @Column(nullable = false)
    private Long sosSessionId;

    private String emotionalState;

    @Column(name = "trigger_description")
    private String trigger;

    private String strategyUsed;

    private String notes;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected EpisodeRecord() {
    }

    private EpisodeRecord(
            Long profileId,
            Long sosSessionId,
            String emotionalState,
            String trigger,
            String strategyUsed,
            String notes,
            LocalDateTime startedAt,
            LocalDateTime endedAt) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID must not be null");
        }
        if (sosSessionId == null) {
            throw new IllegalArgumentException("SOS session ID must not be null");
        }

        this.profileId = profileId;
        this.sosSessionId = sosSessionId;
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.strategyUsed = strategyUsed;
        this.notes = notes;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.createdAt = LocalDateTime.now();
    }

    public static EpisodeRecord create(CreateEpisodeRecordCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create command must not be null");
        }

        return new EpisodeRecord(
                command.profileId(),
                command.sosSessionId(),
                command.emotionalState(),
                command.trigger(),
                command.strategyUsed(),
                command.notes(),
                command.startedAt(),
                command.endedAt());
    }

    public void addNote(AddEpisodeNoteCommand command) {
        ensureCommandTargetsRecord(command == null ? null : command.episodeRecordId());
        this.notes = command.notes();
    }

    private void ensureCommandTargetsRecord(Long episodeRecordId) {
        if (episodeRecordId == null || !episodeRecordId.equals(id)) {
            throw new IllegalArgumentException("Command does not target this episode record");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public Long getSosSessionId() {
        return sosSessionId;
    }

    public String getEmotionalState() {
        return emotionalState;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getStrategyUsed() {
        return strategyUsed;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
