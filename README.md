# Cocktail List API

API REST per la gestione di cocktail con autenticazione OAuth2/JWT tramite Keycloak.

## Indice

- [Tecnologie](#tecnologie)
- [Prerequisiti](#prerequisiti)
- [Installazione](#installazione)
- [Utilizzo](#utilizzo)
- [API Endpoints](#api-endpoints)
- [Autenticazione](#autenticazione)
- [Database](#database)
- [Configurazione](#configurazione)
- [Aggiornamenti Recenti](#aggiornamenti-recenti)

## Tecnologie

- **Java 17** - Linguaggio di programmazione
- **Spring Boot 3.2.0** - Framework applicativo
- **Spring Security** - OAuth2 Resource Server con JWT
- **Spring Data JPA** - ORM e gestione database
- **Keycloak 23.0** - Identity and Access Management
- **MySQL 8.0** - Database relazionale
- **Docker & Docker Compose** - Containerizzazione
- **Maven 3.9** - Build automation
- **Swagger/OpenAPI 3.0** - Documentazione API interattiva

## Prerequisiti

- [Docker Desktop](https://www.docker.com/products/docker-desktop) installato e avviato
- (Opzionale) [Maven 3.9+](https://maven.apache.org/download.cgi) per sviluppo locale
- (Opzionale) [Java 17+](https://adoptium.net/) per sviluppo locale

## Installazione

### 1. Clone del repository

```bash
git clone https://github.com/Simone-Pix/Cocktail_List.git
cd Cocktail_List
```

### 2. Avvio dei container

```powershell
docker-compose up -d
```

Questo comando avvia:
- **MySQL** (porta 3306) - Database
- **Keycloak** (porta 8080) - Identity Provider
- **Spring Boot App** (porta 8081) - API REST

### 3. Verifica dello stato

```powershell
docker-compose ps
```

Tutti i container devono essere nello stato `running/healthy`.

## Utilizzo

### Accesso a Swagger UI

Apri il browser e vai su:

```
http://localhost:8081
```

L'applicazione reindirizza automaticamente a Swagger UI.

### Autenticazione con Swagger

1. In Swagger, vai alla sezione **auth-controller**
2. Apri `GET /api/auth/login`
3. Clicca **"Try it out"**
4. Inserisci le credenziali:
   - **username**: `simone`
   - **password**: `123456`
5. Clicca **"Execute"**
6. Copia il valore di `token` dalla risposta
7. Clicca sul pulsante **"Authorize"** 🔓 in alto a destra
8. Incolla il token nel campo "Value"
9. Clicca **"Authorize"** e poi **"Close"**
10. Ora tutte le chiamate useranno automaticamente il token!

### Test degli endpoint

Dopo l'autenticazione, puoi testare tutti gli endpoint direttamente da Swagger.

## API Endpoints

### 🌐 Pubblici (nessuna autenticazione richiesta)

| Metodo | Endpoint | Descrizione | Paginazione |
|--------|----------|-------------|-------------|
| GET | `/api/public/hello` | Messaggio di benvenuto | - |
| GET | `/api/public/cocktails` | Lista di tutti i cocktail | ✅ `?page=0&size=10&sortBy=name&sortDir=asc` |
| GET | `/api/ingredients` | Lista di tutti gli ingredienti | ✅ `?page=0&size=10&sortBy=name&sortDir=asc` |
| GET | `/api/ingredients/search?name={query}` | Cerca ingredienti per nome | ✅ `&page=0&size=10` |
| GET | `/api/ingredients/{id}` | Dettaglio ingrediente | - |
| GET | `/api/auth/login` | Ottieni token JWT | - |
| POST | `/api/auth/refresh` | Rinnova token con refresh_token | - |

### 👤 USER (richiede ruolo USER o ADMIN)

#### Cocktails

| Metodo | Endpoint | Descrizione | Paginazione |
|--------|----------|-------------|-------------|
| GET | `/api/user/cocktails` | Lista completa di tutti i cocktail | ✅ `?page=0&size=10&sortBy=name&sortDir=asc` |
| GET | `/api/user/cocktails/{id}` | Dettagli cocktail per ID | - |
| GET | `/api/user/cocktails/search?name={query}` | Cerca cocktail per nome | ✅ `&page=0&size=10` |
| GET | `/api/user/cocktails/category/{category}` | Filtra per categoria | ✅ `?page=0&size=10` |
| GET | `/api/user/cocktails/alcoholic?value=true` | Filtra per alcolico/analcolico | - |
| POST | `/api/cocktails` | **Crea nuovo cocktail** (con auto-creazione ingredienti) | - |
| PUT | `/api/cocktails/{id}` | **Aggiorna cocktail esistente** | - |

#### Ingredienti

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| POST | `/api/ingredients` | **Crea nuovo ingrediente** |
| PUT | `/api/ingredients/{id}` | **Aggiorna ingrediente** |

#### Preferiti

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/favorites` | **Ottieni i tuoi preferiti** |
| GET | `/api/favorites/check/{cocktailId}` | Verifica se è preferito |
| POST | `/api/favorites/{cocktailId}` | **Aggiungi ai preferiti** |
| DELETE | `/api/favorites/{cocktailId}` | Rimuovi dai preferiti |
| PUT | `/api/favorites/toggle/{cocktailId}` | Toggle (aggiungi/rimuovi) |
| DELETE | `/api/favorites` | Rimuovi tutti i preferiti |

#### Profilo

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/user/profile` | Informazioni profilo utente |

### 👑 ADMIN (richiede ruolo ADMIN)

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| DELETE | `/api/admin/cocktails/{id}` | **Elimina cocktail** |
| DELETE | `/api/ingredients/{id}` | **Elimina ingrediente** |
| GET | `/api/admin/stats` | Statistiche amministrative |

## Database Schema

Il database utilizza un **design normalizzato** con 4 tabelle:

### 📊 Tabelle

#### 1. `ingredient` (Ingredienti)
```sql
CREATE TABLE ingredient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50),
    unit VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
- **28 ingredienti precaricati** (rum, vodka, gin, limone, zucchero, etc.)
- Categorie: spirit, liqueur, juice, fruit, herb, syrup, other
- Unità: ml, foglie, fette, grammi, etc.

#### 2. `cocktail` (Cocktail)
```sql
CREATE TABLE cocktail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    alcoholic BOOLEAN,
    glass VARCHAR(50),
    instructions TEXT,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```
- Contiene i dati principali del cocktail
- **Non** contiene più il campo `ingredients` (ora normalizzato)

#### 3. `cocktail_ingredient` (Relazione Many-to-Many)
```sql
CREATE TABLE cocktail_ingredient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cocktail_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity VARCHAR(50),
    FOREIGN KEY (cocktail_id) REFERENCES cocktail(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE
);
```
- Join table che collega cocktail e ingredienti
- Campo `quantity` per specificare dosi (es. "50ml", "10 foglie")

#### 4. `favorite` (Preferiti Utente)
```sql
CREATE TABLE favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(255) NOT NULL,
    cocktail_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_cocktail (user_id, cocktail_id),
    FOREIGN KEY (cocktail_id) REFERENCES cocktail(id) ON DELETE CASCADE
);
```
- `user_id` contiene il **subject del JWT** (non FK su Keycloak)
- Un utente può avere un cocktail preferito **solo una volta**

### 🔗 Relazioni

```
ingredient (1) ←→ (N) cocktail_ingredient (N) ←→ (1) cocktail
                                                         ↑
                                                         |
                                              (N) ← favorite → (1) user (JWT)
```

### 🌱 Dati Iniziali

Cocktail precaricati:
- **Mojito**: Rum Bianco (50ml), Lime (15ml), Zucchero (10g), Menta (10 foglie)
- **Negroni**: Gin (30ml), Campari (30ml), Vermouth Rosso (30ml)
- **Margarita**: Tequila (50ml), Triple Sec (30ml), Lime (20ml)

## Autenticazione

### Utenti disponibili

| Email | Password | Ruoli |
|-------|----------|-------|
| simone@test.com | 123456 | USER, ADMIN |

### Gestione utenti Keycloak

Accedi alla console admin di Keycloak:

```
URL: http://localhost:8080/admin
Username: admin
Password: admin
```

Realm: **cocktail_realm**

### Configurazione Keycloak

1. **Crea il Realm**:
   - Nome: `cocktail_realm` (con underscore, non trattino)

2. **Crea il Client**:
   - Client ID: `cocktail-client`
   - Client Protocol: `openid-connect`
   - Access Type: `public`
   - Direct Access Grants Enabled: `ON`
   - Valid Redirect URIs: `http://localhost:8081/*`

3. **Crea l'Utente**:
   - Username/Email: `simone@test.com`
   - Password: `123456` (Temporary: OFF)
   - Role Mappings: aggiungi ruolo `USER`

### Ottenere un token via PowerShell

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/realms/cocktail_realm/protocol/openid-connect/token" -Method POST -Body @{
    grant_type = "password"
    client_id = "cocktail-client"
    username = "simone@test.com"
    password = "123456"
} -ContentType "application/x-www-form-urlencoded"

$token = $response.access_token
Write-Host $token
```

### Chiamare API con il token

```powershell
$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8081/api/user/cocktails/1" -Headers $headers
```

## Funzionalità Chiave

### 🎯 Auto-creazione Ingredienti

Quando crei o modifichi un cocktail, **non serve creare manualmente gli ingredienti**:

```json
POST /api/cocktails
{
  "name": "Mojito",
  "ingredients": [
    {"name": "Rum Bianco", "quantity": "50ml"},
    {"name": "Menta", "quantity": "10 foglie"},
    {"name": "Nuovo Ingrediente", "quantity": "20g"}
  ]
}
```

Il sistema:
1. Cerca ogni ingrediente nel database (case-insensitive)
2. Se esiste, lo usa
3. Se **non** esiste, lo crea automaticamente con categoria "other"

### ⭐ Sistema Preferiti

Ogni utente ha la propria lista di preferiti:

```bash
GET /api/favorites → Lista dei tuoi cocktail preferiti
POST /api/favorites/5 → Aggiungi cocktail ID 5 ai preferiti
PUT /api/favorites/toggle/5 → Toggle (se già preferito lo rimuove, altrimenti lo aggiunge)
GET /api/favorites/check/5 → Controlla se il cocktail 5 è nei tuoi preferiti
```

- I preferiti sono legati all'utente tramite JWT (campo `user_id`)
- Non ci sono Foreign Key su Keycloak (separazione dei database)

### 🔒 Sistema Permessi

| Azione | PUBLIC | USER | ADMIN |
|--------|--------|------|-------|
| Visualizzare cocktail | ✅ (base) | ✅ (completo) | ✅ |
| Creare cocktail | ❌ | ✅ | ✅ |
| Modificare cocktail | ❌ | ✅ | ✅ |
| Eliminare cocktail | ❌ | ❌ | ✅ |
| Creare ingredienti | ❌ | ✅ (auto) | ✅ |
| Eliminare ingredienti | ❌ | ❌ | ✅ |
| Gestire preferiti | ❌ | ✅ (propri) | ✅ |

## Database

Il progetto utilizza due database MySQL separati:

### 1. Database `keycloak`
- Gestito automaticamente da Keycloak
- Contiene utenti, ruoli, client, sessioni
- 91 tabelle generate da Keycloak

### 2. Database `cocktails`
- Database applicativo con **4 tabelle normalizzate**
- Vedi sezione "Database Schema" per dettagli completi

### Accesso MySQL

```powershell
docker exec -it cocktail-mysql mysql -uroot -prootpassword
```

```sql
USE cocktails;
SELECT * FROM cocktail;
SELECT * FROM ingredient;
SELECT * FROM cocktail_ingredient;
SELECT * FROM favorite;
```

### Dati di esempio

Il database viene popolato automaticamente con:
- **28 ingredienti** (rum, vodka, gin, limone, zucchero, menta, etc.)
- **3 cocktail** (Mojito, Margarita, Negroni) con ingredienti completi

## Configurazione

### Variabili d'ambiente (docker-compose.yml)

#### MySQL
- `MYSQL_ROOT_PASSWORD`: rootpassword

#### Keycloak
- `KEYCLOAK_ADMIN`: admin
- `KEYCLOAK_ADMIN_PASSWORD`: admin
- `KC_DB`: mysql
- `KC_DB_URL`: jdbc:mysql://mysql:3306/keycloak
- `KC_DB_USERNAME`: keycloak_user
- `KC_DB_PASSWORD`: keycloak_pass

#### Spring Boot
- `SPRING_PROFILES_ACTIVE`: docker
- `KEYCLOAK_AUTH_SERVER_URL`: http://keycloak:8080

### Porte utilizzate

| Servizio | Porta |
|----------|-------|
| MySQL | 3306 |
| Keycloak | 8080 |
| Spring Boot API | 8081 |

### Profili Spring

- **default**: Per esecuzione locale (Keycloak su localhost:8080)
- **docker**: Per esecuzione in container (Keycloak su keycloak:8080)

## Troubleshooting

### I container non si avviano

```powershell
# Verifica i log
docker-compose logs -f

# Riavvia i servizi
docker-compose down
docker-compose up -d
```

### Realm does not exist / Client not found

Problema: Mismatch tra nome realm in `application.yml` e Keycloak.

**Soluzione**:
1. Assicurati che in `application.yml` ci sia `cocktail_realm` (con underscore)
2. In Keycloak crea realm con nome esatto: `cocktail_realm`
3. Crea client `cocktail-client` con Direct Access Grants abilitato

```yaml
# application.yml - Verifica questi 3 punti
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/cocktail_realm  # ← underscore
          jwk-set-uri: http://localhost:8080/realms/cocktail_realm/protocol/openid-connect/certs  # ← underscore
```

### GET /api/ingredients ritorna array vuoto

Problema: Tabella `ingredient` non esiste dopo reset database.

**Soluzione**:
```powershell
# Reset completo dei volumi Docker
docker-compose down -v
docker-compose up -d
```

Questo ricreerà il database con tutte le tabelle e i 28 ingredienti precaricati.

### GET /api/favorites ritorna errore 500

Problema: Hibernate LAZY proxy non può essere serializzato da Jackson.

**Soluzione**: Cambiato `FetchType.LAZY` in `FetchType.EAGER` nell'entità `Favorite`:

```java
@ManyToOne(fetch = FetchType.EAGER)  // ← EAGER invece di LAZY
@JoinColumn(name = "cocktail_id")
private Cocktail cocktail;
```

### JSON mostra oggetti Java invece di dati

Problema: Cicli di serializzazione o metodi getter obsoleti.

**Soluzione applicata**:
- Aggiunto `@JsonIgnore` su `Ingredient.cocktailIngredients`
- Aggiunto `@JsonBackReference` su `CocktailIngredient.cocktail`
- Rimossi metodi `getIngredients()` e `setIngredients()` obsoleti da `Cocktail.java`

### Keycloak non risponde

```powershell
# Verifica lo stato
docker logs keycloak

# Riavvia Keycloak
docker-compose restart keycloak
```

### Rebuild completo

```powershell
# Ferma tutto e rimuovi volumi
docker-compose down -v

# Rimuovi le immagini
docker rmi cocktail_list-cocktail-app

# Rebuild da zero
docker-compose up --build -d
```

## Note di sviluppo

### Modificare il codice

1. Modifica i file in `src/main/java`
2. Rebuild del container:
   ```powershell
   docker-compose build cocktail-app
   docker-compose up -d --force-recreate cocktail-app
   ```

### Hot reload (sviluppo locale)

```powershell
# Avvia solo MySQL e Keycloak
docker-compose up -d mysql keycloak

# Avvia Spring Boot localmente con Maven
mvn spring-boot:run
```

### Aggiungere un nuovo utente in Keycloak

1. Vai su http://localhost:8080/admin
2. Login con admin/admin
3. Seleziona realm **cocktail_realm** (con underscore)
4. Menu **Users** → **Add user**
5. Compila i campi e salva
6. Tab **Credentials** → Imposta password (Temporary: OFF)
7. Tab **Role mappings** → Assegna ruoli USER/ADMIN

## Changelog

### 📄 Paginazione (Dicembre 2025)

#### ✅ Implementata Paginazione Completa
**Endpoint paginati:**
- `GET /api/public/cocktails` - Lista pubblica cocktail
- `GET /api/user/cocktails` - Lista completa cocktail (autenticato)
- `GET /api/user/cocktails/category/{category}` - Filtro per categoria
- `GET /api/user/cocktails/search?name={query}` - Ricerca per nome
- `GET /api/ingredients` - Lista ingredienti
- `GET /api/ingredients/search?name={query}` - Ricerca ingredienti

**Parametri di paginazione:**
- `page` (default: 0) - Numero pagina (0-based)
- `size` (default: 10) - Elementi per pagina
- `sortBy` (default: "name") - Campo per ordinamento
- `sortDir` (default: "asc") - Direzione: "asc" o "desc"

**Struttura risposta paginata:**
```json
{
  "content": [...],          // Array di elementi della pagina corrente
  "totalElements": 25,       // Totale elementi nel database
  "totalPages": 3,           // Totale pagine disponibili
  "number": 0,               // Numero pagina corrente (0-based)
  "size": 10,                // Elementi per pagina
  "first": true,             // È la prima pagina?
  "last": false,             // È l'ultima pagina?
  "numberOfElements": 10,    // Elementi in questa pagina
  "empty": false             // Pagina vuota?
}
```

**Modifiche al codice:**
- `CocktailController`: 4 endpoint convertiti da `List<Cocktail>` a `Page<Cocktail>`
- `CocktailService`: aggiunti 3 metodi paginati con `PageRequest` e `Sort`
- `CocktailRepository`: aggiunti overload con parametro `Pageable`
- `IngredientController`: 2 endpoint convertiti a `Page<Ingredient>`
- `IngredientService`: aggiunti 2 metodi paginati
- `IngredientRepository`: aggiunto overload `findByNameContainingIgnoreCase(String, Pageable)`

**Vantaggi:**
- ⚡ Performance migliorate (LIMIT/OFFSET SQL automatico)
- 📦 Riduzione banda (solo dati necessari)
- 📱 Frontend-friendly (metadata per UI pagination)
- 🔍 Ordinamento flessibile per qualsiasi campo

**Esempio chiamata:**
```bash
GET http://localhost:8081/api/public/cocktails?page=0&size=5&sortBy=name&sortDir=desc
```

---

### 🗄️ Database Normalizzato (Dicembre 2025)

#### ✅ Database Normalizzato
- Creata tabella `ingredient` (28 ingredienti precaricati)
- Creata tabella `cocktail_ingredient` (join con campo `quantity`)
- Creata tabella `favorite` (user_id da JWT)
- Rimosso campo `ingredients` da tabella `cocktail`

#### ✅ Nuove Entità JPA
- `Ingredient.java` con relazione `@OneToMany` a `CocktailIngredient`
- `CocktailIngredient.java` join entity con `@JsonBackReference`
- `Favorite.java` con `FetchType.EAGER` per evitare proxy Hibernate
- `Cocktail.java` aggiornato con `Set<CocktailIngredient>`

#### ✅ Nuovi Repository
- `IngredientRepository` con query `findByNameIgnoreCase`, `existsByNameIgnoreCase`
- `CocktailIngredientRepository` con `findByCocktailId`, `deleteByCocktailId`
- `FavoriteRepository` con `findByUserId`, `existsByUserIdAndCocktailId`

#### ✅ Nuovi Service
- `IngredientService` con **auto-creazione**: `findOrCreateIngredient(name, category, unit)`
- `FavoriteService` completo (add, remove, toggle, check)
- `CocktailService` modificato per accettare `CocktailRequest` DTO

#### ✅ Nuovi Controller
- `IngredientController` (GET pubblico, POST/PUT USER/ADMIN, DELETE ADMIN)
- `FavoriteController` (tutti i metodi per USER/ADMIN)
- `CocktailController` modificato: POST/PUT per USER/ADMIN (prima solo ADMIN)

#### ✅ Login Semplificato
- `AuthController` usa `@RequestParam` invece di `@RequestBody`
- Risposta con `LoginResponse` DTO (token, expiresIn, tokenType, refreshToken)
- Endpoint: `GET /api/auth/login?username=X&password=Y`

#### ✅ Fix Critici
- Realm rinominato `cocktail-realm` → `cocktail_realm` (underscore)
- JSON serialization: aggiunti `@JsonIgnore`, `@JsonBackReference`, `@JsonManagedReference`
- Rimossi metodi `getIngredients()`/`setIngredients()` obsoleti
- `Favorite.cocktail` cambiato da LAZY a EAGER per serializzazione

#### ✅ Sistema Permessi
- **USER** può creare/modificare cocktail e ingredienti
- **ADMIN** può eliminare cocktail e ingredienti
- Auto-creazione ingredienti durante creazione cocktail

## Licenza

Questo progetto è distribuito sotto licenza MIT.

## Autore

**Simone**
- GitHub: [@Simone-Pix](https://github.com/Simone-Pix)

## Contributi

I contributi sono benvenuti! Sentiti libero di aprire issue o pull request.

---

**Made with coffee and Spring Boot**
