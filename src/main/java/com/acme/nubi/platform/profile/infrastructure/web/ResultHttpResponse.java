package com.acme.nubi.platform.profile.infrastructure.web;

import com.acme.nubi.platform.profile.application.errors.BusinessError;
import com.acme.nubi.platform.shared.application.result.Result;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Traduce un Result de aplicación a una respuesta HTTP con ProblemDetail
 * (RFC 9457): 2xx con el cuerpo del recurso, 4xx con el detalle del problema.
 */
public final class ResultHttpResponse {

    private ResultHttpResponse() {
    }

    public static <T> ResponseEntity<?> ok(Result<T, BusinessError> result) {
        return toHttp(result, HttpStatus.OK);
    }

    public static <T> ResponseEntity<?> created(Result<T, BusinessError> result) {
        return toHttp(result, HttpStatus.CREATED);
    }

    public static <T> ResponseEntity<?> toHttp(
            Result<T, BusinessError> result, HttpStatus successStatus) {
        return result.<ResponseEntity<?>>fold(
                body -> ResponseEntity.status(successStatus).body(body),
                error -> ResponseEntity.status(statusFor(error)).body(problemDetail(error)));
    }

    public static HttpStatus statusFor(BusinessError error) {
        return switch (error.kind()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
    }

    public static ProblemDetail problemDetail(BusinessError error) {
        HttpStatus status = statusFor(error);
        ProblemDetail detail = ProblemDetail.forStatus(status);
        detail.setTitle(defaultTitle(status));
        detail.setDetail(error.message());
        detail.setProperty("code", error.code());
        if (error.field() != null) {
            detail.setProperty("field", error.field());
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put(error.field(), error.message());
            detail.setProperty("errors", fields);
        }
        return detail;
    }

    private static String defaultTitle(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Datos inválidos";
            case NOT_FOUND -> "Recurso no encontrado";
            case CONFLICT -> "Conflicto con el estado actual del recurso";
            default -> status.getReasonPhrase();
        };
    }
}
