package it.generation.suonacongigi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questo DTO rappresenta la richiesta di login, contenente le credenziali dell'utente (username e password).

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Credenziali per l'accesso al sistema")
public class LoginRequest {
    // Il campo "username" rappresenta lo username dell'utente che sta tentando di accedere al sistema.
    @Schema(description = "Username dell'utente", example = "rockstar99")
    @NotBlank(message = "Username obbligatorio")
    private String username;

    // Il campo "password" rappresenta la password dell'utente che sta tentando di accedere al sistema.
    @Schema(description = "Password dell'utente", example = "Segreta123!")
    @NotBlank(message = "Password obbligatoria")
    private String password;
}