package com.cocktail.cocktaillist.controller;

import com.cocktail.cocktaillist.model.Cocktail;
import com.cocktail.cocktaillist.model.Favorite;
import com.cocktail.cocktaillist.service.FavoriteService;
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
 * Controller per la gestione dei preferiti dell'utente.
 * 
 * Funzionalità:
 * - Aggiungere cocktail ai preferiti
 * - Rimuovere dai preferiti
 * - Visualizzare lista preferiti
 * - Toggle (aggiungi/rimuovi con un click)
 */
@RestController
@RequestMapping("/api/favorites")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * Ottiene tutti i preferiti dell'utente autenticato.
     * GET http://localhost:8081/api/favorites
     * Header: Authorization: Bearer <token>
     * 
     * @param jwt Token JWT con user ID
     * @return Lista di cocktail preferiti
     */
    @GetMapping
    public ResponseEntity<List<Cocktail>> getUserFavorites(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject(); // Usa subject come userId
        List<Cocktail> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    /**
     * Verifica se un cocktail è tra i preferiti.
     * GET http://localhost:8081/api/favorites/check/{cocktailId}
     * 
     * @param cocktailId ID del cocktail da verificare
     * @param jwt Token JWT
     * @return JSON {isFavorite: true/false}
     */
    @GetMapping("/check/{cocktailId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        boolean isFavorite = favoriteService.isFavorite(userId, cocktailId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorite", isFavorite);
        return ResponseEntity.ok(response);
    }

    /**
     * Aggiunge un cocktail ai preferiti.
     * POST http://localhost:8081/api/favorites/{cocktailId}
     * Header: Authorization: Bearer <token>
     * 
     * @param cocktailId ID del cocktail da aggiungere
     * @param jwt Token JWT
     * @return Il preferito creato
     */
    @PostMapping("/{cocktailId}")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            Favorite favorite = favoriteService.addFavorite(userId, cocktailId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cocktail aggiunto ai preferiti");
            response.put("cocktailId", cocktailId);
            response.put("addedAt", favorite.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Rimuove un cocktail dai preferiti.
     * DELETE http://localhost:8081/api/favorites/{cocktailId}
     * 
     * @param cocktailId ID del cocktail da rimuovere
     * @param jwt Token JWT
     */
    @DeleteMapping("/{cocktailId}")
    public ResponseEntity<Map<String, String>> removeFavorite(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            favoriteService.removeFavorite(userId, cocktailId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cocktail rimosso dai preferiti");
            response.put("cocktailId", cocktailId.toString());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Toggle: aggiunge se non c'è, rimuove se c'è.
     * PUT http://localhost:8081/api/favorites/toggle/{cocktailId}
     * 
     * @param cocktailId ID del cocktail
     * @param jwt Token JWT
     * @return JSON {added: true/false, message: "..."}
     */
    @PutMapping("/toggle/{cocktailId}")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            boolean added = favoriteService.toggleFavorite(userId, cocktailId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("added", added);
            response.put("message", added ? "Aggiunto ai preferiti" : "Rimosso dai preferiti");
            response.put("cocktailId", cocktailId);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Rimuove tutti i preferiti dell'utente.
     * DELETE http://localhost:8081/api/favorites
     * 
     * @param jwt Token JWT
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearFavorites(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        favoriteService.clearUserFavorites(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Tutti i preferiti sono stati rimossi");
        
        return ResponseEntity.ok(response);
    }
}
