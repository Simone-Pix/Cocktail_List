package com.cocktail.cocktaillist.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entità JPA che mappa la tabella "cocktail" del database.
 * Ogni istanza di questa classe rappresenta una riga della tabella.
 */
@Entity
@Table(name = "cocktail")
public class Cocktail {

    /**
     * Chiave primaria - corrisponde alla colonna "id" (auto-increment)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome del cocktail - campo obbligatorio e univoco
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Descrizione del cocktail
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Relazione One-to-Many con CocktailIngredient
     * Un cocktail ha molti ingredienti (attraverso la tabella di join)
     * cascade = ALL: quando salvi/elimini il cocktail, gestisci anche gli ingredienti
     * orphanRemoval = true: se rimuovi un ingrediente dalla lista, viene eliminato dal DB
     */
    @OneToMany(mappedBy = "cocktail", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<CocktailIngredient> cocktailIngredients = new HashSet<>();

    /**
     * Categoria del cocktail (Rum, Gin, Vodka, Tequila, ecc.)
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Tipo di bicchiere (Highball, Old Fashioned, ecc.)
     */
    @Column(name = "glass_type", length = 50)
    private String glassType;

    /**
     * Metodo di preparazione
     */
    @Column(name = "preparation_method", columnDefinition = "TEXT")
    private String preparationMethod;

    /**
     * URL dell'immagine del cocktail
     */
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    /**
     * Indica se il cocktail è alcolico o meno
     */
    @Column(name = "alcoholic")
    private Boolean alcoholic;

    /**
     * Data/ora di creazione del record
     * Impostata automaticamente al momento dell'inserimento
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data/ora dell'ultimo aggiornamento
     * Aggiornata automaticamente ad ogni modifica
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Metodo chiamato automaticamente PRIMA del persist (INSERT)
     * Imposta created_at e updated_at
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Metodo chiamato automaticamente PRIMA dell'update
     * Aggiorna updated_at
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================
    // COSTRUTTORI
    // ========================================

    /**
     * Costruttore vuoto richiesto da JPA
     */
    public Cocktail() {
    }

    /**
     * Costruttore con campi principali
     */
    public Cocktail(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    // ========================================
    // METODI HELPER PER GESTIRE INGREDIENTI
    // ========================================

    /**
     * Aggiunge un ingrediente al cocktail con la quantità specificata
     * Gestisce automaticamente la relazione bidirezionale
     */
    public void addIngredient(Ingredient ingredient, String quantity) {
        CocktailIngredient cocktailIngredient = new CocktailIngredient(this, ingredient, quantity);
        cocktailIngredients.add(cocktailIngredient);
    }

    /**
     * Rimuove un ingrediente dal cocktail
     */
    public void removeIngredient(CocktailIngredient cocktailIngredient) {
        cocktailIngredients.remove(cocktailIngredient);
        cocktailIngredient.setCocktail(null);
    }

    /**
     * Rimuove tutti gli ingredienti
     */
    public void clearIngredients() {
        //implementare
    }

    // ========================================
    // GETTERS E SETTERS
    // ========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<CocktailIngredient> getCocktailIngredients() {
        return cocktailIngredients;
    }

    public void setCocktailIngredients(Set<CocktailIngredient> cocktailIngredients) {
        this.cocktailIngredients = cocktailIngredients;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGlassType() {
        return glassType;
    }

    public void setGlassType(String glassType) {
        this.glassType = glassType;
    }

    public String getPreparationMethod() {
        return preparationMethod;
    }

    public void setPreparationMethod(String preparationMethod) {
        this.preparationMethod = preparationMethod;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getAlcoholic() {
        return alcoholic;
    }

    public void setAlcoholic(Boolean alcoholic) {
        this.alcoholic = alcoholic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========================================
    // METODI UTILITY
    // ========================================

    @Override
    public String toString() {
        return "Cocktail{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", alcoholic=" + alcoholic +
                '}';
    }
}
