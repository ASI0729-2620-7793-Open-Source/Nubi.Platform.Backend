package com.acme.nubi.platform.iam.domain.model.aggregates;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Aggregate root del Bounded Context IAM: la cuenta del cuidador que inicia sesión en Nubi.
 * Guarda solo el hash de la contraseña, nunca la contraseña en claro.
 */
public class UserAccount {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Long id;
    private final String email;
    private final String fullName;
    private final String passwordHash;
    private final Instant createdAt;

    public UserAccount(Long id, String email, String fullName, String passwordHash, Instant createdAt) {
        this.id = id;
        this.email = Objects.requireNonNull(email);
        this.fullName = fullName;
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.createdAt = createdAt;
    }

    /** Fábrica de una cuenta nueva: valida el correo y el nombre. */
    public static UserAccount register(String email, String fullName, String passwordHash, Instant now) {
        var normalized = normalizeEmail(email);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        return new UserAccount(null, normalized, fullName.trim(), passwordHash, now);
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
