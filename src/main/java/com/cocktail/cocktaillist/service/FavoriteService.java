package com.cocktail.cocktaillist.service;

import com.cocktail.cocktaillist.model.Cocktail;
import com.cocktail.cocktaillist.model.Favorite;
import com.cocktail.cocktaillist.repository.CocktailRepository;
import com.cocktail.cocktaillist.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service per la gestione dei preferiti degli utenti
 */
@Service
@Transactional
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private CocktailRepository cocktailRepository;

    /**
     * Aggiunge un cocktail ai preferiti dell'utente
     * 
     * @param userId ID utente dal JWT
     * @param cocktailId ID cocktail
     * @return Il preferito creato
     * @throws RuntimeException se il cocktail non esiste o è già nei preferiti
     */
    public Favorite addFavorite(String userId, Long cocktailId) {
        // Verifica che il cocktail esista
        Cocktail cocktail = cocktailRepository.findById(cocktailId)
            .orElseThrow(() -> new RuntimeException("Cocktail non trovato con ID: " + cocktailId));
        
        // Verifica che non sia già nei preferiti
        if (favoriteRepository.existsByUserIdAndCocktailId(userId, cocktailId)) {
            throw new RuntimeException("Cocktail già presente nei preferiti");
        }
        
        // Crea e salva il preferito
        Favorite favorite = new Favorite(userId, cocktail);
        return favoriteRepository.save(favorite);
    }    /**
     * Rimuove un cocktail dai preferiti dell'utente
     * 
     * @param userId ID utente dal JWT
     * @param cocktailId ID cocktail
     * @throws RuntimeException se il preferito non esiste
     */
    public void removeFavorite(String userId, Long cocktailId) {
        // Verifica che il preferito esista
        if (!favoriteRepository.existsByUserIdAndCocktailId(userId, cocktailId)) {
            throw new RuntimeException("Cocktail non presente nei preferiti");
        }
        
        favoriteRepository.deleteByUserIdAndCocktailId(userId, cocktailId);
    }

    /**
     * Ottiene tutti i cocktail preferiti di un utente
     * 
     * @param userId ID utente dal JWT
     * @return Lista di cocktail preferiti
     */
    public List<Cocktail> getUserFavorites(String userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        
        // Estrae i cocktail dai preferiti
        return favorites.stream()
                .map(Favorite::getCocktail)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se un cocktail è tra i preferiti dell'utente
     * 
     * @param userId ID utente dal JWT
     * @param cocktailId ID cocktail
     * @return true se è preferito, false altrimenti
     */
    public boolean isFavorite(String userId, Long cocktailId) {
        return favoriteRepository.existsByUserIdAndCocktailId(userId, cocktailId);
    }

    /**
     * Conta quanti preferiti ha un utente
     * 
     * @param userId ID utente dal JWT
     * @return Numero di cocktail preferiti
     */
    public long countUserFavorites(String userId) {
        return favoriteRepository.countByUserId(userId);
    }

    /**
     * Ottiene i cocktail più favoritati (per statistiche)
     * 
     * @return Lista di Object[] con [cocktailId, count]
     */
    public List<Object[]> getMostFavoritedCocktails() {
        return favoriteRepository.findMostFavoritedCocktails();
    }

    /**
     * Rimuove tutti i preferiti di un utente
     * Utile per funzione "Svuota preferiti"
     * 
     * @param userId ID utente dal JWT
     */
    public void clearUserFavorites(String userId) {
        favoriteRepository.deleteByUserId(userId);
    }

    /**
     * Toggle preferito: aggiunge se non c'è, rimuove se c'è
     * 
     * @param userId ID utente dal JWT
     * @param cocktailId ID cocktail
     * @return true se aggiunto, false se rimosso
     */
    public boolean toggleFavorite(String userId, Long cocktailId) {
        if (favoriteRepository.existsByUserIdAndCocktailId(userId, cocktailId)) {
            removeFavorite(userId, cocktailId);
            return false; // Rimosso
        } else {
            addFavorite(userId, cocktailId);
            return true; // Aggiunto
        }
    }
}
