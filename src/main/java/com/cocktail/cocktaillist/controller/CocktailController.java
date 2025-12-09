package com.cocktail.cocktaillist.controller;

import com.cocktail.cocktaillist.model.Cocktail;
import com.cocktail.cocktaillist.service.CocktailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller - Gestisce le richieste HTTP e risponde con JSON.
 * 
 * Endpoints disponibili:
 * - /api/public/**  → Accessibili a tutti (anche senza autenticazione)
 * - /api/user/**    → Richiede autenticazione e ruolo USER
 * - /api/admin/**   → Richiede autenticazione e ruolo ADMIN
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Per frontend React/Angular
public class CocktailController {

    @Autowired
    private CocktailService cocktailService;

    // ========================================
    // ENDPOINT PUBBLICI (senza autenticazione)
    // ========================================

    /**
     * Endpoint di test pubblico.
     * GET http://localhost:8081/api/public/hello
     */
    @GetMapping("/public/hello")
    public Map<String, String> publicHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ciao! Questo è un endpoint pubblico accessibile a tutti");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }

    /**
     * Lista pubblica dei cocktail (solo nomi e categorie).
     * GET http://localhost:8081/api/public/cocktails
     */
    @GetMapping("/public/cocktails")
    public List<Cocktail> getPublicCocktails() {
        return cocktailService.getAllCocktails();
    }

    // ========================================
    // ENDPOINT USER (richiede autenticazione)
    // ========================================

    /**
     * Profilo dell'utente autenticato.
     * GET http://localhost:8081/api/user/profile
     * Header: Authorization: Bearer <token>
     * 
     * @param jwt Token JWT iniettato automaticamente da Spring Security
     */
    @GetMapping("/user/profile")
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", jwt.getClaimAsString("preferred_username"));
        profile.put("email", jwt.getClaimAsString("email"));
        profile.put("firstName", jwt.getClaimAsString("given_name"));
        profile.put("lastName", jwt.getClaimAsString("family_name"));
        profile.put("roles", jwt.getClaimAsStringList("realm_access.roles"));
        profile.put("tokenExpiration", jwt.getExpiresAt());
        return profile;
    }

    /**
     * Lista completa dei cocktail (tutti i dettagli).
     * GET http://localhost:8081/api/user/cocktails
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/user/cocktails")
    @PreAuthorize("hasRole('USER')")
    public List<Cocktail> getAllCocktails() {
        return cocktailService.getAllCocktails();
    }

    /**
     * Dettaglio di un singolo cocktail.
     * GET http://localhost:8081/api/user/cocktails/{id}
     * 
     * @param id ID del cocktail
     */
    @GetMapping("/user/cocktails/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cocktail> getCocktailById(@PathVariable Long id) {
        try {
            Cocktail cocktail = cocktailService.getCocktailById(id);
            return ResponseEntity.ok(cocktail);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cerca cocktail per categoria.
     * GET http://localhost:8081/api/user/cocktails/category/Rum
     * 
     * @param category Categoria da cercare
     */
    @GetMapping("/user/cocktails/category/{category}")
    @PreAuthorize("hasRole('USER')")
    public List<Cocktail> getCocktailsByCategory(@PathVariable String category) {
        return cocktailService.getCocktailsByCategory(category);
    }

    /**
     * Cerca cocktail per nome (ricerca parziale).
     * GET http://localhost:8081/api/user/cocktails/search?name=moj
     * 
     * @param name Parte del nome da cercare
     */
    @GetMapping("/user/cocktails/search")
    @PreAuthorize("hasRole('USER')")
    public List<Cocktail> searchCocktails(@RequestParam String name) {
        return cocktailService.searchCocktailsByName(name);
    }

    /**
     * Filtra cocktail alcolici o analcolici.
     * GET http://localhost:8081/api/user/cocktails/alcoholic?value=true
     * 
     * @param value true per alcolici, false per analcolici
     */
    @GetMapping("/user/cocktails/alcoholic")
    @PreAuthorize("hasRole('USER')")
    public List<Cocktail> getCocktailsByAlcoholic(@RequestParam Boolean value) {
        return cocktailService.getCocktailsByAlcoholic(value);
    }

    // ========================================
    // ENDPOINT ADMIN (richiede ruolo ADMIN)
    // ========================================

    /**
     * Statistiche per amministratori.
     * GET http://localhost:8081/api/admin/stats
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAdminStats(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("admin", jwt.getClaimAsString("preferred_username"));
        stats.put("totalCocktails", cocktailService.countCocktails());
        stats.put("timestamp", java.time.LocalDateTime.now());
        return stats;
    }

    /**
     * Crea un nuovo cocktail.
     * POST http://localhost:8081/api/admin/cocktails
     * Header: Authorization: Bearer <token>
     * Body: JSON del cocktail
     * 
     * @param cocktail Dati del cocktail da creare
     */
    @PostMapping("/admin/cocktails")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Cocktail> createCocktail(@RequestBody Cocktail cocktail) {
        try {
            Cocktail created = cocktailService.createCocktail(cocktail);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Aggiorna un cocktail esistente.
     * PUT http://localhost:8081/api/admin/cocktails/{id}
     * 
     * @param id ID del cocktail da aggiornare
     * @param cocktailDetails Nuovi dati
     */
    @PutMapping("/admin/cocktails/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Cocktail> updateCocktail(
            @PathVariable Long id,
            @RequestBody Cocktail cocktailDetails) {
        try {
            Cocktail updated = cocktailService.updateCocktail(id, cocktailDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un cocktail.
     * DELETE http://localhost:8081/api/admin/cocktails/{id}
     * 
     * @param id ID del cocktail da eliminare
     */
    @DeleteMapping("/admin/cocktails/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCocktail(@PathVariable Long id) {
        try {
            cocktailService.deleteCocktail(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cocktail eliminato con successo");
            response.put("id", id.toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // GESTIONE ERRORI
    // ========================================

    /**
     * Gestisce le eccezioni generiche.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(error);
    }
}


