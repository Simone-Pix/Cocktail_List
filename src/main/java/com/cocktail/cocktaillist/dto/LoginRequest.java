package com.cocktail.cocktaillist.dto;

/**
 * DTO per la richiesta di login
 * Riceve username e password in formato JSON
 */
public class LoginRequest {
    private String username;
    private String password;

    // Costruttori
    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters e Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
