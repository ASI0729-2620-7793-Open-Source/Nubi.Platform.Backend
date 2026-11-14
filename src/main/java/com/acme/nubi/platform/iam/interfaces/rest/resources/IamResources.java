package com.acme.nubi.platform.iam.interfaces.rest.resources;

import com.acme.nubi.platform.iam.domain.model.aggregates.UserAccount;

/** Recursos REST del Bounded Context IAM. */
public final class IamResources {

    private IamResources() {
    }

    public record SignUpResource(String email, String fullName, String password) {
    }

    public record SignInResource(String email, String password) {
    }

    /** Cuenta con la sesión iniciada. El frontend envía {@code id} en la cabecera X-Account-Id. */
    public record AuthenticatedUserResource(Long id, String email, String fullName) {

        public static AuthenticatedUserResource from(UserAccount account) {
            return new AuthenticatedUserResource(account.getId(), account.getEmail(), account.getFullName());
        }
    }
}
