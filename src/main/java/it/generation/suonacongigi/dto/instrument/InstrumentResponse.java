package it.generation.suonacongigi.dto.instrument;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

// Questo DTO rappresenta la risposta contenente i dati di uno strumento musicale restituiti dal server.

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@Schema(description = "Dati dello strumento restituiti dal server")
public class InstrumentResponse {
    @Schema(description = "ID univoco dello strumento", example = "15")
    private Long id;

    @Schema(description = "Nome dello strumento", example = "Chitarra")
    private String name;
}