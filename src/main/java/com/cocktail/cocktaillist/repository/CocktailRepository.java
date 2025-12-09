package com.cocktail.cocktaillist.repository;

import com.cocktail.cocktaillist.model.Cocktail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per accedere ai dati della tabella "cocktail".
 * 
 * Estendendo JpaRepository, Spring Data JPA genera AUTOMATICAMENTE
 * l'implementazione di tutti i metodi CRUD base:
 * - findAll() → SELECT * FROM cocktail
 * - findById(Long id) → SELECT * FROM cocktail WHERE id = ?
 * - save(Cocktail) → INSERT o UPDATE
 * - deleteById(Long id) → DELETE FROM cocktail WHERE id = ?
 * - count() → SELECT COUNT(*) FROM cocktail
 * - existsById(Long id) → SELECT COUNT(1) FROM cocktail WHERE id = ?
 * 
 * NON devi scrivere codice di implementazione!
 * Spring lo genera automaticamente a runtime.
 */
@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {

    // ========================================
    // QUERY METHODS
    // ========================================
    
    /**
     * Trova tutti i cocktail di una specifica categoria.
     * Spring genera automaticamente la query:
     * SELECT * FROM cocktail WHERE category = ?
     * 
     * @param category La categoria da cercare (es: "Rum", "Gin")
     * @return Lista di cocktail della categoria
     */
    List<Cocktail> findByCategory(String category);

    /**
     * Trova un cocktail per nome (case-sensitive).
     * Spring genera: SELECT * FROM cocktail WHERE name = ?
     * 
     * @param name Nome del cocktail
     * @return Optional contenente il cocktail se trovato
     */
    Optional<Cocktail> findByName(String name);

    /**
     * Trova cocktail il cui nome contiene una stringa (case-insensitive).
     * Spring genera: SELECT * FROM cocktail WHERE LOWER(name) LIKE LOWER(?)
     * 
     * @param name Parte del nome da cercare
     * @return Lista di cocktail che matchano
     */
    List<Cocktail> findByNameContainingIgnoreCase(String name);

    /**
     * Trova tutti i cocktail alcolici o analcolici.
     * Spring genera: SELECT * FROM cocktail WHERE alcoholic = ?
     * 
     * @param alcoholic true per alcolici, false per analcolici
     * @return Lista di cocktail
     */
    List<Cocktail> findByAlcoholic(Boolean alcoholic);

    /**
     * Trova cocktail per categoria E se sono alcolici.
     * Spring genera: SELECT * FROM cocktail WHERE category = ? AND alcoholic = ?
     * 
     * @param category Categoria del cocktail
     * @param alcoholic true/false
     * @return Lista di cocktail
     */
    List<Cocktail> findByCategoryAndAlcoholic(String category, Boolean alcoholic);

    /**
     * Controlla se esiste un cocktail con un determinato nome.
     * Spring genera: SELECT COUNT(1) FROM cocktail WHERE name = ?
     * 
     * @param name Nome da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByName(String name);

    // ========================================
    // ESEMPI DI QUERY PERSONALIZZATE (opzionali)
    // ========================================
    
    /*
     * Se hai bisogno di query più complesse, puoi usare @Query:
     * 
     * @Query("SELECT c FROM Cocktail c WHERE c.category = :category ORDER BY c.name")
     * List<Cocktail> findByCategoryOrderByName(@Param("category") String category);
     * 
     * Oppure query SQL native:
     * 
     * @Query(value = "SELECT * FROM cocktail WHERE ingredients LIKE %:ingredient%", nativeQuery = true)
     * List<Cocktail> findByIngredientContaining(@Param("ingredient") String ingredient);
     */
}
