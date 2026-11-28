package com.acme.nubi.platform.crisismanagement.interfaces.rest;

import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import com.acme.nubi.platform.profile.interfaces.acl.ProfilesContextFacade;
import com.acme.nubi.platform.crisismanagement.domain.model.commands.GenerateRecommendationsCommand;
import com.acme.nubi.platform.crisismanagement.domain.model.queries.GetRecommendationsByProfileIdQuery;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;
import com.acme.nubi.platform.crisismanagement.domain.services.RecommendationCommandService;
import com.acme.nubi.platform.crisismanagement.domain.services.RecommendationQueryService;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.resources.RecommendationResource;
import com.acme.nubi.platform.crisismanagement.interfaces.rest.transform.RecommendationResourceFromEntityAssembler;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ProblemDetailResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/profiles/{profileId}/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Recommendations", description = "Personalized recommendations per profile (Crisis Management)")
public class ProfileRecommendationsController {

    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationQueryService recommendationQueryService;

    private final ProfilesContextFacade profiles;

    public ProfileRecommendationsController(RecommendationCommandService recommendationCommandService,
                                            RecommendationQueryService recommendationQueryService,
            ProfilesContextFacade profiles) {
        this.profiles = profiles;
        this.recommendationCommandService = recommendationCommandService;
        this.recommendationQueryService = recommendationQueryService;
    }

    @GetMapping
    @Operation(summary = "Get the recommendations of a profile",
            description = "Only current versions, optionally filtered by topic.")
    public ResponseEntity<List<RecommendationResource>> getRecommendations(
            @PathVariable Long profileId, @RequestParam(required = false) RecommendationTopic topic) {
        if (!profiles.existsActiveProfile(profileId)) {
            throw new ResourceNotFoundException("Profile", profileId);
        }
        var recommendations = recommendationQueryService.handle(
                new GetRecommendationsByProfileIdQuery(profileId, topic));
        return ResponseEntity.ok(recommendations.stream()
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @PostMapping
    @Operation(summary = "Create (generate) the recommendations of a profile",
            description = "Generates one recommendation per topic from the sensory profile. "
                    + "If the profile already has them, returns them without duplicates.")
    public ResponseEntity<?> generateRecommendations(@PathVariable Long profileId) {
        return recommendationCommandService.handle(new GenerateRecommendationsCommand(profileId)).fold(
                recommendations -> ResponseEntity.ok(recommendations.stream()
                        .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                        .toList()),
                ProblemDetailResponseAssembler::toResponseEntity);
    }
}
