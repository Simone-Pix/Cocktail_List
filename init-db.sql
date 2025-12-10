-- Script di inizializzazione - Crea 2 database separati
-- Viene eseguito automaticamente al primo avvio del container MySQL

-- ==================================================
-- CREAZIONE DATABASE SEPARATI
-- ==================================================

-- Database per Keycloak
CREATE DATABASE IF NOT EXISTS keycloak 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Database per Cocktails
CREATE DATABASE IF NOT EXISTS cocktails 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ==================================================
-- CREAZIONE UTENTI CON PERMESSI DEDICATI
-- ==================================================

-- Utente per Keycloak
CREATE USER IF NOT EXISTS 'keycloak_user'@'%' IDENTIFIED BY 'keycloak_pass';
GRANT ALL PRIVILEGES ON keycloak.* TO 'keycloak_user'@'%';

-- Utente per l'applicazione Cocktails
CREATE USER IF NOT EXISTS 'cocktail_user'@'%' IDENTIFIED BY 'cocktail_pass';
GRANT ALL PRIVILEGES ON cocktails.* TO 'cocktail_user'@'%';

-- Applica i permessi
FLUSH PRIVILEGES;

-- ==================================================
-- TABELLE PER L'APPLICAZIONE COCKTAILS
-- ==================================================

-- Seleziona il database cocktails per creare le tabelle
USE cocktails;

-- ==================================================
-- TABELLA INGREDIENTI
-- ==================================================
-- Contiene tutti gli ingredienti disponibili (normalizzati)
-- Un ingrediente può essere usato in molti cocktail
CREATE TABLE IF NOT EXISTS ingredient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    unit VARCHAR(20),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================================================
-- TABELLA COCKTAILS
-- ==================================================
-- Nota: NON ha più la colonna "ingredients" (ora è una relazione)
CREATE TABLE IF NOT EXISTS cocktail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50),
    glass_type VARCHAR(50),
    preparation_method TEXT,
    image_url VARCHAR(255),
    alcoholic BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================================================
