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

## Tecnologie

- **Java 17** - Linguaggio di programmazione
- **Spring Boot 3.2.0** - Framework applicativo
- **Spring Security** - OAuth2 Resource Server con JWT
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
http://localhost:8081/swagger-ui/index.html
```

### Autenticazione con Swagger

#### Metodo 1: Endpoint `/api/auth/login` (Consigliato)

1. In Swagger, vai alla sezione **Authentication**
2. Apri `POST /api/auth/login`
3. Clicca **"Try it out"**
4. Inserisci le credenziali:
   - **username**: `simone@test.com`
   - **password**: `123456`
   - **clientId**: `cocktail-client`
5. Clicca **"Execute"**
6. Copia il valore di `access_token` dalla risposta
7. Clicca sul lucchetto **"Bearer Authentication"** in alto
8. Incolla il token nel campo "Value"
9. Clicca **"Authorize"**

#### Metodo 2: OAuth2 Password Flow

1. Clicca sul lucchetto **"OAuth2"** in alto
2. Inserisci:
   - **username**: `simone@test.com`
   - **password**: `123456`
3. Clicca **"Authorize"**

### Test degli endpoint

Dopo l'autenticazione, puoi testare tutti gli endpoint direttamente da Swagger.

## API Endpoints

### Pubblici (nessuna autenticazione richiesta)

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/public/hello` | Messaggio di benvenuto |
| GET | `/api/public/cocktails` | Lista di tutti i cocktail |
| POST | `/api/auth/login` | Ottieni token JWT |
| POST | `/api/auth/refresh` | Rinnova token con refresh_token |

### USER (richiede ruolo USER o ADMIN)

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/user/cocktails/{id}` | Dettagli cocktail per ID |
| GET | `/api/user/cocktails/search` | Cerca cocktail per nome |
| GET | `/api/user/cocktails/category/{category}` | Filtra per categoria |
| GET | `/api/user/cocktails/alcoholic/{alcoholic}` | Filtra per alcolico/analcolico |
| GET | `/api/user/profile` | Informazioni profilo utente |
| GET | `/api/auth/token-info` | Informazioni token corrente |

### ADMIN (richiede ruolo ADMIN)

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| POST | `/api/admin/cocktails` | Crea nuovo cocktail |
| PUT | `/api/admin/cocktails/{id}` | Aggiorna cocktail esistente |
| DELETE | `/api/admin/cocktails/{id}` | Elimina cocktail |

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

Realm: **cocktail-realm**

### Ottenere un token via PowerShell

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/realms/cocktail-realm/protocol/openid-connect/token" -Method POST -Body @{
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

## Database

### Struttura

Il progetto utilizza due database MySQL separati:

#### 1. Database `keycloak`
- Gestito automaticamente da Keycloak
- Contiene utenti, ruoli, client, sessioni
- 91 tabelle generate da Keycloak

#### 2. Database `cocktails`
- Database applicativo
- Tabella principale: `cocktail`

### Schema tabella `cocktail`

```sql
CREATE TABLE cocktail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    ingredients TEXT,
    category VARCHAR(50),
    glass_type VARCHAR(50),
    preparation_method TEXT,
    image_url VARCHAR(255),
    alcoholic BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Accesso MySQL

```powershell
docker exec -it cocktail-mysql mysql -uroot -prootpassword
```

```sql
USE cocktails;
SELECT * FROM cocktail;
```

### Dati di esempio

Il database viene popolato automaticamente con 5 cocktail:
- Mojito
- Margarita
- Negroni
- Aperol Spritz
- Manhattan

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

### Errore 401 "Bearer token is malformed"

Il problema è stato risolto rimuovendo la validazione `issuer-uri` in `application.yml`. La configurazione attuale valida solo la firma JWT tramite `jwk-set-uri`.

### Keycloak non risponde

```powershell
# Verifica lo stato
docker logs keycloak

# Riavvia Keycloak
docker-compose restart keycloak
```

### Rebuild completo

```powershell
# Ferma tutto
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
   docker-compose up --build -d cocktail-app
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
3. Seleziona realm **cocktail-realm**
4. Menu **Users** → **Add user**
5. Compila i campi e salva
6. Tab **Credentials** → Imposta password
7. Tab **Role mappings** → Assegna ruoli USER/ADMIN

## Licenza

Questo progetto è distribuito sotto licenza MIT.

## Autore

**Simone**
- GitHub: [@Simone-Pix](https://github.com/Simone-Pix)

## Contributi

I contributi sono benvenuti! Sentiti libero di aprire issue o pull request.

---

**Made with coffee and Spring Boot**
