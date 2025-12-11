package com.cocktail.cocktaillist.controller;

import com.cocktail.cocktaillist.dto.LoginRequest;
import com.cocktail.cocktaillist.dto.LoginResponse;
import com.cocktail.cocktaillist.dto.RefreshRequest;
import com.cocktail.cocktaillist.dto.RegisterRequest;
import com.cocktail.cocktaillist.service.KeycloakAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints per autenticazione e gestione token JWT")
public class AuthController {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakUrl;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    private final RestTemplate restTemplate = new RestTemplate();

    // Pattern per validazione email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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

    @PostMapping("/register")
    @Operation(
        summary = "Registra nuovo utente",
        description = "Crea un nuovo account utente con form data. " +
                     "Dopo la registrazione, l'utente può fare login immediatamente. " +
                     "Il nuovo utente riceve automaticamente il ruolo USER."
    )
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        try {
            // Validazione input
            Map<String, String> validationErrors = validateRegistrationData(
                username, email, password, confirmPassword
            );
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("errors", validationErrors));
            }

            // Crea utente in Keycloak
            String userId = keycloakAdminService.createUser(
                username,
                email,
                password,
                firstName != null ? firstName : "",
                lastName != null ? lastName : ""
            );

            // Auto-login dopo registrazione
            ResponseEntity<?> loginResponse = login(username, password);
            
            if (loginResponse.getStatusCode() == HttpStatus.OK && loginResponse.getBody() != null) {
                Object loginBody = loginResponse.getBody();
                
                // Se è un LoginResponse, estrai i dati
                if (loginBody instanceof LoginResponse) {
                    LoginResponse loginData = (LoginResponse) loginBody;
                    Map<String, Object> responseBody = new HashMap<>();
                    responseBody.put("success", true);
                    responseBody.put("message", "✅ Registrazione completata! Sei già loggato.");
                    responseBody.put("userId", userId);
                    responseBody.put("username", username);
                    responseBody.put("token", loginData.getToken());
                    responseBody.put("tokenType", loginData.getTokenType());
                    responseBody.put("expiresIn", loginData.getExpiresIn());
                    responseBody.put("refreshToken", loginData.getRefreshToken());
                    return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
                }
            }
            
            // Se auto-login fallisce, restituisci conferma registrazione
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true,
                        "message", "✅ Registrazione completata! Ora puoi fare login con le tue credenziali.",
                        "userId", userId,
                        "username", username,
                        "loginUrl", "/api/auth/login"
                    ));
            
        } catch (RuntimeException e) {
            // Gestisci errori specifici di Keycloak
            String errorMessage = e.getMessage();
            
            if (errorMessage.contains("Username già in uso")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                            "success", false,
                            "error", "Username già esistente",
                            "message", "⚠️ Questo username è già registrato. Prova a fare login o usa un altro username.",
                            "suggestion", "Se è il tuo account, usa /api/auth/login",
                            "loginUrl", "/api/auth/login"
                        ));
            }
            
            if (errorMessage.contains("Email già registrata")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                            "success", false,
                            "error", "Email già esistente",
                            "message", "⚠️ Questa email è già registrata. Prova a fare login o usa un'altra email.",
                            "suggestion", "Se è il tuo account, usa /api/auth/login",
                            "loginUrl", "/api/auth/login"
                        ));
            }
            
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "error", errorMessage,
                        "message", "❌ Errore durante la registrazione: " + errorMessage
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", "Errore interno del server",
                        "message", "❌ Si è verificato un errore imprevisto: " + e.getMessage()
                    ));
        }
    }

    /**
     * Valida i dati di registrazione
     */
    private Map<String, String> validateRegistrationData(
            String username, String email, String password, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();

        // Username
        if (username == null || username.trim().isEmpty()) {
            errors.put("username", "Username obbligatorio");
        } else if (username.length() < 3) {
            errors.put("username", "Username deve essere almeno 3 caratteri");
        } else if (username.length() < 3) {
            errors.put("username", "Username deve essere almeno 3 caratteri");
        } else if (username.length() > 50) {
            errors.put("username", "Username troppo lungo (max 50 caratteri)");
        }

        // Email
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email obbligatoria");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "Formato email non valido");
        }

        // Password
        if (password == null || password.isEmpty()) {
            errors.put("password", "Password obbligatoria");
        } else if (password.length() < 6) {
            errors.put("password", "Password deve essere almeno 6 caratteri");
        } else if (password.length() > 100) {
            errors.put("password", "Password troppo lunga (max 100 caratteri)");
        }

        // Conferma password
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            errors.put("confirmPassword", "Conferma password obbligatoria");
        } else if (!password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Le password non coincidono");
        }

        return errors;
    }
}
