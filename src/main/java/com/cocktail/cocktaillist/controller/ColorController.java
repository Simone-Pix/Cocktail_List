package com.cocktail.cocktaillist.controller;

import com.cocktail.cocktaillist.model.Color;
import com.cocktail.cocktaillist.repository.ColorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Colori", description = "API per la gestione dei colori dei cocktail preferiti")
public class ColorController {

    private final ColorRepository colorRepository;

    public ColorController(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @GetMapping("/api/public/colors")
    @Operation(
            summary = "Ottieni tutti i colori disponibili",
            description = "Restituisce l'elenco completo di tutti i colori che gli utenti possono assegnare ai propri cocktail preferiti"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista colori recuperata con successo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Color.class)))
    })
    public ResponseEntity<List<Color>> getAllColors() {
        List<Color> colors = colorRepository.findAll();
        return ResponseEntity.ok(colors);
    }

    @GetMapping("/api/public/colors/{id}")
    @Operation(
            summary = "Ottieni un colore per ID",
            description = "Restituisce i dettagli di un colore specifico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colore trovato",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Color.class))),
            @ApiResponse(responseCode = "404", description = "Colore non trovato")
    })
    public ResponseEntity<?> getColorById(@PathVariable Long id) {
        return colorRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Colore non trovato")));
    }

    @PostMapping("/api/admin/colors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crea un nuovo colore (ADMIN)",
            description = "Permette agli amministratori di aggiungere nuovi colori alla palette disponibile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Colore creato con successo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Color.class))),
            @ApiResponse(responseCode = "400", description = "Colore già esistente o dati non validi"),
            @ApiResponse(responseCode = "403", description = "Accesso negato - solo ADMIN")
    })
    public ResponseEntity<?> createColor(@RequestBody Color color) {
        // Validazione duplicati
        if (colorRepository.existsByName(color.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Esiste già un colore con questo nome"));
        }
        if (colorRepository.existsByHexCode(color.getHexCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Esiste già un colore con questo codice esadecimale"));
        }

        Color savedColor = colorRepository.save(color);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedColor);
    }

    @DeleteMapping("/api/admin/colors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Elimina un colore (ADMIN)",
            description = "Permette agli amministratori di rimuovere un colore. I preferiti associati verranno resettati al colore di default"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colore eliminato con successo"),
            @ApiResponse(responseCode = "403", description = "Accesso negato - solo ADMIN"),
            @ApiResponse(responseCode = "404", description = "Colore non trovato")
    })
    public ResponseEntity<?> deleteColor(@PathVariable Long id) {
        if (!colorRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Colore non trovato"));
        }

        colorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Colore eliminato con successo"));
    }
}
