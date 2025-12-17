package com.cocktail.cocktaillist.service;

import com.cocktail.cocktaillist.dto.CocktailRequest;
import com.cocktail.cocktaillist.dto.IngredientRequest;
import com.cocktail.cocktaillist.model.Cocktail;
import com.cocktail.cocktaillist.model.CocktailIngredient;
import com.cocktail.cocktaillist.model.Ingredient;
import com.cocktail.cocktaillist.repository.CocktailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer - Contiene la business logic dell'applicazione.
 * 
 * Separa la logica di business dal Controller (che gestisce HTTP)
 * e dal Repository (che gestisce database).
 * 
 * Vantaggi:
 * - Riusabilità: più controller possono usare lo stesso service
 * - Testabilità: puoi testare la logica senza HTTP o DB reale
 * - Transazioni: gestisce le transazioni del database
 */
@Service
@Transactional
public class CocktailService {

    @Autowired
    private CocktailRepository cocktailRepository;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private com.cocktail.cocktaillist.repository.FavoriteRepository favoriteRepository;

    // ========================================
    // OPERAZIONI DI LETTURA
    // ========================================

    /**
     * Ottiene tutti i cocktail dal database.
     * 
     * @return Lista di tutti i cocktail
     */
    public List<Cocktail> getAllCocktails() {
        return cocktailRepository.findAll();
    }

    /**
     * Ottiene un cocktail specifico per ID.
     * 
     * @param id ID del cocktail
     * @return Il cocktail trovato
     * @throws RuntimeException se il cocktail non esiste
     */
    public Cocktail getCocktailById(Long id) {
        return cocktailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cocktail non trovato con ID: " + id));
    }

    /**
     * Cerca un cocktail per nome.
     * 
     * @param name Nome del cocktail
     * @return Optional contenente il cocktail se trovato
     */
    public Optional<Cocktail> getCocktailByName(String name) {
        return cocktailRepository.findByName(name);
    }

    /**
     * Ottiene tutti i cocktail di una categoria.
     * 
     * @param category Categoria (es: "Rum", "Gin")
     * @return Lista di cocktail della categoria
     */
    public List<Cocktail> getCocktailsByCategory(String category) {
        return cocktailRepository.findByCategory(category);
    }

