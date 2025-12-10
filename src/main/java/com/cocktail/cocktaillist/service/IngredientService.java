package com.cocktail.cocktaillist.service;

import com.cocktail.cocktaillist.model.Ingredient;
import com.cocktail.cocktaillist.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Verifica prima che non esista già uno con lo stesso nome
     * 
     * @param ingredient Ingrediente da creare
     * @return Ingrediente creato
     * @throws RuntimeException se esiste già
     */
    public Ingredient createIngredient(Ingredient ingredient) {
        // Verifica duplicati
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
}
