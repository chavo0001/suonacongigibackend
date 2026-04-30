package it.generation.suonacongigi.dto.artist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

// Questo DTO rappresenta la risposta contenente i dati di un artista. 
// Contiene i campi necessari per definire un artista, 
// con le relative annotazioni di documentazione per Swagger/OpenAPI.
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@Schema(description = "Risposta contenente i dati dell'artista")
public class ArtistResponse {
    // Il campo "id" rappresenta l'identificativo univoco dell'artista.
    @Schema(description = "ID univoco dell'artista", example = "42")
    private Long id;
    
    // Il campo "name" rappresenta il nome dell'artista.
    @Schema(description = "Nome dell'artista", example = "Gigi D'Alessio")
    private String name;
}