package it.generation.suonacongigi.dto.forum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questo DTO rappresenta la richiesta di creazione o modifica di un post all'interno di un thread del forum.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
    // Il campo "content" rappresenta il contenuto del post. 
    // Deve essere non vuoto e avere una lunghezza compresa tra 2 e 2000 caratteri.
    @Schema(description = "Contenuto del post", example = "Questo è il contenuto del post. Deve essere significativo e non troppo breve.")
    @NotBlank @Size(min = 2, max = 2000)
    private String content;
}