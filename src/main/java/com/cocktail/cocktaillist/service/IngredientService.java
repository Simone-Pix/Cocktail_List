package com.cocktail.cocktaillist.service;

import com.cocktail.cocktaillist.model.Ingredient;
import com.cocktail.cocktaillist.repository.IngredientRepository;
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
 * Service per la gestione degli ingredienti
 * Contiene la logica business per ingredienti
 */
@Service
@Transactional
public class IngredientService {

    @Autowired
    private IngredientRepository ingredientRepository;

    /**
     * METODO CHIAVE: Trova un ingrediente per nome, se non esiste lo crea automaticamente
     * 
     * Questo è il cuore della funzionalità "auto-creazione ingredienti"
     * Quando un utente crea un cocktail con un ingrediente nuovo, 
     * non deve prima creare l'ingrediente manualmente
     * 
     * @param name Nome ingrediente
     * @param category Categoria (opzionale, default "Altro")
     * @param unit Unità di misura (opzionale, default "pezzi")
     * @return Ingrediente esistente o appena creato
     */
    public Ingredient findOrCreateIngredient(String name, String category, String unit) {
        // Cerca ingrediente esistente (case-insensitive)
        Optional<Ingredient> existing = ingredientRepository.findByNameIgnoreCase(name);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Non esiste → crealo
        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(name);
        newIngredient.setCategory(category != null ? category : "Altro");
        newIngredient.setUnit(unit != null ? unit : "pezzi");
        
        return ingredientRepository.save(newIngredient);
    }

    /**
     * Ottiene tutti gli ingredienti disponibili
     * 
     * @return Lista di tutti gli ingredienti
     */
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    /**
     * Ottiene un ingrediente per ID
     * 
     * @param id ID ingrediente
     * @return Ingrediente trovato
     * @throws RuntimeException se non trovato
     */
    public Ingredient getIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente non trovato con ID: " + id));
    }

    /**
     * Cerca ingredienti per nome (ricerca parziale)
     * Utile per autocomplete
     * 
     * @param name Parte del nome da cercare
     * @return Lista di ingredienti che matchano
     */
    public List<Ingredient> searchByName(String name) {
        return ingredientRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Ottiene ingredienti di una categoria
     * 
     * @param category Categoria (es: "Spiriti", "Frutta")
     * @return Lista di ingredienti della categoria
     */
    public List<Ingredient> getByCategory(String category) {
        return ingredientRepository.findByCategory(category);
    }

    /**
     * Crea un nuovo ingrediente
     * Verifica prima che non esista già uno con lo stesso nome o ID
     * 
     * @param ingredient Ingrediente da creare
     * @return Ingrediente creato
     * @throws RuntimeException se esiste già
     */
    public Ingredient createIngredient(Ingredient ingredient) {
        // Se viene passato un ID nel JSON, verifica che non esista già
        if (ingredient.getId() != null && ingredientRepository.existsById(ingredient.getId())) {
            throw new RuntimeException("Impossibile creare: esiste già un ingrediente con ID: " + ingredient.getId());
        }
        
        // Forza l'ID a null per garantire che venga generato automaticamente
        ingredient.setId(null);
        
        // Verifica duplicati per nome
        if (ingredientRepository.existsByNameIgnoreCase(ingredient.getName())) {
            throw new RuntimeException("Esiste già un ingrediente con nome: " + ingredient.getName());
        }
        
        return ingredientRepository.save(ingredient);
    }

    /**
     * Aggiorna un ingrediente esistente
     * 
     * @param id ID ingrediente da aggiornare
     * @param ingredientDetails Nuovi dati
     * @return Ingrediente aggiornato
     * @throws RuntimeException se non trovato
     */
    public Ingredient updateIngredient(Long id, Ingredient ingredientDetails) {
        Ingredient ingredient = getIngredientById(id);
        
        ingredient.setName(ingredientDetails.getName());
        ingredient.setCategory(ingredientDetails.getCategory());
        ingredient.setUnit(ingredientDetails.getUnit());
        ingredient.setDescription(ingredientDetails.getDescription());
        
        return ingredientRepository.save(ingredient);
    }

    //get all ingridient ids
    public List<Long> getAllIngredientIds() {
        return ingredientRepository.findAllIds();
    }





    /**
     * Elimina un ingrediente
     * 
     * @param id ID ingrediente da eliminare
     * @throws RuntimeException se non trovato
     */
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingrediente non trovato con ID: " + id);
        }
        ingredientRepository.deleteById(id);
    }

    /**
     * Conta il numero totale di ingredienti
     * 
     * @return Numero di ingredienti
     */
    public long countIngredients() {
        return ingredientRepository.count();
    }

    // ========================================
    // OPERAZIONI DI LETTURA PAGINATE
    // ========================================

    /**
     * Ottiene tutti gli ingredienti con paginazione e ordinamento.
     * 
     * @param page Numero pagina (0-based)
     * @param size Elementi per pagina
     * @param sortBy Campo per ordinamento
     * @param sortDir Direzione ordinamento ("asc" o "desc")
     * @return Pagina di ingredienti
     */
    public Page<Ingredient> getAllIngredientsPaginated(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return ingredientRepository.findAll(pageable);
    }

    /**
     * Cerca ingredienti per nome con paginazione.
     * 
     * @param name Parte del nome da cercare
     * @param page Numero pagina
     * @param size Elementi per pagina
     * @return Pagina di ingredienti che matchano
     */
    public Page<Ingredient> searchByNamePaginated(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ingredientRepository.findByNameContainingIgnoreCase(name, pageable);
    }
}
