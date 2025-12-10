package com.cocktail.cocktaillist.repository;

import com.cocktail.cocktaillist.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per la tabella "favorite"
 * Gestisce i cocktail preferiti degli utenti
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * Trova tutti i preferiti di un utente
     * Spring genera: SELECT * FROM favorite WHERE user_id = ?
     * 
     * @param userId ID utente dal JWT (email o sub)
     * @return Lista di Favorite (include i cocktail tramite JOIN)
     */
    List<Favorite> findByUserId(String userId);

    /**
     * Trova un preferito specifico di un utente
     * Spring genera: SELECT * FROM favorite WHERE user_id = ? AND cocktail_id = ?
     * 
     * Utile per verificare se un cocktail è già nei preferiti
     * 
     * @param userId ID utente
     * @param cocktailId ID cocktail
     * @return Optional contenente il Favorite se trovato
     */
    Optional<Favorite> findByUserIdAndCocktailId(String userId, Long cocktailId);

    /**
     * Elimina un preferito specifico
     * Spring genera: DELETE FROM favorite WHERE user_id = ? AND cocktail_id = ?
     * 
     * @param userId ID utente
     * @param cocktailId ID cocktail
     */
    void deleteByUserIdAndCocktailId(String userId, Long cocktailId);

    /**
     * Verifica se un cocktail è tra i preferiti dell'utente
     * Spring genera: SELECT COUNT(*) > 0 FROM favorite WHERE user_id = ? AND cocktail_id = ?
     * 
     * @param userId ID utente
     * @param cocktailId ID cocktail
     * @return true se è preferito, false altrimenti
     */
    boolean existsByUserIdAndCocktailId(String userId, Long cocktailId);

    /**
     * Conta quanti preferiti ha un utente
     * Spring genera: SELECT COUNT(*) FROM favorite WHERE user_id = ?
     * 
     * @param userId ID utente
     * @return Numero di cocktail preferiti
     */
    long countByUserId(String userId);

    /**
     * Trova i cocktail più favoritati (top N)
     * Query personalizzata per statistiche
     * 
     * @return Lista di Object[] con [cocktail_id, count]
     */
    @Query("SELECT f.cocktail.id, COUNT(f) FROM Favorite f GROUP BY f.cocktail.id ORDER BY COUNT(f) DESC")
    List<Object[]> findMostFavoritedCocktails();

    /**
     * Elimina tutti i preferiti di un utente
     * Utile se implementi una funzione "Cancella account"
     * 
     * @param userId ID utente
     */
    void deleteByUserId(String userId);
}
