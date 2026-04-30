package it.generation.suonacongigi.dto.genre;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// Questo DTO rappresenta la richiesta per la creazione di un nuovo genere musicale

@Data 
@NoArgsConstructor 
@AllArgsConstructor @Builder
@Schema(description = "Richiesta per la creazione di un nuovo genere musicale")
public class GenreRequest {
    @Schema(description = "Nome del genere musicale", example = "Jazz Fusion")
    @NotBlank(message = "Il nome del genere musicale è obbligatorio")
    @Size(min = 3, max = 50)
    private String name;
}