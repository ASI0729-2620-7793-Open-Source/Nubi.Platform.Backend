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

    /** Cuenta con la sesión iniciada. El frontend envía {@code token} como {@code Authorization: Bearer}. */
    public record AuthenticatedUserResource(Long id, String email, String fullName, String token) {

        public static AuthenticatedUserResource from(UserAccount account) {
            return new AuthenticatedUserResource(account.getId(), account.getEmail(), account.getFullName(), null);
        }

        public AuthenticatedUserResource withToken(String issued) {
            return new AuthenticatedUserResource(id, email, fullName, issued);
        }
    }
}
