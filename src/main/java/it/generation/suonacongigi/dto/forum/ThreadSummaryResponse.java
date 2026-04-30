package it.generation.suonacongigi.dto.forum;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questo DTO rappresenta la risposta contenente i dati di un thread del forum, 
// con le relative informazioni aggiuntive come il nome dell'autore, 
// il nome della categoria a cui appartiene il thread, la data di creazione e il numero di post associati al thread.
@Data 
@NoArgsConstructor 
@AllArgsConstructor @Builder
public class ThreadSummaryResponse {
    // Il campo "id" rappresenta l'identificativo univoco del thread.
    @Schema(description = "ID del thread", example = "1")
    private Long id;

    // Il campo "title" rappresenta il titolo del thread.
    @Schema(description = "Titolo del thread", example = "Discussione su come migliorare la tecnica di batteria")
    private String title;

    // Il campo "authorName" rappresenta il nome dell'autore del thread.
    @Schema(description = "Nome dell'autore del thread", example = "Mario Rossi")
    private String authorName;

    // Il campo "categoryName" rappresenta il nome della categoria a cui appartiene il thread.
    @Schema(description = "Nome della categoria a cui appartiene il thread", example = "Musica")
    private String categoryName;

    // Il campo "createdAt" rappresenta la data di creazione del thread.
    @Schema(description = "Data di creazione del thread", example = "2023-01-01T12:00:00")
    private LocalDateTime createdAt;

    // Il campo "postCount" rappresenta il numero di post associati al thread.
    @Schema(description = "Numero di post associati al thread", example = "5")
    private long postCount;
}