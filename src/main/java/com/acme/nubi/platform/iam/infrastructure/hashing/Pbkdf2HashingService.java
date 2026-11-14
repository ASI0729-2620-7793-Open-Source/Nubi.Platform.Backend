package com.acme.nubi.platform.iam.infrastructure.hashing;

import com.acme.nubi.platform.iam.application.internal.outboundservices.hashing.HashingService;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

/**
 * Hash PBKDF2 con sal aleatoria, solo con la JDK (sin dependencias extra).
 * Formato guardado: {@code iteraciones:sal:hash} en Base64.
 */
@Service
public class Pbkdf2HashingService implements HashingService {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String encode(String rawPassword) {
        var salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        var hash = derive(rawPassword, salt, ITERATIONS);
        var encoder = Base64.getEncoder();
        return ITERATIONS + ":" + encoder.encodeToString(salt) + ":" + encoder.encodeToString(hash);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        var parts = encodedPassword.split(":");
        if (parts.length != 3) {
            return false;
        }
        var decoder = Base64.getDecoder();
        var iterations = Integer.parseInt(parts[0]);
        var salt = decoder.decode(parts[1]);
        var expected = decoder.decode(parts[2]);
        return MessageDigest.isEqual(expected, derive(rawPassword, salt, iterations));
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        try {
            var spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña.", exception);
        }
    }
}
