package com.acme.nubi.platform.profile.infrastructure.web;

import com.acme.nubi.platform.profile.application.dto.ProfileRequests.CreateProfileRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.InviteCaregiverRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.LinkTrustedContactRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.ReorderTrustedContactsRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateCaregiverStatusRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateDiagnosisRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdatePhotoRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateProfileRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateSensoryProfileRequest;
import com.acme.nubi.platform.profile.application.errors.BusinessError;
import com.acme.nubi.platform.profile.application.services.ProfileApplicationService;
import com.acme.nubi.platform.shared.application.result.Result;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del perfil y su personalización.
 *
 * <p>La identidad de la cuenta llega en la cabecera {@code X-Account-Id}: la
 * autenticación está fuera de este Bounded Context, de modo que la cabecera
 * sustituye al token hasta que el BC de Autenticación se integre.
 */
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    public static final String ACCOUNT_HEADER = "X-Account-Id";

    private final ProfileApplicationService service;

    // --- US-01: crear y consultar --------------------------------------

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @Valid @RequestBody CreateProfileRequest request) {
        return ResultHttpResponse.created(service.create(accountId, request));
    }

    @GetMapping
    public ResponseEntity<?> listMine(@RequestHeader(ACCOUNT_HEADER) Long accountId) {
        return ResponseEntity.ok(service.listOwned(accountId));
    }

    @GetMapping("/accessible")
    public ResponseEntity<?> listAccessible(@RequestHeader(ACCOUNT_HEADER) Long accountId) {
        return ResponseEntity.ok(service.listAccessible(accountId));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<?> get(
            @RequestHeader(ACCOUNT_HEADER) Long accountId, @PathVariable Long profileId) {
        return ResultHttpResponse.ok(service.get(accountId, profileId));
    }

    // --- US-02: ficha general ------------------------------------------

    @PutMapping("/{profileId}")
    public ResponseEntity<?> update(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResultHttpResponse.ok(service.update(accountId, profileId, request));
    }

    // --- US-03: sensibilidades sensoriales -----------------------------

    @GetMapping("/{profileId}/sensory-profile")
    public ResponseEntity<?> getSensoryProfile(
            @RequestHeader(ACCOUNT_HEADER) Long accountId, @PathVariable Long profileId) {
        return ResultHttpResponse.ok(service.getSensitivities(accountId, profileId));
    }

    @PutMapping("/{profileId}/sensory-profile")
    public ResponseEntity<?> updateSensoryProfile(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @Valid @RequestBody UpdateSensoryProfileRequest request) {
        return ResultHttpResponse.ok(
                service.updateSensitivities(accountId, profileId, request));
    }

    // --- US-04: diagnóstico declarado -----------------------------------

    @GetMapping("/{profileId}/diagnosis")
    public ResponseEntity<?> getDiagnosis(
            @RequestHeader(ACCOUNT_HEADER) Long accountId, @PathVariable Long profileId) {
        return ResultHttpResponse.ok(service.getDiagnosis(accountId, profileId));
    }

    @PutMapping("/{profileId}/diagnosis")
    public ResponseEntity<?> updateDiagnosis(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @Valid @RequestBody UpdateDiagnosisRequest request) {
        return ResultHttpResponse.ok(service.updateDiagnosis(accountId, profileId, request));
    }

    // --- US-05: foto ----------------------------------------------------

    @PutMapping("/{profileId}/photo")
    public ResponseEntity<?> updatePhoto(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @Valid @RequestBody UpdatePhotoRequest request) {
        return ResultHttpResponse.ok(service.updatePhoto(accountId, profileId, request));
    }

    // --- US-06: cuidadores asociados ------------------------------------

    @GetMapping("/{profileId}/caregivers")
    public ResponseEntity<?> listCaregivers(
            @RequestHeader(ACCOUNT_HEADER) Long accountId, @PathVariable Long profileId) {
        return ResultHttpResponse.ok(service.listCaregivers(accountId, profileId));
    }

    @PostMapping("/{profileId}/caregivers")
    public ResponseEntity<?> inviteCaregiver(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @Valid @RequestBody InviteCaregiverRequest request) {
        return ResultHttpResponse.created(
                service.inviteCaregiver(accountId, profileId, request));
    }

    /**
     * Cambia el estado de la invitación del cuidador. Con status = ACCEPTED (o ACTIVE) el
     * invitado acepta con su propia cuenta. Para revocar el acceso se usa DELETE.
     */
    @PatchMapping("/{profileId}/caregivers/{caregiverId}")
    public ResponseEntity<?> updateCaregiver(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @PathVariable Long caregiverId,
            @Valid @RequestBody UpdateCaregiverStatusRequest request) {
        String status = request.status().trim().toUpperCase();
        if (!status.equals("ACCEPTED") && !status.equals("ACTIVE")) {
            return ResultHttpResponse.ok(Result.failure(BusinessError.validation(
                    "status", "caregiver.status.invalid",
                    "El único cambio de estado permitido es ACCEPTED.")));
        }
        return ResultHttpResponse.ok(
                service.acceptCaregiver(accountId, profileId, caregiverId));
    }

    @DeleteMapping("/{profileId}/caregivers/{caregiverId}")
    public ResponseEntity<?> revokeCaregiver(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @PathVariable Long caregiverId) {
        return ResultHttpResponse.ok(
                service.revokeCaregiver(accountId, profileId, caregiverId));
    }

    // --- Contactos de confianza -----------------------------------------

    @GetMapping("/{profileId}/trusted-contacts")
    public ResponseEntity<?> listTrustedContacts(
            @RequestHeader(ACCOUNT_HEADER) Long accountId, @PathVariable Long profileId) {
        return ResultHttpResponse.ok(service.listTrustedContacts(accountId, profileId));
    }

    @PostMapping("/{profileId}/trusted-contacts")
    public ResponseEntity<?> linkTrustedContact(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @Valid @RequestBody LinkTrustedContactRequest request) {
        return ResultHttpResponse.created(
                service.linkTrustedContact(accountId, profileId, request));
    }

    @PutMapping("/{profileId}/trusted-contacts/order")
    public ResponseEntity<?> reorderTrustedContacts(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @Valid @RequestBody ReorderTrustedContactsRequest request) {
        return ResultHttpResponse.ok(
                service.reorderTrustedContacts(accountId, profileId, request));
    }

    @DeleteMapping("/{profileId}/trusted-contacts/{contactId}")
    public ResponseEntity<?> removeTrustedContact(
            @RequestHeader(ACCOUNT_HEADER) Long accountId,
            @PathVariable Long profileId,
            @PathVariable Long contactId) {
        return ResultHttpResponse.ok(
                service.removeTrustedContact(accountId, profileId, contactId));
    }

    // --- Archivado -------------------------------------------------------

    @DeleteMapping("/{profileId}")
    public ResponseEntity<?> archive(
            @RequestHeader(ACCOUNT_HEADER) Long accountId, @PathVariable Long profileId) {
        return ResultHttpResponse.ok(service.archive(accountId, profileId));
    }
}
