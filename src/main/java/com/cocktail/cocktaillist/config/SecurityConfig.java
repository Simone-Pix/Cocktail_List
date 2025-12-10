package com.cocktail.cocktaillist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configurazione Spring Security per OAuth2 con Keycloak
 * Gestisce autenticazione JWT e autorizzazione basata su ruoli
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Configura la catena di filtri di sicurezza
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabilita CSRF per API REST stateless
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/swagger-ui").permitAll() // Home e redirect a Swagger
                .requestMatchers("/api/public/**").permitAll() // Endpoint pubblici
                .requestMatchers("/api/auth/**").permitAll() // Endpoint di autenticazione (login, refresh)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger UI e OpenAPI docs
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN") // Richiede ruolo USER o ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Solo ADMIN
                .anyRequest().authenticated() // Tutti gli altri endpoint richiedono autenticazione
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Nessuna sessione server-side
            );

        return http.build();
    }

    /**
     * Converter per estrarre i ruoli dal JWT di Keycloak
     * Keycloak inserisce i ruoli in "realm_access.roles" e "resource_access.{client}.roles"
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    /**
     * Estrae le authorities (ruoli) dal JWT
     * Combina ruoli da scope standard e da Keycloak realm/resource access
     */
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            // Authorities standard da "scope"
            JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

            // Estrai ruoli Keycloak da "realm_access.roles"
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            Collection<GrantedAuthority> realmRoles = List.of();
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?>) {
                realmRoles = ((List<?>) realmAccess.get("roles")).stream()
                    .filter(role -> role instanceof String)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
            }

            // Estrai ruoli Keycloak da "resource_access.{client}.roles"
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            Collection<GrantedAuthority> resourceRoles = List.of();
            if (resourceAccess != null) {
                resourceRoles = resourceAccess.values().stream()
                    .filter(res -> res instanceof Map)
                    .flatMap(res -> {
                        Object roles = ((Map<?, ?>) res).get("roles");
                        if (roles instanceof List<?>) {
                            return ((List<?>) roles).stream()
                                .filter(role -> role instanceof String)
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
                        }
                        return Stream.empty();
                    })
                    .collect(Collectors.toList());
            }

            // Combina tutte le authorities
            return Stream.of(authorities, realmRoles, resourceRoles)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        };
    }
}
