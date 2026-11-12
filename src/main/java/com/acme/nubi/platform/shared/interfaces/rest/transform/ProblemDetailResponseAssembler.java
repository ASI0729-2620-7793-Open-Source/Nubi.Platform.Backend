package com.acme.nubi.platform.shared.interfaces.rest.transform;

import com.acme.nubi.platform.shared.domain.model.errors.DomainError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Traduce un {@link DomainError} a una respuesta RFC 9457 (Problem Details).
 */
public final class ProblemDetailResponseAssembler {

    private ProblemDetailResponseAssembler() {
    }

    public static ResponseEntity<ProblemDetail> toResponseEntity(DomainError error) {
        var status = switch (error.type()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
        var problemDetail = ProblemDetail.forStatusAndDetail(status, error.message());
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setProperty("code", error.code());
        return ResponseEntity.status(status).body(problemDetail);
    }
}
