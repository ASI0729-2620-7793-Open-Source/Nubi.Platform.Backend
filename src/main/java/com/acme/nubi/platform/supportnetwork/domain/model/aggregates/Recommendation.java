package com.acme.nubi.platform.supportnetwork.domain.model.aggregates;

import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateRecommendationCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.MarkRecommendationAsEffectiveCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    public static final String ACTIVE = "ACTIVE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long profileId;

    @Column(nullable = false)
    private Long episodeRecordId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String strategy;

    @Column(nullable = false)
    private String actionGuide;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Boolean effective;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime markedEffectiveAt;

    protected Recommendation() {
    }

    private Recommendation(
            Long profileId,
            Long episodeRecordId,
            String title,
            String description,
            String strategy,
            String actionGuide) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID must not be null");
        }
        if (episodeRecordId == null) {
            throw new IllegalArgumentException("Episode record ID must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (strategy == null || strategy.isBlank()) {
            throw new IllegalArgumentException("Strategy must not be blank");
        }
        if (actionGuide == null || actionGuide.isBlank()) {
            throw new IllegalArgumentException("Action guide must not be blank");
        }

        this.profileId = profileId;
        this.episodeRecordId = episodeRecordId;
        this.title = title;
        this.description = description;
        this.strategy = strategy;
        this.actionGuide = actionGuide;
        this.status = ACTIVE;
        this.effective = false;
        this.createdAt = LocalDateTime.now();
    }

    public static Recommendation create(CreateRecommendationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create command must not be null");
        }
        return new Recommendation(
                command.profileId(),
                command.episodeRecordId(),
                command.title(),
                command.description(),
                command.strategy(),
                command.actionGuide());
    }

    public void markAsEffective(MarkRecommendationAsEffectiveCommand command) {
        ensureCommandTargetsRecommendation(command == null ? null : command.recommendationId());
        if (!Boolean.TRUE.equals(effective)) {
            this.effective = true;
            this.markedEffectiveAt = LocalDateTime.now();
        }
    }

    private void ensureCommandTargetsRecommendation(Long recommendationId) {
        if (recommendationId == null || !recommendationId.equals(id)) {
            throw new IllegalArgumentException("Command does not target this recommendation");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public Long getEpisodeRecordId() {
        return episodeRecordId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStrategy() {
        return strategy;
    }

    public String getActionGuide() {
        return actionGuide;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getEffective() {
        return effective;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getMarkedEffectiveAt() {
        return markedEffectiveAt;
    }
}
