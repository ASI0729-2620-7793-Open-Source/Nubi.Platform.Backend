package com.acme.nubi.platform.profile.application.errors;

/** Tipo de fallo de negocio, usado para elegir el código HTTP de respuesta. */
public enum ErrorKind {
    /** 400: el dato recibido no cumple una regla o falta un campo. */
    VALIDATION,
    /** 404: el recurso no existe. */
    NOT_FOUND,
    /** 409: el estado actual impide la operación (límite, duplicado, transición). */
    CONFLICT
}
