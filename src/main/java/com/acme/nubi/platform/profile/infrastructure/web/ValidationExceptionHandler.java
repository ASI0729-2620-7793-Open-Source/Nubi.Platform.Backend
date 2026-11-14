package com.acme.nubi.platform.profile.infrastructure.web;

import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte los fallos de validación del marco en ProblemDetail 400 con el
 * detalle del campo inválido (TS-01: "responde con 400 y un detalle del campo").
 */
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> onInvalidArguments(
            MethodArgumentNotValidException ex) {
        ProblemDetail detail = problem("Datos inválidos",
                "Hay campos que no cumplen las reglas de validación.",
                "request.validation");

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        detail.setProperty("errors", errors);
        if (!errors.isEmpty()) {
            detail.setProperty("field", errors.keySet().iterator().next());
        }
        return badRequest(detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> onUnreadableBody(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String message = cause == null || cause.getMessage() == null
                ? "El cuerpo de la petición no tiene un formato válido."
                : cause.getMessage();

        ProblemDetail detail = problem("Datos inválidos", message, "request.malformed");
        String field = extractField(cause);
        if (field != null) {
            detail.setProperty("field", field);
            detail.setProperty("errors", Map.of(field, message));
        }
        return badRequest(detail);
    }

    /**
     * Intenta recuperar el nombre del campo responsable de un fallo de
     * deserialización para poder devolver el detalle que pide TS-01.
     */
    private static String extractField(Throwable cause) {
        if (cause == null) {
            return null;
        }
        String text = cause.getMessage();
        if (text == null) {
            return null;
        }
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("property '([^']+)'").matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = java.util.regex.Pattern.compile("property \"([^\"]+)\"").matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> onTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        ProblemDetail detail = problem("Datos inválidos",
                "El valor de \"" + ex.getName() + "\" no tiene el formato esperado.",
                "request.typeMismatch");
        detail.setProperty("field", ex.getName());
        return badRequest(detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> onConstraintViolation(
            ConstraintViolationException ex) {
        return badRequest(problem("Datos inválidos", ex.getMessage(),
                "request.constraintViolation"));
    }

    private static ProblemDetail problem(String title, String message, String code) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle(title);
        detail.setDetail(message);
        detail.setProperty("code", code);
        return detail;
    }

    private static ResponseEntity<ProblemDetail> badRequest(ProblemDetail detail) {
        return ResponseEntity.badRequest().body(detail);
    }
}
