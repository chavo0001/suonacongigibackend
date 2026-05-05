package it.generation.suonacongigi.controller;

import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.dto.event.*;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.Objects;

// Questo controller gestisce le operazioni CRUD sugli eventi, nonché la registrazione degli utenti agli eventi. Alcuni endpoint sono protetti e accessibili solo agli amministratori, mentre altri sono accessibili a tutti gli utenti autenticati.

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController extends BaseController {

    // Iniezione del servizio che contiene la logica di business per gli eventi.
    private final EventService eventService;

    // Endpoint pubblico per consultare la lista completa degli eventi. Gli utenti autenticati vedono solo quelli a cui possono accedere, mentre gli utenti non autenticati vedono solo quelli pubblici.
    @Operation(summary = "Elenco eventi", description = "Restituisce la lista completa degli eventi. Gli utenti autenticati vedono solo quelli a cui possono accedere.")
    @ApiResponses ({
        @ApiResponse(responseCode = "200", description = "Lista recuperata con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping
    public ResponseEntity<ApiEnvelope<List<EventResponse>>> getAll(@AuthenticationPrincipal User user) {
        // Se l'utente è autenticato, passiamo il suo username al servizio per filtrare gli eventi in base ai permessi. 
        // Se l'utente non è autenticato, passiamo null e il servizio restituirà solo gli eventi pubblici.
        String username = user != null ? user.getUsername() : null;

        // Il servizio si occupa di applicare la logica di filtraggio degli eventi in base all'utente. 
        // Gli utenti autenticati vedono solo gli eventi a cui hanno accesso, 
        // mentre gli utenti non autenticati vedono solo quelli pubblici.
        List<EventResponse> data = eventService.findAll(username);

        return ok(data, "Lista degli eventi recuperata con successo");
    }

    // Endpoint pubblico per consultare i dettagli di un evento specifico tramite ID. 
    // Gli utenti autenticati vedono solo i dettagli degli eventi a cui hanno accesso, 
    // mentre gli utenti non autenticati vedono solo quelli pubblici.
    @Operation(summary = "Dettaglio evento", description = "Restituisce i dettagli di un evento tramite ID.")
    @ApiResponses ({
        @ApiResponse(responseCode = "200", description = "Evento trovato"),
        @ApiResponse(responseCode = "404", description = "Evento non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiEnvelope<EventResponse>> getById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        // Se l'utente è autenticato, passiamo il suo username al servizio per verificare se ha accesso ai dettagli dell'evento.
        // Se l'utente non è autenticato, passiamo null e il servizio restituirà i dettagli solo se l'evento è pubblico.
        String username = user != null ? user.getUsername() : null;

        // Il servizio si occupa di verificare i permessi e restituire i dettagli dell'evento.
        EventResponse data = eventService.findById(Objects.requireNonNull(id), username);

        return ok(data, "Evento recuperato con successo");
    }

    // Endpoint protetto per recuperare la lista degli utenti registrati a un evento tramite ID. 
    // Accessibile solo agli amministratori.
    @Operation(summary = "Elenco registrati evento", description = "Restituisce la lista degli utenti registrati a un evento. Solo per amministratori.")
    @ApiResponses ({
        @ApiResponse(responseCode = "200", description = "Lista recuperata con successo"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "404", description = "Evento non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}/registrants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<List<String>>> getRegistrants(
        @Parameter(description = "ID dell'evento di cui recuperare i registrati", example = "123")
        @PathVariable Long id) {
        // Il servizio si occupa di recuperare la lista degli utenti registrati all'evento specificato.
        List<String> data = eventService.getRegistrants(Objects.requireNonNull(id));

        return ok(data, "Lista degli utenti registrati all'evento recuperata con successo");
    }

    // Endpoint protetto per la creazione di un nuovo evento. Accettando un DTO con i dati dell'evento, 
    // valida i dati e delega la logica al servizio. 
    // Restituisce i dettagli dell'evento creato in caso di successo. 
    // Accessibile solo agli amministratori.
    @Operation(summary = "Crea evento", description = "Crea un nuovo evento. Solo per amministratori.")
    @ApiResponses ({
        @ApiResponse(responseCode = "200", description = "Evento creato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<EventResponse>> create(@Valid @RequestBody EventRequest req, @AuthenticationPrincipal User user) {
        // @Valid: Innesca il motore di validazione Bean Validation via Reflection sui metadati del DTO.
        String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());

        // Il controller si limita a delegare la logica di creazione dell'evento al servizio, 
        // restituendo un ResponseEntity con il risultato. 
        // La validazione dei dati avviene automaticamente grazie alle annotazioni di validazione sui campi del DTO.

        EventResponse data = eventService.create(Objects.requireNonNull(req), username);

        return ok(data, "Evento creato con successo");
    }

    @Operation(summary = "Aggiorna un evento esistente (ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Evento aggiornato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati di aggiornamento non validi"),
        @ApiResponse(responseCode = "401", description = "Token non valido o assente"),
        @ApiResponse(responseCode = "403", description = "Accesso negato: solo amministratori possono eseguire questa operazione"),
        @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<EventResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest req,
            @AuthenticationPrincipal User userDetails) {
        
        String username = Objects.requireNonNull(userDetails.getUsername());
        EventResponse data = eventService.update(Objects.requireNonNull(id), Objects.requireNonNull(req), username);

        return ok(data, "Evento aggiornato con successo");
    }

    // Endpoint protetto per registrare un utente a un evento tramite ID.
    @Operation(summary = "Registra a evento", description = "Permette a un utente autenticato di registrarsi a un evento. Restituisce 200 OK se la registrazione è avvenuta con successo, 400 Bad Request se l'utente è già registrato o se l'evento è al completo.")
    @ApiResponses ({
        @ApiResponse(responseCode = "200", description = "Registrazione avvenuta con successo"),
        @ApiResponse(responseCode = "400", description = "Utente già registrato o evento al completo"),
        @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
        @ApiResponse(responseCode = "404", description = "Evento non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/{id}/register")
    public ResponseEntity<ApiEnvelope<Void>> register(
        @Parameter
        @PathVariable Long id, 
        @AuthenticationPrincipal User user) {
        // Il controller si limita a delegare la logica di registrazione al servizio, 
        // restituendo un ResponseEntity con il risultato.
        String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());

        // Il servizio si occupa di verificare se l'utente è già registrato, 
        // Se l'evento è al completo e di registrare l'utente all'evento.
        eventService.register(Objects.requireNonNull(id), username);

        // Se il servizio non ha lanciato eccezioni, significa che la registrazione è avvenuta con successo
        return ok(null, "Registrazione avvenuta con successo");
    }

    // Endpoint protetto per disiscrivere un utente da un evento tramite ID.
    @Operation(summary = "Disiscrivi da evento", description = "Permette a un utente autenticato di disiscriversi da un evento.")
    @ApiResponses ({
        @ApiResponse(responseCode = "204", description = "Disiscrizione avvenuta con successo"),
        @ApiResponse(responseCode = "400", description = "Utente non registrato all'evento"),
        @ApiResponse(responseCode = "401", description = "Utente non autenticato"),
        @ApiResponse(responseCode = "404", description = "Evento non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/{id}/register")
    public ResponseEntity<ApiEnvelope<Void>> unregister(
        @Parameter(description = "ID dell'evento da cui disiscrivere l'utente", example = "42")
        @PathVariable Long id, @AuthenticationPrincipal User user) {
            // Il controller si limita a delegare la logica di disiscrizione al servizio, 
            // restituendo un ResponseEntity con il risultato.
            String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());

            // Il servizio si occupa di verificare se l'utente è registrato all'evento e di disiscriverlo.
            eventService.unregister(Objects.requireNonNull(id), username);

            // Se il servizio non ha lanciato eccezioni, significa che la disiscrizione è avvenuta con successo
            return ok(null, "Disiscrizione avvenuta con successo");
    }

    // Endpoint protetto per eliminare un evento tramite ID. 
    // Accessibile solo agli amministratori.
    @Operation(summary = "Elimina evento", description = "Elimina un evento tramite ID. Solo per amministratori.")
    @ApiResponses ({
        @ApiResponse(responseCode = "204", description = "Evento eliminato con successo"),
        @ApiResponse(responseCode = "404", description = "Evento non trovato"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiEnvelope<Void>> delete(
        @Parameter(description = "ID dell'evento da eliminare", example = "42")
        @PathVariable Long id) {
            // Il controller si limita a delegare la logica di eliminazione al servizio, 
            // restituendo un ResponseEntity con il risultato.
            eventService.delete(Objects.requireNonNull(id));

            // Se il servizio non ha lanciato eccezioni, significa che l'eliminazione è avvenuta con successo
            return ok(null, "Evento eliminato con successo");
    }
}