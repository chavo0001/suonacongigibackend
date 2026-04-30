package it.generation.suonacongigi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Risposta di avvenuta verifica mail")
public class EmailResponse {

    // Il campo "username" rappresenta lo username dell'utente autenticato.
    @Schema(description = "Username dell'utente che ha confermato la mail", example = "rockstar99")
    private String username;

    // Il campo "role" rappresenta il ruolo assegnato all'utente.
    @Schema(description = "Email dell'utente", example = "test@gmail.com")
    private String email;
}
