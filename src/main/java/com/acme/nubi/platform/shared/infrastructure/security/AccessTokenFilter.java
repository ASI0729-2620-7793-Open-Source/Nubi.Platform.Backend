package com.acme.nubi.platform.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Exige {@code Authorization: Bearer <token>} en la API. La cuenta sale del token y reemplaza a
 * cualquier cabecera {@code X-Account-Id} que envíe el cliente. Quedan abiertas el registro, el
 * inicio de sesión y las consultas de los catálogos.
 */
@Component
public class AccessTokenFilter extends OncePerRequestFilter {

    static final String ACCOUNT_HEADER = "X-Account-Id";
    private static final String BEARER = "Bearer ";
    private static final Set<String> OPEN_POSTS =
            Set.of("/api/v1/authentication/sign-in", "/api/v1/authentication/sign-up");
    private static final List<String> OPEN_CATALOGS =
            List.of("/api/v1/calming-resources", "/api/v1/action-guides");

    private final AccessTokenService tokens;
    private final boolean required;

    public AccessTokenFilter(
            AccessTokenService tokens,
            @Value("${nubi.security.token-required:true}") boolean required) {
        this.tokens = tokens;
        this.required = required;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();
        if (!required || !path.startsWith("/api/v1/") || method.equals("OPTIONS")) {
            return true;
        }
        if (method.equals("POST") && OPEN_POSTS.contains(path)) {
            return true;
        }
        return method.equals("GET") && OPEN_CATALOGS.stream()
                .anyMatch(open -> path.equals(open) || path.startsWith(open + "/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        Optional<Long> account = header != null && header.startsWith(BEARER)
                ? tokens.verify(header.substring(BEARER.length()).trim())
                : Optional.empty();
        if (account.isEmpty()) {
            reject(response);
            return;
        }
        chain.doFilter(withAccount(request, String.valueOf(account.get())), response);
    }

    private static void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,"
                + "\"detail\":\"A valid access token is required.\",\"code\":\"auth.token.invalid\"}");
    }

    private static HttpServletRequest withAccount(HttpServletRequest request, String accountId) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                return ACCOUNT_HEADER.equalsIgnoreCase(name) ? accountId : super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return ACCOUNT_HEADER.equalsIgnoreCase(name)
                        ? Collections.enumeration(List.of(accountId))
                        : super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = new ArrayList<>(Collections.list(super.getHeaderNames()));
                if (names.stream().noneMatch(ACCOUNT_HEADER::equalsIgnoreCase)) {
                    names.add(ACCOUNT_HEADER);
                }
                return Collections.enumeration(names);
            }
        };
    }
}
