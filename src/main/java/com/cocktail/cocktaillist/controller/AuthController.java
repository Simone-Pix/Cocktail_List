package com.cocktail.cocktaillist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints per ottenere e visualizzare token JWT")
public class AuthController {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    @Operation(
        summary = "Login e ottieni token JWT",
        description = "Inserisci username e password per ottenere il token JWT da Keycloak. " +
                     "Il token ricevuto può essere copiato e usato nel 'Bearer Authentication' di Swagger."
    )
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "cocktail-client") String clientId
    ) {
        try {
            // Prepara la richiesta a Keycloak
            String tokenUrl = keycloakUrl + "/realms/cocktail-realm/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("username", username);
            body.add("password", password);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            // Chiama Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                
                // Formatta la risposta per essere più leggibile
                Map<String, Object> result = new HashMap<>();
                result.put("access_token", tokenResponse.get("access_token"));
                result.put("token_type", tokenResponse.get("token_type"));
                result.put("expires_in", tokenResponse.get("expires_in"));
                result.put("refresh_token", tokenResponse.get("refresh_token"));
                result.put("refresh_expires_in", tokenResponse.get("refresh_expires_in"));
                
                // Istruzioni per l'uso
                result.put("instructions", "Copia il valore di 'access_token' e usalo nel 'Bearer Authentication' di Swagger");
                
                return ResponseEntity.ok(result);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenziali non valide"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'autenticazione: " + e.getMessage()));
        }
    }

    @GetMapping("/token-info")
    @Operation(
        summary = "Visualizza informazioni del token corrente",
        description = "Mostra il contenuto decodificato del token JWT corrente (richiede autenticazione)"
    )
    public ResponseEntity<?> getTokenInfo(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nessun token presente. Usa prima il 'Bearer Authentication' per autenticarti."));
        }
        
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("subject", jwt.getSubject());
        tokenInfo.put("issuer", jwt.getIssuer().toString());
        tokenInfo.put("expires_at", jwt.getExpiresAt());
        tokenInfo.put("issued_at", jwt.getIssuedAt());
        tokenInfo.put("preferred_username", jwt.getClaimAsString("preferred_username"));
        tokenInfo.put("email", jwt.getClaimAsString("email"));
        tokenInfo.put("roles", jwt.getClaimAsMap("realm_access"));
        tokenInfo.put("raw_token", jwt.getTokenValue());
        
        return ResponseEntity.ok(tokenInfo);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Rinnova il token usando refresh_token",
        description = "Ottieni un nuovo access_token usando il refresh_token"
    )
    public ResponseEntity<?> refreshToken(
            @RequestParam String refreshToken,
            @RequestParam(defaultValue = "cocktail-client") String clientId
    ) {
        try {
            String tokenUrl = keycloakUrl + "/realms/cocktail-realm/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", clientId);
            body.add("refresh_token", refreshToken);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(response.getBody());
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token non valido"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il refresh: " + e.getMessage()));
        }
    }
}