    /**
     * Cerca cocktail il cui nome contiene una stringa.
     * 
     * @param name Parte del nome da cercare
     * @return Lista di cocktail che matchano
     */
    public List<Cocktail> searchCocktailsByName(String name) {
        return cocktailRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Ottiene cocktail alcolici o analcolici.
     * 
     * @param alcoholic true per alcolici, false per analcolici
     * @return Lista di cocktail
     */
    public List<Cocktail> getCocktailsByAlcoholic(Boolean alcoholic) {
        return cocktailRepository.findByAlcoholic(alcoholic);
    }

    /**
     * Conta il numero totale di cocktail.
     * 
     * @return Numero di cocktail nel database
     */
    public long countCocktails() {
        return cocktailRepository.count();
    }

    /**
     * Ottiene tutti gli ID dei cocktail ordinati in modo crescente.
     * Utile per identificare gap negli ID dopo eliminazioni.
     * 
     * @return Lista di ID ordinati
     */
    public List<Long> getAllCocktailIds() {
        return cocktailRepository.findAllIds();
    }

    // ========================================
    // OPERAZIONI DI LETTURA PAGINATE
    // ========================================

    /**
     * Ottiene tutti i cocktail con paginazione e ordinamento.
     * 
     * @param page Numero pagina (0-based)
     * @param size Elementi per pagina
     * @param sortBy Campo per ordinamento
     * @param sortDir Direzione ordinamento ("asc" o "desc")
     * @return Pagina di cocktail
     */
    public Page<Cocktail> getAllCocktailsPaginated(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return cocktailRepository.findAll(pageable);
    }

    /**
     * Cerca cocktail per categoria con paginazione.
     * 
     * @param category Categoria da cercare
     * @param page Numero pagina
     * @param size Elementi per pagina
     * @return Pagina di cocktail della categoria
     */
    public Page<Cocktail> getCocktailsByCategoryPaginated(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return cocktailRepository.findByCategory(category, pageable);
    }

    /**
     * Cerca cocktail per nome con paginazione.
     * 
     * @param name Parte del nome da cercare
     * @param page Numero pagina
     * @param size Elementi per pagina
     * @return Pagina di cocktail che matchano
     */
    public Page<Cocktail> searchCocktailsByNamePaginated(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return cocktailRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    // ========================================
    // OPERAZIONI DI SCRITTURA
    // ========================================

    /**
     * Crea un nuovo cocktail con ingredienti auto-creati se necessario.
     * 
     * @param request Il DTO con i dati del cocktail e gli ingredienti
     * @return Il cocktail creato (con ID generato e relazioni)
     * @throws RuntimeException se esiste già un cocktail con lo stesso nome
     */
    public Cocktail createCocktail(CocktailRequest request) {
        // Validazione: nome obbligatorio
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Il nome del cocktail è obbligatorio");
        }
        
        // Validazione: controlla se esiste già
        if (cocktailRepository.existsByName(request.getName())) {
            throw new RuntimeException("Esiste già un cocktail con nome: " + request.getName());
        }

        // Validazione: un cocktail deve avere almeno un ingrediente
        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            throw new RuntimeException("Un cocktail deve avere almeno un ingrediente");
        }

        // Crea l'entità cocktail
        Cocktail cocktail = new Cocktail();
        cocktail.setName(request.getName());
        
        // Imposta descrizione con default se nulla
        cocktail.setDescription(
            request.getDescription() != null && !request.getDescription().trim().isEmpty() 
                ? request.getDescription() 
                : "Un buonissimo cocktail"
        );
        
        // Imposta category con default se nulla
        cocktail.setCategory(
            request.getCategory() != null && !request.getCategory().trim().isEmpty()
                ? request.getCategory()
                : "Altro"
        );
        
        // Imposta glassType con default se nullo
        cocktail.setGlassType(
            request.getGlassType() != null && !request.getGlassType().trim().isEmpty()
                ? request.getGlassType()
                : "Bicchiere standard"
        );
        
        // Imposta metodo di preparazione con default se nullo
        cocktail.setPreparationMethod(
            request.getPreparationMethod() != null && !request.getPreparationMethod().trim().isEmpty()
                ? request.getPreparationMethod()
                : "Mescolare"
        );
        
        // imageUrl può essere null (opzionale)
        cocktail.setImageUrl(request.getImageUrl());
        
        // Imposta alcoholic con default se nullo
        cocktail.setAlcoholic(
            request.getAlcoholic() != null ? request.getAlcoholic() : true
        );

        // Gestione ingredienti: auto-crea se non esistono
        if (request.getIngredients() != null) {
            for (IngredientRequest ingReq : request.getIngredients()) {
                // Validazione: nome ingrediente obbligatorio
                if (ingReq.getName() == null || ingReq.getName().trim().isEmpty()) {
                    throw new RuntimeException("Il nome dell'ingrediente è obbligatorio");
                }
                
                // Validazione: quantità obbligatoria
                if (ingReq.getQuantity() == null || ingReq.getQuantity().trim().isEmpty()) {
                    throw new RuntimeException("La quantità dell'ingrediente è obbligatoria");
                }
                
                // findOrCreateIngredient cerca l'ingrediente, se non esiste lo crea
                Ingredient ingredient = ingredientService.findOrCreateIngredient(
                    ingReq.getName(), 
                    ingReq.getCategory(), 
                    ingReq.getUnit()
                );
                
                // Aggiungi la relazione cocktail-ingrediente con quantità
                cocktail.addIngredient(ingredient, ingReq.getQuantity());
            }
        }
        
        // Salva nel database (cascade salva anche le relazioni CocktailIngredient)
        return cocktailRepository.save(cocktail);
    }

    /**
     * Aggiorna un cocktail esistente (SENZA modificare ingredienti).
     * Per gestire gli ingredienti usa i metodi dedicati:
     * - addIngredientsToCoktail()
     * - removeIngredientFromCocktail()
     * - updateIngredientQuantity()
     *
     * @param id ID del cocktail da aggiornare
     * @param request Nuovi dettagli del cocktail (ingredienti vengono ignorati)
     * @return Il cocktail aggiornato
     * @throws RuntimeException se il cocktail non esiste
     */
    public Cocktail updateCocktail(Long id, CocktailRequest request) {
        // Trova il cocktail esistente
        Cocktail existingCocktail = getCocktailById(id);

        // Aggiorna solo i campi base (NON gli ingredienti)
        if (request.getName() != null) {
            existingCocktail.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingCocktail.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            existingCocktail.setCategory(request.getCategory());
        }
        if (request.getGlassType() != null) {
            existingCocktail.setGlassType(request.getGlassType());
        }
        if (request.getPreparationMethod() != null) {
            existingCocktail.setPreparationMethod(request.getPreparationMethod());
        }
        if (request.getImageUrl() != null) {
            existingCocktail.setImageUrl(request.getImageUrl());
        }
        if (request.getAlcoholic() != null) {
            existingCocktail.setAlcoholic(request.getAlcoholic());
        }

        // NOTA: Gli ingredienti NON vengono più modificati qui
        // Usa gli endpoint dedicati per gestire ingredienti

        // Salva le modifiche (l'@PreUpdate aggiornerà updated_at automaticamente)
        return cocktailRepository.save(existingCocktail);
    }

