package com.acme.nubi.platform.profile.domain.errors;

/**
 * Viola de una regla de negocio dentro del dominio. Es unchecked para que los
 * agregados no dependan de la capa de aplicación; el servicio la convierte en
 * un Result&lt;BusinessError&gt; antes de llegar al controlador.
 */
public class DomainRuleViolation extends RuntimeException {

    private final DomainRuleKind kind;
    private final String code;
    private final String field;

    public DomainRuleViolation(String code, String message) {
        this(DomainRuleKind.VALIDATION, code, message, null);
    }

    public DomainRuleViolation(String code, String message, String field) {
        this(DomainRuleKind.VALIDATION, code, message, field);
    }

    public DomainRuleViolation(DomainRuleKind kind, String code, String message) {
        this(kind, code, message, null);
    }

    public DomainRuleViolation(DomainRuleKind kind, String code, String message, String field) {
        super(message);
        this.kind = kind;
        this.code = code;
        this.field = field;
    }

    public static DomainRuleViolation notFound(String code, String message) {
        return new DomainRuleViolation(DomainRuleKind.NOT_FOUND, code, message, null);
    }

    public static DomainRuleViolation conflict(String code, String message, String field) {
        return new DomainRuleViolation(DomainRuleKind.CONFLICT, code, message, field);
    }

    public DomainRuleKind kind() {
        return kind;
    }

    public String code() {
        return code;
    }

    public String field() {
        return field;
    }
}
