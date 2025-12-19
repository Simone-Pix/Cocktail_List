package com.cocktail.cocktaillist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Immagini", description = "API per la gestione delle immagini dei cocktail")
public class ImageController {

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Carica un'immagine",
            description = "Permette a utenti autenticati di caricare immagini per i cocktail. Usa form-data con campo 'file'.",
            security = @SecurityRequirement(name = "OAuth2")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Immagine caricata con successo",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "File non valido o mancante"),
            @ApiResponse(responseCode = "401", description = "Non autenticato"),
            @ApiResponse(responseCode = "500", description = "Errore durante il salvataggio")
    })
    public ResponseEntity<?> uploadImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "File mancante o vuoto"));
        }

        try {
            // Crea directory se non esiste
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Genera nome univoco per il file
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;

            // Salva il file
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Costruisci URL pubblico
            String imageUrl = "/api/images/" + filename;

            return ResponseEntity.ok(Map.of(
                    "message", "Immagine caricata con successo",
                    "filename", filename,
                    "url", imageUrl,
                    "uploadedBy", jwt != null ? jwt.getClaimAsString("preferred_username") : "unknown"
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il salvataggio: " + e.getMessage()));
        }
    }

    /**
     * GET immagine (pubblico)
     */
    @GetMapping("/{filename}")
    @Operation(
            summary = "Scarica un'immagine",
            description = "Endpoint pubblico per servire le immagini dei cocktail"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Immagine trovata",
                    content = @Content(mediaType = "image/jpeg")),
            @ApiResponse(responseCode = "404", description = "Immagine non trovata")
    })
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Determina il content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE immagine (solo ADMIN)
     */
    @DeleteMapping("/delete/{filename}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Elimina un'immagine (ADMIN)",
            description = "Permette agli amministratori di eliminare immagini"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Immagine eliminata con successo"),
            @ApiResponse(responseCode = "403", description = "Accesso negato - solo ADMIN"),
            @ApiResponse(responseCode = "404", description = "Immagine non trovata")
    })
    public ResponseEntity<?> deleteImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(filename).normalize();

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Immagine non trovata"));
            }

            Files.delete(filePath);
            return ResponseEntity.ok(Map.of("message", "Immagine eliminata con successo"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'eliminazione: " + e.getMessage()));
        }
    }
}
