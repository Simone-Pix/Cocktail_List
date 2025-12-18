package com.cocktail.cocktaillist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurazione Web MVC
 * 
 * NOTA: Il redirect automatico a Swagger UI è stato RIMOSSO per permettere
 * l'accesso alle API da app mobile e client frontend.
 * 
 * Per accedere a Swagger UI: http://localhost:8081/swagger-ui/index.html
 * Per testare le API: usa direttamente gli endpoint /api/*
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Redirect rimosso - app mobile necessitano accesso diretto alle API
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*") // Permetti tutte le origini (mobile, web, etc.)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true) // Necessario per JWT Bearer tokens
            .maxAge(3600); // Cache preflight per 1 ora
    }
}
