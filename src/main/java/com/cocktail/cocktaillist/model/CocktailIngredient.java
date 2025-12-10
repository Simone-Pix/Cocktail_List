package com.cocktail.cocktaillist.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

/**
 * Entity JPA per la tabella "cocktail_ingredient"
 * Rappresenta la relazione Many-to-Many tra Cocktail e Ingredient
 * Include la quantità specifica per ogni ingrediente nel cocktail
 */
@Entity
@Table(name = "cocktail_ingredient")
public class CocktailIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relazione Many-to-One verso Cocktail
     * Molti CocktailIngredient appartengono a un Cocktail
     * JsonBackReference previene loop infiniti nella serializzazione JSON
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    @JsonBackReference
    private Cocktail cocktail;

    /**
     * Relazione Many-to-One verso Ingredient
     * Molti CocktailIngredient usano lo stesso Ingredient
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /**
     * Quantità specifica (es: "50ml", "1 pezzo", "10 foglie")
     */
    @Column(name = "quantity", nullable = false, length = 50)
    private String quantity;

    // Costruttori
    public CocktailIngredient() {
    }

    public CocktailIngredient(Cocktail cocktail, Ingredient ingredient, String quantity) {
        this.cocktail = cocktail;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cocktail getCocktail() {
        return cocktail;
    }

    public void setCocktail(Cocktail cocktail) {
        this.cocktail = cocktail;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
