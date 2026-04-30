package it.generation.suonacongigi.controller;

import io.swagger.v3.oas.annotations.Operation; 
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.dto.user.MusicalProfileRequest; 
import it.generation.suonacongigi.dto.user.UserResponse;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

// Questo controller gestisce le operazioni relative ai profili utente, come la visualizzazione del proprio profilo,
// l'aggiornamento della biografia e delle preferenze musicali. 
// Alcuni endpoint sono accessibili a tutti gli utenti autenticati, 
// mentre altri sono riservati agli amministratori per la gestione dei profili altrui 
// o la consultazione della lista completa degli utenti.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoint per la gestione dei profili utente, biografie e preferenze musicali")
@SecurityRequirement(name = "bearerAuth")
public class UserController extends BaseController {

    // Iniezione del servizio che contiene la logica di business per i profili utente.
    private final UserService userService;

    @Operation(
        summary = "Ottieni il mio profilo", 
        description = "Recupera i dati completi dell'utente correntemente autenticato tramite il token JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profilo utente recuperato con successo"),
        @ApiResponse(responseCode = "401", description = "Token non valido o assente")
    })
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiEnvelope<UserResponse>> me(@AuthenticationPrincipal User userDetails) {
        // Recupera il nome utente dell'utente autenticato e delega al servizio la logica di recupero del profilo completo,
        String username = Objects.requireNonNull(Objects.requireNonNull(userDetails).getUsername());

        // restituendo un ResponseEntity con il risultato.
        UserResponse data = userService.getMyProfile(username);

        return ok(data, "Profilo utente recuperato con successo");
    }

    // Endpoint protetto per l'aggiornamento del profilo musicale, accessibile a tutti gli utenti autenticati.
    @Operation(
        summary = "Aggiorna profilo musicale", 
        description = "Sincronizza le liste di generi, strumenti e artisti preferiti dell'utente autenticato."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profilo musicale aggiornato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati di profilo musicale non validi"),
        @ApiResponse(responseCode = "401", description = "Token non valido o assente")
    })
    @PutMapping("/me")
    public ResponseEntity<ApiEnvelope<UserResponse>> updateMyMusicalProfile(
        @Valid @RequestBody MusicalProfileRequest req,
        @AuthenticationPrincipal User userDetails) {

            // Recupera il nome utente dell'utente autenticato 
            String username = Objects.requireNonNull(Objects.requireNonNull(userDetails).getUsername());

            // certifica localmente i dati di input e delega al servizio la logica di aggiornamento del profilo musicale,
            MusicalProfileRequest cleanReq = Objects.requireNonNull(req);
        
            // Restituisce un ResponseEntity con il risultato dell'aggiornamento del profilo musicale.
            UserResponse data = userService.updateMusicalProfile(username, cleanReq);

            return ok(data, "Profilo musicale aggiornato con successo");
    }

    // Endpoint protetto per la consultazione della lista completa degli utenti, accessibile solo agli amministratori.
    @Operation(
        summary = "Lista completa utenti (ADMIN)", 
        description = "Restituisce l'elenco di tutti gli utenti registrati nella piattaforma. Accessibile solo agli amministratori."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista utenti recuperata con successo"),
        @ApiResponse(responseCode = "401", description = "Token non valido o assente"),
        @ApiResponse(responseCode = "403", description = "Accesso negato: solo amministratori possono eseguire questa operazione")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiEnvelope<List<UserResponse>>> listAll() {
        // Il controller si limita a delegare la logica di recupero della lista completa degli utenti al servizio
        List<UserResponse> data = userService.findAll();

        return ok(data, "Lista utenti recuperata con successo");
    }
}