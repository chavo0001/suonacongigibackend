package it.generation.suonacongigi.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Questo DTO rappresenta i dati completi dell'utente restituiti dal server, 
// inclusi il profilo musicale e il ruolo. Contiene campi come id, username, email

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dati completi dell'utente inclusi profilo e ruolo")
public class UserResponse {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Schema(description = "ID univoco dell'utente", example = "1")
    @NotNull
    private Long id;

    // Il campo "username" rappresenta il nome utente dell'utente. È obbligatorio.
    @Schema(description = "Username identificativo", example = "gigi_valvola")
    @NotBlank
    private String username;

    // Il campo "email" rappresenta l'indirizzo email dell'utente. È obbligatorio.
    @Schema(description = "Indirizzo email dell'utente", example = "gigi@suonacongigi.it")
    @NotBlank
    private String email;

    // Il campo "role" rappresenta il ruolo assegnato all'utente. È obbligatorio.       
    @Schema(description = "Ruolo assegnato (USER, ADMIN)", example = "USER")
    @NotBlank
    private String role;

    // Il campo "musicalProfile" rappresenta i dettagli del profilo musicale dell'utente. È facoltativo.
    @Schema(description = "Dettagli del profilo musicale (generi, strumenti, artisti)")
    private MusicalProfileResponse musicalProfile;
}