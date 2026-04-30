package it.generation.suonacongigi.dto.instrument;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// Questo DTO rappresenta la richiesta per la creazione o modifica di uno strumento musicale.

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@Schema(description = "Richiesta per la creazione o modifica di uno strumento")
public class InstrumentRequest {
    @Schema(description = "Nome dello strumento", example = "Chitarra")
    @NotBlank(message = "Il nome dello strumento è obbligatorio")
    @Size(min = 2, max = 50)
    private String name;
}