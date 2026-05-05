package it.generation.suonacongigi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.dto.instrument.InstrumentRequest;
import it.generation.suonacongigi.dto.instrument.InstrumentResponse;
import it.generation.suonacongigi.service.InstrumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

// Questo controller gestisce le operazioni CRUD sugli strumenti musicali, 
// con endpoint pubblici per la consultazione e endpoint protetti per la creazione 
// e l'eliminazione (solo per amministratori).
@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
@Tag(name = "Instruments", description = "Gestione del catalogo strumenti musicali")
public class InstrumentController extends BaseController {

    // Iniezione del servizio che contiene la logica di business per gli strumenti musicali.
    private final InstrumentService instrumentService;

    // Endpoint pubblico per consultare l'anagrafica degli strumenti musicali, senza restrizioni di accesso.
    @Operation(summary = "Elenco strumenti", description = "Restituisce la lista completa degli strumenti musicali disponibili.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista recuperata con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping
    public ResponseEntity<ApiEnvelope<List<InstrumentResponse>>> getAll() {
        // Il controller si limita a delegare la logica di recupero della lista degli strumenti al servizio, 
        // restituendo un ResponseEntity con il risultato.
        List<InstrumentResponse> data = instrumentService.findAll();

        return ok(data, "Lista degli strumenti musicali recuperata con successo");
    }

    // Endpoint pubblico per consultare i dettagli di uno strumento specifico, senza restrizioni di accesso.
    @Operation(summary = "Dettaglio strumento", description = "Restituisce i dati di uno strumento tramite ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Strumento trovato"),
        @ApiResponse(responseCode = "404", description = "Strumento non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiEnvelope<InstrumentResponse>> getById(
        @Parameter(description = "ID dello strumento da cercare", example = "15") 
        @PathVariable Long id) {
            // Il controller si limita a delegare la logica di recupero dei dettagli dello strumento al servizio

            InstrumentResponse data = instrumentService.findById(Objects.requireNonNull(id));

            return ok(data, "Strumento musicale recuperato con successo");
    }

    // Endpoint protetto per la creazione di un nuovo strumento musicale, accessibile solo agli utenti con ruolo ADMIN.
    @Operation(summary = "Crea strumento", description = "Crea un nuovo strumento musicale. Solo per amministratori.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Strumento creato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<InstrumentResponse>> create(
        @Parameter(description = "Dati dello strumento da creare") @Valid @RequestBody InstrumentRequest req) {
            // Il controller si limita a delegare la logica di creazione dello strumento al servizio

            InstrumentResponse data = instrumentService.create(Objects.requireNonNull(req));

            return ok(data, "Strumento musicale creato con successo");
    }
    
    // Endpoint protetto per l'eliminazione di uno strumento tramite ID, accessibile solo agli utenti con ruolo ADMIN.
    @Operation(summary = "Elimina strumento", description = "Elimina uno strumento tramite ID. Solo per amministratori.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Strumento eliminato con successo"),
        @ApiResponse(responseCode = "404", description = "Strumento non trovato"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<Void>> delete(
        @Parameter(description = "ID dello strumento da eliminare", example = "15")
        @PathVariable Long id) {
            // Certificazione locale per il parametro ID, 
            // anche se @PathVariable e @Parameter dovrebbero garantire che sia presente e valido.
            instrumentService.delete(Objects.requireNonNull(id));
            
            // Se il servizio non ha lanciato eccezioni, significa che l'eliminazione è avvenuta con successo
            return ok(null, "Strumento musicale eliminato con successo");
    }
}