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

    @Autowired
    private com.cocktail.cocktaillist.repository.ColorRepository colorRepository;

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
     * Ottiene tutti i cocktail preferiti di un utente (solo cocktail, senza colori)
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
     * Ottiene tutti i preferiti con colori personalizzati
     * 
     * @param userId ID utente dal JWT
     * @return Lista di Favorite (con cocktail + colore)
     */
    public List<Favorite> getUserFavoritesWithColors(String userId) {
        return favoriteRepository.findByUserId(userId);
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

    /**
     * Aggiorna il colore di un cocktail preferito tramite ID colore.
     * 
     * @param userId ID dell'utente dal JWT
     * @param cocktailId ID del cocktail
     * @param colorId ID del colore da assegnare
     * @return Il preferito aggiornato
     * @throws RuntimeException se il cocktail non è nei preferiti o il colore non esiste
     */
    public Favorite updateFavoriteColor(String userId, Long cocktailId, Long colorId) {
        // Verifica che il cocktail sia nei preferiti dell'utente
        Favorite favorite = favoriteRepository.findByUserIdAndCocktailId(userId, cocktailId)
                .orElseThrow(() -> new RuntimeException(
                        "Cocktail non trovato nei preferiti. Aggiungi prima il cocktail ai preferiti."));
        
        // Verifica che il colore esista
        com.cocktail.cocktaillist.model.Color color = colorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Colore non trovato con ID: " + colorId));
        
        // Aggiorna il colore
        favorite.setColor(color);
        return favoriteRepository.save(favorite);
    }

    /**
     * Aggiorna il colore di un cocktail preferito tramite nome colore.
     * 
     * @param userId ID dell'utente dal JWT
     * @param cocktailId ID del cocktail
     * @param colorName Nome del colore da assegnare (es: "Rosso Classico")
     * @return Il preferito aggiornato
     * @throws RuntimeException se il cocktail non è nei preferiti o il colore non esiste
     */
    public Favorite updateFavoriteColorByName(String userId, Long cocktailId, String colorName) {
        // Verifica che il cocktail sia nei preferiti dell'utente
        Favorite favorite = favoriteRepository.findByUserIdAndCocktailId(userId, cocktailId)
                .orElseThrow(() -> new RuntimeException(
                        "Cocktail non trovato nei preferiti. Aggiungi prima il cocktail ai preferiti."));
        
        // Cerca il colore per nome
        com.cocktail.cocktaillist.model.Color color = colorRepository.findByName(colorName)
                .orElseThrow(() -> new RuntimeException("Colore non trovato con nome: " + colorName));
        
        // Aggiorna il colore
        favorite.setColor(color);
        return favoriteRepository.save(favorite);
    }
}
