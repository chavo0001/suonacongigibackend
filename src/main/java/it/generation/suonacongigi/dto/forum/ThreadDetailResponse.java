package it.generation.suonacongigi.dto.forum;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questo DTO rappresenta la risposta contenente i dettagli di un thread del forum, 
// con le relative informazioni aggiuntive come il nome della categoria e la lista dei post associati al thread.
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ThreadDetailResponse {
    // Il campo "id" rappresenta l'identificativo univoco del thread.
    @Schema(description = "ID del thread", example = "1")
    private Long id;

    // Il campo "title" rappresenta il titolo del thread.
    @Schema(description = "Titolo del thread", example = "Discussione su come migliorare la tecnica di batteria")
    private String title;

    // Il campo "categoryName" rappresenta il nome della categoria a cui appartiene il thread.
    @Schema(description = "Nome della categoria a cui appartiene il thread", example = "Musica")            
    private String categoryName;

    // Il campo "posts" rappresenta la lista dei post associati al thread.
    @Schema(description = "Lista dei post associati al thread")
    private List<PostResponse> posts;
}