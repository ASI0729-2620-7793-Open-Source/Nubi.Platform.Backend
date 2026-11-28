package com.acme.nubi.platform.supportnetwork.interfaces.rest.controllers;

import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import com.acme.nubi.platform.supportnetwork.application.internal.commandservices.RecommendationCommandServiceImpl;
import com.acme.nubi.platform.supportnetwork.application.internal.queryservices.RecommendationQueryServiceImpl;
import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.MarkRecommendationAsEffectiveCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetActiveRecommendationsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllRecommendationsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetRecommendationByIdQuery;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateRecommendationResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.RecommendationResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.UpdateRecommendationResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.CreateRecommendationCommandFromResourceAssembler;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.RecommendationResourceFromEntityAssembler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController("supportNetworkRecommendationsController")
@RequestMapping("/api/v1/support-network/recommendations")
public class RecommendationsController {

    /** Cuenta que consulta: la fija el filtro a partir del token de acceso. */
    private static final String ACCOUNT_HEADER = "X-Account-Id";

    private final RecommendationCommandServiceImpl commandService;
    private final RecommendationQueryServiceImpl queryService;

    public RecommendationsController(
            RecommendationCommandServiceImpl commandService,
            RecommendationQueryServiceImpl queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<RecommendationResource> create(
            @RequestBody CreateRecommendationResource resource) {
        Recommendation recommendation = commandService.handle(
                CreateRecommendationCommandFromResourceAssembler.toCommandFromResource(resource));
        RecommendationResource response =
                RecommendationResourceFromEntityAssembler.toResourceFromEntity(recommendation);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{recommendationId}")
                .buildAndExpand(recommendation.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{recommendationId}")
    public ResponseEntity<RecommendationResource> getById(
            @RequestHeader(name = ACCOUNT_HEADER, required = false) Long accountId,
            @PathVariable Long recommendationId) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return queryService.handle(new GetRecommendationByIdQuery(recommendationId))
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("The requested resource does not exist."));
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResource>> getAllByProfileId(
            @RequestHeader(name = ACCOUNT_HEADER, required = false) Long accountId,
            @RequestParam Long profileId) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<RecommendationResource> resources = queryService
                .handle(new GetAllRecommendationsByProfileIdQuery(profileId))
                .stream()
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RecommendationResource>> getActiveByProfileId(
            @RequestHeader(name = ACCOUNT_HEADER, required = false) Long accountId,
            @RequestParam Long profileId) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<RecommendationResource> resources = queryService
                .handle(new GetActiveRecommendationsByProfileIdQuery(profileId))
                .stream()
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /** Actualiza la recomendación; hoy solo admite marcarla como efectiva ({"effective": true}). */
    @PatchMapping("/{recommendationId}")
    public ResponseEntity<RecommendationResource> update(
            @PathVariable Long recommendationId,
            @RequestBody UpdateRecommendationResource resource) {
        if (resource == null || !Boolean.TRUE.equals(resource.effective())) {
            throw new IllegalArgumentException("Solo se puede marcar la recomendación como efectiva (effective = true).");
        }
        try {
            Recommendation recommendation = commandService.handle(
                    new MarkRecommendationAsEffectiveCommand(recommendationId));
            return ResponseEntity.ok(
                    RecommendationResourceFromEntityAssembler.toResourceFromEntity(recommendation));
        } catch (EntityNotFoundException exception) {
            throw new ResourceNotFoundException("The requested resource does not exist.");
        }
    }
}