    /**
     * Elimina un cocktail per ID.
     * 
     * @param id ID del cocktail da eliminare
     * @throws RuntimeException se il cocktail non esiste
     */
    public void deleteCocktail(Long id) {
        // Verifica che esista prima di eliminare
        if (!cocktailRepository.existsById(id)) {
            throw new RuntimeException("Impossibile eliminare: cocktail non trovato con ID: " + id);
        }
        
        cocktailRepository.deleteById(id);
    }

    // ========================================
    // OPERAZIONI SUGLI INGREDIENTI DEI COCKTAIL
    // ========================================

    /**
     * Aggiunge uno o più ingredienti a un cocktail esistente.
     * Se un ingrediente non esiste nel database, viene creato automaticamente.
     *
     * @param cocktailId ID del cocktail
     * @param ingredientRequests Lista di ingredienti da aggiungere
     * @return Il cocktail aggiornato
     * @throws RuntimeException se il cocktail non esiste
     */
    public Cocktail addIngredientsToCocktail(Long cocktailId, List<IngredientRequest> ingredientRequests) {
        Cocktail cocktail = getCocktailById(cocktailId);

        if (ingredientRequests == null || ingredientRequests.isEmpty()) {
            throw new RuntimeException("La lista ingredienti non può essere vuota");
        }

        for (IngredientRequest ingReq : ingredientRequests) {
            // Trova o crea l'ingrediente
            Ingredient ingredient = ingredientService.findOrCreateIngredient(
                ingReq.getName(),
                ingReq.getCategory(),
                ingReq.getUnit()
            );

            // Aggiungi al cocktail
            cocktail.addIngredient(ingredient, ingReq.getQuantity());
        }

        return cocktailRepository.save(cocktail);
    }

    /**
     * Rimuove un ingrediente specifico da un cocktail.
     *
     * @param cocktailId ID del cocktail
     * @param ingredientId ID dell'ingrediente da rimuovere
     * @return Il cocktail aggiornato
     * @throws RuntimeException se il cocktail o l'ingrediente non esistono
     */
    public Cocktail removeIngredientFromCocktail(Long cocktailId, Long ingredientId) {
        Cocktail cocktail = getCocktailById(cocktailId);

        // Trova la relazione CocktailIngredient da rimuovere
        CocktailIngredient toRemove = cocktail.getCocktailIngredients().stream()
            .filter(ci -> ci.getIngredient().getId().equals(ingredientId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Ingrediente con ID " + ingredientId + " non trovato nel cocktail"
            ));

        // Rimuovi la relazione
        cocktail.removeIngredient(toRemove);

        return cocktailRepository.save(cocktail);
    }

    /**
     * Rimuove un ingrediente specifico da un cocktail tramite nome (case-sensitive).
     *
     * @param cocktailId ID del cocktail
     * @param ingredientName Nome dell'ingrediente da rimuovere
     * @return Il cocktail aggiornato
     * @throws RuntimeException se il cocktail o l'ingrediente non esistono
     */
    public Cocktail removeIngredientFromCocktailByName(Long cocktailId, String ingredientName) {
        Cocktail cocktail = getCocktailById(cocktailId);

        // Trova la relazione CocktailIngredient da rimuovere cercando per nome
        CocktailIngredient toRemove = cocktail.getCocktailIngredients().stream()
            .filter(ci -> ci.getIngredient().getName().equals(ingredientName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Ingrediente '" + ingredientName + "' non trovato nel cocktail"
            ));

        // Rimuovi la relazione
        cocktail.removeIngredient(toRemove);

        return cocktailRepository.save(cocktail);
    }

