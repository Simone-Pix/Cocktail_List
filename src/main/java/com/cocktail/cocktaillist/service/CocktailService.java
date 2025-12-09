package com.cocktail.cocktaillist.service;

import com.cocktail.cocktaillist.model.Cocktail;
import com.cocktail.cocktaillist.repository.CocktailRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Inietta automaticamente il repository
     * Spring crea l'istanza e la passa qui
     */
    @Autowired
    private CocktailRepository cocktailRepository;

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

    // ========================================
    // OPERAZIONI DI SCRITTURA
    // ========================================

    /**
     * Crea un nuovo cocktail.
     * 
     * @param cocktail Il cocktail da creare (senza ID)
     * @return Il cocktail creato (con ID generato)
     * @throws RuntimeException se esiste già un cocktail con lo stesso nome
     */
    public Cocktail createCocktail(Cocktail cocktail) {
        // Validazione: controlla se esiste già
        if (cocktailRepository.existsByName(cocktail.getName())) {
            throw new RuntimeException("Esiste già un cocktail con nome: " + cocktail.getName());
        }
        
        // Salva nel database
        return cocktailRepository.save(cocktail);
    }

    /**
     * Aggiorna un cocktail esistente.
     * 
     * @param id ID del cocktail da aggiornare
     * @param cocktailDetails Nuovi dettagli del cocktail
     * @return Il cocktail aggiornato
     * @throws RuntimeException se il cocktail non esiste
     */
    public Cocktail updateCocktail(Long id, Cocktail cocktailDetails) {
        // Trova il cocktail esistente
        Cocktail existingCocktail = getCocktailById(id);
        
        // Aggiorna i campi (solo se non null)
        if (cocktailDetails.getName() != null) {
            existingCocktail.setName(cocktailDetails.getName());
        }
        if (cocktailDetails.getDescription() != null) {
            existingCocktail.setDescription(cocktailDetails.getDescription());
        }
        if (cocktailDetails.getIngredients() != null) {
            existingCocktail.setIngredients(cocktailDetails.getIngredients());
        }
        if (cocktailDetails.getCategory() != null) {
            existingCocktail.setCategory(cocktailDetails.getCategory());
        }
        if (cocktailDetails.getGlassType() != null) {
            existingCocktail.setGlassType(cocktailDetails.getGlassType());
        }
        if (cocktailDetails.getPreparationMethod() != null) {
            existingCocktail.setPreparationMethod(cocktailDetails.getPreparationMethod());
        }
        if (cocktailDetails.getImageUrl() != null) {
            existingCocktail.setImageUrl(cocktailDetails.getImageUrl());
        }
        if (cocktailDetails.getAlcoholic() != null) {
            existingCocktail.setAlcoholic(cocktailDetails.getAlcoholic());
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

