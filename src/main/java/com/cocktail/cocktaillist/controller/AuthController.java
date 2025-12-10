package com.cocktail.cocktaillist.controller;

import com.cocktail.cocktaillist.dto.LoginRequest;
import com.cocktail.cocktaillist.dto.LoginResponse;
import com.cocktail.cocktaillist.dto.RefreshRequest;
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
@Tag(name = "Authentication", description = "Endpoints per autenticazione e gestione token JWT")
public class AuthController {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    @Operation(
        summary = "Login con username e password",
        description = "Effettua il login con username e password e ricevi il token JWT. " +
                     "Usa il token nel campo 'Bearer Authentication' di Swagger per autenticarti negli altri endpoint."
    )
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        try {
            // Prepara la richiesta di refresh
            String tokenUrl = keycloakUrl + "/realms/cocktail_realm/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", "cocktail-client");
            body.add("username", loginRequest.getUsername());
            body.add("password", loginRequest.getPassword());
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            // Chiama Keycloak per ottenere il token
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> keycloakResponse = response.getBody();
                
                // Crea risposta pulita con solo i dati necessari
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setToken((String) keycloakResponse.get("access_token"));
                loginResponse.setExpiresIn((Integer) keycloakResponse.get("expires_in"));
                loginResponse.setRefreshToken((String) keycloakResponse.get("refresh_token"));
                
                return ResponseEntity.ok(loginResponse);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenziali non valide"));
            
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
        summary = "Rinnova il token JWT",
        description = "Ottieni un nuovo access_token usando il refresh_token ricevuto al login"
    )
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {
        RefreshRequest refreshRequest = new RefreshRequest(refreshToken);
        try {
            String tokenUrl = keycloakUrl + "/realms/cocktail-realm/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", "cocktail-client");
            body.add("refresh_token", refreshRequest.getRefreshToken());
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> keycloakResponse = response.getBody();
                
                // Crea risposta pulita
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setToken((String) keycloakResponse.get("access_token"));
                loginResponse.setExpiresIn((Integer) keycloakResponse.get("expires_in"));
                loginResponse.setRefreshToken((String) keycloakResponse.get("refresh_token"));
                
                return ResponseEntity.ok(loginResponse);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token non valido o scaduto"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il refresh: " + e.getMessage()));
        }
    }
}
