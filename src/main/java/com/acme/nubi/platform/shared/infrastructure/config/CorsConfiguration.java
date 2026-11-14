package com.acme.nubi.platform.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite al frontend (otro origen en producción) llamar a la API.
 *
 * En desarrollo el proxy de Vite ya no es necesario para el Bounded Context
 * Perfil, que se sirve en http://localhost:8080; sin esto el navegador
 * bloquearía las peticiones con cabecera X-Account-Id (pre-flight OPTIONS).
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    private static final String API_PATTERN = "/api/v1/**";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(API_PATTERN)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
