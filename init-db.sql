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

-- Tabella Cocktails
CREATE TABLE IF NOT EXISTS cocktail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    ingredients TEXT,
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
-- DATI DI ESEMPIO
-- ==================================================

-- Cocktails classici
INSERT INTO cocktail (name, description, ingredients, category, glass_type, preparation_method, alcoholic) VALUES
('Mojito', 
 'Cocktail cubano rinfrescante a base di rum, menta e lime', 
 '50ml Rum Bianco, 1 Lime, 10 foglie Menta, 2 cucchiaini Zucchero, 100ml Soda',
 'Rum', 
 'Highball', 
 'Pestare menta e lime con zucchero, aggiungere rum e ghiaccio, completare con soda', 
 TRUE),

('Negroni', 
 'Aperitivo italiano classico amaro e forte', 
 '30ml Gin, 30ml Campari, 30ml Vermouth Rosso',
 'Gin', 
 'Old Fashioned', 
 'Mescolare tutti gli ingredienti con ghiaccio, servire con una scorza d''arancia', 
 TRUE),

('Margarita', 
 'Classico messicano con tequila e lime', 
 '50ml Tequila, 25ml Triple Sec, 25ml Succo di Lime',
 'Tequila', 
 'Margarita', 
 'Shakerare tutti gli ingredienti con ghiaccio, servire con bordo salato', 
 TRUE),

('Bloody Mary', 
 'Cocktail salato e speziato perfetto per il brunch', 
 '45ml Vodka, 120ml Succo di Pomodoro, Salsa Worcestershire, Tabasco, Sale, Pepe',
 'Vodka', 
 'Highball', 
 'Mescolare vodka e succo di pomodoro con spezie, servire con ghiaccio', 
 TRUE),

('Aperol Spritz', 
 'Aperitivo italiano leggero e frizzante', 
 '60ml Aperol, 90ml Prosecco, 30ml Soda, Fetta di Arancia',
 'Aperitivo', 
 'Wine Glass', 
 'Versare Aperol, Prosecco e soda in un bicchiere con ghiaccio, decorare con arancia', 
 TRUE);

-- ==================================================
-- RIEPILOGO STRUTTURA
-- ==================================================
-- Database "keycloak":
--   - Usato esclusivamente da Keycloak
--   - Utente: keycloak_user / keycloak_pass
--   - Conterrà ~67 tabelle create automaticamente da Keycloak
--
-- Database "cocktails":
--   - Usato dall'applicazione Spring Boot
--   - Utente: cocktail_user / cocktail_pass
--   - Contiene la tabella "cocktail" e future tabelle business
-- ==================================================
