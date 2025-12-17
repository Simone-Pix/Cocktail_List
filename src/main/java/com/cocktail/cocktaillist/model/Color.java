package com.cocktail.cocktaillist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "color")
@Schema(description = "Colore per personalizzare l'aspetto del cocktail preferito")
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID univoco del colore", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Schema(description = "Nome del colore", example = "Rosso Classico", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(name = "hex_code", nullable = false, unique = true, length = 7)
    @Schema(description = "Codice esadecimale del colore", example = "#DC143C", requiredMode = Schema.RequiredMode.REQUIRED)
    private String hexCode;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descrizione del colore", example = "Rosso intenso per cocktail energici")
    private String description;

    @Column(name = "created_at", updatable = false)
    @Schema(description = "Data di creazione", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "color", fetch = FetchType.LAZY)
    @JsonIgnore
    @Schema(hidden = true)
    private List<Favorite> favorites;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
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

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }
}
