package com.acme.nubi.platform.shared.domain.model.errors;

/**
 * Error de negocio que devuelven los servicios dentro de un {@code Result}.
 *
 * @param code    código estable, por ejemplo "SelfRegulation.CalmSessionNotFound"
 * @param message descripción legible del error
 * @param type    clasificación que la capa REST traduce a un código HTTP
 */
public record DomainError(String code, String message, ErrorType type) {

    public static DomainError validation(String code, String message) {
        return new DomainError(code, message, ErrorType.VALIDATION);
    }

    public static DomainError notFound(String code, String message) {
        return new DomainError(code, message, ErrorType.NOT_FOUND);
    }

    public static DomainError conflict(String code, String message) {
        return new DomainError(code, message, ErrorType.CONFLICT);
    }
}
