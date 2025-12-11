package com.cocktail.cocktaillist.service;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service per gestire operazioni admin su Keycloak
 * - Creazione utenti
 * - Assegnazione ruoli
 * - Gestione credenziali
 */
@Service
public class KeycloakAdminService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    /**
     * Ottiene un client Keycloak con privilegi admin
     */
    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm("master")  // Admin usa realm master
                .username(adminUsername)
                .password(adminPassword)
                .clientId("admin-cli")
                .build();
    }

    /**
     * Crea un nuovo utente in Keycloak
     * 
     * @param username Username univoco
     * @param email Email univoca
     * @param password Password in chiaro (Keycloak la cripta)
     * @param firstName Nome
     * @param lastName Cognome
     * @return User ID del nuovo utente
     * @throws RuntimeException se la creazione fallisce
     */
    public String createUser(String username, String email, String password, 
                           String firstName, String lastName) {
        Keycloak keycloak = getKeycloakInstance();
        
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Verifica se username o email già esistono
            if (!usersResource.search(username).isEmpty()) {
                throw new RuntimeException("Username già in uso");
            }
            
            List<UserRepresentation> existingUsers = usersResource.search(null, null, null, email, null, null);
            if (!existingUsers.isEmpty()) {
                throw new RuntimeException("Email già registrata");
            }

            // Crea rappresentazione utente
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);  // Per sviluppo, true. In produzione: false + email verification

            // Crea utente
            Response response = usersResource.create(user);
            
            if (response.getStatus() != 201) {
                throw new RuntimeException("Errore nella creazione utente: " + response.getStatusInfo());
            }

            // Estrai User ID dalla location
            String locationPath = response.getLocation().getPath();
            String userId = locationPath.substring(locationPath.lastIndexOf('/') + 1);
            
            response.close();

            // Imposta password
            setUserPassword(userId, password);

            // Assegna ruolo USER
            assignUserRole(userId);

            return userId;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la creazione utente: " + e.getMessage(), e);
        } finally {
            keycloak.close();
        }
    }

    /**
     * Imposta la password per un utente
     */
    private void setUserPassword(String userId, String password) {
        Keycloak keycloak = getKeycloakInstance();
        
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);  // Password permanente

            keycloak.realm(realm).users().get(userId).resetPassword(credential);
        } finally {
            keycloak.close();
        }
    }

    /**
     * Assegna il ruolo USER a un utente
     */
    private void assignUserRole(String userId) {
        Keycloak keycloak = getKeycloakInstance();
        
        try {
            RealmResource realmResource = keycloak.realm(realm);
            
            // Ottieni il ruolo USER
            RoleRepresentation userRole = realmResource.roles().get("USER").toRepresentation();
            
            // Assegna ruolo all'utente
            realmResource.users().get(userId).roles().realmLevel()
                    .add(Collections.singletonList(userRole));
                    
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'assegnazione ruolo USER: " + e.getMessage(), e);
        } finally {
            keycloak.close();
        }
    }

    /**
     * Verifica se un username è già in uso
     */
    public boolean usernameExists(String username) {
        Keycloak keycloak = getKeycloakInstance();
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users().search(username);
            return !users.isEmpty();
        } finally {
            keycloak.close();
        }
    }

    /**
     * Verifica se un'email è già registrata
     */
    public boolean emailExists(String email) {
        Keycloak keycloak = getKeycloakInstance();
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users()
                    .search(null, null, null, email, null, null);
            return !users.isEmpty();
        } finally {
            keycloak.close();
        }
    }
}
