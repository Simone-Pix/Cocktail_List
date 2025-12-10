package com.cocktail.cocktaillist.dto;

/**
 * DTO per la richiesta di refresh token
 */
public class RefreshRequest {
    private String refreshToken;

    // Costruttori
    public RefreshRequest() {
    }

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters e Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
