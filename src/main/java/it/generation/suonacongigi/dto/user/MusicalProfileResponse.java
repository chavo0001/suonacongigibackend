package it.generation.suonacongigi.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import io.swagger.v3.oas.annotations.media.Schema;
import it.generation.suonacongigi.dto.artist.ArtistResponse;
import it.generation.suonacongigi.dto.genre.GenreResponse;
import it.generation.suonacongigi.dto.instrument.InstrumentResponse;

// Questo DTO rappresenta la risposta contenente i dettagli testuali del profilo musicale di un utente, 
// inclusi i nomi dei generi musicali, degli strumenti suonati e degli artisti preferiti, restituiti dal server.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder 
@Schema(description = "Dettagli testuali del profilo musicale")
public class MusicalProfileResponse {
    
    // Il campo "bio" rappresenta la biografia dell'utente. È facoltativo.
    @Schema(description = "Biografia dell'utente", example = "Chitarrista blues...")
    private String bio;

    @Schema(description = "Nomi dei generi musicali", example = "[\"Blues\", \"Rock\"]")
    private Set<GenreResponse> genres;
    
    @Schema(description = "Nomi degli strumenti suonati", example = "[\"Chitarra Elettrica\"]")
    private Set<InstrumentResponse> instruments;
    
    @Schema(description = "Nomi degli artisti preferiti", example = "[\"Eric Clapton\", \"Jimi Hendrix\"]")
    private Set<ArtistResponse> favoriteArtists;
}