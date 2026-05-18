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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoint per la gestione dei profili utente, biografie e preferenze musicali")
@SecurityRequirement(name = "bearerAuth")
public class UserController extends BaseController {

    private final UserService userService;

    @Operation(summary = "Ottieni il mio profilo", description = "Recupera i dati completi dell'utente correntemente autenticato tramite il token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profilo utente recuperato con successo"),
            @ApiResponse(responseCode = "401", description = "Token non valido o assente")
    })
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiEnvelope<UserResponse>> me(@AuthenticationPrincipal User userDetails) {
        String username = Objects.requireNonNull(Objects.requireNonNull(userDetails).getUsername());
        UserResponse data = userService.getMyProfile(username);
        return ok(data, "Profilo utente recuperato con successo");
    }

    @Operation(summary = "Aggiorna profilo musicale", description = "Sincronizza le liste di generi, strumenti e artisti preferiti dell'utente autenticato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profilo musicale aggiornato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati di profilo musicale non validi"),
            @ApiResponse(responseCode = "401", description = "Token non valido o assente")
    })
    @PutMapping("/me")
    public ResponseEntity<ApiEnvelope<UserResponse>> updateMyMusicalProfile(
            @Valid @RequestBody MusicalProfileRequest req,
            @AuthenticationPrincipal User userDetails) {
        String username = Objects.requireNonNull(Objects.requireNonNull(userDetails).getUsername());
        MusicalProfileRequest cleanReq = Objects.requireNonNull(req);
        UserResponse data = userService.updateMusicalProfile(username, cleanReq);
        return ok(data, "Profilo musicale aggiornato con successo");
    }

    @Operation(summary = "Lista completa utenti (ADMIN)", description = "Restituisce l'elenco di tutti gli utenti registrati nella piattaforma. Accessibile solo agli amministratori.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista utenti recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Token non valido o assente"),
            @ApiResponse(responseCode = "403", description = "Accesso negato: solo amministratori possono eseguire questa operazione")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiEnvelope<List<UserResponse>>> listAll() {
        List<UserResponse> data = userService.findAll();
        return ok(data, "Lista utenti recuperata con successo");
    }
    @Operation(summary = "Elimina utente (ADMIN)", description = "Elimina definitivamente un utente dal sistema. Solo gli amministratori possono eseguire questa operazione.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente eliminato con successo"),
            @ApiResponse(responseCode = "403", description = "Accesso negato: solo amministratori"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiEnvelope<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(Objects.requireNonNull(id));
        return ok(null, "Utente eliminato con successo");
    }
}
