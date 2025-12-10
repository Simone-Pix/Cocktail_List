package com.cocktail.cocktaillist.repository;

import com.cocktail.cocktaillist.model.CocktailIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository per la tabella "cocktail_ingredient"
 * Gestisce la relazione Many-to-Many tra Cocktail e Ingredient
 */
@Repository
public interface CocktailIngredientRepository extends JpaRepository<CocktailIngredient, Long> {

    /**
     * Trova tutti gli ingredienti di un cocktail specifico
     * Spring genera: SELECT * FROM cocktail_ingredient WHERE cocktail_id = ?
     * 
     * @param cocktailId ID del cocktail
     * @return Lista di CocktailIngredient (con ingrediente e quantità)
     */
    List<CocktailIngredient> findByCocktailId(Long cocktailId);

    /**
     * Trova tutti i cocktail che usano un ingrediente specifico
     * Spring genera: SELECT * FROM cocktail_ingredient WHERE ingredient_id = ?
     * 
     * Utile per statistiche: "Quali cocktail usano Rum Bianco?"
     * 
     * @param ingredientId ID dell'ingrediente
     * @return Lista di CocktailIngredient
     */
    List<CocktailIngredient> findByIngredientId(Long ingredientId);

    /**
     * Elimina tutti gli ingredienti di un cocktail
     * Spring genera: DELETE FROM cocktail_ingredient WHERE cocktail_id = ?
     * 
     * Utile quando aggiorni un cocktail e vuoi ricreare gli ingredienti da zero
     * 
     * @param cocktailId ID del cocktail
     */
    void deleteByCocktailId(Long cocktailId);

    /**
     * Conta quanti cocktail usano un ingrediente
     * Spring genera: SELECT COUNT(*) FROM cocktail_ingredient WHERE ingredient_id = ?
     * 
     * @param ingredientId ID dell'ingrediente
     * @return Numero di cocktail che usano questo ingrediente
     */
    long countByIngredientId(Long ingredientId);
}
