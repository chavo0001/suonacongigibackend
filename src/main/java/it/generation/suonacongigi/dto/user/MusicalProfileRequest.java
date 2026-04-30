package it.generation.suonacongigi.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import io.swagger.v3.oas.annotations.media.Schema;

// Questo DTO rappresenta la richiesta per aggiornare le preferenze musicali di un utente, 
// contenente i set di ID dei generi musicali, degli strumenti suonati e degli artisti preferiti.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Richiesta per aggiornare le preferenze musicali (ID degli oggetti)")
public class MusicalProfileRequest {

    @Schema(description = "Testo della biografia", example = "Chitarrista blues con la passione per il vintage.", maxLength = 1000)
    @Size(max = 1000, message = "La bio non può superare i 1000 caratteri")
    private String bio;
    
    @Schema(description = "Set di ID dei generi musicali", example = "[1, 2, 5]")
    @NotNull(message = "La lista dei generi è obbligatorio (può essere vuota)")
    private Set<Long> genreIds;

    @Schema(description = "Set di ID degli strumenti suonati", example = "[3, 8]")
    @NotNull(message = "La lista degli strumenti è obbligatoria (può essere vuota)")
    private Set<Long> instrumentIds;

    @Schema(description = "Set di ID degli artisti preferiti", example = "[10, 15, 22]")
    @NotNull(message = "La lista degli artisti è obbligatoria (può essere vuota)")
    private Set<Long> artistIds;
}