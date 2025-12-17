package com.cocktail.cocktaillist.dto;

import com.cocktail.cocktaillist.model.Cocktail;
import com.cocktail.cocktaillist.model.Color;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cocktail con informazioni sul preferito e colore personalizzato")
public class CocktailWithFavoriteInfo {

    @Schema(description = "Informazioni del cocktail")
    private Cocktail cocktail;

    @Schema(description = "Indica se il cocktail è nei preferiti dell'utente", example = "true")
    private Boolean isFavorite;

    @Schema(description = "Colore personalizzato del preferito (null se non è un preferito)")
    private Color favoriteColor;

    public CocktailWithFavoriteInfo() {
    }

    public CocktailWithFavoriteInfo(Cocktail cocktail, Boolean isFavorite, Color favoriteColor) {
        this.cocktail = cocktail;
        this.isFavorite = isFavorite;
        this.favoriteColor = favoriteColor;
    }

    // Getters e Setters
    public Cocktail getCocktail() {
        return cocktail;
    }

    public void setCocktail(Cocktail cocktail) {
        this.cocktail = cocktail;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Color getFavoriteColor() {
        return favoriteColor;
    }

    public void setFavoriteColor(Color favoriteColor) {
        this.favoriteColor = favoriteColor;
    }
}
