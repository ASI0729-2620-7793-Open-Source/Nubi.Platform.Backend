package com.acme.nubi.platform.shared.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Emite y verifica los tokens de acceso: {@code base64url(cuenta.vencimiento).base64url(HMAC)}.
 * La clave llega en {@code NUBI_TOKEN_SECRET}; si no se define, se genera una al arrancar y los
 * tokens dejan de valer cuando el servicio se reinicia.
 */
@Service
public class AccessTokenService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Duration LIFETIME = Duration.ofDays(7);
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final SecretKeySpec key;
    private final Clock clock;

    @Autowired
    public AccessTokenService(@Value("${nubi.security.token-secret:}") String secret) {
        this(secret, Clock.systemUTC());
    }

    AccessTokenService(String secret, Clock clock) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length == 0) {
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        }
        this.key = new SecretKeySpec(bytes, ALGORITHM);
        this.clock = clock;
    }

    public String issue(Long accountId) {
        long expiresAt = clock.instant().plus(LIFETIME).getEpochSecond();
        byte[] payload = (accountId + "." + expiresAt).getBytes(StandardCharsets.UTF_8);
        return ENCODER.encodeToString(payload) + "." + ENCODER.encodeToString(sign(payload));
    }

    /** Cuenta del token, o vacío si está mal formado, alterado o vencido. */
    public Optional<Long> verify(String token) {
        if (token == null) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            byte[] payload = DECODER.decode(parts[0]);
            if (!MessageDigest.isEqual(sign(payload), DECODER.decode(parts[1]))) {
                return Optional.empty();
            }
            String[] values = new String(payload, StandardCharsets.UTF_8).split("\\.");
            if (Long.parseLong(values[1]) <= clock.instant().getEpochSecond()) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(values[0]));
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException exception) {
            return Optional.empty();
        }
    }

    private byte[] sign(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return mac.doFinal(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HMAC no disponible", exception);
        }
    }
}
