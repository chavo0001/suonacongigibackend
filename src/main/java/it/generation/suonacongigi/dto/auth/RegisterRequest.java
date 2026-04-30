package it.generation.suonacongigi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import it.generation.suonacongigi.dto.user.MusicalProfileRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dati completi per la registrazione di un nuovo utente e del suo profilo")
public class RegisterRequest {

    @NotBlank(message = "Lo username è obbligatorio")
    @Size(min = 3, max = 20, message = "Lo username deve essere tra 3 e 20 caratteri")
    private String username;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve contenere almeno 6 caratteri")
    private String password;
    
    @Valid 
    @NotNull(message = "Il profilo musicale è obbligatorio in fase di registrazione")
    private MusicalProfileRequest musicalProfile;
}