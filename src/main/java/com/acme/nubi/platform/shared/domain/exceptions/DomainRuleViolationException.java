package com.acme.nubi.platform.shared.domain.exceptions;

/**
 * Se lanza cuando una operación viola una regla de negocio del dominio. Se traduce a 409 en la capa REST.
 */
public class DomainRuleViolationException extends RuntimeException {

    public DomainRuleViolationException(String message) {
        super(message);
    }
}
