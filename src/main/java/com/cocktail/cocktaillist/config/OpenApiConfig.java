package com.cocktail.cocktaillist.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione Swagger/OpenAPI con autenticazione OAuth2 Keycloak
 * Accessibile su: http://localhost:8081/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        // Per Swagger (browser), usa sempre localhost perché il browser non può risolvere 'keycloak'
        String tokenUrl = "http://localhost:8080/realms/cocktail-realm/protocol/openid-connect/token";
        
        return new OpenAPI()
            .info(new Info()
                .title("Cocktail List API")
                .version("1.0")
                .description("API REST per gestione cocktail con autenticazione Keycloak OAuth2/JWT\n\n" +
                    "**Per autenticarti:**\n" +
                    "1. Clicca 'Authorize'\n" +
                    "2. Inserisci le credenziali:\n" +
                    "   - Username: \n" +
                    "   - Password: \n" +
                    "   - Client ID: cocktail-client (già precompilato)\n" +
                    "3. Clicca 'Authorize'\n\n" +
                    "Oppure usa direttamente il token JWT nel campo 'Bearer Authentication'"))
            .addSecurityItem(new SecurityRequirement()
                .addList("OAuth2")
                .addList("Bearer Authentication"))
            .components(new Components()
                // OAuth2 Password Flow - Login con username/password
                .addSecuritySchemes("OAuth2", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .description("Autenticazione OAuth2 con Keycloak - Inserisci username e password per ottenere il token automaticamente")
                        .flows(new OAuthFlows()
                            .password(new OAuthFlow()
                                .tokenUrl(tokenUrl)
                                .scopes(new Scopes()
                                    .addString("profile", "Profilo utente")
                                    .addString("email", "Email utente")))))
                // Bearer Token manuale - Se hai già il token
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .name("Bearer Authentication")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Inserisci manualmente il token JWT se lo hai già ottenuto")));
    }
}
