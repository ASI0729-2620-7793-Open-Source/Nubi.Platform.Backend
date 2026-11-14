package com.acme.nubi.platform.profile.application.errors;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;
import com.acme.nubi.platform.shared.application.result.Result;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Convierte las excepciones de dominio en resultados y encadena operaciones.
 * Es el puente único entre las reglas de negocio y el patrón Result.
 */
public final class DomainGuard {

    private DomainGuard() {
    }

    /** Ejecuta una operación de dominio que puede violar una regla. */
    public static <T> Result<T, BusinessError> guard(Supplier<T> operation) {
        try {
            return Result.success(operation.get());
        } catch (DomainRuleViolation violation) {
            return Result.failure(toError(violation));
        }
    }

    /** Traduce una violación a su error de aplicación. */
    public static BusinessError toError(DomainRuleViolation violation) {
        return switch (violation.kind()) {
            case VALIDATION -> BusinessError.validation(
                    violation.field(), violation.code(), violation.getMessage());
            case NOT_FOUND -> BusinessError.notFound(
                    violation.code(), violation.getMessage());
            case CONFLICT -> BusinessError.conflict(
                    violation.field(), violation.code(), violation.getMessage());
        };
    }

    /** Aplica una transformación solo cuando el resultado fue exitoso. */
    public static <T, R> Result<R, BusinessError> map(
            Result<T, BusinessError> source, Function<T, R> mapper) {
        if (source.isFailure()) {
            return Result.failure(source.failure().orElseThrow());
        }
        return Result.success(mapper.apply(source.success().orElseThrow()));
    }

    /** Encadena dos operaciones que devuelven Result. */
    public static <T, R> Result<R, BusinessError> flatMap(
            Result<T, BusinessError> source,
            Function<T, Result<R, BusinessError>> next) {
        if (source.isFailure()) {
            return Result.failure(source.failure().orElseThrow());
        }
        return next.apply(source.success().orElseThrow());
    }
}
