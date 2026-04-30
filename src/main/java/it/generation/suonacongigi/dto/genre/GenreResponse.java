package it.generation.suonacongigi.dto.genre;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

// Questo DTO rappresenta la risposta contenente i dati di un genere musicale.

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@Schema(description = "Rappresentazione dati genere in uscita")
public class GenreResponse {
    @Schema(description = "ID univoco del genere musicale", example = "7")
    private Long id;

    @Schema(description = "Nome del genere musicale", example = "Jazz Fusion")
    private String name;
}