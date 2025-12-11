package com.cocktail.cocktaillist.repository;

import com.cocktail.cocktaillist.model.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per la tabella "ingredient"
 * 
 * Spring Data JPA genera automaticamente l'implementazione dei metodi.
 * Non serve scrivere codice SQL!
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    /**
     * Trova un ingrediente per nome (case-insensitive)
     * Spring genera: SELECT * FROM ingredient WHERE LOWER(name) = LOWER(?)
     * 
     * Utile per evitare duplicati quando l'utente crea un ingrediente
     * Es: "Rum Bianco" == "rum bianco" == "RUM BIANCO"
     * 
     * @param name Nome dell'ingrediente
     * @return Optional contenente l'ingrediente se trovato
     */
    Optional<Ingredient> findByNameIgnoreCase(String name);

    /**
     * Trova tutti gli ingredienti di una categoria
     * Spring genera: SELECT * FROM ingredient WHERE category = ?
     * 
     * @param category Categoria (es: "Spiriti", "Frutta", "Erbe")
     * @return Lista di ingredienti della categoria
     */
    List<Ingredient> findByCategory(String category);

    /**
     * Cerca ingredienti il cui nome contiene una stringa (case-insensitive)
     * Spring genera: SELECT * FROM ingredient WHERE LOWER(name) LIKE LOWER(?)
     * 
     * Utile per autocomplete e ricerca
     * Es: searchByName("rum") → trova "Rum Bianco", "Rum Scuro", etc.
     * 
     * @param name Parte del nome da cercare
     * @return Lista di ingredienti che matchano
     */
    List<Ingredient> findByNameContainingIgnoreCase(String name);


    @Query("SELECT i.id FROM Ingredient i ORDER BY i.id")
    List<Long> findAllIds();
    /**
     * Versione paginata: cerca ingredienti il cui nome contiene una stringa
     * 
     * @param name Parte del nome da cercare
     * @param pageable Informazioni di paginazione (page, size, sort)
     * @return Page di ingredienti che matchano con metadati di paginazione
     */
    Page<Ingredient> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Verifica se esiste un ingrediente con quel nome (case-insensitive)
     * Spring genera: SELECT COUNT(*) > 0 FROM ingredient WHERE LOWER(name) = LOWER(?)
     * 
     * @param name Nome da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByNameIgnoreCase(String name);
}
