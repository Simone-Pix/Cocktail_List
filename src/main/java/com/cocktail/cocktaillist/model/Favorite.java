package com.cocktail.cocktaillist.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity JPA per la tabella "favorite"
 * Rappresenta i cocktail preferiti di un utente
 * 
 * NOTA: userId non è una FK perché gli utenti sono gestiti da Keycloak (database separato)
 * L'userId viene estratto dal JWT al runtime
 */
@Entity
@Table(name = "favorite", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cocktail_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID utente dal JWT Keycloak
     * Può essere: jwt.getSubject() (UUID) o jwt.getClaim("email")
     */
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    /**
     * Relazione Many-to-One verso Cocktail
     * Un cocktail può essere preferito da molti utenti
     * EAGER per evitare problemi di serializzazione JSON con proxy Hibernate
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;

    /**
     * Data/ora in cui è stato aggiunto ai preferiti
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Lifecycle hook
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Costruttori
    public Favorite() {
    }

    public Favorite(String userId, Cocktail cocktail) {
        this.userId = userId;
        this.cocktail = cocktail;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Cocktail getCocktail() {
        return cocktail;
    }

    public void setCocktail(Cocktail cocktail) {
        this.cocktail = cocktail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
