package com.cocktail.cocktaillist.service;

import com.cocktail.cocktaillist.dto.CocktailRequest;
import com.cocktail.cocktaillist.dto.IngredientRequest;
import com.cocktail.cocktaillist.model.Cocktail;
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
        // Validazione: controlla se esiste già
        if (cocktailRepository.existsByName(request.getName())) {
            throw new RuntimeException("Esiste già un cocktail con nome: " + request.getName());
        }
        
        // Crea l'entità cocktail
        Cocktail cocktail = new Cocktail();
        cocktail.setName(request.getName());
        cocktail.setDescription(request.getDescription());
        cocktail.setCategory(request.getCategory());
        cocktail.setGlassType(request.getGlassType());
        cocktail.setPreparationMethod(request.getPreparationMethod());
        cocktail.setImageUrl(request.getImageUrl());
        cocktail.setAlcoholic(request.getAlcoholic());
        
        // Gestione ingredienti: auto-crea se non esistono
        if (request.getIngredients() != null) {
            for (IngredientRequest ingReq : request.getIngredients()) {
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
     * Aggiorna un cocktail esistente con gestione ingredienti.
     * 
     * @param id ID del cocktail da aggiornare
     * @param request Nuovi dettagli del cocktail
     * @return Il cocktail aggiornato
     * @throws RuntimeException se il cocktail non esiste
     */
    public Cocktail updateCocktail(Long id, CocktailRequest request) {
        // Trova il cocktail esistente
        Cocktail existingCocktail = getCocktailById(id);
        
        // Aggiorna i campi (solo se non null)
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
        
        // Aggiorna ingredienti: rimuovi vecchi e aggiungi nuovi
        if (request.getIngredients() != null) {
            // Rimuovi tutte le relazioni esistenti
            existingCocktail.clearIngredients();
            
            // Aggiungi i nuovi ingredienti con auto-creazione
            for (IngredientRequest ingReq : request.getIngredients()) {
                Ingredient ingredient = ingredientService.findOrCreateIngredient(
                    ingReq.getName(), 
                    ingReq.getCategory(), 
                    ingReq.getUnit()
                );
                existingCocktail.addIngredient(ingredient, ingReq.getQuantity());
            }
        }
        
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
}

