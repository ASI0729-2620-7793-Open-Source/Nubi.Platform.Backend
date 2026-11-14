package com.acme.nubi.platform.profile.infrastructure.web;

import com.acme.nubi.platform.profile.application.dto.ProfileRequests.ChangePlanRequest;
import com.acme.nubi.platform.profile.application.services.SubscriptionApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de la suscripción y los límites de plan (US-46 a US-48).
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionApplicationService service;

    /** Estado actual: crea implícitamente la suscripción FREEMIUM si falta. */
    @GetMapping("/current")
    public ResponseEntity<?> current(
            @RequestHeader(ProfileController.ACCOUNT_HEADER) Long accountId) {
        return ResultHttpResponse.ok(service.current(accountId));
    }

    @PutMapping("/current/plan")
    public ResponseEntity<?> changePlan(
            @RequestHeader(ProfileController.ACCOUNT_HEADER) Long accountId,
            @Valid @RequestBody ChangePlanRequest request) {
        return ResultHttpResponse.ok(service.changePlan(accountId, request));
    }
}