-- TABELLA DI JOIN: COCKTAIL-INGREDIENTI
-- ==================================================
-- Collega cocktail agli ingredienti con quantità specifica
-- Relazione Many-to-Many: un cocktail ha molti ingredienti, un ingrediente è in molti cocktail
CREATE TABLE IF NOT EXISTS cocktail_ingredient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cocktail_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity VARCHAR(50) NOT NULL,
    FOREIGN KEY (cocktail_id) REFERENCES cocktail(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE RESTRICT,
    INDEX idx_cocktail (cocktail_id),
    INDEX idx_ingredient (ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================================================
-- TABELLA PREFERITI UTENTI
-- ==================================================
-- Salva i cocktail preferiti di ogni utente
-- user_id viene dal JWT (sub o email), non è FK perché gli utenti sono in Keycloak
CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    cocktail_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cocktail_id) REFERENCES cocktail(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_cocktail (user_id, cocktail_id),
    INDEX idx_user (user_id),
    INDEX idx_cocktail (cocktail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================================================
-- DATI DI ESEMPIO - INGREDIENTI
-- ==================================================

INSERT INTO ingredient (name, category, unit) VALUES
-- Spiriti
('Rum Bianco', 'Spiriti', 'ml'),
('Rum Scuro', 'Spiriti', 'ml'),
('Gin', 'Spiriti', 'ml'),
('Vodka', 'Spiriti', 'ml'),
('Tequila', 'Spiriti', 'ml'),
('Campari', 'Spiriti', 'ml'),
('Vermouth Rosso', 'Spiriti', 'ml'),
('Triple Sec', 'Spiriti', 'ml'),
('Aperol', 'Spiriti', 'ml'),

-- Frutta e Succhi
('Lime', 'Frutta', 'pezzi'),
('Limone', 'Frutta', 'pezzi'),
('Arancia', 'Frutta', 'pezzi'),
('Succo di Lime', 'Succhi', 'ml'),
('Succo di Limone', 'Succhi', 'ml'),
('Succo di Pomodoro', 'Succhi', 'ml'),

-- Erbe e Spezie
('Menta', 'Erbe', 'foglie'),
('Basilico', 'Erbe', 'foglie'),
('Salsa Worcestershire', 'Condimenti', 'gocce'),
('Tabasco', 'Condimenti', 'gocce'),
('Sale', 'Spezie', 'pizzico'),
('Pepe', 'Spezie', 'pizzico'),

-- Dolcificanti
('Zucchero', 'Dolcificanti', 'cucchiaini'),
('Zucchero di Canna', 'Dolcificanti', 'cucchiaini'),
('Sciroppo di Zucchero', 'Dolcificanti', 'ml'),

-- Bibite
('Soda', 'Bibite', 'ml'),
('Acqua Tonica', 'Bibite', 'ml'),
('Ginger Beer', 'Bibite', 'ml'),
('Prosecco', 'Vini', 'ml');

-- ==================================================
-- DATI DI ESEMPIO - COCKTAILS
-- ==================================================

INSERT INTO cocktail (name, description, category, glass_type, preparation_method, alcoholic) VALUES
('Mojito', 
 'Cocktail cubano rinfrescante a base di rum, menta e lime', 
 'Rum', 
 'Highball', 
 'Pestare menta e lime con zucchero, aggiungere rum e ghiaccio, completare con soda', 
 TRUE),

('Negroni', 
 'Aperitivo italiano classico amaro e forte', 
 'Gin', 
 'Old Fashioned', 
 'Mescolare tutti gli ingredienti con ghiaccio, servire con una scorza d''arancia', 
 TRUE),

('Margarita', 
 'Classico messicano con tequila e lime', 
 'Tequila', 
 'Margarita', 
 'Shakerare tutti gli ingredienti con ghiaccio, servire con bordo salato', 
 TRUE),

('Bloody Mary', 
 'Cocktail salato e speziato perfetto per il brunch', 
 'Vodka', 
 'Highball', 
 'Mescolare vodka e succo di pomodoro con spezie, servire con ghiaccio', 
 TRUE),

('Aperol Spritz', 
 'Aperitivo italiano leggero e frizzante', 
 'Aperitivo', 
 'Wine Glass', 
 'Versare Aperol, Prosecco e soda in un bicchiere con ghiaccio, decorare con arancia', 
 TRUE);

-- ==================================================
-- DATI DI ESEMPIO - RELAZIONI COCKTAIL-INGREDIENTI
-- ==================================================

-- Mojito (id=1): Rum, Lime, Menta, Zucchero, Soda
INSERT INTO cocktail_ingredient (cocktail_id, ingredient_id, quantity) VALUES
(1, 1, '50ml'),   -- Rum Bianco
(1, 10, '1 pezzo'),  -- Lime
(1, 16, '10 foglie'), -- Menta
(1, 22, '2 cucchiaini'), -- Zucchero
(1, 25, '100ml'); -- Soda

-- Negroni (id=2): Gin, Campari, Vermouth
INSERT INTO cocktail_ingredient (cocktail_id, ingredient_id, quantity) VALUES
(2, 3, '30ml'),   -- Gin
(2, 6, '30ml'),   -- Campari
(2, 7, '30ml');   -- Vermouth Rosso

-- Margarita (id=3): Tequila, Triple Sec, Succo di Lime
INSERT INTO cocktail_ingredient (cocktail_id, ingredient_id, quantity) VALUES
(3, 5, '50ml'),   -- Tequila
(3, 8, '25ml'),   -- Triple Sec
(3, 13, '25ml');  -- Succo di Lime

-- Bloody Mary (id=4): Vodka, Succo di Pomodoro, Worcestershire, Tabasco, Sale, Pepe
INSERT INTO cocktail_ingredient (cocktail_id, ingredient_id, quantity) VALUES
(4, 4, '45ml'),   -- Vodka
(4, 15, '120ml'), -- Succo di Pomodoro
(4, 18, '3 gocce'), -- Worcestershire
(4, 19, '2 gocce'), -- Tabasco
(4, 20, '1 pizzico'), -- Sale
(4, 21, '1 pizzico'); -- Pepe

-- Aperol Spritz (id=5): Aperol, Prosecco, Soda
INSERT INTO cocktail_ingredient (cocktail_id, ingredient_id, quantity) VALUES
(5, 9, '60ml'),   -- Aperol
(5, 28, '90ml'),  -- Prosecco
(5, 25, '30ml');  -- Soda

-- ==================================================
-- RIEPILOGO STRUTTURA
-- ==================================================
-- Database "keycloak":
--   - Usato esclusivamente da Keycloak
--   - Utente: keycloak_user / keycloak_pass
--   - Contiene utenti, ruoli, sessioni (~67 tabelle)
--
-- Database "cocktails":
--   - Usato dall'applicazione Spring Boot
--   - Utente: cocktail_user / cocktail_pass
--   
--   Tabelle:
--   1. ingredient: Ingredienti disponibili (28 precaricati)
--   2. cocktail: Cocktail disponibili (5 esempi)
--   3. cocktail_ingredient: Relazione cocktail-ingredienti con quantità
--   4. favorite: Preferiti degli utenti (user_id dal JWT Keycloak)
--
--   Relazioni:
--   - cocktail 1:N cocktail_ingredient N:1 ingredient (Many-to-Many)
--   - cocktail 1:N favorite (Un cocktail può essere preferito da molti utenti)
-- ==================================================
