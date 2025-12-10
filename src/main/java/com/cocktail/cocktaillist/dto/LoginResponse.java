package com.cocktail.cocktaillist.dto;

/**
 * DTO per la risposta di login
 * Restituisce solo il token JWT e informazioni essenziali
 */
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private int expiresIn;
    private String refreshToken;

    // Costruttori
    public LoginResponse() {
    }

    public LoginResponse(String token, int expiresIn, String refreshToken) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
    }

    // Getters e Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
