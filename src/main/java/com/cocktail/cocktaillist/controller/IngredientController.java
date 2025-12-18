package com.cocktail.cocktaillist.controller;

import com.cocktail.cocktaillist.model.Ingredient;
import com.cocktail.cocktaillist.service.IngredientService;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller per la gestione degli ingredienti.
 * 
 * Funzionalità:
 * - Visualizzare tutti gli ingredienti
 * - Cercare ingredienti per nome (autocomplete)
 * - Creare nuovi ingredienti
 * - Modificare ingredienti esistenti
 * - Eliminare ingredienti (se non usati in cocktail)
 */
@RestController
@RequestMapping("/api/ingredients")
@CrossOrigin(origins = "http://localhost:3000")
public class IngredientController {

    @Autowired
    private IngredientService ingredientService;

    /**
     * Lista paginata di tutti gli ingredienti.
     * GET http://localhost:8081/api/ingredients?page=0&size=10&sortBy=name&sortDir=asc
     * 
     * @param page Numero pagina (default 0)
     * @param size Elementi per pagina (default 10)
     * @param sortBy Campo per ordinamento (default "name")
     * @param sortDir Direzione ordinamento: "asc" o "desc" (default "asc")
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllIngredients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<Ingredient> ingredientsPage = ingredientService.getAllIngredientsPaginated(page, size, sortBy, sortDir);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ingredients", ingredientsPage.getContent());
        response.put("currentPage", ingredientsPage.getNumber());
        response.put("pageSize", ingredientsPage.getSize());
        response.put("totalIngredients", ingredientsPage.getTotalElements());
        response.put("totalPages", ingredientsPage.getTotalPages());
        response.put("isFirst", ingredientsPage.isFirst());
        response.put("isLast", ingredientsPage.isLast());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cerca ingredienti per nome (autocomplete, paginato).
     * GET http://localhost:8081/api/ingredients/search?name=rum&page=0&size=10
     * 
     * @param name Parte del nome da cercare
     * @param page Numero pagina (default 0)
     * @param size Elementi per pagina (default 10)
     * @return Pagina di ingredienti che matchano
     */
    @GetMapping("/search")
    @Operation(
        summary = "Analizza gli Id dei cocktail per identificare quanti ID dei Cocktail Rimangono"
    )
    public ResponseEntity<Page<Ingredient>> searchIngredients(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Ingredient> results = ingredientService.searchByNamePaginated(name, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * Dettaglio di un singolo ingrediente.
     * GET http://localhost:8081/api/ingredients/{id}
     * 
     * @param id ID dell'ingrediente
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        try {
            Ingredient ingredient = ingredientService.getIngredientById(id);
            return ResponseEntity.ok(ingredient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea un nuovo ingrediente (USER e ADMIN).
     * POST http://localhost:8081/api/ingredients
     * Header: Authorization: Bearer <token>
     * Body: JSON dell'ingrediente
     * 
     * @param ingredient Dati del nuovo ingrediente
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Crea un nuovo ingrediente",
        description = "Crea un nuovo ingrediente. NON includere il campo 'id' nella richiesta (viene generato automaticamente). " +
                      "Campi obbligatori: name. " +
                      "Campi con default: category='Spezia particolare', unit='pezzi', description='Ingrediente speciale'",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Esempio request body",
            content = @io.swagger.v3.oas.annotations.media.Content(
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Minimo",
                        summary = "Solo nome (usa default per altri campi)",
                        value = "{\"name\": \"Test Ingrediente\"}"
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Completo",
                        summary = "Tutti i campi specificati",
                        value = "{\"name\": \"Rum Havana Club\", \"category\": \"Spirit\", \"unit\": \"ml\", \"description\": \"Rum cubano invecchiato\"}"
                    )
                }
            )
        )
    )
    public ResponseEntity<?> createIngredient(@RequestBody Ingredient ingredient) {
        try {
            Ingredient created = ingredientService.createIngredient(ingredient);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Aggiorna un ingrediente esistente (USER e ADMIN).
     * PUT http://localhost:8081/api/ingredients/{id}
     * 
     * @param id ID dell'ingrediente da aggiornare
     * @param ingredientDetails Nuovi dati
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateIngredient(
            @PathVariable Long id,
            @RequestBody Ingredient ingredientDetails) {
        try {
            Ingredient updated = ingredientService.updateIngredient(id, ingredientDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un ingrediente (solo ADMIN).
     * DELETE http://localhost:8081/api/ingredients/{id}
     * 
     * Nota: fallisce se l'ingrediente è usato in cocktail esistenti
     * 
     * @param id ID dell'ingrediente da eliminare
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteIngredient(@PathVariable Long id) {
        try {
            ingredientService.deleteIngredient(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ingrediente eliminato con successo");
            response.put("id", id.toString());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Ottieni tutti gli ingredienti raggruppati per categoria.
     * GET http://localhost:8081/api/ingredients/grouped-by-category
     * 
     * Risposta esempio:
     * {
     *   "Spiriti": [
     *     {"id": 1, "name": "Vodka", "category": "Spiriti"},
     *     {"id": 2, "name": "Rum", "category": "Spiriti"}
     *   ],
     *   "Frutta": [
     *     {"id": 10, "name": "Limone", "category": "Frutta"}
     *   ]
     * }
     */
    @GetMapping("/grouped-by-category")
     @Operation(
        summary = "Visualizzazione ingredienti raggruppati per categoria" 
    )
    public ResponseEntity<Map<String, List<Ingredient>>> getIngredientsGroupedByCategory() {
        Map<String, List<Ingredient>> grouped = ingredientService.getIngredientsGroupedByCategory();
        return ResponseEntity.ok(grouped);
    }
}
