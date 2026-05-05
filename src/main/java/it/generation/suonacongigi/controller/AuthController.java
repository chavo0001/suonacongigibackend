package it.generation.suonacongigi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.generation.suonacongigi.dto.auth.AuthResponse;
import it.generation.suonacongigi.dto.auth.LoginRequest;
import it.generation.suonacongigi.dto.auth.RegisterRequest;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

// Questo controller gestisce le operazioni di registrazione e login degli utenti, restituendo un Token JWT in caso di successo.

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Servizi per la registrazione e l'accesso (rilascio Token JWT)")
@SecurityRequirements(value = {}) // Indica che questi endpoint non richiedono autenticazione (Override del requisito globale).
public class AuthController extends BaseController {

    // Iniezione del servizio che contiene la logica di business per l'autenticazione.
    private final AuthService authService;

    // Endpoint pubblico per la registrazione di un nuovo utente. Accetta un DTO con i dati di registrazione, valida i dati e delega la logica al servizio. Restituisce un Token JWT in caso di successo.
    @Operation(summary = "Registrazione nuovo utente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registrazione effettuata con successo, restituisce Token JWT"),
        @ApiResponse(responseCode = "400", description = "Dati di registrazione non validi"),
        @ApiResponse(responseCode = "409", description = "Username già esistente")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiEnvelope<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) { 
        // @Valid: Innesca il motore di validazione Bean Validation via Reflection sui metadati del DTO.
        RegisterRequest cleanReq = Objects.requireNonNull(req);

        AuthResponse data = authService.register(cleanReq);

        return ok(data, "Utente registrato correttamente");
    }

    // Endpoint pubblico per l'accesso (login) di un utente. Accetta un DTO con username e password, valida i dati e delega la logica al servizio. Restituisce un Token JWT in caso di successo.
    @Operation(summary = "Accesso utente (Login)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login effettuato con successo, restituisce Token JWT"),
        @ApiResponse(responseCode = "401", description = "Credenziali non valide")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiEnvelope<AuthResponse>> login(@Valid @RequestBody LoginRequest req) { 
        // @Valid: Innesca il motore di validazione Bean Validation via Reflection sui metadati del DTO.
        LoginRequest cleanReq = Objects.requireNonNull(req);

        AuthResponse data = authService.login(cleanReq);
        // Il controller si limita a delegare la logica di login al servizio, restituendo un ResponseEntity con il risultato. La validazione dei dati avviene automaticamente grazie alle annotazioni di validazione sui campi del DTO.
        return ok(data, "Login effettuato con successo");
    }
}