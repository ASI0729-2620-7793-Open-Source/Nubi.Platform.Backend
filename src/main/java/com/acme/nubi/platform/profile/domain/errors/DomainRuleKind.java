package com.acme.nubi.platform.profile.domain.errors;

/**
 * Naturaleza de una regla violada. El dominio no conoce HTTP; la capa de
 * aplicación traduce estos valores a los códigos de estado que corresponda.
 */
public enum DomainRuleKind {
    /** El dato recibido no cumple la regla o falta un campo. */
    VALIDATION,
    /** El recurso referenciado no existe. */
    NOT_FOUND,
    /** El estado actual impide la operación: límite, duplicado o transición. */
    CONFLICT
}
