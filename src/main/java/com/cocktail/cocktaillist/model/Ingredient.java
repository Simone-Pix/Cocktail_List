package com.cocktail.cocktaillist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity JPA per la tabella "ingredient"
 * Rappresenta un ingrediente disponibile nel database
 */
@Entity
@Table(name = "ingredient")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome dell'ingrediente (es: "Rum Bianco", "Lime", "Menta")
     * Deve essere univoco
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Categoria ingrediente (es: "Spiriti", "Frutta", "Erbe")
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Unità di misura (es: "ml", "pezzi", "foglie")
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * Descrizione opzionale dell'ingrediente
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Data/ora di creazione
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Relazione inversa: lista di cocktail che usano questo ingrediente
     * mappedBy = "ingredient" si riferisce al campo "ingredient" in CocktailIngredient
     * @JsonIgnore per evitare loop di serializzazione
     */
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<CocktailIngredient> cocktailIngredients = new HashSet<>();

    // Lifecycle hook
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Costruttori
    public Ingredient() {
    }

    public Ingredient(String name, String category, String unit) {
        this.name = name;
        this.category = category;
        this.unit = unit;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<CocktailIngredient> getCocktailIngredients() {
        return cocktailIngredients;
    }

    public void setCocktailIngredients(Set<CocktailIngredient> cocktailIngredients) {
        this.cocktailIngredients = cocktailIngredients;
    }
}
