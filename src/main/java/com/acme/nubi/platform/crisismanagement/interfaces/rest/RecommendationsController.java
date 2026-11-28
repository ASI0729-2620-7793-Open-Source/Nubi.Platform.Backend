package com.acme.nubi.platform.crisismanagement.interfaces.rest;

import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.RatePreferenceCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetRecommendationByIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.services.RecommendationCommandService;
import com.acme.nubi.platform.crisismanagement.domain.services.RecommendationQueryService;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.RatePreferenceResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.RecommendationResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.transform.RecommendationResourceFromEntityAssembler;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ProblemDetailResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Recommendations", description = "Personalized recommendations per profile (Crisis Management)")
public class RecommendationsController {

    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationQueryService recommendationQueryService;

    public RecommendationsController(RecommendationCommandService recommendationCommandService,
                                     RecommendationQueryService recommendationQueryService) {
        this.recommendationCommandService = recommendationCommandService;
        this.recommendationQueryService = recommendationQueryService;
    }

    @GetMapping("/{recommendationId}")
    @Operation(summary = "Get a recommendation by id")
    public ResponseEntity<RecommendationResource> getRecommendationById(@PathVariable Long recommendationId) {
        return recommendationQueryService.handle(new GetRecommendationByIdQuery(recommendationId))
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("The requested resource does not exist."));
    }

    @PutMapping(value = "/{recommendationId}/preferences", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rate a recommendation",
            description = "Records a caregiver's usefulness rating (1 to 5) and/or favorite mark. "
                    + "Rating again updates the existing preference.")
    public ResponseEntity<?> ratePreference(@PathVariable Long recommendationId,
                                            @RequestBody RatePreferenceResource resource) {
        return recommendationCommandService.handle(new RatePreferenceCommand(
                        recommendationId, resource.accountId(), resource.usefulnessRating(), resource.favorite()))
                .fold(
                        recommendation -> ResponseEntity.ok(
                                RecommendationResourceFromEntityAssembler.toResourceFromEntity(recommendation)),
                        ProblemDetailResponseAssembler::toResponseEntity);
    }
}
