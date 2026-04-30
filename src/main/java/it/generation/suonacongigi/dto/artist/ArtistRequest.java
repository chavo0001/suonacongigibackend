package it.generation.suonacongigi.dto.artist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// Questo DTO rappresenta la richiesta per la creazione o modifica di un artista. 
// Contiene i campi necessari per definire un artista, con le relative annotazioni di validazione 
// e documentazione per Swagger/OpenAPI.
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@Schema(description = "Richiesta per la creazione o modifica di un artista")
public class ArtistRequest {
    // Il campo "name" rappresenta il nome dell'artista.
    @Schema(description = "Nome dell'artista", example = "Gigi D'Alessio")
    // @NotBlank: Il campo non può essere null, vuoto o contenere solo spazi bianchi.
    @NotBlank(message = "Il nome dell'artista è obbligatorio")
    // @Size: Il campo deve avere una lunghezza compresa tra 2 e 100 caratteri.
    @Size(min = 2, max = 100)
    // Il campo "name" è obbligatorio e deve rispettare le regole di validazione specificate.
    private String name;
}