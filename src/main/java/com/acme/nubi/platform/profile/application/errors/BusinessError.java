package com.acme.nubi.platform.profile.application.errors;

/**
 * Error de negocio del Bounded Context Perfil y Personalización.
 * Se devuelve dentro de un Result para que los servicios no lancen excepciones.
 */
public record BusinessError(ErrorKind kind, String code, String message, String field) {

    public static BusinessError validation(String field, String code, String message) {
        return new BusinessError(ErrorKind.VALIDATION, code, message, field);
    }

    public static BusinessError notFound(String code, String message) {
        return new BusinessError(ErrorKind.NOT_FOUND, code, message, null);
    }

    public static BusinessError conflict(String code, String message) {
        return new BusinessError(ErrorKind.CONFLICT, code, message, null);
    }

    public static BusinessError conflict(String field, String code, String message) {
        return new BusinessError(ErrorKind.CONFLICT, code, message, field);
    }
}
