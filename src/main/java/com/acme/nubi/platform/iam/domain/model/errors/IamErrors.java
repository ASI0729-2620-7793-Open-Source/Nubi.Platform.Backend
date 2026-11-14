package com.acme.nubi.platform.iam.domain.model.errors;

import com.acme.nubi.platform.shared.domain.model.errors.DomainError;

/** Errores de negocio del Bounded Context IAM. */
public final class IamErrors {

    private IamErrors() {
    }

    public static DomainError emailAlreadyRegistered() {
        return DomainError.conflict("iam.email.duplicated", "Ya existe una cuenta con ese correo.");
    }

    public static DomainError invalidCredentials() {
        return DomainError.validation("iam.credentials.invalid", "El correo o la contraseña no son correctos.");
    }

    public static DomainError weakPassword() {
        return DomainError.validation("iam.password.weak", "La contraseña debe tener al menos 6 caracteres.");
    }

    public static DomainError invalidData(String message) {
        return DomainError.validation("iam.data.invalid", message);
    }

    public static DomainError accountNotFound(Long accountId) {
        return DomainError.notFound("iam.account.notFound", "No existe la cuenta " + accountId + ".");
    }
}
