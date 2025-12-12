package com.cocktail.cocktaillist.controller;

import com.cocktail.cocktaillist.dto.CocktailRequest;
import com.cocktail.cocktaillist.model.Cocktail;
import com.cocktail.cocktaillist.service.CocktailService;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
     * Lista pubblica dei cocktail paginata.
     * GET http://localhost:8081/api/public/cocktails?page=0&size=10&sortBy=name&sortDir=asc
     * 
     * @param page Numero pagina (default 0)
     * @param size Elementi per pagina (default 10)
     * @param sortBy Campo per ordinamento (default "name")
     * @param sortDir Direzione ordinamento: "asc" o "desc" (default "asc")
     */
    @GetMapping("/public/cocktails")
    public Page<Cocktail> getPublicCocktails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return cocktailService.getAllCocktailsPaginated(page, size, sortBy, sortDir);
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

        // Estrai i ruoli correttamente da realm_access
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        profile.put("roles", realmAccess != null ? realmAccess.get("roles") : null);

        profile.put("tokenExpiration", jwt.getExpiresAt());
        return profile;
    }

    /**
     * Lista completa dei cocktail paginata (tutti i dettagli).
     * GET http://localhost:8081/api/user/cocktails?page=0&size=10&sortBy=name&sortDir=asc
     * Header: Authorization: Bearer <token>
     * 
     * @param page Numero pagina (default 0)
     * @param size Elementi per pagina (default 10)
     * @param sortBy Campo per ordinamento (default "name")
     * @param sortDir Direzione ordinamento: "asc" o "desc" (default "asc")
     */
    @GetMapping("/user/cocktails")
    @PreAuthorize("hasRole('USER')")
    public Page<Cocktail> getAllCocktails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return cocktailService.getAllCocktailsPaginated(page, size, sortBy, sortDir);
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
     * Cerca cocktail per categoria (paginato).
     * GET http://localhost:8081/api/user/cocktails/category/Rum?page=0&size=10
     * 
     * @param category Categoria da cercare
     * @param page Numero pagina (default 0)
     * @param size Elementi per pagina (default 10)
     */
    @GetMapping("/user/cocktails/category/{category}")
    @PreAuthorize("hasRole('USER')")
    public Page<Cocktail> getCocktailsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return cocktailService.getCocktailsByCategoryPaginated(category, page, size);
    }

    /**
     * Cerca cocktail per nome (ricerca parziale, paginato).
     * GET http://localhost:8081/api/user/cocktails/search?name=moj&page=0&size=10
     * 
     * @param name Parte del nome da cercare
     * @param page Numero pagina (default 0)
     * @param size Elementi per pagina (default 10)
     */
    @GetMapping("/user/cocktails/search")
    @PreAuthorize("hasRole('USER')")
    public Page<Cocktail> searchCocktails(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return cocktailService.searchCocktailsByNamePaginated(name, page, size);
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
     * Crea un nuovo cocktail (USER e ADMIN).
     * POST http://localhost:8081/api/cocktails
     * Header: Authorization: Bearer <token>
     * Body: JSON con CocktailRequest
     * 
     * @param request Dati del cocktail da creare con lista ingredienti
     */
    @PostMapping("/cocktails")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "Crea un nuovo cocktail",
        description = "Crea un nuovo cocktail con i dati forniti. Richiede autenticazione con ruolo USER o ADMIN."+
                      "INSERIRE UN SOLO COCKTAIL PER VOLTA"+
                      "Se viene inserito un ID di un Cocktail esistente,"+
                      "verrà creato un nuovo cocktail con un nuovo ID automaticamente."
    )
    public ResponseEntity<Cocktail> createCocktail(@RequestBody CocktailRequest request) {
        try {
            Cocktail created = cocktailService.createCocktail(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Aggiorna un cocktail esistente (USER e ADMIN).
     * PUT http://localhost:8081/api/cocktails/{id}
     * NOTA: NON modifica gli ingredienti, usa gli endpoint dedicati
     *
     * @param id ID del cocktail da aggiornare
     * @param request Nuovi dati con CocktailRequest (ingredienti ignorati)
     */
    @PutMapping("/cocktails/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "Aggiorna le info di un cocktail",
        description = "Aggiorna nome, descrizione, categoria, ecc. SENZA modificare gli ingredienti. " +
                      "Per gestire ingredienti usa gli endpoint dedicati."
    )
    public ResponseEntity<Cocktail> updateCocktail(
            @PathVariable Long id,
            @RequestBody CocktailRequest request) {
        try {
            Cocktail updated = cocktailService.updateCocktail(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Aggiunge uno o più ingredienti a un cocktail (USER e ADMIN).
     * POST http://localhost:8081/api/cocktails/{id}/ingredients
     * Body: { "ingredients": [{"name": "Lime", "quantity": "20ml"}] }
     *
     * @param id ID del cocktail
     * @param request Request con lista ingredienti da aggiungere
     */
    @PostMapping("/cocktails/{id}/ingredients")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "Aggiungi ingredienti a un cocktail",
        description = "Aggiunge uno o più ingredienti al cocktail. " +
                      "Se un ingrediente non esiste, viene creato automaticamente. " +
                      "NON rimuove gli ingredienti esistenti."
    )
    public ResponseEntity<Cocktail> addIngredients(
            @PathVariable Long id,
            @RequestBody CocktailRequest request) {
        try {
            Cocktail updated = cocktailService.addIngredientsToCocktail(id, request.getIngredients());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Rimuove un ingrediente da un cocktail (USER e ADMIN).
     * DELETE http://localhost:8081/api/cocktails/{cocktailId}/ingredients/{ingredientId}
     *
     * @param cocktailId ID del cocktail
     * @param ingredientId ID dell'ingrediente da rimuovere
     */
    @DeleteMapping("/cocktails/{cocktailId}/ingredients/{ingredientId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "Rimuovi un ingrediente da un cocktail",
        description = "Rimuove la relazione tra il cocktail e l'ingrediente specificato. " +
                      "L'ingrediente rimane nel database, viene solo scollegato dal cocktail."
    )
    public ResponseEntity<Cocktail> removeIngredient(
            @PathVariable Long cocktailId,
            @PathVariable Long ingredientId) {
        try {
            Cocktail updated = cocktailService.removeIngredientFromCocktail(cocktailId, ingredientId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Aggiorna la quantità di un ingrediente in un cocktail (USER e ADMIN).
     * PATCH http://localhost:8081/api/cocktails/{cocktailId}/ingredients/{ingredientId}
     * Body: { "quantity": "60ml" }
     *
     * @param cocktailId ID del cocktail
     * @param ingredientId ID dell'ingrediente
     * @param quantityRequest Oggetto con la nuova quantità
     */
    @PatchMapping("/cocktails/{cocktailId}/ingredients/{ingredientId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "Aggiorna la quantità di un ingrediente",
        description = "Modifica solo la quantità di un ingrediente già presente nel cocktail."
    )
    public ResponseEntity<Cocktail> updateIngredientQuantity(
            @PathVariable Long cocktailId,
            @PathVariable Long ingredientId,
            @RequestBody Map<String, String> quantityRequest) {
        try {
            String newQuantity = quantityRequest.get("quantity");
            if (newQuantity == null || newQuantity.isEmpty()) {
                throw new RuntimeException("Il campo 'quantity' è obbligatorio");
            }
            Cocktail updated = cocktailService.updateIngredientQuantity(cocktailId, ingredientId, newQuantity);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Elimina un cocktail (solo ADMIN).
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

    /**
     * Analizza gli ID dei cocktail per identificare gap e fornire statistiche.
     * GET http://localhost:8081/api/admin/cocktails/gaps
     * 
     * Utile per vedere quali ID sono stati eliminati e quali sono disponibili.
     * 
     * @return JSON con ID esistenti, ID mancanti, totale e max ID
     */
    @GetMapping("/admin/cocktails/gaps")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Analizza gli Id dei cocktail per identificare quanti ID dei Cocktail Rimangono",
        description = "analizza gli iD e restituisce il numero di ID disponibili"
                
    )
    public ResponseEntity<Map<String, Object>> getIdGaps() {
        List<Long> allIds = cocktailService.getAllCocktailIds();
        List<Long> missingIds = new ArrayList<>();
        
        if (!allIds.isEmpty()) {
            long maxId = allIds.get(allIds.size() - 1);
            for (long i = 1; i <= maxId; i++) {
                if (!allIds.contains(i)) {
                    missingIds.add(i);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("existingIds", allIds);
        result.put("missingIds", missingIds);
        result.put("totalCocktails", allIds.size());
        result.put("maxId", allIds.isEmpty() ? 0 : allIds.get(allIds.size() - 1));
        result.put("nextAvailableId", allIds.isEmpty() ? 1 : allIds.get(allIds.size() - 1) + 1);
        
        return ResponseEntity.ok(result);
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


