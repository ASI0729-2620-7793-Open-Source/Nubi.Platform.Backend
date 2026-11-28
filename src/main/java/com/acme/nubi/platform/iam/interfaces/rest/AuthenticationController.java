package com.acme.nubi.platform.iam.interfaces.rest;

import com.acme.nubi.platform.shared.infrastructure.security.AccessTokenService;
import com.acme.nubi.platform.iam.domain.model.commands.SignInCommand;
import com.acme.nubi.platform.iam.domain.model.commands.SignUpCommand;
import com.acme.nubi.platform.iam.domain.model.errors.IamErrors;
import com.acme.nubi.platform.iam.domain.services.AuthenticationCommandService;
import com.acme.nubi.platform.iam.interfaces.rest.resources.IamResources.AuthenticatedUserResource;
import com.acme.nubi.platform.iam.interfaces.rest.resources.IamResources.SignInResource;
import com.acme.nubi.platform.iam.interfaces.rest.resources.IamResources.SignUpResource;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ProblemDetailResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Caregiver sign-up and sign-in")
public class AuthenticationController {

    private final AuthenticationCommandService authenticationCommandService;

    private final AccessTokenService tokens;

    public AuthenticationController(
            AuthenticationCommandService authenticationCommandService, AccessTokenService tokens) {
        this.authenticationCommandService = authenticationCommandService;
        this.tokens = tokens;
    }

    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a caregiver account")
    public ResponseEntity<?> signUp(@RequestBody SignUpResource resource) {
        return authenticationCommandService
                .handle(new SignUpCommand(resource.email(), resource.fullName(), resource.password()))
                .fold(account -> ResponseEntity.status(HttpStatus.CREATED)
                                .body(AuthenticatedUserResource.from(account)
                                        .withToken(tokens.issue(account.getId()))),
                        ProblemDetailResponseAssembler::toResponseEntity);
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sign in with email and password")
    public ResponseEntity<?> signIn(@RequestBody SignInResource resource) {
        return authenticationCommandService
                .handle(new SignInCommand(resource.email(), resource.password()))
                .fold(account -> ResponseEntity.ok(AuthenticatedUserResource.from(account)
                                .withToken(tokens.issue(account.getId()))),
                        ProblemDetailResponseAssembler::toResponseEntity);
    }

    @GetMapping("/me")
    @Operation(summary = "Get the signed-in account", description = "Uses the account of the access token.")
    public ResponseEntity<?> me(@RequestHeader("X-Account-Id") Long accountId) {
        return authenticationCommandService.findById(accountId)
                .<ResponseEntity<?>>map(account -> ResponseEntity.ok(AuthenticatedUserResource.from(account)))
                .orElseGet(() -> ProblemDetailResponseAssembler.toResponseEntity(IamErrors.accountNotFound(accountId)));
    }
}
