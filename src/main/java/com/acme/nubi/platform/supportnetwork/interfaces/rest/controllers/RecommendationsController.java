package com.acme.nubi.platform.supportnetwork.interfaces.rest.controllers;

import com.acme.nubi.platform.supportnetwork.application.internal.commandservices.RecommendationCommandServiceImpl;
import com.acme.nubi.platform.supportnetwork.application.internal.queryservices.RecommendationQueryServiceImpl;
import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.Recommendation;
import com.acme.nubi.platform.supportnetwork.domain.model.commands.MarkRecommendationAsEffectiveCommand;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetActiveRecommendationsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetAllRecommendationsByProfileIdQuery;
import com.acme.nubi.platform.supportnetwork.domain.model.queries.GetRecommendationByIdQuery;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.CreateRecommendationResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.resources.RecommendationResource;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.CreateRecommendationCommandFromResourceAssembler;
import com.acme.nubi.platform.supportnetwork.interfaces.rest.transform.RecommendationResourceFromEntityAssembler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationsController {

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
    public ResponseEntity<RecommendationResource> getById(@PathVariable Long recommendationId) {
        return queryService.handle(new GetRecommendationByIdQuery(recommendationId))
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResource>> getAllByProfileId(
            @RequestParam Long profileId) {
        List<RecommendationResource> resources = queryService
                .handle(new GetAllRecommendationsByProfileIdQuery(profileId))
                .stream()
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RecommendationResource>> getActiveByProfileId(
            @RequestParam Long profileId) {
        List<RecommendationResource> resources = queryService
                .handle(new GetActiveRecommendationsByProfileIdQuery(profileId))
                .stream()
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{recommendationId}/effective")
    public ResponseEntity<RecommendationResource> markAsEffective(
            @PathVariable Long recommendationId) {
        try {
            Recommendation recommendation = commandService.handle(
                    new MarkRecommendationAsEffectiveCommand(recommendationId));
            return ResponseEntity.ok(
                    RecommendationResourceFromEntityAssembler.toResourceFromEntity(recommendation));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
