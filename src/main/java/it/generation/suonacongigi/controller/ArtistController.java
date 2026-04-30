package it.generation.suonacongigi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.generation.suonacongigi.dto.artist.ArtistRequest;
import it.generation.suonacongigi.dto.artist.ArtistResponse;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.service.ArtistService;
import it.generation.suonacongigi.util.PageableUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;

// Questo controller gestisce le operazioni CRUD sull'anagrafica degli artisti.

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
@Tag(name = "Artists", description = "Gestione anagrafica artisti")
public class ArtistController extends BaseController {

    // Iniezione del servizio che contiene la logica di business per gli artisti.
    private final ArtistService artistService;

    // Endpoint pubblico per consultare l'anagrafica artisti, senza restrizioni di accesso.
    @Operation(
        summary = "Ottieni lista artisti paginata", 
        description = "Restituisce una pagina di artisti ordinati per nome. " +
                      "È possibile specificare il numero della pagina (0-based) e la dimensione."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina recuperata con successo"),
        @ApiResponse(responseCode = "400", description = "Parametri di paginazione non validi")
    })
    @GetMapping
    public ResponseEntity<ApiEnvelope<Page<ArtistResponse>>> getAll(
        @ParameterObject 
        @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        // Sanificazione del Pageable per garantire la conformità alla Null Safety 
        // e proteggere l'app da richieste massive (Anti-DOS).
        Pageable requestPageable = PageableUtils.sanitize(pageable, 100);

        Page<ArtistResponse> data = artistService.findAll(Objects.requireNonNull(requestPageable));

        // Uso del metodo ok() ereditato dal BaseController
        return ok(data, "Lista artisti recuperata con successo"); 
    }

    // Endpoint pubblico per consultare i dettagli di un artista specifico, senza restrizioni di accesso.
    @Operation(summary = "Dettaglio artista", description = "Restituisce i dati di un artista tramite ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Artista trovato"),
        @ApiResponse(responseCode = "404", description = "Artista non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiEnvelope<ArtistResponse>> getById(
        @Parameter(description = "ID dell'artista da cercare", example = "42") 
        @PathVariable Long id) {
            ArtistResponse data = artistService.findById(Objects.requireNonNull(id));
            return ok(data, "Artista recuperato con successo");
    }

    // Endpoint protetto per la creazione di un nuovo artista, accessibile solo agli utenti con ruolo ADMIN.
    @Operation(summary = "Crea artista", description = "Crea un nuovo artista. Solo per amministratori.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Artista creato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<ArtistResponse>> create(
        @Parameter(description = "Dati dell'artista da creare") @Valid @RequestBody ArtistRequest req) {
            ArtistResponse data = artistService.create(Objects.requireNonNull(req));
            return ok(data, "Artista creato con successo");
    }
    
    // Endpoint protetto per l'eliminazione di un artista tramite ID, accessibile solo agli utenti con ruolo ADMIN.
    @Operation(summary = "Elimina artista", description = "Elimina un artista tramite ID. Solo per amministratori.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Artista eliminato con successo"),
        @ApiResponse(responseCode = "404", description = "Artista non trovato"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<Void>> delete(
        @Parameter(description = "ID dell'artista da eliminare", example = "42") @PathVariable Long id) {
        // Endpoint riservato agli amministratori per l'eliminazione di un artista.
        artistService.delete(Objects.requireNonNull(id));
        // Se il servizio non ha lanciato eccezioni, significa che l'eliminazione è avvenuta con successo
        return ok(null, "Artista eliminato con successo");
    }
}