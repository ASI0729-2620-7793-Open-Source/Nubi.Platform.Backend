package com.acme.nubi.platform.iam.application.internal.outboundservices.hashing;

/** Puerto de salida: cómo se protege la contraseña lo decide la infraestructura. */
public interface HashingService {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
