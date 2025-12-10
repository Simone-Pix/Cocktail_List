package com.cocktail.cocktaillist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurazione Web MVC
 * Configura il redirect automatico dalla home page a Swagger UI
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Redirect da / a /swagger-ui/index.html
     * Quando visiti http://localhost:8081/ vieni automaticamente reindirizzato a Swagger
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/swagger-ui/index.html");
        registry.addRedirectViewController("/swagger-ui", "/swagger-ui/index.html");
    }
}
