package com.acme.nubi.platform.supportnetwork.domain.model.aggregates;

import com.acme.nubi.platform.supportnetwork.domain.model.commands.CancelHelpRequestCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.ConfirmHelpRequestCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.CreateHelpRequestCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "help_requests")
public class HelpRequest {

    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long profileId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime cancelledAt;

    protected HelpRequest() {
    }

    private HelpRequest(Long profileId, String message) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID must not be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be blank");
        }

        this.profileId = profileId;
        this.message = message;
        this.status = PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public static HelpRequest create(CreateHelpRequestCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create command must not be null");
        }
        return new HelpRequest(command.profileId(), command.message());
    }

    public void confirm(ConfirmHelpRequestCommand command) {
        ensureCommandTargetsRequest(command == null ? null : command.helpRequestId());
        ensurePending();
        this.status = CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel(CancelHelpRequestCommand command) {
        ensureCommandTargetsRequest(command == null ? null : command.helpRequestId());
        ensurePending();
        this.status = CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    private void ensureCommandTargetsRequest(Long helpRequestId) {
        if (helpRequestId == null || !helpRequestId.equals(id)) {
            throw new IllegalArgumentException("Command does not target this help request");
        }
    }

    private void ensurePending() {
        if (!PENDING.equals(status)) {
            throw new IllegalStateException("Only a pending help request can be confirmed or cancelled");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
}
