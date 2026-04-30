package it.generation.suonacongigi.controller;
 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.dto.genre.GenreRequest;
import it.generation.suonacongigi.dto.genre.GenreResponse;
import it.generation.suonacongigi.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

// Questo controller gestisce le operazioni CRUD sui generi musicali.

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@Tag(name = "Genres", description = "Gestione generi musicali")
public class GenreController extends BaseController {

    // Iniezione del servizio che contiene la logica di business per i generi musicali.
    private final GenreService genreService;

    // Endpoint pubblico per consultare l'anagrafica dei generi musicali, senza restrizioni di accesso.
    @Operation(summary = "Elenco generi", description = "Restituisce la lista completa dei generi musicali registrati.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista recuperata con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping
    public ResponseEntity<ApiEnvelope<List<GenreResponse>>> getAll() {

        List<GenreResponse> data = genreService.findAll();

        return ok(data, "Lista dei generi musicali recuperata con successo");
    }

    // Endpoint pubblico per consultare i dettagli di un genere musicale specifico, senza restrizioni di accesso.
    @Operation(summary = "Dettaglio genere", description = "Restituisce i dati di un genere tramite ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Genere trovato"),
        @ApiResponse(responseCode = "404", description = "Genere non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiEnvelope<GenreResponse>> getById(
        @Parameter(description = "ID del genere da cercare", example = "7") 
        @PathVariable Long id) {
            // Il controller si limita a delegare la logica di recupero dei dettagli del genere al servizio,
            GenreResponse data = genreService.findById(Objects.requireNonNull(id));

            return ok(data, "Genere musicale recuperato con successo");
    }

    // Endpoint protetto per la creazione di un nuovo genere musicale, accessibile solo agli utenti con ruolo ADMIN.
    @Operation(summary = "Crea genere", description = "Crea un nuovo genere musicale. Solo per amministratori.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Genere creato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<GenreResponse>> create(
        @Parameter(description = "Dati del genere da creare") 
        @Valid @RequestBody GenreRequest req) {
            // Il controller si limita a delegare la logica di creazione del genere al servizio
            GenreResponse data = genreService.create(Objects.requireNonNull(req));

            return ok(data, "Genere musicale creato con successo");
    }

    // Endpoint protetto per l'eliminazione di un genere musicale tramite ID, accessibile solo agli utenti con ruolo ADMIN.
    @Operation(summary = "Elimina genere", description = "Elimina un genere tramite ID. Solo per amministratori.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Genere eliminato con successo"),
        @ApiResponse(responseCode = "404", description = "Genere non trovato"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<Void>> delete(
        @Parameter(description = "ID del genere da eliminare", example = "7")
        @PathVariable Long id) {
        // Certificazione locale per il parametro ID, 
        // anche se @PathVariable e @Parameter dovrebbero garantire che sia presente e valido.
        genreService.delete(Objects.requireNonNull(id));

        // Se il servizio non ha lanciato eccezioni, significa che l'eliminazione è avvenuta con successo
        return ok(null, "Genere musicale eliminato con successo");
    }
}