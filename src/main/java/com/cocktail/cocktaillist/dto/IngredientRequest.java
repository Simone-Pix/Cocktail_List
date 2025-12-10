package com.cocktail.cocktaillist.dto;

/**
 * DTO per un singolo ingrediente nella richiesta di creazione cocktail
 */
public class IngredientRequest {
    private String name;        // Es: "Rum Bianco"
    private String quantity;    // Es: "50ml"
    private String category;    // Opzionale: "Spiriti" (per auto-creazione)
    private String unit;        // Opzionale: "ml" (per auto-creazione)

    // Costruttori
    public IngredientRequest() {
    }

    public IngredientRequest(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
