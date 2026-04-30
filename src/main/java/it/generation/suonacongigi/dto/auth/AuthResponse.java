package it.generation.suonacongigi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questo DTO rappresenta la risposta di autenticazione, contenente il token JWT e i dati dell'utente autenticato.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Risposta di autenticazione contenente il token JWT e i dati utente")
public class AuthResponse {
    // Il campo "token" rappresenta il Token JWT da utilizzare per autenticazione.
    @Schema(description = "Token JWT da utilizzare per autenticazione", 
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb2Nrc3Rhcjk5Iiwi...")
    private String token;

    // Il campo "username" rappresenta lo username dell'utente autenticato.
    @Schema(description = "Username dell'utente autenticato", example = "rockstar99")
    private String username;

    // Il campo "role" rappresenta il ruolo assegnato all'utente.
    @Schema(description = "Ruolo assegnato all'utente", example = "USER")
    private String role;
}