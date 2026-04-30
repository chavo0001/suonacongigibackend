package it.generation.suonacongigi.dto.forum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questo DTO rappresenta la risposta contenente i dati di una categoria del forum, 
// con le relative informazioni aggiuntive come il numero di thread associati alla categoria.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    // Il campo "id" rappresenta l'identificativo univoco della categoria.
    @Schema(description = "ID della categoria", example = "1")
    private Long id;

    // Il campo "name" rappresenta il nome della categoria.
    @Schema(description = "Nome della categoria", example = "Musica")
    private String name;

    // Il campo "description" rappresenta la descrizione della categoria.
    @Schema(description = "Descrizione della categoria", example = "Discussioni relative alla musica")
    private String description;

    // Il campo "threadCount" rappresenta il numero di thread associati alla categoria.
    @Schema(description = "Numero di thread associati alla categoria", example = "10")
    private long threadCount;
}