    /**
     * Aggiorna la quantità di un ingrediente in un cocktail.
     *
     * @param cocktailId ID del cocktail
     * @param ingredientId ID dell'ingrediente
     * @param newQuantity Nuova quantità
     * @return Il cocktail aggiornato
     * @throws RuntimeException se il cocktail o l'ingrediente non esistono
     */
    public Cocktail updateIngredientQuantity(Long cocktailId, Long ingredientId, String newQuantity) {
        Cocktail cocktail = getCocktailById(cocktailId);

        // Trova la relazione CocktailIngredient da aggiornare
        CocktailIngredient toUpdate = cocktail.getCocktailIngredients().stream()
            .filter(ci -> ci.getIngredient().getId().equals(ingredientId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Ingrediente con ID " + ingredientId + " non trovato nel cocktail"
            ));

        // Aggiorna la quantità
        toUpdate.setQuantity(newQuantity);

        return cocktailRepository.save(cocktail);
    }

    /**
     * Elimina tutti i cocktail (usa con cautela!).
     * Utile per test o reset del database.
     */
    public void deleteAllCocktails() {
        cocktailRepository.deleteAll();
    }

    // ========================================
    // METODI UTILITY
    // ========================================

    /**
     * Verifica se esiste un cocktail con un determinato nome.
     *
     * @param name Nome da verificare
     * @return true se esiste, false altrimenti
     */
    public boolean existsByName(String name) {
        return cocktailRepository.existsByName(name);
    }

    // ========================================
    // METODI PER STATISTICHE ADMIN
    // ========================================

    /**
     * Conta i cocktail alcolici.
     *
     * @return Numero di cocktail alcolici
     */
    public long countAlcoholic() {
        return cocktailRepository.findByAlcoholic(true).size();
    }

    /**
     * Conta i cocktail analcolici.
     *
     * @return Numero di cocktail analcolici
     */
    public long countNonAlcoholic() {
        return cocktailRepository.findByAlcoholic(false).size();
    }

    /**
     * Ottiene le top 5 categorie con più cocktail.
     *
     * @return Mappa con categoria e conteggio
     */
    public java.util.Map<String, Long> getTopCategories() {
        return getAllCocktails().stream()
            .filter(c -> c.getCategory() != null && !c.getCategory().trim().isEmpty())
            .collect(java.util.stream.Collectors.groupingBy(
                Cocktail::getCategory,
                java.util.stream.Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey,
                java.util.Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
    }

    /**
     * Ottiene l'ultimo cocktail creato.
     *
     * @return Optional con l'ultimo cocktail creato
     */
    public Optional<Cocktail> getLastCreatedCocktail() {
        return getAllCocktails().stream()
            .max(java.util.Comparator.comparing(Cocktail::getCreatedAt));
    }

    /**
     * Ottiene l'ultimo cocktail modificato.
     *
     * @return Optional con l'ultimo cocktail modificato
     */
    public Optional<Cocktail> getLastUpdatedCocktail() {
        return getAllCocktails().stream()
            .max(java.util.Comparator.comparing(Cocktail::getUpdatedAt));
    }

    /**
     * Ottiene tutti i cocktail con informazioni sui preferiti e colori personalizzati dell'utente.
     * Per ogni cocktail restituisce:
     * - I dati completi del cocktail
     * - isFavorite: true se è nei preferiti dell'utente, false altrimenti
     * - favoriteColor: l'oggetto Color personalizzato se è un preferito, null altrimenti
     * 
     * @param userId ID dell'utente dal JWT
     * @param page Numero pagina
     * @param size Dimensione pagina
     * @return Page di CocktailWithFavoriteInfo
     */
    public Page<com.cocktail.cocktaillist.dto.CocktailWithFavoriteInfo> getCocktailsWithFavoriteInfo(
            String userId, int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Cocktail> cocktailsPage = cocktailRepository.findAll(pageable);
        
        // Ottieni tutti i preferiti dell'utente con i colori caricati esplicitamente
        List<com.cocktail.cocktaillist.model.Favorite> userFavorites = 
                favoriteRepository.findByUserIdWithColors(userId);
        
        // Crea una mappa cocktailId -> Favorite per accesso veloce
        java.util.Map<Long, com.cocktail.cocktaillist.model.Favorite> favoritesMap = 
                userFavorites.stream()
                    .collect(java.util.stream.Collectors.toMap(
                        fav -> fav.getCocktail().getId(),
                        fav -> fav
                    ));
        
        // Mappa ogni cocktail al DTO con info sui preferiti
        return cocktailsPage.map(cocktail -> {
            com.cocktail.cocktaillist.model.Favorite favorite = favoritesMap.get(cocktail.getId());
            boolean isFavorite = favorite != null;
            com.cocktail.cocktaillist.model.Color favoriteColor = isFavorite ? favorite.getColor() : null;
            
            return new com.cocktail.cocktaillist.dto.CocktailWithFavoriteInfo(
                    cocktail, isFavorite, favoriteColor);
        });
    }
}

